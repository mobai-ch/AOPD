package LogUtil;

import CsvUtil.RowElement;

public class Log {
    private double matchScore;
    private long   orderId;
    private long   driverId;
    private long   orderResponseTime;
    private long   driverResponseTime;
    private double travelTime;
    private double walkTimeA;
    private double deliverTime;
    private double walkTimeB;
    private long   orderTime;
    private long   driverTime;

    public void setDriverTime(long driverTime) { this.driverTime = driverTime; }

    public void setOrderTime(long orderTime) { this.orderTime = orderTime; }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public void setDriverId(long driverId) {
        this.driverId = driverId;
    }

    public void setMatchScore(double matchScore) {
        this.matchScore = matchScore;
    }

    public void setOrderResponseTime(long orderResponseTime) {
        this.orderResponseTime = orderResponseTime;
    }

    public void setDriverResponseTime(long driverResponseTime) {
        this.driverResponseTime = driverResponseTime;
    }

    public void setTravelTime(double travelTime) {
        this.travelTime = travelTime;
    }

    public void setDeliverTime(double deliverTime) { this.deliverTime = deliverTime; }

    public void setWalkTimeA(double walkTimeA) { this.walkTimeA = walkTimeA; }

    public void setWalkTimeB(double walkTimeB) { this.walkTimeB = walkTimeB; }

    public double getMatchScore() {
        return matchScore;
    }

    public double getTravelTime() {
        return travelTime;
    }

    public long getDriverId() {
        return driverId;
    }

    public long getOrderId() {
        return orderId;
    }

    public long getDriverResponseTime() {
        return driverResponseTime;
    }

    public long getOrderResponseTime() {
        return orderResponseTime;
    }

    public double getDeliverTime() { return deliverTime; }

    public double getWalkTimeA() { return walkTimeA; }

    public double getWalkTimeB() { return walkTimeB; }

    public long getDriverTime() { return driverTime; }

    public long getOrderTime() { return orderTime; }

    public RowElement toRowElement(){
        RowElement rowElement = new RowElement();
        String[] elements = new String[11];
        elements[0]  = Double.toString(matchScore);
        elements[1]  = Long.toString(orderId);
        elements[2]  = Long.toString(driverId);
        elements[3]  = Long.toString(orderResponseTime);
        elements[4]  = Long.toString(driverResponseTime);
        elements[5]  = Double.toString(travelTime);
        elements[6]  = Double.toString(walkTimeA);
        elements[7]  = Double.toString(deliverTime);
        elements[8]  = Double.toString(walkTimeB);
        elements[9]  = Long.toString(orderTime);
        elements[10] = Long.toString(driverTime);
        rowElement.setData(elements);
        return rowElement;
    }
}
