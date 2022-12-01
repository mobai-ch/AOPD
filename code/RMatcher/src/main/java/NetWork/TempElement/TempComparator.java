package NetWork.TempElement;


public class TempComparator implements Comparable<TempComparator>{
    private final double travelTime;
    private final int    node;

    public TempComparator(int node, double travelTime){
        this.travelTime = travelTime;
        this.node = node;
    }


    public double getTravelTime() {
        return travelTime;
    }


    public int getNode(){
        return this.node;
    }

    @Override
    public int compareTo(TempComparator o) {
        return this.travelTime - o.getTravelTime() > 0 ? 1: -1;
    }
}
