package NetWork.Element;

import java.util.ArrayList;
import java.util.List;


public class Edge {
    private final List<Double>  weightSet;
    private final List<Integer> infoSet;


    public Edge(int weightNum, int infoNum){
        this.weightSet = new ArrayList<>();
        for(int i=0; i<weightNum; i++){
            this.weightSet.add(0.0);
        }
        this.infoSet = new ArrayList<>();
        for(int i=0; i<infoNum; i++){
            this.infoSet.add(0);
        }
    }

    public void setWeight(int index, double weight){
        this.weightSet.set(index, weight);
    }

    public void setInfo(int index, int info){
        this.infoSet.set(index, info);
    }

    public double getWeight(int pos){
        return this.weightSet.get(pos);
    }


    public int getInfo(int pos){
        return this.infoSet.get(pos);
    }
}
