import pandas as pd
import os, sys
import shutil
import datetime
import numpy as np
import json

defaultDataDir = "../../Experiment/ExpRecords/EndData"
anaDataDir = "../../Experiment/ExpRecords/DataCopy"
defaultTimeDir = "../../Experiment/ExpRecords/TimeData"
timeDataDir = "../../Experiment/ExpRecords/TimeDataCopy"


'''
If I want to set the date format, I can use the code as follows after plot

# Define the date format
date_form = DateFormatter("%m-%d")
ax.xaxis.set_major_formatter(date_form) 
'''

class AnaDataItem:
    def __init__(self):
        self.filePath = ""
        self.anaResults = {}

    def initFromFileName(self, fileName):
        '''
            Initialize information according to the filename 
        '''
        self.filePath = os.path.join(anaDataDir, fileName)
        infos = (fileName.split(".")[0]).split("-")
        self.cityName = infos[0]
        self.selectM = infos[1]
        startTime = "{}:{}".format(int(int(infos[2])/60), int(infos[2])%60)
        endTime = "{}:{}".format(int(int(infos[3])/60), int(infos[3])%60)
        self.timePeriod = startTime + "-" + endTime
        self.matchM = infos[4]
        self.maxResp = int(infos[5])
        self.maxWalk = int(infos[6])
        self.maxDrive = int(infos[7])
        YY = int(infos[8][0: 4])
        MM = int(infos[8][4: 6])
        DD = int(infos[8][6: 8])
        self.mdate = datetime.datetime(YY, MM, DD)
    
    def computeDataItem(self):
        if self.filePath == "":
            return
        df = pd.read_csv(self.filePath, header=None)
        df = df[df[0] > 0]
        self.anaResults["passengerResp"] = df[3].sum()
        self.anaResults["passengerResp"] + self.maxResp * (12000 - df.shape[0])
        self.anaResults["passengerResp"] = self.anaResults["passengerResp"]/12000
        self.anaResults["totalScore"] = df[0].sum()
        self.anaResults["driverResp"] = df[4].mean()
        self.anaResults["walkTime"] = (df[6].mean() + df[8].mean())/2
        # self.anaResults["driveTime"] = df[5] - df[8] - df[[6, 7]].max(axis=1) + df[7]
        self.anaResults["matchN"] = df.shape[0]
        self.anaResults["CR"] = 0
        self.anaResults["tenRed"] = 0
        self.anaResults["twiRed"] = 0
        self.anaResults["thiRed"] = 0
        self.anaResults["reduction"] = 0
        self.anaResults["zeroRed"] = 1
        self.anaResults["DTR"] = 0
        self.anaResults["DTRR"] = 0
        self.anaResults["DTR2"] = 0
        self.anaResults["DTR4"] = 0
        self.anaResults["DTR6"] = 0
        self.anaResults["DTRR1"] = 0
        self.anaResults["DTRR2"] = 0
        self.anaResults["DTRR3"] = 0

    def turnInfo2Params(self):
        params = {
            "cityName"  : self.cityName, 
            "selectM"   : self.selectM, 
            "timePeriod": self.timePeriod, 
            "matchM"    : self.matchM, 
            "maxResp"   : self.maxResp, 
            "maxWalk"   : self.maxWalk, 
            "mdate"     : self.mdate,
            "maxDrive"  : self.maxDrive
        }
        return params
    
