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

public class VehicleRoadNetwork {
    private final HashMap<Integer, Coordinate>               nodes;
    private final HashMap<Integer, HashMap<Integer, Double>> edges;
    private final HashMap<Long, Integer>                    geoMap;
    private final HashMap<Integer, Long>                   rgeoMap;
    private final KdTree                                    kdTree;

    public VehicleRoadNetwork(){
        this.nodes   = new HashMap<>();
        this.edges   = new HashMap<>();
        this.kdTree  = new KdTree();
        this.geoMap  = new HashMap<>();
        this.rgeoMap = new HashMap<>();
    }

    public void InitNetwork(String nodeFileName, String edgeFileName, boolean reversed){
        CsvUtil csvUtil = new CsvUtil();
        List<RowElement> rows = csvUtil.readCsvFile(nodeFileName);
        for(RowElement row: rows){
            int     Id    = Integer.parseInt(row.col(0));
            long    geoId = Long.parseLong(row.col(1));
            double  lat   = Double.parseDouble(row.col(2));
            double  lon   = Double.parseDouble(row.col(3));
            this.nodes.put(Id, new Coordinate(lat, lon));
            this.geoMap.put(geoId, Id);
            this.rgeoMap.put(Id, geoId);
            this.kdTree.addCoordinate(Id, new Coordinate(lat, lon));
            this.edges.put(Id, new HashMap<>());
        }
        rows = csvUtil.readCsvFile(edgeFileName);
        for(RowElement row: rows){
            int     origin      = Integer.parseInt(row.col(0));
            int     destination = Integer.parseInt(row.col(1));
            double  travelTime  = Double.parseDouble(row.col(2));
            if(reversed){
                this.edges.get(destination).put(origin, travelTime);
            }else{
                this.edges.get(origin).put(destination, travelTime);
            }
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

    public HashMap<Integer, Path> shortestPathDistance(int origin, int destination, double range){
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


    public boolean checkGeoID(long geoID){
        return this.geoMap.containsKey(geoID);
    }


    public int addVirtualOrigin(HashMap<Integer, Double> travelTimes){
        int virtualOrigin = Integer.MAX_VALUE;
        this.edges.put(virtualOrigin, new HashMap<>());
        for(int destination: travelTimes.keySet()){
            this.edges.get(virtualOrigin).put(destination, travelTimes.get(destination));
        }
        return virtualOrigin;
    }

    public void delVirtualOrigin(){
        int virtualOrigin = Integer.MAX_VALUE;
        this.edges.remove(virtualOrigin);
    }


    public int addVirtualDestination(HashMap<Integer, Double> travelTimes){
        int virtualDestination = Integer.MAX_VALUE - 1;
        for(int origin: travelTimes.keySet()){
            this.edges.get(origin).put(virtualDestination, travelTimes.get(origin));
        }
        this.edges.put(virtualDestination, new HashMap<>());
        return virtualDestination;
    }


    public void delVirtualDestination(HashMap<Integer, Double> travelTimes){
        int virtualDestination = Integer.MAX_VALUE - 1;
        for(int origin: travelTimes.keySet()){
            this.edges.get(origin).remove(virtualDestination);
        }
        this.edges.remove(virtualDestination);
    }


    public Coordinate getCoordinate(int node){
        return this.nodes.get(node);
    }


    public HashMap<Integer, Double> turnGeo2VID(HashMap<Long, Double> pTravelTime){
        HashMap<Integer, Double> travelTime = new HashMap<>();
        for(Long geoId: pTravelTime.keySet()){
            if(this.geoMap.containsKey(geoId)){
                travelTime.put(this.geoMap.get(geoId), pTravelTime.get(geoId));
            }
        }
        return travelTime;
    }


    public Long getGeoId(int node){
        return this.rgeoMap.get(node);
    }

    public HashMap<Long, Integer> getGeoMap() {
        return geoMap;
    }

    public HashMap<Integer, Coordinate> getNodes(){
        return this.nodes;
    }
}
