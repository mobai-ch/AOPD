package NetWork.Element;

public class Coordinate {

    private final double latitude;
    private final double longitude;


    public Coordinate(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public double getLatitude(){
        return this.latitude;
    }

    public double getLongitude(){
        return this.longitude;
    }
}
