package RequestProcessor;


import LogUtil.Log;
import LogUtil.LogSet;
import NetWork.BipartiteGraph;
import NetWork.Element.Coordinate;
import NetWork.Element.Driver;
import NetWork.Element.Order;
import NetWork.Element.Path;
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

public class NPDRequestProcessor {

    private PedestrianRoadNetwork   pNetWork;
    private VehicleRoadNetwork      vNetWork;
    private VehicleRoadNetwork      spvNetwork;
    private PVUtil                  pvUtil;


    private final BipartiteGraph          bipartiteGraph;
    private final HashMap<Long, Order>    orders;
    private final HashMap<Long, Driver>   drivers;


    private final RouteGenerator routeGenerator;


    private final LogSet logSet;

    public NPDRequestProcessor() {
        bipartiteGraph   = new BipartiteGraph();
        logSet           = new LogSet();
        orders           = new HashMap<>();
        drivers          = new HashMap<>();
        routeGenerator   = new RouteGenerator();
    }


    public void UpdateNetwork(PedestrianRoadNetwork pedestrianRoadNetwork,
                              VehicleRoadNetwork vehicleRoadNetwork, VehicleRoadNetwork spvNetwork, PVUtil pvUtil){
        this.pNetWork   = pedestrianRoadNetwork;
        this.vNetWork   = vehicleRoadNetwork;
        this.spvNetwork = spvNetwork;
        this.pvUtil     = pvUtil;
        routeGenerator.UpdateRoadNetwork(pedestrianRoadNetwork, vehicleRoadNetwork);
    }


    public LogSet getLogSet(){
        return this.logSet;
    }

