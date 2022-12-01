package RequestProcessor;

import LogUtil.Log;
import LogUtil.LogSet;
import NetWork.BipartiteGraph;
import NetWork.Element.*;
import NetWork.PedestrianRoadNetwork;
import NetWork.SearchUtil.MatchPair;
import NetWork.SearchUtil.PVUtil;
import NetWork.SearchUtil.SearchResult;
import NetWork.TripartiteGraph;
import NetWork.VehicleRoadNetwork;
import Request.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class NPNDRequestProcessorE {
    private PedestrianRoadNetwork pNetwork;
    private VehicleRoadNetwork    vNetWork;
    private PVUtil                pvUtil;

    private final TripartiteGraph       tripartiteGraph;
    private final BipartiteGraph        bipartiteGraph;

    private final LogSet                logSet;

    private final RouteGenerator        routeGenerator;

    public NPNDRequestProcessorE (){
        tripartiteGraph  = new TripartiteGraph();
        bipartiteGraph   = new BipartiteGraph();
        logSet           = new LogSet();
        this.routeGenerator   = new RouteGenerator();
    }

    public void UpdateNetwork(PedestrianRoadNetwork pedestrianRoadNetwork,
                              VehicleRoadNetwork vehicleRoadNetwork,
                              PVUtil pvUtil){
        this.pNetwork    = pedestrianRoadNetwork;
        this.vNetWork    = vehicleRoadNetwork;
        this.pvUtil      = pvUtil;
        this.routeGenerator.UpdateRoadNetwork(pedestrianRoadNetwork, vehicleRoadNetwork);
    }

    public LogSet getLogSet(){
        return this.logSet;
    }

    public void ProcessRequest(Request request, String matchType, int mDriveTime){
        int type = request.getType();
        HashMap<Integer, Edge> edges = new HashMap<>();
        if(type == 1){
            try {
                int pos = this.pvUtil.findNearestPNode(request.getCoordinateInfo().get("origin"));
                int pof = this.pNetwork.findNearestNode(request.getCoordinateInfo().get("origin"));
                int pds = this.pvUtil.findNearestPNode(request.getCoordinateInfo().get("destination"));
                int pdf = this.pNetwork.findNearestNode(request.getCoordinateInfo().get("destination"));

                double noTravelTime = this.pNetwork.shortestPath(pof, pos, Double.MAX_VALUE).get(pos).travelTime;
                double ndTravelTime = this.pNetwork.shortestPath(pdf, pds, Double.MAX_VALUE).get(pds).travelTime;

                double nTravelTime = Math.max(noTravelTime, ndTravelTime);

                // 初始化订单
                Order order = new Order(request.getRequestId(), request.getRequestTime(),
                        (long) (nTravelTime + request.getMaxWalkingTime() + 1), request.getMaxResponseTime());

                Coordinate s  = request.getCoordinateInfo().get("origin");
                Coordinate d  = request.getCoordinateInfo().get("destination");
                int        ns = pNetwork.findNearestNode(s);
                int        nd = pNetwork.findNearestNode(d);
                int        os = pvUtil.findNearestNode(s);
                int        od = pvUtil.findNearestNode(d);

                order.setOrigin(ns);
                order.setDestination(nd);
                order.setvDestination(od);
                order.setvOrigin(os);

                HashMap<Integer, Path> oPaths = vNetWork.shortestPath(os, od, Double.MAX_VALUE);
                double minTravelTime = oPaths.get(od).travelTime;

                Edge edge = new Edge(2, 1);
                edge.setWeight(0, minTravelTime);
                edge.setWeight(1, 0.0);
                edge.setInfo(0, od);
                edges.put(os, edge);

                order.setMinTravelTime(minTravelTime);
                tripartiteGraph.addOrder(order, edges);
                HashMap<Long, SearchResult> searchResult = tripartiteGraph.search(request.getRequestId(), 1);
                List<MatchPair> matchPairs = new ArrayList<>();

                switch (matchType) {
                    case "greedy":
                        matchPairs = bipartiteGraph.greedyInsert(request, searchResult);
                        break;
                    case "greedyT":
                        matchPairs = bipartiteGraph.greedyInsertWithThreshold(request, searchResult, 0.75);
                        break;
                    case "KMT":
                        matchPairs = bipartiteGraph.kmInsertWithThreshold(request, searchResult, 0);
                        break;
                    case "BKM":
                        bipartiteGraph.directInsert(request, searchResult);
                        tripartiteGraph.delOutOfTimeLimit(request);
                        break;
                    case "default":
                        break;
                }

                for (MatchPair matchPair : matchPairs) {
                    Log log = routeGenerator.GenerateMatchLog(matchPair);
                    logSet.addLog(log);
                }

                tripartiteGraph.delPair(matchPairs);
                bipartiteGraph.delPair(matchPairs);
            }catch (Exception ex){ ex.printStackTrace(); }
        }else{
            Driver driver = new Driver(request.getRequestId(), mDriveTime, request.getRequestTime(), request.getMaxResponseTime());
            Coordinate s  = request.getCoordinateInfo().get("origin");
            int        ns = vNetWork.findNearestNode(s);
            driver.setNode(ns);
            HashMap<Integer, Path>   sPaths = vNetWork.shortestPath(ns, -1, driver.getMaxDeliveryTime());

            for(int pickUp: sPaths.keySet()){
                Edge edge = new Edge(1, 1);
                edge.setWeight(0, sPaths.get(pickUp).travelTime);
                edges.put(pickUp, edge);
            }
            tripartiteGraph.addDriver(driver, edges);
            HashMap<Long, SearchResult> searchResult = tripartiteGraph.search(request.getRequestId(), 0);

            List<MatchPair> matchPairs = new ArrayList<>();

            switch (matchType) {
                case "greedy"  : matchPairs = bipartiteGraph.greedyInsert(request, searchResult); break;
                case "greedyT" : matchPairs = bipartiteGraph.greedyInsertWithThreshold(request, searchResult, 0.75); break;
                case "KMT"     : matchPairs = bipartiteGraph.kmInsertWithThreshold(request, searchResult, 0); break;
                case "BKM"     : bipartiteGraph.directInsert(request, searchResult); tripartiteGraph.delOutOfTimeLimit(request);break;
                case "default" : break;
            }

//            matchPairs = UpdateTheMatchPairWithPD(matchPairs);

            tripartiteGraph.delPair(matchPairs);
            bipartiteGraph.delPair(matchPairs);

            for(MatchPair matchPair: matchPairs){
                Log log = routeGenerator.GenerateMatchLog(matchPair);
                logSet.addLog(log);
            }
        }
    }

    public void OfflineKM(Request request){
        List<MatchPair> matchPairs = bipartiteGraph.KmAlgorithm(request, 0.0);
        for(MatchPair matchPair: matchPairs){
            Log log = routeGenerator.GenerateMatchLog(matchPair);
            logSet.addLog(log);
        }
    }

    public List<MatchPair> UpdateTheMatchPairWithPD(List<MatchPair> matchPairs){
        List<MatchPair> nMatchPairs = new ArrayList<>();
        for(int i=0; i<matchPairs.size(); i++){
            try {
                MatchPair matchPair = matchPairs.get(i);

                if (matchPair.getPickUp() == -1) {
                    continue;
                }

                int ns = matchPair.getDriver().getNode();
                int os = matchPair.getOrder().getOrigin();
                int od = matchPair.getOrder().getDestination();
                HashMap<Integer, Path> nsPath = vNetWork.shortestPath(ns, -1, Double.MAX_VALUE);
                HashMap<Integer, Path> osPath = pNetwork.shortestPath(os, -1, matchPair.getOrder().getMaxWalkTime());
                HashMap<Integer, Path> odPath = pNetwork.shortestPath(od, -1, matchPair.getOrder().getMaxWalkTime());
                HashMap<Integer, Double> osTravelTime = vNetWork.turnGeo2VID(pNetwork.turnGeo2PID(osPath));
                HashMap<Integer, Double> odTravelTime = vNetWork.turnGeo2VID(pNetwork.turnGeo2PID(odPath));
                HashMap<Integer, Double> osMTravelTime = new HashMap<>();
                for (int pickUp : osTravelTime.keySet()) {
                    if (nsPath.containsKey(pickUp)) {
                        osMTravelTime.put(pickUp, Math.max(osTravelTime.get(pickUp), nsPath.get(pickUp).travelTime));
                    }
                }

                int source = vNetWork.addVirtualOrigin(osMTravelTime);
                int destination = vNetWork.addVirtualDestination(odTravelTime);

                HashMap<Integer, Path> vPaths = vNetWork.shortestPath(source, destination, Double.MAX_VALUE);
                List<Integer> nodes = vPaths.get(destination).getNodes();
                matchPairs.get(i).setPickUp(nodes.get(1));
                matchPairs.get(i).setDropOff(nodes.get(nodes.size() - 2));
                vNetWork.delVirtualOrigin();
                vNetWork.delVirtualDestination(odTravelTime);
                nMatchPairs.add(matchPairs.get(i));
            }catch (Exception ignore) { }
        }
        return nMatchPairs;
    }
}
