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



public class PDRequestProcessorEMA {

    private PedestrianRoadNetwork pNetwork;
    private VehicleRoadNetwork    vNetWork;
    private VehicleRoadNetwork    rvNetwork;
    private PVUtil pvUtil;

    private final TripartiteGraph       tripartiteGraph;
    private final BipartiteGraph        bipartiteGraph;


    private final LogSet                logSet;


    private final RouteGenerator        routeGenerator;


    public PDRequestProcessorEMA(){
        tripartiteGraph  = new TripartiteGraph();
        bipartiteGraph   = new BipartiteGraph();
        logSet           = new LogSet();
        routeGenerator   = new RouteGenerator();
    }


    public void UpdateNetwork(PedestrianRoadNetwork pedestrianRoadNetwork,
                              VehicleRoadNetwork vehicleRoadNetwork,
                              VehicleRoadNetwork rVehicleRoadNetwork,
                              PVUtil pvUtil){
        this.pNetwork    = pedestrianRoadNetwork;
        this.vNetWork    = vehicleRoadNetwork;
        this.rvNetwork   = rVehicleRoadNetwork;
        this.pvUtil      = pvUtil;
        routeGenerator.UpdateRoadNetwork(pedestrianRoadNetwork, vehicleRoadNetwork);
    }

    public LogSet getLogSet(){
        return this.logSet;
    }

