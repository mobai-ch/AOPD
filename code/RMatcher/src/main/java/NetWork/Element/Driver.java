package NetWork.Element;

public class Driver {
    private final long requestId;
    private final long maxDeliveryTime;
    private final long arrivalTime;
    private int        node;
    private final long maxResponseTime;

    public Driver(long requestId, int maxDeliveryTime, long arrivalTime, long maxResponseTime){
        this.requestId = requestId;
        this.maxDeliveryTime = maxDeliveryTime;
        this.arrivalTime = arrivalTime;
        this.maxResponseTime = maxResponseTime;
    }


    public void setNode(int node) {
        this.node = node;
    }


    public int getNode() {
        return node;
    }


    public long getArrivalTime() {
        return arrivalTime;
    }


    public long getMaxDeliveryTime() {
        return maxDeliveryTime;
    }


    public long getMaxResponseTime() {
        return maxResponseTime;
    }

    public long getRequestId() {
        return requestId;
    }
}
