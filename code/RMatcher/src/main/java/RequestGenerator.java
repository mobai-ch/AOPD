import CsvUtil.CsvUtil;
import NetWork.Element.Coordinate;
import NetWork.PedestrianRoadNetwork;
import NetWork.SearchUtil.PVUtil;
import NetWork.VehicleRoadNetwork;
import RankUtil.ScoreComputer;
import Request.Request;
import CsvUtil.RowElement;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RequestGenerator {
    private final CsvUtil                 csvUtil;
    private long                          currentNetworkTime;
    private VehicleRoadNetwork            vehicleRoadNetwork;
    private VehicleRoadNetwork            reversedRoadNetwork;
    private PedestrianRoadNetwork         pedestrianRoadNetwork;
    private VehicleRoadNetwork            staticVehicleRoadNetwork;
    private PVUtil                        pvUtil;
    private final String                  baseFilePath;
    private String                        currentDate;
    private ScoreComputer                 scoreComputer;


    public RequestGenerator(String baseFilePath){
        this.baseFilePath             = baseFilePath;
        this.csvUtil                  = new CsvUtil();
        this.pedestrianRoadNetwork    = new PedestrianRoadNetwork(1.5);
        this.reversedRoadNetwork      = new VehicleRoadNetwork();
        this.vehicleRoadNetwork       = new VehicleRoadNetwork();
        this.currentNetworkTime       = -1;
        this.staticVehicleRoadNetwork = new VehicleRoadNetwork();
        this.scoreComputer            = new ScoreComputer();
    }


    public void upDateNetwork(long time, int timeZone, String date, boolean ifUpdateFrequently){

        String realTimeDir = this.combine(this.baseFilePath, "Graph/GraphWithEnv/drive_");
        boolean ifUpdate = true;

        int timeSlice    = (int) (((time + timeZone * 60 * 60) / 60 % (60 * 24)) / 30);
        if(this.currentNetworkTime != -1){
            int currentSlice = (int) (((this.currentNetworkTime + timeZone * 60 * 60) / 60 % (60 * 24)) / 30);
            if(timeSlice == currentSlice && currentDate.equals(date)){
                ifUpdate = false;
            }
        }
        this.currentDate = date;
        if(ifUpdate){
            if(this.currentNetworkTime == -1){

                String pedestrianNodeFile       = this.combine(this.baseFilePath, "Graph/OriginGraph/wnode.csv");
                String pedestrianEdgeFile       = this.combine(this.baseFilePath, "Graph/OriginGraph/walk.csv");
                String staticVehicleNodeFile    = this.combine(this.baseFilePath, "Graph/OriginGraph/dnode.csv");
                String staticVehicleEdgeFile    = this.combine(this.baseFilePath, "Graph/OriginGraph/drive.csv");
                this.pedestrianRoadNetwork      = new PedestrianRoadNetwork(1.5);
                this.pedestrianRoadNetwork.InitNetwork(pedestrianNodeFile, pedestrianEdgeFile);
                this.staticVehicleRoadNetwork   = new VehicleRoadNetwork();
                this.staticVehicleRoadNetwork.InitNetwork(staticVehicleNodeFile, staticVehicleEdgeFile, false);
                this.pvUtil                     = new PVUtil(staticVehicleRoadNetwork.getGeoMap(),
                        pedestrianRoadNetwork.getGeoMap(), staticVehicleRoadNetwork.getNodes());
            }
            if(this.currentNetworkTime != -1 && !ifUpdateFrequently) return;

            this.currentNetworkTime = time;
            int startTime = timeSlice * 30;
            int endTime = timeSlice * 30 + 30;
            String vehEdgeFile = realTimeDir + String.format("%s_%d_%d.csv", date, startTime, endTime);
            String vehNodeFile = this.combine(this.baseFilePath, "Graph/OriginGraph/dnode.csv");
            this.vehicleRoadNetwork = new VehicleRoadNetwork();
            this.reversedRoadNetwork = new VehicleRoadNetwork();
            this.vehicleRoadNetwork.InitNetwork(vehNodeFile, vehEdgeFile, false);
            this.reversedRoadNetwork.InitNetwork(vehNodeFile, vehEdgeFile, true);
        }
    }

    // 获取一组请求数据
    public List<Request> getRequests(String date, double start, double end, int timeZone, int num,
                                     int maxPResponseTime, int maxVResponseTime, double maxWalkingTime){
        List<Request> requests = new ArrayList<Request>();
        String requestFile = "order_01-30/total_ride_request/order_" + date;
        requestFile = this.combine(baseFilePath, requestFile);
        List<RowElement> rows = this.csvUtil.readCsvFile(requestFile);
        int pos = 0, orderNum = 0;
        for(RowElement row: rows){
            long startTime   = Long.parseLong(row.col(1));
            long endTime     = Long.parseLong(row.col(2));
            double originLon = Double.parseDouble(row.col(3));
            double originLat = Double.parseDouble(row.col(4));
            double destLon   = Double.parseDouble(row.col(5));
            double destLat   = Double.parseDouble(row.col(6));
            double tempStart = (startTime + timeZone * 60 * 60) / 60 % (60 * 24);
            double tempEnd   = (endTime + timeZone * 60 * 60) / 60 % (60 * 24);
            if(tempStart > start && tempStart < end){

                Request orderRequest = new Request(1, pos, startTime, maxPResponseTime, maxWalkingTime);
                orderRequest.getCoordinateInfo().put("origin", new Coordinate(originLat, originLon));
                orderRequest.getCoordinateInfo().put("destination", new Coordinate(destLat, destLon));
                requests.add(orderRequest);
                orderNum += 1;
            }
            if(tempEnd > start && tempEnd < end){

                Request vehicleRequest = new Request(0, pos, endTime, maxVResponseTime, maxWalkingTime);
                vehicleRequest.getCoordinateInfo().put("origin", new Coordinate(destLat, destLon));
                requests.add(vehicleRequest);
            }
            pos += 1;

            if(orderNum >= num){ break; }
        }
        Collections.sort(requests);
        return requests;
    }


    public VehicleRoadNetwork getVehicleRoadNetWork(){
        return this.vehicleRoadNetwork;
    }


    public VehicleRoadNetwork getReversedRoadNetwork(){
        return this.reversedRoadNetwork;
    }

    public PedestrianRoadNetwork getPedestrianRoadNetwork(){
        return this.pedestrianRoadNetwork;
    }

    public VehicleRoadNetwork getStaticVehicleRoadNetwork() { return this.staticVehicleRoadNetwork; }

    public PVUtil getPvUtil(){ return this.pvUtil; }

    public ScoreComputer getScoreComputer(){return this.scoreComputer; }


    public String combine(String path1,String path2){
        File file1 = new File(path1);
        File file2 = new File(file1, path2);
        return file2.getPath();
    }

    public void InitScoreComputer(String fileName){
        String path = baseFilePath + "/Hotness/" + fileName;
        this.scoreComputer.InitHotness(path);
    }

}
