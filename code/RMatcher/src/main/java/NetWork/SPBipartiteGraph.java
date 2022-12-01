package NetWork;

import NetWork.Element.Driver;
import NetWork.Element.Edge;
import NetWork.Element.Order;
import NetWork.SearchUtil.KmUtil;
import NetWork.SearchUtil.MatchPair;
import NetWork.SearchUtil.SearchResult;
import NetWork.SearchUtil.TempPair;
import Request.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class SPBipartiteGraph {

    public HashMap<Long, Order> orders;
    public HashMap<Long, Driver>  drivers;


    private final HashMap<Long, HashMap<Long, Edge>> owEdge;
    private final HashMap<Long, HashMap<Long, Edge>> woEdge;


    public SPBipartiteGraph(){
        this.orders  = new HashMap<>();
        this.drivers = new HashMap<>();
        this.owEdge  = new HashMap<>();
        this.woEdge  = new HashMap<>();
    }


    public void addEdges(Request request, List<TempPair> pairs){

        long rId  = request.getRequestId();
        int  type = request.getType();
        if(type == 0){

            this.woEdge.put(rId, new HashMap<>());
        }else{
            this.owEdge.put(rId, new HashMap<>());
        }


        if(pairs.size() == 1 && pairs.get(0).getIsEmpty() == 1){
            if(type == 0){
                Driver driver = pairs.get(0).getDriver();
                this.drivers.put(driver.getRequestId(), driver);
            }else{
                Order order = pairs.get(0).getOrder();
                this.orders.put(order.getRequestId(), order);
            }
            return;
        }


        for(TempPair pair: pairs){
            try {
                Order order = pair.getOrder();
                Driver driver = pair.getDriver();


                if(request.getRequestTime() - order.getArrivalTime() > order.getMaxResponseTime() ||
                        request.getRequestTime() - driver.getArrivalTime() > driver.getMaxResponseTime()){ continue;}

                Edge edge = new Edge(1, 2);
                edge.setWeight(0, pair.getTravelTime());
                edge.setInfo(0, pair.getPickUp());
                edge.setInfo(1, pair.getDropOff());
                this.owEdge.get(order.getRequestId()).put(driver.getRequestId(), edge);
                this.woEdge.get(driver.getRequestId()).put(order.getRequestId(), edge);
            }catch (Exception ignore) {}
        }
    }


    public void delOrder(long requestId){
        this.orders.remove(requestId);
        for(long driverId: owEdge.get(requestId).keySet()){
            this.woEdge.get(driverId).remove(requestId);
        }
        this.owEdge.remove(requestId);
    }


    public void delDriver(long requestId){
        this.drivers.remove(requestId);
        for(long orderId: woEdge.get(requestId).keySet()){
            this.owEdge.get(orderId).remove(requestId);
        }
        this.woEdge.remove(requestId);
    }


    public void delPair(HashMap<Long, Long> mPair){
        for(long driverId: mPair.keySet()){
            this.delDriver(driverId);
            long orderId = mPair.get(driverId);
            this.delOrder(orderId);
        }
    }

    public void delPair(List<MatchPair> matchPairs){
        for(MatchPair matchPair: matchPairs){
            try {
                long driverId = matchPair.getDriver().getRequestId();
                if (driverId != -1) {
                    delDriver(driverId);
                }
            }catch (Exception ignore){}

            try {
                long orderId = matchPair.getOrder().getRequestId();
                if (orderId != -1) {
                    delOrder(orderId);
                }
            }catch (Exception ignore){}
        }
    }


    public List<MatchPair> greedyInsert(Request request, List<TempPair> tempPairs){

        int IsEmpty = 0;

        this.addEdges(request, tempPairs);

        long requestTime           = request.getRequestTime();
        double maxTravelTime = 9999999.0;
        List<MatchPair> matchPairs = new ArrayList<>();
        MatchPair matchPair        = new MatchPair();
        HashSet<Long> orderPaired  = new HashSet<>();
        HashSet<Long> driverPaired = new HashSet<>();

        if(tempPairs.size() == 1){
            if(tempPairs.get(0).getIsEmpty() == 1){
                IsEmpty = 1;
            }
        }

        if(IsEmpty == 0) {

            for (TempPair pair : tempPairs) {
                if (pair.getTravelTime() < maxTravelTime) {
                    matchPair.setDriver(pair.getDriver());
                    matchPair.setOrder(pair.getOrder());
                    matchPair.setPickUp(pair.getPickUp());
                    matchPair.setDropOff(pair.getDropOff());
                    maxTravelTime = pair.getTravelTime();
                }
            }

            matchPair.setMatchedTime(requestTime);


            if (maxTravelTime != 9999999.0) {
                matchPairs.add(matchPair);
            }
        }

        for(MatchPair pair: matchPairs){
            orderPaired.add(pair.getOrder().getRequestId());
            driverPaired.add(pair.getDriver().getRequestId());
        }


        for(long driverId: drivers.keySet()){

            if(drivers.get(driverId).getArrivalTime() + drivers.get(driverId).getMaxResponseTime() <= requestTime
                    && !driverPaired.contains(driverId)){
                MatchPair mPair = new MatchPair();
                mPair.setDropOff(-1);
                mPair.setPickUp(-1);
                mPair.setDriver(drivers.get(driverId));
                mPair.setMatchedTime(requestTime);
                matchPairs.add(mPair);
            }
        }
        for(long orderId: orders.keySet()){

            if(orders.get(orderId).getArrivalTime() + orders.get(orderId).getMaxResponseTime() <= requestTime
                    && !orderPaired.contains(orderId)){
                MatchPair mPair = new MatchPair();
                mPair.setDropOff(-1);
                mPair.setPickUp(-1);
                mPair.setOrder(orders.get(orderId));
                mPair.setMatchedTime(requestTime);
                matchPairs.add(mPair);
            }
        }


        return matchPairs;
    }


    public void directInsert(Request request, List<TempPair> pairs){
        this.addEdges(request, pairs);
    }
}
