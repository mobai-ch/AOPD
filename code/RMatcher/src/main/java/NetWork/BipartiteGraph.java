package NetWork;

import NetWork.Element.Driver;
import NetWork.Element.Edge;
import NetWork.Element.Order;
import NetWork.SearchUtil.KmUtil;
import NetWork.SearchUtil.MatchPair;
import NetWork.SearchUtil.SearchResult;
import Request.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class BipartiteGraph {

    public HashMap<Long, Order>   orders;
    public HashMap<Long, Driver>  drivers;


    private HashMap<Long, HashMap<Long, Edge>> owEdge;
    private HashMap<Long, HashMap<Long, Edge>> woEdge;


    public BipartiteGraph(){
        this.orders  = new HashMap<>();
        this.drivers = new HashMap<>();
        this.owEdge  = new HashMap<>();
        this.woEdge  = new HashMap<>();
    }


    public void addEdges(Request request, HashMap<Long, SearchResult> searchResults){

        long rId  = request.getRequestId();
        int  type = request.getType();
        if(type == 0){

            this.woEdge.put(rId, new HashMap<>());
        }else{
            this.owEdge.put(rId, new HashMap<>());
        }


        for(long requestId: searchResults.keySet()){
            try {
                Order order = searchResults.get(requestId).getOrder();
                Driver driver = searchResults.get(requestId).getDriver();

                if(request.getRequestTime() - order.getArrivalTime() > order.getMaxResponseTime() ||
                    request.getRequestTime() - driver.getArrivalTime() > driver.getMaxResponseTime()){ continue;}

                this.orders.put(order.getRequestId(), order);
                this.drivers.put(driver.getRequestId(), driver);
                Edge edge = new Edge(1, 2);
                edge.setWeight(0, searchResults.get(requestId).getMatchScore());
                edge.setInfo(0, searchResults.get(requestId).getPickUp());
                edge.setInfo(1, searchResults.get(requestId).getDropOff());
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
                if(matchPair.getDriver() == null){
                    delOrder(matchPair.getOrder().getRequestId());
                }
                if(matchPair.getOrder() == null){
                    delDriver(matchPair.getDriver().getRequestId());
                }
            }catch (Exception ignore){}
        }
    }


    public List<MatchPair> greedyInsert(Request request, HashMap<Long, SearchResult> searchResults){

        this.addEdges(request, searchResults);

        long requestTime           = request.getRequestTime();
        double maxScore = 0.0;
        List<MatchPair> matchPairs = new ArrayList<>();
        MatchPair matchPair        = new MatchPair();
        HashSet<Long> orderPaired  = new HashSet<>();
        HashSet<Long> driverPaired = new HashSet<>();


        for(long requestId: searchResults.keySet()){
            if(searchResults.get(requestId).getMatchScore() > maxScore){
                matchPair.setDriver(searchResults.get(requestId).getDriver());
                matchPair.setOrder(searchResults.get(requestId).getOrder());
                matchPair.setPickUp(searchResults.get(requestId).getPickUp());
                matchPair.setDropOff(searchResults.get(requestId).getDropOff());
                maxScore = searchResults.get(requestId).getMatchScore();
                matchPair.setMatchedTime(requestTime);
            }
        }


        if(maxScore != 0.0) {
            matchPairs.add(matchPair);
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


    public List<MatchPair> greedyInsertWithThreshold(Request request, HashMap<Long, SearchResult> searchResults, double threshold){

        this.addEdges(request, searchResults);

        long requestTime           = request.getRequestTime();
        double maxScore = 0.0;
        List<MatchPair> matchPairs = new ArrayList<>();
        MatchPair matchPair        = new MatchPair();
        HashSet<Long> orderPaired  = new HashSet<>();
        HashSet<Long> driverPaired = new HashSet<>();


        for(long orderId: orders.keySet()){

            if(orders.get(orderId).getArrivalTime() + orders.get(orderId).getMaxResponseTime() <= requestTime
                    && !orderPaired.contains(orderId) && owEdge.containsKey(orderId)){
                MatchPair mPair = new MatchPair();
                double mScore = 0.0; int mPick = -1; int mDrop = -1; long mDriveId = -1;

                for(Long driverId: owEdge.get(orderId).keySet()){
                    if(owEdge.get(orderId).get(driverId).getWeight(0) > mScore
                            && !driverPaired.contains(mDriveId)){
                        mScore = owEdge.get(orderId).get(driverId).getWeight(0);
                        mPick  = owEdge.get(orderId).get(driverId).getInfo(0);
                        mDrop  = owEdge.get(orderId).get(driverId).getInfo(1);
                        mDriveId = driverId;
                    }
                }

                if(mDriveId != -1){
                    mPair.setDropOff(mDrop);
                    mPair.setPickUp(mPick);
                    mPair.setMatchedTime(requestTime);
                    mPair.setOrder(this.orders.get(orderId));
                    mPair.setDriver(this.drivers.get(mDriveId));
                    orderPaired.add(orderId);
                    driverPaired.add(mDriveId);
                }
            }
        }


        for(long requestId: searchResults.keySet()){
            if(searchResults.get(requestId).getMatchScore() > maxScore){
                matchPair.setDriver(searchResults.get(requestId).getDriver());
                matchPair.setOrder(searchResults.get(requestId).getOrder());
                matchPair.setPickUp(searchResults.get(requestId).getPickUp());
                matchPair.setDropOff(searchResults.get(requestId).getDropOff());
                maxScore = searchResults.get(requestId).getMatchScore();
                matchPair.setMatchedTime(requestTime);
            }
        }


        if(maxScore >= threshold) {
            matchPairs.add(matchPair);
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
                mPair.setOrder(null);
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
                mPair.setDriver(null);
                mPair.setMatchedTime(requestTime);
                matchPairs.add(mPair);
            }
        }

        return matchPairs;
    }


    public List<MatchPair> kmInsertWithThreshold(Request request, HashMap<Long,SearchResult> searchResults, double threshold){
        this.addEdges(request, searchResults);
        List<MatchPair> matchPairs = this.KmAlgorithm(request, threshold);

        HashSet<Long> orderPaired  = new HashSet<>();
        HashSet<Long> driverPaired = new HashSet<>();
        long requestTime           = request.getRequestTime();


        for(MatchPair matchPair: matchPairs){
            orderPaired.add(matchPair.getOrder().getRequestId());
            driverPaired.add(matchPair.getDriver().getRequestId());
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


    public void directInsert(Request request, HashMap<Long, SearchResult> searchResults){
        this.addEdges(request, searchResults);
    }

    public List<MatchPair> KmAlgorithm(Request request, double threshold){
        List<MatchPair> matchPairs = new ArrayList<>();
        HashMap<Long, Integer> W = new HashMap<>();
        HashMap<Long, Integer> T = new HashMap<>();
        int num = 1;
        for(Long driverId: drivers.keySet()){
            W.put(driverId, num); num += 1;
        }
        num = 1;
        for(Long orderId: orders.keySet()){
            T.put(orderId, num); num += 1;
        }

        KmUtil kmUtil = new KmUtil(owEdge, W, T);
        kmUtil.KM();
        HashMap<Long, Long> ret = kmUtil.GetMatchResult();

        for(long orderId: ret.keySet()){
            long driverId = ret.get(orderId);
            if(owEdge.get(orderId).get(driverId).getWeight(0) > threshold) {
                MatchPair matchPair = new MatchPair();
                matchPair.setDriver(this.drivers.get(driverId));
                matchPair.setOrder(this.orders.get(orderId));
                matchPair.setMatchedTime(request.getRequestTime());
                matchPair.setPickUp(owEdge.get(orderId).get(driverId).getInfo(0));
                matchPair.setDropOff(owEdge.get(orderId).get(driverId).getInfo(1));
                matchPairs.add(matchPair);
            }
        }

        return matchPairs;
    }

}
