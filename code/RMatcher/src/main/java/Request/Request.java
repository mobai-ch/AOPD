package Request;

import NetWork.Element.Coordinate;

import java.util.Comparator;
import java.util.HashMap;

public class Request implements Comparable<Request> {
    private final int                         type;
    private final HashMap<String, Coordinate> coordinateInfo;
    private final HashMap<String, Double>     timeInfo;
    private final long                        requestId;
    private final long                        requestTime;
    private final long                        maxResponseTime;
    private final double                      maxWalkingTime;

    public Request(int type, long requestId, long arrivalTime, long maxResponseTime, double maxWalkingTime){
        this.coordinateInfo = new HashMap<>();
        this.timeInfo = new HashMap<>();
        this.type = type;
        this.requestId = requestId;
        this.requestTime = arrivalTime;
        this.maxResponseTime = maxResponseTime;
        this.maxWalkingTime = maxWalkingTime;
    }


    public long getRequestTime() {
        return requestTime;
    }

    public long getMaxResponseTime() {
        return maxResponseTime;
    }

    public long getRequestId() {
        return requestId;
    }

    public HashMap<String, Coordinate> getCoordinateInfo() {
        return coordinateInfo;
    }


    public HashMap<String, Double> getTimeInfo() {
        return timeInfo;
    }

    public double getMaxWalkingTime(){ return this.maxWalkingTime; }


    public int getType() {
        return type;
    }

    @Override
    public int compareTo(Request o) {
        return (int)(this.requestTime - o.getRequestTime());
    }
}
