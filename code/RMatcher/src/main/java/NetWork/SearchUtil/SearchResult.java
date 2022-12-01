package NetWork.SearchUtil;

import NetWork.Element.Driver;
import NetWork.Element.Order;

public class SearchResult {
    private int    type;
    private int    pickUp;
    private int    dropOff;
    private Driver driver;
    private Order  order;
    private double matchScore;


    public void setOrder(Order order) {
        this.order = order;
    }

    public void setPickUp(int pickUp) {
        this.pickUp = pickUp;
    }

    public void setDropOff(int dropOff) {
        this.dropOff = dropOff;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public void setMatchScore(double matchScore) {
        this.matchScore = matchScore;
    }

    public void setType(int type) {
        this.type = type;
    }


    public int getType() {
        return type;
    }

    public int getDropOff() {
        return dropOff;
    }

    public double getMatchScore() {
        return matchScore;
    }

    public int getPickUp() {
        return pickUp;
    }

    public Driver getDriver() {
        return driver;
    }

    public Order getOrder() {
        return order;
    }
}
