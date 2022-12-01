package RequestProcessor;

import LogUtil.Log;
import LogUtil.LogSet;
import NetWork.*;
import NetWork.Element.*;
import NetWork.SearchUtil.MatchPair;
import NetWork.SearchUtil.PVUtil;
import NetWork.SearchUtil.SearchResult;
import NetWork.SearchUtil.TempPair;
import Request.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NPDRequestProcessorPT {
    private PedestrianRoadNetwork pNetwork;
    private VehicleRoadNetwork vNetWork;
    private PVUtil pvUtil;
    private VehicleRoadNetwork rvNetwork;
    private SPBipartiteGraph spBipartiteGraph;


    private final LogSet logSet;


    private final RouteGenerator        routeGenerator;


    public NPDRequestProcessorPT (){
        logSet           = new LogSet();
        this.spBipartiteGraph = new SPBipartiteGraph();
        this.routeGenerator   = new RouteGenerator();
    }


    public void UpdateNetwork(PedestrianRoadNetwork pedestrianRoadNetwork,
                              VehicleRoadNetwork vehicleRoadNetwork,
                              VehicleRoadNetwork rVehicleRoadNetwork, PVUtil pvUtil){
        this.pNetwork    = pedestrianRoadNetwork;
        this.vNetWork    = vehicleRoadNetwork;
        this.pvUtil      = pvUtil;
        this.rvNetwork   = rVehicleRoadNetwork;
        this.routeGenerator.UpdateRoadNetwork(pedestrianRoadNetwork, vehicleRoadNetwork);
    }


    public LogSet getLogSet(){
        return this.logSet;
    }

    public void ProcessRequest(Request request, String matchType, int mDriveTime){
        int type = request.getType();
        if(type == 1){
            try {
                int pos = this.pvUtil.findNearestPNode(request.getCoordinateInfo().get("origin"));
                int pof = this.pNetwork.findNearestNode(request.getCoordinateInfo().get("origin"));
                int pds = this.pvUtil.findNearestPNode(request.getCoordinateInfo().get("destination"));
                int pdf = this.pNetwork.findNearestNode(request.getCoordinateInfo().get("destination"));

                double noTravelTime = this.pNetwork.shortestPath(pof, pos, Double.MAX_VALUE).get(pos).travelTime;
                double ndTravelTime = this.pNetwork.shortestPath(pdf, pds, Double.MAX_VALUE).get(pds).travelTime;

                double nTravelTime = Math.max(noTravelTime, ndTravelTime);


                Order order = new Order(request.getRequestId(), request.getRequestTime(),
                        (long) (nTravelTime + request.getMaxWalkingTime()), request.getMaxResponseTime());

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

                List<MatchPair> matchPairs = new ArrayList<>();

                HashMap<Integer, Path>  sPaths = rvNetwork.shortestPath(order.getvOrigin(), -1, mDriveTime);
                HashMap<Integer, Path>  ssPaths = vNetWork.shortestPath(order.getvOrigin(), order.getvDestination(), 9999999.0);
                if(!ssPaths.containsKey(order.getvDestination())) return;
                if(ssPaths.get(order.getvDestination()).travelTime == 0){
                    System.out.println("Error occur 1");
                    return;
                }

                List<TempPair> pairs = new ArrayList<>();
                for(long driverId: spBipartiteGraph.drivers.keySet()){
                    Driver driver = spBipartiteGraph.drivers.get(driverId);
                    if(sPaths.containsKey(driver.getNode()) && sPaths.get(driver.getNode()).travelTime < mDriveTime){
                        TempPair tempPair = new TempPair(); tempPair.setOrder(order); tempPair.setDriver(driver);
                        tempPair.setTravelTime(sPaths.get(driver.getNode()).travelTime); tempPair.setPickUp(order.getvOrigin());
                        tempPair.setDropOff(order.getvDestination()); pairs.add(tempPair); tempPair.setIsEmpty(0);
                    }
                }
                if(pairs.size() == 0){
                    TempPair tempPair = new TempPair();
                    tempPair.setOrder(order); pairs.add(tempPair); tempPair.setIsEmpty(1);
                }

                switch (matchType){
                    case "greedy": matchPairs = spBipartiteGraph.greedyInsert(request, pairs); break;
                    case "default": break;
                }

                spBipartiteGraph.delPair(matchPairs);


                matchPairs = UpdateTheMatchPairWithPD(matchPairs);

                for (MatchPair matchPair : matchPairs) {
                    Log log = routeGenerator.GenerateMatchLog(matchPair);
                    logSet.addLog(log);
                }

            }catch (Exception ex){ ex.printStackTrace(); }
        }else{
            Driver driver = new Driver(request.getRequestId(), mDriveTime, request.getRequestTime(), request.getMaxResponseTime());
            Coordinate s  = request.getCoordinateInfo().get("origin");
            int        ns = vNetWork.findNearestNode(s);
            driver.setNode(ns);
            HashMap<Integer, Path>   sPaths = vNetWork.shortestPath(ns, -1, mDriveTime);
            List<MatchPair> matchPairs = new ArrayList<>();

            List<TempPair> pairs = new ArrayList<>();
            for(long orderId: spBipartiteGraph.orders.keySet()){
                Order order = spBipartiteGraph.orders.get(orderId);
                if(sPaths.containsKey(order.getvOrigin()) && sPaths.get(order.getvOrigin()).travelTime < mDriveTime){
                    TempPair tempPair = new TempPair(); tempPair.setOrder(order); tempPair.setDriver(driver);
                    tempPair.setTravelTime(sPaths.get(order.getvOrigin()).travelTime); tempPair.setPickUp(order.getvOrigin());
                    tempPair.setDropOff(order.getvDestination()); pairs.add(tempPair); tempPair.setIsEmpty(0);
                }
            }
            if(pairs.size() == 0){
                TempPair tempPair = new TempPair();
                tempPair.setDriver(driver); pairs.add(tempPair); tempPair.setIsEmpty(1);
            }

            switch (matchType){
                case "greedy": matchPairs = spBipartiteGraph.greedyInsert(request, pairs); break;
                case "default": break;
            }


            spBipartiteGraph.delPair(matchPairs);

            matchPairs = UpdateTheMatchPairWithPD(matchPairs);

            for(MatchPair matchPair: matchPairs){
                Log log = routeGenerator.GenerateMatchLog(matchPair);
                logSet.addLog(log);
            }
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