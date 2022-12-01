package NetWork;

import NetWork.Element.Driver;
import NetWork.Element.Order;
import NetWork.Element.Edge;
import NetWork.SearchUtil.MatchPair;
import NetWork.SearchUtil.SearchResult;
import Request.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TripartiteGraph {

    private final HashMap<Long, HashMap<Integer, Edge>> wpEdge;
    private final HashMap<Integer, HashMap<Long, Edge>> pwEdge;
    private final HashMap<Long, HashMap<Integer, Edge>> opEdge;
    private final HashMap<Integer, HashMap<Long, Edge>> poEdge;


    private final HashMap<Long, Order>  orders;
    private final HashMap<Long, Driver> drivers;

    public TripartiteGraph(){
        this.wpEdge  = new HashMap<>();
        this.pwEdge  = new HashMap<>();
        this.opEdge  = new HashMap<>();
        this.poEdge  = new HashMap<>();
        this.orders  = new HashMap<>();
        this.drivers = new HashMap<>();
    }


    public void addOrder(Order order, HashMap<Integer, Edge> oEdge){

        long requestId = order.getRequestId();
        this.orders.put(requestId, order);
        this.opEdge.put(requestId, new HashMap<>());

        for(Integer pickUp: oEdge.keySet()){
            this.opEdge.get(requestId).put(pickUp, oEdge.get(pickUp));
            if(!this.poEdge.containsKey(pickUp)){
                this.poEdge.put(pickUp, new HashMap<>());
            }
            if(!this.pwEdge.containsKey(pickUp)){
                this.pwEdge.put(pickUp, new HashMap<>());
            }
            this.poEdge.get(pickUp).put(requestId, oEdge.get(pickUp));
        }
    }


    public void addDriver(Driver driver, HashMap<Integer, Edge> wEdge){

        long requestId = driver.getRequestId();
        this.drivers.put(requestId, driver);
        this.wpEdge.put(requestId, new HashMap<>());

        for(Integer pickUp: wEdge.keySet()){
            this.wpEdge.get(requestId).put(pickUp, wEdge.get(pickUp));
            if(!this.poEdge.containsKey(pickUp)){
                this.poEdge.put(pickUp, new HashMap<>());
            }
            if(!this.pwEdge.containsKey(pickUp)){
                this.pwEdge.put(pickUp, new HashMap<>());
            }
            this.pwEdge.get(pickUp).put(requestId, wEdge.get(pickUp));
        }
    }


    public HashMap<Long, SearchResult> search(long requestId, int type){
        HashMap<Long, SearchResult> resultHashMap = new HashMap<>();
        if(type == 0){

            for(int pickUp: wpEdge.get(requestId).keySet()){
                for(long oID: poEdge.get(pickUp).keySet()){
                    if(!resultHashMap.containsKey(oID) && this.orders.get(oID) != null){
                        resultHashMap.put(oID, new SearchResult());
                        resultHashMap.get(oID).setMatchScore(0.0);
                    }
                    double minTravelTime = this.orders.get(oID).getMinTravelTime();
                    double travelTime = Math.max(poEdge.get(pickUp).get(oID).getWeight(1),
                            wpEdge.get(requestId).get(pickUp).getWeight(0)) + poEdge.get(pickUp).get(oID).getWeight(0);
                    double matchScore = minTravelTime / travelTime;
                    if(matchScore > resultHashMap.get(oID).getMatchScore() && this.orders.get(oID) != null){
                        resultHashMap.get(oID).setMatchScore(matchScore);
                        resultHashMap.get(oID).setDriver(this.drivers.get(requestId));
                        resultHashMap.get(oID).setDropOff(poEdge.get(pickUp).get(oID).getInfo(0));
                        resultHashMap.get(oID).setOrder(this.orders.get(oID));
                        resultHashMap.get(oID).setPickUp(pickUp);
                        resultHashMap.get(oID).setType(0);
                    }
                }
            }
        }else{

            for(int pickUp: opEdge.get(requestId).keySet()){
                for(long dID: pwEdge.get(pickUp).keySet()){
                    if(!resultHashMap.containsKey(dID) && this.drivers.get(dID) != null){
                        resultHashMap.put(dID, new SearchResult());
                        resultHashMap.get(dID).setMatchScore(0.0);
                    }
                    double minTravelTime = this.orders.get(requestId).getMinTravelTime();
                    double travelTime = Math.max(opEdge.get(requestId).get(pickUp).getWeight(1),
                            pwEdge.get(pickUp).get(dID).getWeight(0)) + opEdge.get(requestId).get(pickUp).getWeight(0);
                    double matchScore = minTravelTime / travelTime;
                    if(matchScore > resultHashMap.get(dID).getMatchScore() && this.drivers.get(dID) != null){
                        resultHashMap.get(dID).setMatchScore(matchScore);
                        resultHashMap.get(dID).setDriver(this.drivers.get(dID));
                        resultHashMap.get(dID).setDropOff(opEdge.get(requestId).get(pickUp).getInfo(0));
                        resultHashMap.get(dID).setOrder(this.orders.get(requestId));
                        resultHashMap.get(dID).setPickUp(pickUp);
                        resultHashMap.get(dID).setType(1);
                    }
                }
            }
        }
        return resultHashMap;
    }


    public void delDriver(long requestId){
        this.drivers.remove(requestId);
        for(int pickUp: this.wpEdge.get(requestId).keySet()){
            this.pwEdge.get(pickUp).remove(requestId);
        }
        this.wpEdge.remove(requestId);
    }


    public void delOrder(long requestId){
        this.orders.remove(requestId);
        for(int pickUp: this.opEdge.get(requestId).keySet()){
            this.poEdge.get(pickUp).remove(requestId);
        }
        this.opEdge.remove(requestId);
    }

    public void delPair(HashMap<Long, Long> mPair){
        for(long vRequest: mPair.keySet()){
            long oRequest = mPair.get(vRequest);
            delDriver(vRequest);
            delOrder(oRequest);
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

    public void delOutOfTimeLimit(Request request){
        long requestTime = request.getRequestTime();
        List<Long> orderIds = new ArrayList<>();
        List<Long> driverIds = new ArrayList<>();
        for(long orderId: orders.keySet()){
            if(requestTime - orders.get(orderId).getArrivalTime() > orders.get(orderId).getMaxResponseTime()){
                orderIds.add(orderId);
            }
        }
        for(long driverId: drivers.keySet()){
            if(requestTime - drivers.get(driverId).getArrivalTime() > drivers.get(driverId).getMaxResponseTime()){
                driverIds.add(driverId);
            }
        }
        for(long orderId: orderIds){
            delOrder(orderId);
        }
        for(long driverId: driverIds){
            delDriver(driverId);
        }
    }
}