class DataInitUtil:
    def __init__(self):
        self.InitDataCopy()
        self.anaDatas = []

    def InitDataCopy(self):

        fileNames = os.listdir(defaultDataDir)
        for fileName in fileNames:
            shutil.copyfile(os.path.join(defaultDataDir, fileName), os.path.join(anaDataDir, fileName))
        
        fileNames = os.listdir(defaultTimeDir)
        for fileName in fileNames:
            shutil.copyfile(os.path.join(defaultTimeDir, fileName), os.path.join(timeDataDir, fileName))
    
    def getDataAnaItems(self):
        '''
            Get all dataItems
        '''
        fileNames = os.listdir(anaDataDir)
        for fileName in fileNames:
            if fileName.split(".")[-1] == "csv":
                adItem = AnaDataItem()
                adItem.initFromFileName(fileName)
                adItem.computeDataItem()
                self.anaDatas.append(adItem)
        print("All data collected!")
        self.__generateReductionRates()
        print("Reduction ratio calculated!")
        self.__generatePDReductionRates()
        print("PD reduction ratio calculated!")
        self.__generateCompetitiveRatio()
        print("Competitive ratio calulated!")
        self.__generateDriverTimeDecay()
        print("Driving time reduction calculated!")

    def __generateCompetitiveRatio(self):
        '''
            Get competitive ratio needed in 
        '''
        for adItem in self.anaDatas:
            if adItem.selectM == "BKM":
                break
            params = adItem.turnInfo2Params()
            params["selectM"] = "PD"
            params["matchM"] = "BKM"
            adItem_best = self.getDataByParams(params)
            if adItem_best != None:
                adItem.anaResults["CR"] = adItem.anaResults["totalScore"] / adItem_best.anaResults["totalScore"]
            # print("{}, {}, {}".format(adItem.matchM, adItem.selectM, adItem.CR))

    def __generateReductionRates(self):
        '''
            Get reduction rates
        '''
        for adItem in self.anaDatas:
            if adItem.selectM == "NPD":
                params = adItem.turnInfo2Params()
                params["selectM"] = "NPND"
                adItem_npnd = self.getDataByParams(params)
                # calculate the reduction ratio and related occupation ratio
                npd_df = pd.read_csv(adItem.filePath, header=None)
                npnd_df = pd.read_csv(adItem_npnd.filePath, header=None)
                npd_df = npd_df[npd_df[0]!=0]
                npnd_df = npnd_df[npnd_df[0]!=0]
                reductionRatios = []
                npd_dict = {}

                npd_list_time = npd_df[5].values.tolist()
                npd_list_passenger = npd_df[1].values.tolist()
                npd_list_driver = npd_df[2].values.tolist()
                
                npnd_list_time = npnd_df[5].values.tolist()
                npnd_list_passenger = npnd_df[1].values.tolist()
                npnd_list_driver = npnd_df[2].values.tolist()

                for i in range(len(npd_list_passenger)):
                    npd_dict[npd_list_passenger[i]] = {}
                    npd_dict[npd_list_passenger[i]][npd_list_driver[i]] = npd_list_time[i]

                for i in range(len(npnd_list_passenger)):
                    if npnd_list_passenger[i] in npd_dict.keys():
                        if npnd_list_driver[i] in npd_dict[npnd_list_passenger[i]].keys():
                            score = 1 - npd_dict[npnd_list_passenger[i]][npnd_list_driver[i]]/npnd_list_time[i]
                            reductionRatios.append(score)

                reductionRatios = np.array(reductionRatios)


                adItem.anaResults["tenRed"] = float(np.sum(reductionRatios >= 0.1))/float(np.sum(reductionRatios >= 0))
                adItem.anaResults["twiRed"] = float(np.sum(reductionRatios >= 0.2))/float(np.sum(reductionRatios >= 0))
                adItem.anaResults["thiRed"] = float(np.sum(reductionRatios >= 0.3))/float(np.sum(reductionRatios >= 0))
                adItem.anaResults["reduction"] = np.mean(reductionRatios)

    def __generatePDReductionRates(self):
        for adItem in self.anaDatas:
            if adItem.selectM == "PD" and adItem.matchM != "BKM":
                params = adItem.turnInfo2Params()
                params["selectM"] = "NPND"
                adItem_npnd = self.getDataByParams(params)
                # calculate the reduction ratio and related occupation ratio
                pd_df = pd.read_csv(adItem.filePath, header=None)
                npnd_df = pd.read_csv(adItem_npnd.filePath, header=None)
                pd_df = pd_df[pd_df[0]!=0]
                npnd_df = npnd_df[npnd_df[0]!=0]
                reductionRatios = []
                pd_dict = {}


                pd_list_time = pd_df[5].values.tolist()
                pd_list_passenger = pd_df[1].values.tolist()
                
                npnd_list_time = npnd_df[5].values.tolist()
                npnd_list_passenger = npnd_df[1].values.tolist()

                for i in range(len(pd_list_passenger)):
                    pd_dict[pd_list_passenger[i]] = pd_list_time[i]

                for i in range(len(npnd_list_passenger)):
                    if npnd_list_passenger[i] in pd_dict.keys():
                        score = 1 - pd_dict[npnd_list_passenger[i]]/npnd_list_time[i]
                        reductionRatios.append(score)

                reductionRatios = np.array(reductionRatios)

  
                adItem.anaResults["zeroRed"] = float(np.sum(reductionRatios >= 0))/float(np.sum(reductionRatios >= -2))
                adItem.anaResults["tenRed"] = float(np.sum(reductionRatios >= 0.1))/float(np.sum(reductionRatios >= -2))
                adItem.anaResults["twiRed"] = float(np.sum(reductionRatios >= 0.2))/float(np.sum(reductionRatios >= -2))
                adItem.anaResults["thiRed"] = float(np.sum(reductionRatios >= 0.3))/float(np.sum(reductionRatios >= -2))
                adItem.anaResults["reduction"] = np.mean(reductionRatios)

                # print(adItem.anaResults["PDtenRed"])
    
    def __generateDriverTimeDecay(self):
        for adItem in self.anaDatas:
            if adItem.selectM == "PD" and adItem.matchM != "BKM":
                params = adItem.turnInfo2Params()
                params["selectM"] = "NPD"
                adItem_npd = self.getDataByParams(params)
                # calculate the reduction ratio and related occupation ratio
                pd_df = pd.read_csv(adItem.filePath, header=None)
                npd_df = pd.read_csv(adItem_npd.filePath, header=None)

                params["selectM"] = "NPND"
                adItem_npnd = self.getDataByParams(params)
                npnd_df = pd.read_csv(adItem_npnd.filePath, header=None)

                pd_df = pd_df[pd_df[0]!=0]
                npd_df = npd_df[npd_df[0]!=0]
                npnd_df = npnd_df[npnd_df[0]!=0]
                
                DTR_pd = []
                DTR_npd = []
                DTRR_pd = []
                DTRR_npd = []

                pd_dict = {}
                npd_dict = {}


                pd_df_DT = pd_df[5] - pd_df[8]
                pd_list_time = pd_df_DT.values.tolist()
                pd_list_driver = pd_df[2].values.tolist()
                
                npd_df_DT = npd_df[5] - npd_df[8]
                npd_list_time = npd_df_DT.values.tolist()
                npd_list_driver = npd_df[2].values.tolist()

                npnd_df_DT = npnd_df[5] - npnd_df[8]
                npnd_list_time = npnd_df_DT.values.tolist()
                npnd_list_driver = npnd_df[2].values.tolist()

                for i in range(len(pd_list_driver)):
                    pd_dict[pd_list_driver[i]] = pd_list_time[i]

                for i in range(len(npd_list_driver)):
                    npd_dict[npd_list_driver[i]] = npd_list_time[i]

                for i in range(len(npnd_list_driver)):
                    if npnd_list_driver[i] in pd_dict.keys():
                        score = npnd_list_time[i] - pd_dict[npnd_list_driver[i]]
                        decayR = 1 - pd_dict[npnd_list_driver[i]]/npnd_list_time[i]
                        DTR_pd.append(score)
                        DTRR_pd.append(decayR)

                    if npnd_list_driver[i] in npd_dict.keys():
                        score = npnd_list_time[i] - npd_dict[npnd_list_driver[i]]
                        decayR = 1 - npd_dict[npnd_list_driver[i]]/npnd_list_time[i]
                        DTR_npd.append(score)
                        DTRR_npd.append(decayR)

                DTR_pd = np.array(DTR_pd)
                DTR_npd = np.array(DTR_npd)
                DTRR_pd = np.array(DTRR_pd)
                DTRR_npd = np.array(DTRR_npd)

                adItem.anaResults["DTR"] = np.mean(DTR_pd)
                adItem.anaResults["DTRR"] = np.mean(DTRR_pd)

                adItem.anaResults["DTR2"] = float(np.sum(DTR_pd > 120))/float(np.sum(DTR_pd > -100000))
                adItem.anaResults["DTR4"] = float(np.sum(DTR_pd > 240))/float(np.sum(DTR_pd > -100000))
                adItem.anaResults["DTR6"] = float(np.sum(DTR_pd > 360))/float(np.sum(DTR_pd > -100000))

                adItem.anaResults["DTRR1"] = float(np.sum(DTRR_pd >= 0.1))/float(np.sum(DTRR_pd > -100000))
                adItem.anaResults["DTRR2"] = float(np.sum(DTRR_pd >= 0.2))/float(np.sum(DTRR_pd > -100000))
                adItem.anaResults["DTRR3"] = float(np.sum(DTRR_pd >= 0.3))/float(np.sum(DTRR_pd > -100000))

                adItem_npd.anaResults["DTR"] = np.mean(DTR_npd)
                adItem_npd.anaResults["DTRR"] = np.mean(DTRR_npd)

                adItem_npd.anaResults["DTR2"] = float(np.sum(DTR_npd > 120))/float(np.sum(DTR_pd > -100000))
                adItem_npd.anaResults["DTR4"] = float(np.sum(DTR_npd > 240))/float(np.sum(DTR_pd > -100000))
                adItem_npd.anaResults["DTR6"] = float(np.sum(DTR_npd > 360))/float(np.sum(DTR_pd > -100000))

                adItem_npd.anaResults["DTRR1"] = float(np.sum(DTRR_npd >= 0.1))/float(np.sum(DTRR_npd > -10000))
                adItem_npd.anaResults["DTRR2"] = float(np.sum(DTRR_npd >= 0.2))/float(np.sum(DTRR_npd > -10000))
                adItem_npd.anaResults["DTRR3"] = float(np.sum(DTRR_npd >= 0.3))/float(np.sum(DTRR_npd > -10000))

    '''
        Get data by parameters
    '''
    def getDataByInfo(self, defaultParams, changeParams):
        '''
        params: default parameters
        '''
        params = dict(defaultParams)
        for key in changeParams.keys():
           params[key] = changeParams[key]
           
        for adItem in self.anaDatas:
            if adItem.cityName == params["cityName"] and adItem.selectM == params["selectM"] \
                and adItem.timePeriod == params["timePeriod"] and adItem.maxDrive == params["maxDrive"] and\
                adItem.matchM == params["matchM"] and adItem.maxResp == params["maxResp"] \
                    and adItem.maxWalk == params["maxWalk"] and adItem.mdate == params["mdate"]:
                return adItem 
    
    def getDataByParams(self, params):
        for adItem in self.anaDatas:
            if adItem.cityName == params["cityName"] and adItem.selectM == params["selectM"] \
                and adItem.timePeriod == params["timePeriod"] and adItem.maxDrive == params["maxDrive"] and \
                adItem.matchM == params["matchM"] and adItem.maxResp == params["maxResp"] \
                    and adItem.maxWalk == params["maxWalk"] and adItem.mdate == params["mdate"]:
                return adItem 


