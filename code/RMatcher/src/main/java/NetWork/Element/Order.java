package NetWork.Element;

public class Order {

    private final long requestId;
    private final long arrivalTime;
    private final long maxWalkTime;
    private final long maxResponseTime;

    private int        origin;
    private int        destination;
    private double     minTravelTime;
    private int        vOrigin;
    private int        vDestination;


    public Order(long requestId, long arrivalTime, long maxWalkTime, long maxResponseTime){
        this.requestId = requestId;
        this.arrivalTime = arrivalTime;
        this.maxWalkTime = maxWalkTime;
        this.maxResponseTime = maxResponseTime;
    }


    public void setOrigin(int origin){
        this.origin = origin;
    }


    public void setDestination(int destination){
        this.destination = destination;
    }


    public void setMinTravelTime(double minTravelTime){
        this.minTravelTime = minTravelTime;
    }


    public void setvDestination(int vDestination) { this.vDestination = vDestination; }


    public void setvOrigin(int vOrigin) { this.vOrigin = vOrigin; }


    public long getRequestId() {
        return requestId;
    }


    public long getMaxResponseTime() {
        return maxResponseTime;
    }


    public long getArrivalTime() {
        return arrivalTime;
    }


    public int getDestination() {
        return destination;
    }


    public int getOrigin() {
        return origin;
    }


    public long getMaxWalkTime() {
        return maxWalkTime;
    }


    public double getMinTravelTime() {
        return minTravelTime;
    }


    public int getvDestination() { return vDestination; }


    public int getvOrigin() { return vOrigin; }
}
