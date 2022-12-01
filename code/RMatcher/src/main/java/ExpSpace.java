import LogUtil.Log;
import LogUtil.LogUtil;
import NetWork.BipartiteGraph;
import NetWork.Element.*;
import NetWork.PedestrianRoadNetwork;
import NetWork.SearchUtil.KmUtil;
import NetWork.SearchUtil.MatchPair;
import NetWork.SearchUtil.PVUtil;
import NetWork.SearchUtil.SearchResult;
import NetWork.TempElement.TempComparator;
import NetWork.TripartiteGraph;
import NetWork.VehicleRoadNetwork;
import RankUtil.ScoreComputer;
import Request.Request;
import RequestProcessor.*;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class ExpSpace {

    public static void main(String[] args){
//        basicModelTest();
//        basicFunctionTest();
//        partExperimentCode();
//        KMTest();
//        for(int i = 50; i<=250; i+=50) {
//            experimentInDataset("chengdu", "20161101", 450, 540, 8, "greedy", i);
//            experimentInDataset("chengdu", "20161101", 450, 540, 8, "greedyT", i);
//            experimentInDataset("chengdu", "20161101", 450, 540, 8, "KMT", i);
//        }
//        getMaxMatchScore("chengdu", "20161101", 450, 540, 8);
//        PDExpInDifferentDay("chengdu", 8, "greedyT", 100, 100);
//        EnumPDAll("chengdu", 8, 900);
        EnumPDAllDiv("chengdu", 8, 900);
        EnumPDAllDiv("NYC", 0, 900);
        EnumPDAllTime("NYC", 0, 900);
        EnumPDAllTime("chengdu", 8, 900);
//        SinglePD("chengdu", 8, 900);
    }

    public static void EnumPDAllTime(String cityDir, int timeZone, long maxPickTime){
        String[] methods = {"KMT", "greedy", "greedyT", "BKM"};
        int   [] maxReps = {60, 120, 180, 240, 300};
        double[] walkTimes = {30, 60, 90, 120, 150};
        int[] timeS = {450, 720, 1020};
        int[] timeE = {540, 810, 1140};
        String[] defaultMethods = {"KMT", "BKM"};

        long date = 20161101;
        if(cityDir.equals("NYC")){
            date = 20141015;
        }

        int driveTimesDefault = 300;
        int timeSDefault = 450, timeEDefault = 540;
        int walkTimeDefault = 120;
        int respDefault = 120;

        for(int i=0; i<5; i++){
            anaTimeSPD(cityDir, timeZone, maxPickTime, date, "KMT", respDefault, walkTimes[i], timeSDefault, timeEDefault, driveTimesDefault);
        }

        for(int i=0; i<3; i++){
            anaTimeSPDEMA(cityDir, timeZone, maxPickTime, date, "KMT", respDefault, walkTimes[i], timeSDefault, timeEDefault, driveTimesDefault);
        }
//
        for(int i=3; i<5; i++){
            anaTimeSPDEMA(cityDir, timeZone, maxPickTime, date, "KMT", respDefault, walkTimes[i], timeSDefault, timeEDefault, driveTimesDefault);
        }
    }


    public static void EnumPDAllDiv(String cityDir, int timeZone, long maxPickTime){
        String[] methods = {"KMT", "greedy", "greedyT", "BKM"};
        int   [] maxReps = {30, 60, 90, 120, 150};
        double[] walkTimes  = {30, 60, 90, 120, 150};
        int[] driveTimes = {120, 300, 480, 600, 900};
        int[] timeS = {450, 720, 1020};
        int[] timeE = {540, 810, 1140};
//        String[] defaultMethods = {"greedy", "BKM"};
        String[] defaultMethods = {"greedy"};

        long date = 20161101;
        if(cityDir.equals("NYC")){
            date = 20141015;
        }

        int driveTimesDefault = 300;
        int timeSDefault = 450, timeEDefault = 540;
        int walkTimeDefault = 120;
        int respDefault = 120;


        for (int i=0; i<5; i++) {
            for (int j = 0; j < defaultMethods.length; j++) {
                OnePDResultDiv2(cityDir, timeZone, maxPickTime, date, defaultMethods[j], respDefault, walkTimeDefault, timeSDefault, timeEDefault, driveTimes[i]);
            }
        }


        for (int i=0; i<5; i++) {
            for (int j = 0; j < defaultMethods.length; j++) {
                OnePDResultDiv2(cityDir, timeZone, maxPickTime, date, defaultMethods[j], respDefault, walkTimes[i], timeSDefault, timeEDefault, driveTimesDefault);
            }
        }


        for (int i=0; i<3; i++) {
            for (int j = 0; j < defaultMethods.length; j++) {
                OnePDResultDiv2(cityDir, timeZone, maxPickTime, date, defaultMethods[j], respDefault, walkTimeDefault, timeS[i], timeE[i], driveTimesDefault);
            }
        }

        for (int j = 0; j < defaultMethods.length; j++) {
            if (cityDir.equals("NYC")) {
                for (int i = 20141013; i < 20141018; i++) {
                    OnePDResultDiv2(cityDir, timeZone, maxPickTime, i, defaultMethods[j], respDefault, walkTimeDefault, timeSDefault, timeEDefault, driveTimesDefault);
                }
            } else {
                for (int i = 20161101; i < 20161106; i++) {
                    OnePDResultDiv2(cityDir, timeZone, maxPickTime, i, defaultMethods[j], respDefault, walkTimeDefault, timeSDefault, timeEDefault, driveTimesDefault);
                }
            }
        }
//
        // Different response time
        for(int j=0; j<defaultMethods.length; j++) {
            for (int i = 0; i < 5; i++) {
                OnePDResultDiv2(cityDir, timeZone, maxPickTime, date, defaultMethods[0], maxReps[i], walkTimeDefault, timeSDefault, timeEDefault, driveTimesDefault);
            }
        }

    }

    public static void SinglePD(String cityDir, int timeZone, long maxPickTime){
        String[] methods = {"KMT", "greedy", "greedyT", "BKM"};
        int   [] maxReps = {60, 120, 180, 240, 300};
        double[] walkTimes = {30, 60, 90, 120, 150};
        int[] timeS = {450, 720, 1020};
        int[] timeE = {540, 810, 1140};
        String[] defaultMethods = {"KMT", "BKM"};

        long date = 20161101;
        if(cityDir.equals("NYC")){
            date = 20141015;
        }

        int driveTimesDefault = 300;
        int timeSDefault = 450, timeEDefault = 540;
        int walkTimeDefault = 120;
        int respDefault = 120;
        String defaultMethod = "greedy";

        OnePDResultDiv2(cityDir, timeZone, maxPickTime, date, defaultMethod, respDefault, walkTimeDefault, timeSDefault, timeEDefault, driveTimesDefault);

    }

    public static void OnePDResultDiv(String cityDir, int timeZone, long maxPickTime, long date,
                                   String method, int maxRep, double walkTime, int sTime, int eTime, int mDriveTime){
        RequestGenerator requestGenerator = new RequestGenerator("D:\\research\\crowdsourcing\\DataSet\\" + cityDir);

        String ts = Integer.toString(sTime);
        String te = Integer.toString(eTime);

        File file = new File("D:\\research\\crowdsourcing\\ExpRecords\\EndData\\" +
                cityDir+"-"+"PD"+"-"+ts+"-"+te+"-"+method+"-"
                + Integer.toString(maxRep) + "-" + Long.toString((long)walkTime) + "-"
                +Long.toString(maxPickTime)+"-"+Long.toString(date)+".csv");

        if(file.exists()){
            System.out.printf("File of %d %d:%d-%d:%d maxRep:%d walkTime: %d Date: %d method:%s is created\n",
                    date, (int)(sTime/60), sTime%60, (int)(eTime/60), eTime%60, maxRep, (long)walkTime, date, method);
            return;
        }

        List<Request> requests = requestGenerator.getRequests(Long.toString(date), sTime, eTime, timeZone,
                12000, maxRep, maxRep, walkTime);

        LogUtil logUtil = new LogUtil();
        logUtil.InitRecordPath("D:\\research\\crowdsourcing\\ExpRecords\\EndData\\");
        PDRequestProcessor   pdRequestProcessor   = new PDRequestProcessor();
        NPDRequestProcessorE npdRequestProcessor  = new NPDRequestProcessorE();
        NPNDRequestProcessorE npndRequestProcessor = new NPNDRequestProcessorE();

        long runTime_1 = 0, runTime_2 = 0, runTime_3 = 0;

        int requestVisited = 0;
        int workerNum = 0, taskNum = 0;
        Request tempRequest = null;

        // PD
        for(Request request: requests){
            tempRequest = request;
            requestGenerator.upDateNetwork(request.getRequestTime(), timeZone, Long.toString(date), true);
            PedestrianRoadNetwork pNetwork  = requestGenerator.getPedestrianRoadNetwork();
            VehicleRoadNetwork    vNetWork  = requestGenerator.getVehicleRoadNetWork();
            VehicleRoadNetwork    rvNetwork = requestGenerator.getReversedRoadNetwork();
            PVUtil                pvUtil    = requestGenerator.getPvUtil();

            pdRequestProcessor.UpdateNetwork(pNetwork, vNetWork, rvNetwork, pvUtil);

            long time_1 = System.currentTimeMillis();
            pdRequestProcessor.ProcessRequest(request, method, mDriveTime);
            long time_2 = System.currentTimeMillis();

            long Time_1 = time_2 - time_1;

            if(request.getType() == 0){
                workerNum += 1;
                logUtil.setWRunTimes(method + "_" + "PD", workerNum, Time_1);
            }else{
                taskNum += 1;
                logUtil.setTRunTimes(method + "_" + "PD", taskNum, Time_1);
            }

            runTime_1 += Time_1;

            if(requestVisited % 100 == 0){
                double pdScore   = logUtil.getScoreSum(pdRequestProcessor.getLogSet());
                int    pdNum     = logUtil.getMatchedNum(pdRequestProcessor.getLogSet());
                System.out.printf("request num: %d, PD score: %f, PD number: %d, PD average: %f\n",
                        requestVisited, pdScore, pdNum, pdScore/pdNum);
            }
            requestVisited += 1;
        }

        if(method.equals("BKM") && tempRequest != null){
            pdRequestProcessor.OfflineKM(tempRequest);
        }

        logUtil.recordLogSet(pdRequestProcessor.getLogSet(),
                cityDir+"-"+"PD"+"-"+ts+"-"+te+"-"+method+"-"
                        + Integer.toString(maxRep) + "-" + Long.toString((long)walkTime) + "-"
                        +Long.toString(mDriveTime)+"-"+Long.toString(date)+".csv");

        if(method.equals("BKM") && tempRequest != null){
            logUtil.saveTRunTime(cityDir);
            logUtil.saveWRuntime(cityDir);
            return;
        }

        pdRequestProcessor = new PDRequestProcessor();
        requestVisited = 0; workerNum = 0; taskNum = 0; tempRequest = null;

        // NPD
        for(Request request: requests){
            tempRequest = request;
            requestGenerator.upDateNetwork(request.getRequestTime(), timeZone, Long.toString(date), true);
            PedestrianRoadNetwork pNetwork  = requestGenerator.getPedestrianRoadNetwork();
            VehicleRoadNetwork    vNetWork  = requestGenerator.getVehicleRoadNetWork();
            PVUtil                pvUtil    = requestGenerator.getPvUtil();

            npdRequestProcessor.UpdateNetwork(pNetwork, vNetWork, pvUtil);

            long time_2 = System.currentTimeMillis();
            npdRequestProcessor.ProcessRequest(request, method, mDriveTime);
            long time_3 = System.currentTimeMillis();

            long Time_2 = time_3 - time_2;

            if(request.getType() == 0){
                workerNum += 1;
                logUtil.setWRunTimes(method + "_" + "NPD", workerNum, Time_2);
            }else{
                taskNum += 1;
                logUtil.setTRunTimes(method + "_" + "NPD", taskNum, Time_2);
            }

            runTime_2 += Time_2;

            if(requestVisited % 100 == 0){
                double npdScore  = logUtil.getScoreSum(npdRequestProcessor.getLogSet());
                int    npdNum    = logUtil.getMatchedNum(npdRequestProcessor.getLogSet());
                System.out.printf("request num: %d, NPD score: %f, NPD number: %d, NPD average: %f\n",
                        requestVisited, npdScore, npdNum, npdScore/npdNum);
            }
            requestVisited += 1;
        }

        logUtil.recordLogSet(npdRequestProcessor.getLogSet(),
                cityDir+"-"+"NPD"+"-"+ts+"-"+te+"-"+method+"-"
                        + Integer.toString(maxRep) + "-" + Long.toString((long)walkTime) + "-"
                        +Long.toString(mDriveTime)+"-"+Long.toString(date)+".csv");

        npdRequestProcessor = new NPDRequestProcessorE();
        requestVisited = 0; workerNum = 0; taskNum = 0; tempRequest = null;

        // NPND
        for(Request request: requests){
            tempRequest = request;
            requestGenerator.upDateNetwork(request.getRequestTime(), timeZone, Long.toString(date), true);
            PedestrianRoadNetwork pNetwork  = requestGenerator.getPedestrianRoadNetwork();
            VehicleRoadNetwork    vNetWork  = requestGenerator.getVehicleRoadNetWork();
            PVUtil                pvUtil    = requestGenerator.getPvUtil();

            npndRequestProcessor.UpdateNetwork(pNetwork, vNetWork, pvUtil);

            long time_3 = System.currentTimeMillis();
            npndRequestProcessor.ProcessRequest(request, method, mDriveTime);
            long time_4 = System.currentTimeMillis();

            long Time_3 = time_4 - time_3;

            if(request.getType() == 0){
                workerNum += 1;
                logUtil.setWRunTimes(method + "_" + "NPND", workerNum, Time_3);
            }else{
                taskNum += 1;
                logUtil.setTRunTimes(method + "_" + "NPND", taskNum, Time_3);
            }

            runTime_3 += Time_3;

            if(requestVisited % 100 == 0){
                double npndScore = logUtil.getScoreSum(npndRequestProcessor.getLogSet());
                int    npndNum   = logUtil.getMatchedNum(npndRequestProcessor.getLogSet());
                System.out.printf("request num: %d NPND score: %f; NPND number: %d; NPND average: %f;\n",
                        requestVisited, npndScore, npndNum, npndScore/npndNum);
            }
            requestVisited += 1;
        }

        logUtil.recordLogSet(npndRequestProcessor.getLogSet(),
                cityDir+"-"+"NPND"+"-"+ts+"-"+te+"-"+method+"-"
                        + Integer.toString(maxRep) + "-" + Long.toString((long)walkTime) + "-"
                        +Long.toString(mDriveTime)+"-"+Long.toString(date)+".csv");

        logUtil.saveTRunTime(cityDir);
        logUtil.saveWRuntime(cityDir);

        System.out.printf("The number of request: %d, average pd runTime: %d ms, average npd runTime: %d ms, average npnd runTime: %d ms\n",
                requests.size(), runTime_1/requests.size(), runTime_2/requests.size(), runTime_3/requests.size());
//
        System.out.printf("File of %d %d:%d-%d:%d maxRep:%d walkTime: %d Date: %d method:%s is created\n",
                date, (int)(sTime/60), sTime%60, (int)(eTime/60), eTime%60, maxRep, (long)walkTime, date, method);
    }

    public static void anaTimeSPD(String cityDir, int timeZone, long maxPickTime, long date,
                                      String method, int maxRep, double walkTime, int sTime, int eTime, int mDriveTime){
        RequestGenerator requestGenerator = new RequestGenerator("D:\\research\\crowdsourcing\\DataSet\\" + cityDir);

        List<Request> requests = requestGenerator.getRequests(Long.toString(date), sTime, eTime, timeZone,
                12000, maxRep, maxRep, walkTime);

        LogUtil logUtil = new LogUtil();
        logUtil.InitRecordPath("D:\\research\\crowdsourcing\\ExpRecords\\EndData\\");
        PDRequestProcessor   pdRequestProcessor   = new PDRequestProcessor();

        int requestVisited = 0;
        int workerNum = 0, taskNum = 0;
        Request tempRequest = null;

        // PD
        for(int i=0; i<12000; i++){
            Request request = requests.get(i);
            tempRequest = request;
            requestGenerator.upDateNetwork(request.getRequestTime(), timeZone, Long.toString(date), true);
            PedestrianRoadNetwork pNetwork  = requestGenerator.getPedestrianRoadNetwork();
            VehicleRoadNetwork    vNetWork  = requestGenerator.getVehicleRoadNetWork();
            VehicleRoadNetwork    rvNetwork = requestGenerator.getReversedRoadNetwork();
            PVUtil                pvUtil    = requestGenerator.getPvUtil();

            pdRequestProcessor.UpdateNetwork(pNetwork, vNetWork, rvNetwork, pvUtil);

            long time_1 = System.currentTimeMillis();
            pdRequestProcessor.ProcessRequest(request, method, mDriveTime);
            long time_2 = System.currentTimeMillis();

            long Time_1 = time_2 - time_1;
            MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
            MemoryUsage memoryUsage = bean.getHeapMemoryUsage();

            int nWalkTime = (int) walkTime;

            // 保存运算时间
            if(request.getType() == 0){
                workerNum += 1;
                logUtil.setWRunTimes(method + Integer.toString(nWalkTime) + "-PD", workerNum, Time_1);
            }else{
                taskNum += 1;
                logUtil.setTRunTimes(method + Integer.toString(nWalkTime) + "_" + "PD", taskNum, Time_1);
                logUtil.setMemory(method + Integer.toString(nWalkTime) + "_" + "PD", taskNum, (long)memoryUsage.getUsed());
            }

            if(requestVisited % 100 == 0){
                double pdScore   = logUtil.getScoreSum(pdRequestProcessor.getLogSet());
                int    pdNum     = logUtil.getMatchedNum(pdRequestProcessor.getLogSet());
                System.out.printf("request num: %d, PD score: %f, PD number: %d, PD average: %f\n",
                        requestVisited, pdScore, pdNum, pdScore/pdNum);
            }
            requestVisited += 1;
        }

        logUtil.saveTRunTime(cityDir);
        logUtil.saveWRuntime(cityDir);
        logUtil.saveMemory(cityDir);
    }

    public static void anaTimeSPDEMA(String cityDir, int timeZone, long maxPickTime, long date,
                                  String method, int maxRep, double walkTime, int sTime, int eTime, int mDriveTime){
        RequestGenerator requestGenerator = new RequestGenerator("D:\\research\\crowdsourcing\\DataSet\\" + cityDir);

        List<Request> requests = requestGenerator.getRequests(Long.toString(date), sTime, eTime, timeZone,
                12000, maxRep, maxRep, walkTime);

        LogUtil logUtil = new LogUtil();
        logUtil.InitRecordPath("D:\\research\\crowdsourcing\\ExpRecords\\EndData\\");
        PDRequestProcessorEMA   pdRequestProcessorema   = new PDRequestProcessorEMA();

        int requestVisited = 0;
        int workerNum = 0, taskNum = 0;
        Request tempRequest = null;

        // PD
        for(int i=0; i<12000; i++){
            Request request = requests.get(i);
            tempRequest = request;
            requestGenerator.upDateNetwork(request.getRequestTime(), timeZone, Long.toString(date), true);
            PedestrianRoadNetwork pNetwork  = requestGenerator.getPedestrianRoadNetwork();
            VehicleRoadNetwork    vNetWork  = requestGenerator.getVehicleRoadNetWork();
            VehicleRoadNetwork    rvNetwork = requestGenerator.getReversedRoadNetwork();
            PVUtil                pvUtil    = requestGenerator.getPvUtil();

            pdRequestProcessorema.UpdateNetwork(pNetwork, vNetWork, rvNetwork, pvUtil);

            // 获取处理开始时间和结束时间
            long time_1 = System.currentTimeMillis();
            pdRequestProcessorema.ProcessRequest(request, method, mDriveTime);
            long time_2 = System.currentTimeMillis();

            long Time_1 = time_2 - time_1;
            MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
            MemoryUsage memoryUsage = bean.getHeapMemoryUsage();

            int nWalkTime = (int) walkTime;

            // 保存运算时间
            if (request.getType() == 0) {
                workerNum += 1;
                logUtil.setWRunTimes(method + Integer.toString(nWalkTime) + "-EMA", workerNum, Time_1);
            } else {
                taskNum += 1;
                logUtil.setTRunTimes(method + Integer.toString(nWalkTime) + "_" + "EMA", taskNum, Time_1);
                logUtil.setMemory(method + Integer.toString(nWalkTime) + "_" + "EMA", taskNum, (long) memoryUsage.getUsed());
            }

            if (requestVisited % 100 == 0) {
                double pdScore = logUtil.getScoreSum(pdRequestProcessorema.getLogSet());
                int pdNum = logUtil.getMatchedNum(pdRequestProcessorema.getLogSet());
                System.out.printf("request num: %d, PD score: %f, PD number: %d, PD average: %f\n",
                        requestVisited, pdScore, pdNum, pdScore / pdNum);
            }
            requestVisited += 1;
        }

        logUtil.saveTRunTime(cityDir);
        logUtil.saveWRuntime(cityDir);
        logUtil.saveMemory(cityDir);
    }


    public static void OnePDResultDiv2(String cityDir, int timeZone, long maxPickTime, long date,
                                      String method, int maxRep, double walkTime, int sTime, int eTime, int mDriveTime){
        RequestGenerator requestGenerator = new RequestGenerator("D:\\research\\crowdsourcing\\DataSet\\" + cityDir);

        String ts = Integer.toString(sTime);
        String te = Integer.toString(eTime);

        File file = new File("D:\\research\\crowdsourcing\\ExpRecords\\EndData\\" +
                cityDir+"-"+"PD"+"-"+ts+"-"+te+"-"+method+"-"
                + Integer.toString(maxRep) + "-" + Long.toString((long)walkTime) + "-"
                +Long.toString(maxPickTime)+"-"+Long.toString(date)+".csv");

        if(file.exists()){
            System.out.printf("File of %d %d:%d-%d:%d maxRep:%d walkTime: %d Date: %d method:%s is created\n",
                    date, (int)(sTime/60), sTime%60, (int)(eTime/60), eTime%60, maxRep, (long)walkTime, date, method);
            return;
        }

        List<Request> requests = requestGenerator.getRequests(Long.toString(date), sTime, eTime, timeZone,
                12000, maxRep, maxRep, walkTime);

        LogUtil logUtil = new LogUtil();
        logUtil.InitRecordPath("D:\\research\\crowdsourcing\\ExpRecords\\EndData\\");
        PDRequestProcessor   pdRequestProcessor   = new PDRequestProcessor();
        NPDRequestProcessorPT npdRequestProcessor  = new NPDRequestProcessorPT();
        NPNDRequestProcessorPT npndRequestProcessor = new NPNDRequestProcessorPT();

        long runTime_1 = 0, runTime_2 = 0, runTime_3 = 0;

        int requestVisited = 0;
        int workerNum = 0, taskNum = 0;
        Request tempRequest = null;

        // PD
        for(Request request: requests){
            tempRequest = request;
            requestGenerator.upDateNetwork(request.getRequestTime(), timeZone, Long.toString(date), true);
            PedestrianRoadNetwork pNetwork  = requestGenerator.getPedestrianRoadNetwork();
            VehicleRoadNetwork    vNetWork  = requestGenerator.getVehicleRoadNetWork();
            VehicleRoadNetwork    rvNetwork = requestGenerator.getReversedRoadNetwork();
            PVUtil                pvUtil    = requestGenerator.getPvUtil();

            pdRequestProcessor.UpdateNetwork(pNetwork, vNetWork, rvNetwork, pvUtil);


            long time_1 = System.currentTimeMillis();
            pdRequestProcessor.ProcessRequest(request, method, mDriveTime);
            long time_2 = System.currentTimeMillis();

            long Time_1 = time_2 - time_1;


            if(request.getType() == 0){
                workerNum += 1;
                logUtil.setWRunTimes(method + "_" + "PD", workerNum, Time_1);
            }else{
                taskNum += 1;
                logUtil.setTRunTimes(method + "_" + "PD", taskNum, Time_1);
            }


            runTime_1 += Time_1;

            if(requestVisited % 100 == 0){
                double pdScore   = logUtil.getScoreSum(pdRequestProcessor.getLogSet());
                int    pdNum     = logUtil.getMatchedNum(pdRequestProcessor.getLogSet());
                System.out.printf("request num: %d, PD score: %f, PD number: %d, PD average: %f\n",
                        requestVisited, pdScore, pdNum, pdScore/pdNum);
            }
            requestVisited += 1;
        }

        if(method.equals("BKM") && tempRequest != null){
            pdRequestProcessor.OfflineKM(tempRequest);
        }


        logUtil.recordLogSet(pdRequestProcessor.getLogSet(),
                cityDir+"-"+"PD"+"-"+ts+"-"+te+"-"+method+"-"
                        + Integer.toString(maxRep) + "-" + Long.toString((long)walkTime) + "-"
                        +Long.toString(mDriveTime)+"-"+Long.toString(date)+".csv");

        if(method.equals("BKM") && tempRequest != null){
            logUtil.saveTRunTime(cityDir);
            logUtil.saveWRuntime(cityDir);
            return;
        }


        pdRequestProcessor = new PDRequestProcessor();
        requestVisited = 0; workerNum = 0; taskNum = 0; tempRequest = null;

        // NPD
        for(Request request: requests){
            tempRequest = request;
            requestGenerator.upDateNetwork(request.getRequestTime(), timeZone, Long.toString(date), true);
            PedestrianRoadNetwork pNetwork  = requestGenerator.getPedestrianRoadNetwork();
            VehicleRoadNetwork    vNetWork  = requestGenerator.getVehicleRoadNetWork();
            PVUtil                pvUtil    = requestGenerator.getPvUtil();
            VehicleRoadNetwork    rvNetwork = requestGenerator.getReversedRoadNetwork();

            npdRequestProcessor.UpdateNetwork(pNetwork, vNetWork, rvNetwork, pvUtil);


            long time_2 = System.currentTimeMillis();
            npdRequestProcessor.ProcessRequest(request, method, mDriveTime);
            long time_3 = System.currentTimeMillis();

            long Time_2 = time_3 - time_2;


            if(request.getType() == 0){
                workerNum += 1;
                logUtil.setWRunTimes(method + "_" + "NPD", workerNum, Time_2);
            }else{
                taskNum += 1;
                logUtil.setTRunTimes(method + "_" + "NPD", taskNum, Time_2);
            }


            runTime_2 += Time_2;

            if(requestVisited % 100 == 0){
                double npdScore  = logUtil.getScoreSum(npdRequestProcessor.getLogSet());
                int    npdNum    = logUtil.getMatchedNum(npdRequestProcessor.getLogSet());
                System.out.printf("request num: %d, NPD score: %f, NPD number: %d, NPD average: %f\n",
                        requestVisited, npdScore, npdNum, npdScore/npdNum);
            }
            requestVisited += 1;
        }

        logUtil.recordLogSet(npdRequestProcessor.getLogSet(),
                cityDir+"-"+"NPD"+"-"+ts+"-"+te+"-"+method+"-"
                        + Integer.toString(maxRep) + "-" + Long.toString((long)walkTime) + "-"
                        +Long.toString(mDriveTime)+"-"+Long.toString(date)+".csv");


        npdRequestProcessor = new NPDRequestProcessorPT();
        requestVisited = 0; workerNum = 0; taskNum = 0; tempRequest = null;

        // NPND
        for(Request request: requests){
            tempRequest = request;
            requestGenerator.upDateNetwork(request.getRequestTime(), timeZone, Long.toString(date), true);
            PedestrianRoadNetwork pNetwork  = requestGenerator.getPedestrianRoadNetwork();
            VehicleRoadNetwork    vNetWork  = requestGenerator.getVehicleRoadNetWork();
            PVUtil                pvUtil    = requestGenerator.getPvUtil();
            VehicleRoadNetwork    rvNetwork = requestGenerator.getReversedRoadNetwork();

            npndRequestProcessor.UpdateNetwork(pNetwork, vNetWork, rvNetwork, pvUtil);


            long time_3 = System.currentTimeMillis();
            npndRequestProcessor.ProcessRequest(request, method, mDriveTime);
            long time_4 = System.currentTimeMillis();

            long Time_3 = time_4 - time_3;


            if(request.getType() == 0){
                workerNum += 1;
                logUtil.setWRunTimes(method + "_" + "NPND", workerNum, Time_3);
            }else{
                taskNum += 1;
                logUtil.setTRunTimes(method + "_" + "NPND", taskNum, Time_3);
            }


            runTime_3 += Time_3;

            if(requestVisited % 100 == 0){
                double npndScore = logUtil.getScoreSum(npndRequestProcessor.getLogSet());
                int    npndNum   = logUtil.getMatchedNum(npndRequestProcessor.getLogSet());
                System.out.printf("request num: %d NPND score: %f; NPND number: %d; NPND average: %f;\n",
                        requestVisited, npndScore, npndNum, npndScore/npndNum);
            }
            requestVisited += 1;
        }

        logUtil.recordLogSet(npndRequestProcessor.getLogSet(),
                cityDir+"-"+"NPND"+"-"+ts+"-"+te+"-"+method+"-"
                        + Integer.toString(maxRep) + "-" + Long.toString((long)walkTime) + "-"
                        +Long.toString(mDriveTime)+"-"+Long.toString(date)+".csv");

        logUtil.saveTRunTime(cityDir);
        logUtil.saveWRuntime(cityDir);

        System.out.printf("File of %d %d:%d-%d:%d maxRep:%d walkTime: %d Date: %d method:%s is created\n",
                date, (int)(sTime/60), sTime%60, (int)(eTime/60), eTime%60, maxRep, (long)walkTime, date, method);
    }


}