def Time_DataUtil():
    anaData = {}
    Infos = ["task", "worker", "memory"]
    TypeMap = {
        "EMA": "Baseline", 
        "PD" : "AOPD"
    }
    cityNames = ["chengdu", "NYC"]
    walkTimes = [30, 60, 90, 120, 150]
    for cityName in cityNames:
        if cityName not in anaData.keys():
            anaData[cityName] = {}
        for Type in TypeMap.keys():
            if Type not in anaData[cityName].keys():
                anaData[cityName][Type] = {}
            for walkTime in walkTimes:
                if walkTime not in anaData[cityName][Type].keys():
                    anaData[cityName][Type][walkTime] = {}
                for info in Infos:
                    if info == "worker":
                        fileName = "{}_KMT{}-{}_{}.json".format(cityName, walkTime, Type, info)
                    else:
                        fileName = "{}_KMT{}_{}_{}.json".format(cityName, walkTime, Type, info)
                    filePath = os.path.join(anaDataDir, fileName)
                    anaData[cityName][Type][walkTime][info] = anaJson(filePath)
    return anaData


def TimeMemUtil():
    anaData = {}
    cityNames = ["chengdu", "NYC"]
    walkTimes = [30, 60, 90, 120, 150]
    maxDrives = [120, 300, 480, 600, 900]
    maxResps = [30, 60, 90, 120, 150]
    dates = {
        "chengdu": [20161101, 20161102, 20161103, 20161104, 20161105],
        "NYC": [20141013, 20141014, 20141015, 20141016, 20141017]
    }
    walkDefault = 120
    driveDefault = 300
    respDefault = 120
    for cityName in cityNames:
        anaData[cityName] = {"AOPD":[], "Baseline":[]}
    for cityName in cityNames:

        for walkTime in walkTimes:
            [PDT, PDW, PDM, EMAT, EMAW, EMAM] = getFileList(cityName, respDefault, driveDefault, walkTime)
            anaData[cityName]["AOPD"].append({"Task": anaJson(PDT), "Worker": anaJson(PDW), "Memory": anaJson(PDM), \
                "maxDrive": driveDefault, "maxWalk": walkTime, "maxResp": respDefault})
            anaData[cityName]["Baseline"].append({"Task": anaJson(EMAT), "Worker": anaJson(EMAW), "Memory": anaJson(EMAM), \
                "maxDrive": driveDefault, "maxWalk": walkTime, "maxResp": respDefault})

        for maxDrive in maxDrives:
            [PDT, PDW, PDM, EMAT, EMAW, EMAM] = getFileList(cityName, respDefault, maxDrive, walkDefault)
            anaData[cityName]["AOPD"].append({"Task": anaJson(PDT), "Worker": anaJson(PDW), "Memory": anaJson(PDM), \
                "maxDrive": maxDrive, "maxWalk": walkDefault, "maxResp": respDefault})
            anaData[cityName]["Baseline"].append({"Task": anaJson(EMAT), "Worker": anaJson(EMAW), "Memory": anaJson(EMAM), \
                "maxDrive": maxDrive, "maxWalk": walkDefault, "maxResp": respDefault})

        for maxResp in maxResps:
            [PDT, PDW, PDM, EMAT, EMAW, EMAM] = getFileList(cityName, maxResp, driveDefault, walkDefault)
            anaData[cityName]["AOPD"].append({"Task": anaJson(PDT), "Worker": anaJson(PDW), "Memory": anaJson(PDM), \
                "maxDrive": driveDefault, "maxWalk": walkDefault, "maxResp": maxResp})
            anaData[cityName]["Baseline"].append({"Task": anaJson(EMAT), "Worker": anaJson(EMAW), "Memory": anaJson(EMAM), \
                "maxDrive": driveDefault, "maxWalk": walkDefault, "maxResp": maxResp})
    return anaData


