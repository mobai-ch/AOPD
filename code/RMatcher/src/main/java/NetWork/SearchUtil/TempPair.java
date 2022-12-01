package NetWork.SearchUtil;

import NetWork.Element.Driver;
import NetWork.Element.Order;

public class TempPair {
    private Order order;
    private Driver driver;
    private int    pickUp;
    private int    dropOff;
    private double travelTime;
    private int    isEmpty;

    public void setTravelTime(double travelTime) {
        this.travelTime = travelTime;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public Order getOrder() {
        return order;
    }

    public Driver getDriver() {
        return driver;
    }

    public double getTravelTime() {
        return travelTime;
    }

    public void setPickUp(int pickUp) {
        this.pickUp = pickUp;
    }

    public void setDropOff(int dropOff) {
        this.dropOff = dropOff;
    }

    public int getPickUp() {
        return pickUp;
    }

    public int getDropOff() {
        return dropOff;
    }

    public int getIsEmpty() {
        return isEmpty;
    }

    public void setIsEmpty(int isEmpty) {
        this.isEmpty = isEmpty;
    }
}
