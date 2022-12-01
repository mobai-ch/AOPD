package NetWork.Element;

public class RouteElement {
    private final int     pickUp;
    private final int     dropOff;
    private final Driver  driver;
    private final Order   order;
    private final long    currentTime;

    public RouteElement(int pickUp, int dropOff, Driver driver, Order order, long currentTime){
        this.pickUp = pickUp;
        this.dropOff = dropOff;
        this.driver = driver;
        this.order = order;
        this.currentTime = currentTime;
    }


    public int getPickUp(){
        return this.pickUp;
    }


    public int getDropOff(){
        return this.dropOff;
    }


    public Driver getDriver(){
        return this.driver;
    }


    public Order getOrder(){
        return this.order;
    }


    public long getCurrentTime(){
        return this.currentTime;
    }
}
