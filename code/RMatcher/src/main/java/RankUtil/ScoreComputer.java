package RankUtil;

import CsvUtil.CsvUtil;
import CsvUtil.RowElement;
import NetWork.Element.Coordinate;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;

public class ScoreComputer {
    private final HashMap<Integer, HashMap<Integer, Double>> hotness;
    private final CsvUtil csvUtil;

    public ScoreComputer(){
        hotness = new HashMap<>();
        csvUtil = new CsvUtil();
    }

    // Initialize the hotness
    public void InitHotness(String hotnessFile){
        List<RowElement> rowElements = csvUtil.readCsvFile(hotnessFile);
        for(RowElement rowElement: rowElements){
            String[] data = rowElement.getData();
            int nodeId = (int)Double.parseDouble(data[0]);
            int angleInfo = (int)Double.parseDouble(data[1]);
            double hotInfo = Double.parseDouble(data[2]);
            if(!hotness.containsKey(nodeId)){
                hotness.put(nodeId, new HashMap<>());
            }
            hotness.get(nodeId).put(angleInfo, hotInfo);
        }
    }

    // Compute the score
    public double computeScore(int nodeId, Coordinate pickUpPoint, Coordinate destination, double pick2DesDistance, double pick2DesTravelTime, double walkingTime){
        if(!hotness.containsKey(nodeId)){
            return 0.0;
        }else if(!hotness.get(nodeId).containsKey(computeAngle(pickUpPoint, destination))){
            return 0.0;
        }
        return hotness.get(nodeId).get(computeAngle(pickUpPoint, destination)) * 0.2 + 0.8 * (10/(walkingTime*1.25) + (pick2DesDistance*1.10 + pick2DesTravelTime * 0.2));
    }

    int computeAngle(Coordinate pickUpPoint, Coordinate destination){
        double ydiff = destination.getLatitude() - pickUpPoint.getLatitude();
        double xdiff = destination.getLongitude() - pickUpPoint.getLongitude();

        double temp = 0;

        if(xdiff >= 0 && ydiff >= 0) {
            temp = Math.atan2(ydiff, xdiff) / Math.PI * 180;
        } else if (xdiff <=0 && ydiff >= 0){
            temp = (Math.PI - Math.atan2(Math.abs(ydiff), xdiff)) / Math.PI * 180;
        }else if(xdiff <= 0 && ydiff <= 0) {
            temp = Math.atan2(Math.abs(ydiff), Math.abs(xdiff)) / Math.PI * 180 + 180;
        }else {
            temp = 360 - Math.atan2(Math.abs(ydiff), Math.abs(xdiff)) / Math.PI * 180;
        }

        return (int)(temp/30);
    }
}
