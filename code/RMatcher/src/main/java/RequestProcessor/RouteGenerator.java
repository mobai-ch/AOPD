package RequestProcessor;

import LogUtil.Log;
import NetWork.Element.Coordinate;
import NetWork.Element.Order;
import NetWork.Element.Path;
import NetWork.PedestrianRoadNetwork;
import NetWork.SearchUtil.MatchPair;
import NetWork.SearchUtil.PVUtil;
import NetWork.VehicleRoadNetwork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RouteGenerator {
    private PedestrianRoadNetwork pedestrianRoadNetwork;
    private VehicleRoadNetwork    vehicleRoadNetwork;
    private PVUtil                pvUtil;


    public void UpdateRoadNetwork(PedestrianRoadNetwork pedestrianRoadNetwork, VehicleRoadNetwork vehicleRoadNetwork){
        this.pedestrianRoadNetwork = pedestrianRoadNetwork;
        this.vehicleRoadNetwork    = vehicleRoadNetwork;
    }


    public Log GenerateMatchLog(MatchPair matchPair){
        Log log = new Log();

        if(matchPair.getPickUp() == -1){
            log.setMatchScore(0.0);

            try {
                log.setDriverId(matchPair.getDriver().getRequestId());
                log.setDriverResponseTime(matchPair.getDriver().getMaxResponseTime());
                log.setDriverTime(matchPair.getDriver().getArrivalTime());
            }catch (Exception ex){
                log.setDriverId(-1);
            }

            try {
                log.setOrderId(matchPair.getOrder().getRequestId());
                log.setOrderResponseTime(matchPair.getOrder().getMaxResponseTime());
                log.setOrderTime(matchPair.getOrder().getArrivalTime());
            }catch (Exception ex){
                log.setOrderId(-1);
            }

            log.setTravelTime(-1);
            return log;
        }

        int pedestrianSource      = matchPair.getOrder().getOrigin();
        int pedestrianDestination = matchPair.getOrder().getDestination();


        HashMap<Integer, Path> pPaths  = pedestrianRoadNetwork.shortestPath(pedestrianSource,
                -1, matchPair.getOrder().getMaxWalkTime());
        HashMap<Integer, Path> dPaths = pedestrianRoadNetwork.shortestPath(pedestrianDestination,
                -1, matchPair.getOrder().getMaxWalkTime());
        HashMap<Integer, Double> p = vehicleRoadNetwork.turnGeo2VID(pedestrianRoadNetwork.turnGeo2PID(pPaths));
        HashMap<Integer, Double> d = vehicleRoadNetwork.turnGeo2VID(pedestrianRoadNetwork.turnGeo2PID(dPaths));
        int s = vehicleRoadNetwork.addVirtualOrigin(p);
        int e = vehicleRoadNetwork.addVirtualDestination(d);
        HashMap<Integer, Path> shortestTravelTime = vehicleRoadNetwork.shortestPath(s, e, Double.MAX_VALUE);
        vehicleRoadNetwork.delVirtualOrigin();
        vehicleRoadNetwork.delVirtualDestination(d);
        double minTravelTime = shortestTravelTime.get(e).travelTime;


        int pp = pedestrianRoadNetwork.getPID(vehicleRoadNetwork.getGeoId(matchPair.getPickUp()));
        int pd = pedestrianRoadNetwork.getPID(vehicleRoadNetwork.getGeoId(matchPair.getDropOff()));

        double pickTime = pedestrianRoadNetwork.shortestPath(pedestrianSource, pp, Double.MAX_VALUE).get(pp).travelTime;
        double dropTime = pedestrianRoadNetwork.shortestPath(pd, pedestrianDestination, Double.MAX_VALUE).get(pedestrianDestination).travelTime;

        HashMap<Integer, Path> vFirstStep   = vehicleRoadNetwork.shortestPath(
                matchPair.getDriver().getNode(), matchPair.getPickUp(), Double.MAX_VALUE);

        HashMap<Integer, Path> vSecondStep  = vehicleRoadNetwork.shortestPath(
                matchPair.getPickUp(), matchPair.getDropOff(), Double.MAX_VALUE);

        double vPickTime, vSecondTime, travelTime;
        if(!vFirstStep.containsKey(matchPair.getPickUp()) || !vSecondStep.containsKey(matchPair.getDropOff())){
            travelTime = -1.0;
            vPickTime = -1.0;
            vSecondTime = -1.0;
        }else {
            vPickTime   = vFirstStep.get(matchPair.getPickUp()).travelTime;
            vSecondTime = vSecondStep.get(matchPair.getDropOff()).travelTime;
            travelTime = Math.max(pickTime, vPickTime) + vSecondTime + dropTime;
        }

        double matchScore = minTravelTime/travelTime;

        if(travelTime == 0){
            System.out.println("Error occur 2");
        }


        log.setMatchScore(matchScore);
        log.setDriverId(matchPair.getDriver().getRequestId());
        log.setOrderId(matchPair.getOrder().getRequestId());
        log.setDriverResponseTime(matchPair.getMatchedTime() - matchPair.getDriver().getArrivalTime());
        log.setOrderResponseTime(matchPair.getMatchedTime()  - matchPair.getOrder().getArrivalTime());
        log.setTravelTime(travelTime);
        log.setWalkTimeA(pickTime);
        log.setWalkTimeB(dropTime);
        log.setDeliverTime(vPickTime);
        log.setDriverTime(matchPair.getDriver().getArrivalTime());
        log.setOrderTime(matchPair.getOrder().getArrivalTime());

        return  log;
    }

    public HashMap<String, Path> getAssignResult(MatchPair matchPair){
        return new HashMap<>();
    }

}
