package LogUtil;

import CsvUtil.CsvUtil;
import com.alibaba.fastjson.JSON;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;

public class LogUtil {
    private String recordPath;
    private final HashMap<String, Double>  runTimes;
    private final HashMap<String, HashMap<Integer, Long>>  wRunTimes;
    private final HashMap<String, HashMap<Integer, Long>>  tRunTimes;
    private final HashMap<String, HashMap<Integer, Long>>  Memory;

    public LogUtil(){
        this.runTimes = new HashMap<>();
        wRunTimes = new HashMap<>();
        tRunTimes = new HashMap<>();
        Memory = new HashMap<>();
    }

    public void saveRunTime(String key, double runtime){
        runTimes.put(key, runtime);
    }

    public void InitRecordPath(String mRecordPath){
        recordPath = mRecordPath;
    }

    public double getScoreSum(LogSet logSet){
        double scoreSum = 0.0;
        for(int i=0; i<logSet.size(); i++){
            scoreSum += logSet.getLog(i).getMatchScore();
        }
        return scoreSum;
    }


    public void setWRunTimes(String matchType, int workerNum, long runTime){
        if(!this.wRunTimes.containsKey(matchType)){
            this.wRunTimes.put(matchType, new HashMap<>());
        }
        this.wRunTimes.get(matchType).put(workerNum, runTime);
    }


    public void setTRunTimes(String matchType, int taskNum, long runTime){
        if(!this.tRunTimes.containsKey(matchType)){
            this.tRunTimes.put(matchType, new HashMap<>());
        }
        this.tRunTimes.get(matchType).put(taskNum, runTime);
    }

    public void setMemory(String matchType, int taskNum, long memory){
        if(!this.Memory.containsKey(matchType)){
            this.Memory.put(matchType, new HashMap<>());
        }
        this.Memory.get(matchType).put(taskNum, memory);
    }

    public int getMatchedNum(LogSet logSet){
        int num = 0;
        for(int i=0; i<logSet.size(); i++){
            if(logSet.getLog(i).getDriverId() != -1 && logSet.getLog(i).getOrderId() != -1) num += 1;
        }
        return num;
    }


    public void saveWRuntime(String cityName){
        for(String matchType: this.wRunTimes.keySet()) {
            String fileName = cityName + "_" + matchType + "_" + "worker.json";
            fileName = this.combine(this.recordPath, fileName);
            String json = JSON.toJSONString(this.wRunTimes.get(matchType));
            try {
                File file = new File(fileName);
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(json);
                fileWriter.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public void saveTRunTime(String cityName){
        for(String matchType: this.tRunTimes.keySet()) {
            String fileName = cityName + "_" + matchType + "_" + "task.json";
            fileName = this.combine(this.recordPath, fileName);
            String json = JSON.toJSONString(this.tRunTimes.get(matchType));
            try {
                File file = new File(fileName);
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(json);
                fileWriter.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public void saveMemory(String cityName){
        for(String matchType: this.Memory.keySet()) {
            String fileName = cityName + "_" + matchType + "_" + "memory.json";
            fileName = this.combine(this.recordPath, fileName);
            String json = JSON.toJSONString(this.Memory.get(matchType));
            try {
                File file = new File(fileName);
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(json);
                fileWriter.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }


    public void recordLogSet(LogSet logSet, String fileName){
        String combinedFile = combine(recordPath, fileName);
        CsvUtil csvUtil = new CsvUtil();
        csvUtil.saveCsvFile(logSet.getRowElements(), combinedFile);
    }


    public void turnRunTime2File(String fileName){
        fileName = this.combine(this.recordPath, fileName);
        String json = JSON.toJSONString(this.runTimes);
        try {
            File file = new File(fileName);
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(json);
            fileWriter.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }


    public String combine(String path1, String path2){
        File file1 = new File(path1);
        File file2 = new File(file1, path2);
        return file2.getPath();
    }
}