    public void ProcessRequest(Request request, String matchType, int mDriveTime){
        int type = request.getType();
        HashMap<Integer, Edge> edges = new HashMap<>();
        if(type == 1){
            int pos = this.pvUtil.findNearestPNode(request.getCoordinateInfo().get("origin"));
            int pof = this.pNetwork.findNearestNode(request.getCoordinateInfo().get("origin"));
            int pds = this.pvUtil.findNearestPNode(request.getCoordinateInfo().get("destination"));
            int pdf = this.pNetwork.findNearestNode(request.getCoordinateInfo().get("destination"));

            double noTravelTime = this.pNetwork.shortestPath(pof, pos, Double.MAX_VALUE).get(pos).travelTime;
            double ndTravelTime = this.pNetwork.shortestPath(pdf, pds, Double.MAX_VALUE).get(pds).travelTime;

            double nTravelTime = Math.max(noTravelTime, ndTravelTime);


            Order order = new Order(request.getRequestId(), request.getRequestTime(),
                    (long) (nTravelTime + request.getMaxWalkingTime() + 1), request.getMaxResponseTime());


            Coordinate s  = request.getCoordinateInfo().get("origin");
            Coordinate d  = request.getCoordinateInfo().get("destination");
            int        ns = pNetwork.findNearestNode(s);
            int        nd = pNetwork.findNearestNode(d);


            order.setOrigin(ns);
            order.setDestination(nd);

            HashMap<Integer, Path> sPaths = pNetwork.shortestPath(ns, -1, order.getMaxWalkTime());
            HashMap<Integer, Path> dPaths = pNetwork.shortestPath(nd, -1, order.getMaxWalkTime());


            HashMap<Integer, Double> dVID   = vNetWork.turnGeo2VID(pNetwork.turnGeo2PID(dPaths));
            HashMap<Integer, Double> s2Pick = vNetWork.turnGeo2VID(pNetwork.turnGeo2PID(sPaths));
            int rvOrigin = rvNetwork.addVirtualOrigin(dVID);
            HashMap<Integer, Path> d2Pick = rvNetwork.shortestPath(rvOrigin, -1, Double.MAX_VALUE);
            rvNetwork.delVirtualOrigin();


            double minTravelTime = Double.MAX_VALUE;
            for(int pickUp: s2Pick.keySet()){
                if(d2Pick.containsKey(pickUp)){

                    if(s2Pick.get(pickUp) + d2Pick.get(pickUp).travelTime < minTravelTime){
                        minTravelTime = s2Pick.get(pickUp) + d2Pick.get(pickUp).travelTime;
                    }

                    Edge edge = new Edge(2, 1);
                    edge.setWeight(0, d2Pick.get(pickUp).travelTime);
                    edge.setWeight(1, s2Pick.get(pickUp));
                    edge.setInfo(0, d2Pick.get(pickUp).getNodes().get(1));
                    edges.put(pickUp, edge);
                }
            }
            order.setMinTravelTime(minTravelTime);
            tripartiteGraph.addOrder(order, edges);

            HashMap<Long, SearchResult> searchResult = tripartiteGraph.search(request.getRequestId(), 1);

            for(Long driveId: bipartiteGraph.drivers.keySet()){
                Driver driver = bipartiteGraph.drivers.get(driveId);
                if(driver != null) {

                    HashMap<Integer, Path> dPaths2 = vNetWork.shortestPath(driver.getNode(), -1, driver.getMaxDeliveryTime());
                    HashMap<Integer, Path> pPaths1 = pNetwork.shortestPath(order.getOrigin(),
                            -1, order.getMaxWalkTime());
                    HashMap<Integer, Path> dPaths1 = pNetwork.shortestPath(order.getDestination(),
                            -1, order.getMaxWalkTime());
                    HashMap<Integer, Double> p1 = vNetWork.turnGeo2VID(pNetwork.turnGeo2PID(pPaths1));
                    HashMap<Integer, Double> d1 = vNetWork.turnGeo2VID(pNetwork.turnGeo2PID(dPaths1));
                    HashMap<Integer, Double> p2 = new HashMap<>();
                    for (Integer tTey : p1.keySet()) {
                        if (dPaths2.containsKey(tTey)) {
                            p2.put(tTey, Math.max(dPaths2.get(tTey).travelTime, p1.get(tTey)));
                        }
                    }
                    int s1 = vNetWork.addVirtualOrigin(p2);
                    int e1 = vNetWork.addVirtualDestination(d1);
                    HashMap<Integer, Path> shortestTravelTime = vNetWork.shortestPath(s1, e1, Double.MAX_VALUE);
                    try {
                        double minTravelTime1 = shortestTravelTime.get(e1).travelTime;
//                        System.out.println(minTravelTime1);
                    }catch (Exception ignore){}
                    vNetWork.delVirtualOrigin();
                    vNetWork.delVirtualDestination(d1);
                }
            }


            List<MatchPair> matchPairs = new ArrayList<>();

            switch (matchType) {
                case "greedy"  : matchPairs = bipartiteGraph.greedyInsert(request, searchResult); break;
                case "greedyT" : matchPairs = bipartiteGraph.greedyInsertWithThreshold(request, searchResult, 0.75); break;
                case "KMT"     : matchPairs = bipartiteGraph.kmInsertWithThreshold(request, searchResult, 0); break;
                case "BKM"     : bipartiteGraph.directInsert(request, searchResult); tripartiteGraph.delOutOfTimeLimit(request);break;
                case "default" : break;
            }



            for(MatchPair matchPair: matchPairs){
                Log log = routeGenerator.GenerateMatchLog(matchPair);
                logSet.addLog(log);
            }

            tripartiteGraph.delPair(matchPairs);
            bipartiteGraph.delPair(matchPairs);
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

            for(long key: bipartiteGraph.orders.keySet()){
                Order order = bipartiteGraph.orders.get(key);

                if (order != null) {
                    HashMap<Integer, Path> pPaths1 = pNetwork.shortestPath(order.getOrigin(),
                            -1, order.getMaxWalkTime());
                    HashMap<Integer, Path> dPaths1 = pNetwork.shortestPath(order.getDestination(),
                            -1, order.getMaxWalkTime());
                    HashMap<Integer, Double> p1 = vNetWork.turnGeo2VID(pNetwork.turnGeo2PID(pPaths1));
                    HashMap<Integer, Double> d1 = vNetWork.turnGeo2VID(pNetwork.turnGeo2PID(dPaths1));
                    HashMap<Integer, Double> p2 = new HashMap<>();
                    for (Integer tTey : p1.keySet()) {
                        if (sPaths.containsKey(tTey)) {
                            p2.put(tTey, Math.max(sPaths.get(tTey).travelTime, p1.get(tTey)));
                        }
                    }
                    int s1 = vNetWork.addVirtualOrigin(p2);
                    int e1 = vNetWork.addVirtualDestination(d1);
                    HashMap<Integer, Path> shortestTravelTime = vNetWork.shortestPath(s1, e1, Double.MAX_VALUE);
                    try {
                        double minTravelTime1 = shortestTravelTime.get(e1).travelTime;
//                        System.out.println(minTravelTime1);
                    }catch (Exception ignore){}
                    vNetWork.delVirtualOrigin();
                    vNetWork.delVirtualDestination(d1);
                }
            }

            List<MatchPair> matchPairs = new ArrayList<>();


            switch (matchType) {
                case "greedy"  : matchPairs = bipartiteGraph.greedyInsert(request, searchResult); break;
                case "greedyT" : matchPairs = bipartiteGraph.greedyInsertWithThreshold(request, searchResult, 0.75); break;
                case "KMT"     : matchPairs = bipartiteGraph.kmInsertWithThreshold(request, searchResult, 0); break;
                case "BKM"     : bipartiteGraph.directInsert(request, searchResult); tripartiteGraph.delOutOfTimeLimit(request);break;
                case "default" : break;
            }

            tripartiteGraph.delPair(matchPairs);
            bipartiteGraph.delPair(matchPairs);

//            matchPairs = UpdateTheMatchPairWithPD(matchPairs);


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
        for(int i=0; i<matchPairs.size(); i++){
            MatchPair matchPair = matchPairs.get(i);

            if(matchPair.getPickUp() == -1){
                continue;
            }

            int ns = matchPair.getDriver().getNode();
            int os = matchPair.getOrder().getOrigin();
            int od = matchPair.getOrder().getDestination();
            HashMap<Integer, Path> nsPath = vNetWork.shortestPath(ns, -1, Double.MAX_VALUE);
            HashMap<Integer, Path> osPath = pNetwork.shortestPath(os, -1, matchPair.getOrder().getMaxWalkTime());
            HashMap<Integer, Path> odPath = pNetwork.shortestPath(od, -1, matchPair.getOrder().getMaxWalkTime());
            HashMap<Integer, Double> osTravelTime   = vNetWork.turnGeo2VID(pNetwork.turnGeo2PID(osPath));
            HashMap<Integer, Double> odTravelTime   = vNetWork.turnGeo2VID(pNetwork.turnGeo2PID(odPath));
            HashMap<Integer, Double> osMTravelTime  = new HashMap<>();
            for(int pickUp: osTravelTime.keySet()) {
                if (nsPath.containsKey(pickUp)) {
                    osMTravelTime.put(pickUp, Math.max(osTravelTime.get(pickUp), nsPath.get(pickUp).travelTime));
                }
            }

            int source      = vNetWork.addVirtualOrigin(osMTravelTime);
            int destination = vNetWork.addVirtualDestination(odTravelTime);

            HashMap<Integer, Path> vPaths = vNetWork.shortestPath(source, destination, Double.MAX_VALUE);
            List<Integer> nodes = vPaths.get(destination).getNodes();
            matchPairs.get(i).setPickUp(nodes.get(1));
            matchPairs.get(i).setDropOff(nodes.get(nodes.size()-2));
            // 删除虚拟点
            vNetWork.delVirtualOrigin();
            vNetWork.delVirtualDestination(odTravelTime);
        }
        return matchPairs;
    }
}