    public void ProcessRequest(Request request, String matchType){
        int type = request.getType();
        if(type == 0){

            Driver driver = new Driver(request.getRequestId(), 900, request.getRequestTime(),
                    request.getMaxResponseTime());
            Coordinate s  = request.getCoordinateInfo().get("origin");
            int        ns = spvNetwork.findNearestNode(s);

            driver.setNode(ns);

            HashMap<Long, SearchResult> searchResults = new HashMap<>();
            drivers.put(request.getRequestId(), driver);
            HashMap<Integer, Path> dPaths = spvNetwork.shortestPath(ns, -1, driver.getMaxDeliveryTime());


            for(long orderId: orders.keySet()){
                Order order = orders.get(orderId);

                if(request.getRequestTime() - order.getArrivalTime() > order.getMaxResponseTime()){
                    continue;
                }

                int os = order.getvOrigin();
                int od = order.getvDestination();
                try {
                    if (dPaths.containsKey(os)) {
                        HashMap<Integer, Path> oPaths = spvNetwork.shortestPath(os, od, Double.MAX_VALUE);
                        SearchResult searchResult = new SearchResult();
                        searchResult.setMatchScore(oPaths.get(od).travelTime / (oPaths.get(od).travelTime + dPaths.get(os).travelTime));
                        searchResult.setOrder(order);
                        searchResult.setDriver(driver);
                        searchResult.setType(request.getType());
                        searchResult.setPickUp(os);
                        searchResult.setDropOff(od);
                        searchResults.put(orderId, searchResult);
                    }
                }catch (Exception ignore){}
            }


            List<MatchPair> matchPairs = new ArrayList<>();
            switch (matchType) {
                case "greedy"  : matchPairs = bipartiteGraph.greedyInsert(request, searchResults); break;
                case "greedyT" : matchPairs = bipartiteGraph.greedyInsertWithThreshold(request, searchResults, 0.7); break;
                case "KMT"     : matchPairs = bipartiteGraph.kmInsertWithThreshold(request, searchResults, 0.7); break;
                case "BKM"     : bipartiteGraph.directInsert(request, searchResults); break;
                case "default" : break;
            }

            bipartiteGraph.delPair(matchPairs);
            matchPairs = UpdateTheMatchPairWithPD(matchPairs);


            for(MatchPair matchPair: matchPairs){
                Log log = routeGenerator.GenerateMatchLog(matchPair);
                logSet.addLog(log);
            }


            removeItemOutOfTime(matchPairs);
        }else{

            Order order = new Order(request.getRequestId(), request.getRequestTime(),
                    1500, request.getMaxResponseTime());
            Coordinate s  = request.getCoordinateInfo().get("origin");
            Coordinate d  = request.getCoordinateInfo().get("destination");
            int        ns = pNetWork.findNearestNode(s);
            int        nd = pNetWork.findNearestNode(d);
            int        os = pvUtil.findNearestNode(s);
            int        od = pvUtil.findNearestNode(d);
            order.setOrigin(ns);
            order.setDestination(nd);
            order.setvDestination(od);
            order.setvOrigin(os);
            orders.put(request.getRequestId(), order);

            HashMap<Long, SearchResult> searchResults = new HashMap<>();


            HashMap<Integer, Path> oPaths = spvNetwork.shortestPath(os, od, Double.MAX_VALUE);
            for(long driverId: drivers.keySet()){
                Driver driver = drivers.get(driverId);

                if(request.getRequestTime() - driver.getArrivalTime() > driver.getMaxResponseTime()){
                    continue;
                }

                HashMap<Integer, Path> dPaths = spvNetwork.shortestPath(driver.getNode(), -1, driver.getMaxDeliveryTime());
                if(dPaths.containsKey(os)){
                    try {
                        SearchResult searchResult = new SearchResult();
                        searchResult.setMatchScore(oPaths.get(od).travelTime / (oPaths.get(od).travelTime + dPaths.get(os).travelTime));
                        searchResult.setOrder(order);
                        searchResult.setDriver(driver);
                        searchResult.setType(request.getType());
                        searchResult.setPickUp(os);
                        searchResult.setDropOff(od);
                        searchResults.put(driverId, searchResult);
                    }catch (Exception ignore) {}
                }
            }


            List<MatchPair> matchPairs = new ArrayList<>();

            switch (matchType) {
                case "greedy"  : matchPairs = bipartiteGraph.greedyInsert(request, searchResults); break;
                case "greedyT" : matchPairs = bipartiteGraph.greedyInsertWithThreshold(request, searchResults, 0.7); break;
                case "KMT"     : matchPairs = bipartiteGraph.kmInsertWithThreshold(request, searchResults, 0.7); break;
                case "BKM"     : bipartiteGraph.directInsert(request, searchResults); break;
                case "default" : break;
            }

            bipartiteGraph.delPair(matchPairs);
            matchPairs = UpdateTheMatchPairWithPD(matchPairs);


            for(MatchPair matchPair: matchPairs){
                Log log = routeGenerator.GenerateMatchLog(matchPair);
                logSet.addLog(log);
            }


            removeItemOutOfTime(matchPairs);
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
            HashMap<Integer, Path> osPath = pNetWork.shortestPath(os, -1, matchPair.getOrder().getMaxWalkTime());
            HashMap<Integer, Path> odPath = pNetWork.shortestPath(od, -1, matchPair.getOrder().getMaxWalkTime());
            HashMap<Integer, Double> osTravelTime   = vNetWork.turnGeo2VID(pNetWork.turnGeo2PID(osPath));
            HashMap<Integer, Double> odTravelTime   = vNetWork.turnGeo2VID(pNetWork.turnGeo2PID(odPath));
            HashMap<Integer, Double> osMTravelTime  = new HashMap<>();
            for(int pickUp: osTravelTime.keySet()) {
                if (nsPath.containsKey(pickUp)) {
                    osMTravelTime.put(pickUp, Math.max(osTravelTime.get(pickUp), nsPath.get(pickUp).travelTime));
                }
            }

            int source      = vNetWork.addVirtualOrigin(osMTravelTime);
            int destination = vNetWork.addVirtualDestination(odTravelTime);

            try {
                HashMap<Integer, Path> vPaths = vNetWork.shortestPath(source, destination, Double.MAX_VALUE);
                List<Integer> nodes = vPaths.get(destination).getNodes();
                matchPairs.get(i).setPickUp(nodes.get(1));
                matchPairs.get(i).setDropOff(nodes.get(nodes.size() - 2));
            }catch (Exception ignore){ }
            vNetWork.delVirtualOrigin();
            vNetWork.delVirtualDestination(odTravelTime);
        }
        return matchPairs;
    }

    public void OfflineKM(Request request){
        List<MatchPair> matchPairs = bipartiteGraph.KmAlgorithm(request, 0.0);
        for(MatchPair matchPair: matchPairs){
            Log log = routeGenerator.GenerateMatchLog(matchPair);
            logSet.addLog(log);
        }
    }

    public void removeItemOutOfTime(List<MatchPair> matchPairs){
        for(MatchPair matchPair: matchPairs){
            try {
                long driverId = matchPair.getDriver().getRequestId();
                if (driverId != -1) {
                    drivers.remove(driverId);
                }
            }catch (Exception ignore){}

            try {
                long orderId = matchPair.getOrder().getRequestId();
                if (orderId != -1) {
                    orders.remove(orderId);
                }
            }catch (Exception ignore){}
        }
    }
}
