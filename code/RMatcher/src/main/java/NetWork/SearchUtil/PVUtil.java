package NetWork.SearchUtil;

import NetWork.Element.Coordinate;

import java.util.HashMap;

public class PVUtil {
    private final KdTree kdTree;
    private HashMap<Integer, Integer> vpMap;

    public PVUtil(HashMap<Long, Integer> VNodes, HashMap<Integer, Long> PNodes, HashMap<Integer, Coordinate> vCoors){
        kdTree = new KdTree();
        vpMap  = new HashMap<>();

        for(int pId: PNodes.keySet()){
            if(VNodes.containsKey(PNodes.get(pId))){
                int         vId         = VNodes.get(PNodes.get(pId));
                Coordinate  vCoordinate = vCoors.get(vId);
                kdTree.addCoordinate(vId, vCoordinate);
                vpMap.put(vId, pId);
            }
        }
    }


    public int findNearestNode(Coordinate coordinate){
        return kdTree.findNearestNode(coordinate);
    }

    public int findNearestPNode(Coordinate coordinate) {return this.vpMap.get(kdTree.findNearestNode(coordinate));}
}
