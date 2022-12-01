package NetWork;

import CsvUtil.CsvUtil;
import NetWork.Element.Coordinate;
import NetWork.Element.Path;
import NetWork.SearchUtil.KdTree;
import CsvUtil.RowElement;
import NetWork.TempElement.TempComparator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

public class PedestrianRoadNetwork {
    private final HashMap<Integer, Coordinate>               nodes;
    private final HashMap<Integer, HashMap<Integer, Double>> edges;
    private final HashMap<Integer, Long>                     geoMap;
    private final HashMap<Long, Integer>                    rgeoMap;
    private final Double                                     speed;
    private final KdTree                                     kdTree;

    public PedestrianRoadNetwork(double speed){
        this.nodes   = new HashMap<>();
        this.edges   = new HashMap<>();
        this.kdTree  = new KdTree();
        this.geoMap  = new HashMap<>();
        this.rgeoMap = new HashMap<>();
        this.speed   = speed;
    }


    public void InitNetwork(String nodeFileName, String edgeFileName){
        CsvUtil csvUtil = new CsvUtil();
        List<RowElement> rows = csvUtil.readCsvFile(nodeFileName);
        for(RowElement row: rows){
            int     Id    = Integer.parseInt(row.col(0));
            long    geoId = Long.parseLong(row.col(1));
            double  lat   = Double.parseDouble(row.col(2));
            double  lon   = Double.parseDouble(row.col(3));
            this.nodes.put(Id, new Coordinate(lat, lon));
            this.geoMap.put(Id, geoId);
            this.rgeoMap.put(geoId, Id);
            this.kdTree.addCoordinate(Id, new Coordinate(lat, lon));
            this.edges.put(Id, new HashMap<>());
        }
        rows = csvUtil.readCsvFile(edgeFileName);
        for(RowElement row: rows){
            int     origin      = Integer.parseInt(row.col(0));
            int     destination = Integer.parseInt(row.col(1));
            double  travelTime  = Double.parseDouble(row.col(2));
            this.edges.get(origin).put(destination, travelTime/speed);
        }
    }

    public HashMap<Integer, Path> shortestPath(int origin, int destination, double range){
        HashMap<Integer, Path>        paths   = new HashMap<>();
        HashSet<Integer>              visited = new HashSet<>();
        PriorityQueue<TempComparator> digger  = new PriorityQueue<>();

        Path path = new Path();
        path.travelTime = 0.0;
        path.addNode(origin);
        paths.put(origin, path);

        digger.add(new TempComparator(origin, 0.0));

        while (!digger.isEmpty()){

            TempComparator tempComparator = digger.poll();
            int node = tempComparator.getNode();

            if(node == destination){
                return paths;
            }

            if(visited.contains(node)){
                continue;
            }

            for(int neighbor: edges.get(node).keySet()){
                double travelTime = edges.get(node).get(neighbor);
                double nTravelTime = paths.get(node).travelTime + travelTime;
                if(nTravelTime < range && (!paths.containsKey(neighbor) || nTravelTime < paths.get(neighbor).travelTime)){
                    path = paths.get(node).copy();
                    path.travelTime = nTravelTime;
                    path.addNode(neighbor);
                    paths.put(neighbor, path);
                    digger.add(new TempComparator(neighbor, nTravelTime));
                }
            }

            visited.add(node);
        }

        return paths;
    }

    public void updateNetworkByRealTimeData(int origin, int destination, double travelTime){
        this.edges.get(origin).put(destination, travelTime);
    }

    public int findNearestNode(Coordinate coordinate){
        return this.kdTree.findNearestNode(coordinate);
    }

    public boolean checkGeoID(int geoID){
        return this.geoMap.containsKey(geoID);
    }

    public Coordinate getCoordinate(int node){ return this.nodes.get(node); }

    public HashMap<Long, Double> turnGeo2PID(HashMap<Integer, Path> paths){
        HashMap<Long, Double> travelTimes = new HashMap<>();
        for(Integer pId: paths.keySet()){
            long geoID = geoMap.get(pId);
            travelTimes.put(geoID, paths.get(pId).travelTime);
        }
        return travelTimes;
    }

    public int getPID(long geoId){
        return this.rgeoMap.get(geoId);
    }

    public HashMap<Integer, Long> getGeoMap() {
        return geoMap;
    }

}