def getFileList(cityName, respDefault, driveDefault, walkTime):
    PDT = os.path.join(anaDataDir, "{}_greedy-{}-{}-{}-PD_task.json".format(cityName, respDefault, driveDefault, walkTime))
    PDW = os.path.join(anaDataDir, "{}_greedy-{}-{}-{}-PD_worker.json".format(cityName, respDefault, driveDefault, walkTime))
    PDM = os.path.join(anaDataDir, "{}_greedy-{}-{}-{}-PD_memory.json".format(cityName, respDefault, driveDefault, walkTime))
    EMAT = os.path.join(anaDataDir, "{}_greedy-{}-{}-{}-EMA_task.json".format(cityName, respDefault, driveDefault, walkTime))
    EMAW = os.path.join(anaDataDir, "{}_greedy-{}-{}-{}-EMA_worker.json".format(cityName, respDefault, driveDefault, walkTime))
    EMAM = os.path.join(anaDataDir, "{}_greedy-{}-{}-{}-EMA_memory.json".format(cityName, respDefault, driveDefault, walkTime))
    return [PDT, PDW, PDM, EMAT, EMAW, EMAM]

def getDateFile(cityName, respDefault, driveDefault, walkTime, timePeriod):
    PDT = os.path.join(anaDataDir, "{}_greedy-{}-{}-{}{}-PD_task.json".format(cityName, respDefault, driveDefault, walkTime))
    PDW = os.path.join(anaDataDir, "{}_greedy-{}-{}-{}{}-PD_worker.json".format(cityName, respDefault, driveDefault, walkTime))
    PDM = os.path.join(anaDataDir, "{}_greedy-{}-{}-{}{}-PD_memory.json".format(cityName, respDefault, driveDefault, walkTime))
    EMAT = os.path.join(anaDataDir, "{}_greedy-{}-{}-{}{}-EMA_task.json".format(cityName, respDefault, driveDefault, walkTime))
    EMAW = os.path.join(anaDataDir, "{}_greedy-{}-{}-{}{}-EMA_worker.json".format(cityName, respDefault, driveDefault, walkTime))
    EMAM = os.path.join(anaDataDir, "{}_greedy-{}-{}-{}{}-EMA_memory.json".format(cityName, respDefault, driveDefault, walkTime))
    return [PDT, PDW, PDM, EMAT, EMAW, EMAM]

def anaJson(filePath):
    sumer = 0
    count = 0
    with open(filePath, "r") as f:
        datas = f.read()
        datas = datas[1: -1]
        datas = datas.split(",")
        for data in datas:
            [a, b] = data.split(":")
            count += 1
            sumer += int(b)
    return sumer/count

if __name__ == '__main__':
    dataInitUtil = DataInitUtil()
    dataInitUtil.getDataAnaItems()
    # anaData = Time_DataUtil()
