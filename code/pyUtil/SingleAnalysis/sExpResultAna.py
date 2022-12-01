import pandas as pd
import numpy as np
import os, sys
import json

maxWalkingTime = -1

class SingleFileInfo:
    def __init__(self, df : pd.DataFrame, walkingTime):
        self.info = {}
        for index, row in df.iterrows():
            orderId = int(row[1])
            driverId = int(row[2])
            travelTime = float(row[5])
            walkTime = (float(row[6]) + float(row[8]))/2
            driveTime = float(row[5]) - float(row[8]) 
            # walkTime = float(row[8])
            if travelTime > 0 and walkingTime == 30:        # Collect the orders with the long distance to the nearest point - level-diff system for the requesters with difficuties  
                if walkTime < walkingTime * 10:
                    self.info[orderId] = [driverId, travelTime, walkTime, driveTime]
            elif walkingTime != 30 :
                self.info[orderId] = [driverId, travelTime, walkTime, driveTime]

class ResultInfo:
    def __init__(self, fileDir, startT, endT, date, respTime, walkingTime, drivingTime):
        self.fileDir = fileDir
        self.startT = startT
        self.endT = endT
        self.date = date
        self.drivingTime = drivingTime
        self.walkingTime = walkingTime
        self.respTime = respTime

        self.getPInfos()
        self.getNPNDInfos()
        self.getPRunningTime()
        self.computeCountInfo()
        self.getPRunningTime()

    # Get the infomation in a single DBP record file
    def getPInfos(self) -> SingleFileInfo:
        filePath = os.path.join(self.fileDir, self.getNameString("P"))
        df = pd.read_csv(filePath, header=None)
        self.dbpInfo = self.getSingleFileInfo(df)

    # Get the information in a single 
    def getNPNDInfos(self) -> SingleFileInfo:
        filePath = os.path.join(self.fileDir, self.getNameString("NPND"))
        df = pd.read_csv(filePath, header=None)
        self.npndInfo = self.getSingleFileInfo(df)

    # Get the running time
    def getPRunningTime(self):
        task_fileName = "chengdu_greedy{}-{}-P_task.json".format(self.walkingTime, self.drivingTime)
        worker_fileName = "chengdu_greedy{}-{}-P_worker.json".format(self.walkingTime, self.drivingTime)
        task_filePath = os.path.join(self.fileDir, task_fileName)
        worker_filePath = os.path.join(self.fileDir, worker_fileName)
        taskRunTimeCount = 0
        taskRunTimeSum  = 0.0
        workerRunTimeCount = 0
        workerRunTimeSum = 0.0

        with open(task_filePath, "r") as f:
            str = f.read()
            data = eval(str)
            for key in data.keys():
                taskRunTimeCount += 1
                taskRunTimeSum += float(data[key])

        with open(worker_filePath, 'r') as f:
            str = f.read()
            data = eval(str)
            for key in data.keys():
                workerRunTimeCount += 1
                workerRunTimeSum += float(data[key])

        self.runTime = (taskRunTimeSum + workerRunTimeSum)/(taskRunTimeCount + workerRunTimeCount)

    def getSingleFileInfo(self, df: pd.DataFrame):
        return SingleFileInfo(df, self.walkingTime)

    def getNameString(self, Type):
        return "chengdu-{}-{}-{}-greedy-{}-{}-{}-{}.csv".format(
            Type, self.startT, self.endT, self.respTime, self.walkingTime,
            self.drivingTime, self.date
        )

    def computeCountInfo(self):
        temp_travel_time_cmp_ratio = []
        temp_drive_time_cmp_ratio = []
        temp_walkTimes = []

        # Compute the travel time reduction ratio of all orders
        # print(self.dbpInfo.info)
        # print(self.npndInfo.info)

        for dbpId in self.dbpInfo.info.keys():
            if dbpId in self.npndInfo.info.keys():
                if self.dbpInfo.info[dbpId][0] == self.npndInfo.info[dbpId][0]:
                    dbpTravelTime = self.dbpInfo.info[dbpId][1]
                    npndTravelTime = self.npndInfo.info[dbpId][1]
                    dbpWalkTime = self.dbpInfo.info[dbpId][2]
                    dbpDriveTime = self.dbpInfo.info[dbpId][3]
                    npndDriveTime = self.npndInfo.info[dbpId][3]
                    temp_travel_time_cmp_ratio.append(dbpTravelTime/npndTravelTime)
                    temp_drive_time_cmp_ratio.append(dbpDriveTime/npndDriveTime)
                    temp_walkTimes.append(dbpWalkTime)
        
        temp_travel_time_cmp_ratio = np.array(temp_travel_time_cmp_ratio)
        temp_walkTimes = np.array(temp_walkTimes)
        temp_drive_time_cmp_ratio = np.array(temp_drive_time_cmp_ratio)

        # print(temp_travel_time_cmp_ratio)

        # find the percent of orders decrease greater than 10%
        self.ten_percents = np.sum(np.where(temp_travel_time_cmp_ratio < 0.9, 1, 0))/temp_travel_time_cmp_ratio.shape[0] * 100
        # find the percent of orders decrease greater than 20%
        self.twenty_percents = np.sum(np.where(temp_travel_time_cmp_ratio < 0.8, 1, 0))/temp_travel_time_cmp_ratio.shape[0] * 100
        # find the percent of orders decrease greater than 30%
        self.thirty_percents = np.sum(np.where(temp_travel_time_cmp_ratio < 0.7, 1, 0))/temp_travel_time_cmp_ratio.shape[0] * 100

        # Compute the average reduction ratio
        self.avgReduction = (1 - np.mean(temp_travel_time_cmp_ratio)) * 100
        self.avgDriveReduction = (1 - np.mean(temp_drive_time_cmp_ratio)) * 100

        # Compute the average walking time
        self.avgWalkTime = np.mean(temp_walkTimes)


    def showCountInfo(self):
        results = "10%:{}; 20%:{}; 30%:{}; Average reduction:{}; Average walking time:{}; Average running time:{}; Average driving reduction:{}".format(
            self.ten_percents, self.twenty_percents, self.thirty_percents, 
            self.avgReduction, self.avgWalkTime, self.runTime, self.avgDriveReduction
        )

        print(results)

