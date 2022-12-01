package NetWork.SearchUtil;

import NetWork.Element.Coordinate;
import wcontour.KDTree;

public class KdTree {
    private final KDTree<Integer> data;

    public KdTree(){
        this.data = new KDTree.Euclidean<>(2);
    }


    public void addCoordinate(int node, Coordinate coordinate){
        double[] val = {coordinate.getLatitude(), coordinate.getLongitude()};
        this.data.addPoint(val, node);
    }


    public int findNearestNode(Coordinate coordinate){
        double[] input = {coordinate.getLatitude(), coordinate.getLongitude()};
        return this.data.nearestNeighbours(input, 1).get(0).payload;
    }
}
