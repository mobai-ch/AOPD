package NetWork.SearchUtil;

import NetWork.Element.Driver;
import NetWork.Element.Order;

public class MatchPair {
    private Order   order;
    private Driver  driver;
    private int     pickUp;
    private int     dropOff;
    private long    matchedTime;


    public void setDropOff(int dropOff) {
        this.dropOff = dropOff;
    }

    public void setPickUp(int pickUp) {
        this.pickUp = pickUp;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void setMatchedTime(long matchedTime) {
        this.matchedTime = matchedTime;
    }


    public int getPickUp() {
        return pickUp;
    }

    public int getDropOff() {
        return dropOff;
    }

    public Driver getDriver() {
        return driver;
    }

    public Order getOrder() {
        return order;
    }

    public long getMatchedTime() {
        return matchedTime;
    }
}