if __name__ == '__main__':
    fileDir = "D:\\research\\crowdsourcing\\ExpRecords\\EndData"           # dataset directory
    resultInfo = ResultInfo(fileDir, 450, 540, 20161101, 120, 30, 300)     # Resp walk drive
    resultInfo.showCountInfo()
    # resultInfo = ResultInfo(fileDir, 450, 540, 20161101, 120, 60, 300)
    # resultInfo.showCountInfo()
    # resultInfo = ResultInfo(fileDir, 450, 540, 20161101, 120, 90, 300)
    # resultInfo.showCountInfo()
    # resultInfo = ResultInfo(fileDir, 450, 540, 20161101, 120, 120, 300)
    # resultInfo.showCountInfo()
    # resultInfo = ResultInfo(fileDir, 450, 540, 20161101, 120, 150, 300)
    # resultInfo.showCountInfo()
    # resultInfo = ResultInfo(fileDir, 450, 540, 20161101, 120, 120, 120)     # Resp walk drive
    # resultInfo.showCountInfo()
    # resultInfo = ResultInfo(fileDir, 450, 540, 20161101, 120, 120, 300)
    # resultInfo.showCountInfo()
    # resultInfo = ResultInfo(fileDir, 450, 540, 20161101, 120, 120, 480)
    # resultInfo.showCountInfo()
    # resultInfo = ResultInfo(fileDir, 450, 540, 20161101, 120, 120, 600)
    # resultInfo.showCountInfo()
    # resultInfo = ResultInfo(fileDir, 450, 540, 20161101, 120, 120, 900)
    # resultInfo.showCountInfo()
    # startT endT date Response walking driving