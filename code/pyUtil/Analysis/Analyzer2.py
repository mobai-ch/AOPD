import pandas as pd
import numpy as np
import datetime
import seaborn as sns
from DataInit import DataInitUtil, AnaDataItem, Time_DataUtil, TimeMemUtil
import matplotlib.pyplot as plt
import os, sys
from openpyxl import load_workbook


defaultParams = {
    "NYC": {
        "cityName": "NYC", 
        "selectM": "PD", 
        "timePeriod": "7:30-9:0", 
        "matchM": "greedy", 
        "maxResp": 120, 
        "maxWalk": 120, 
        "mdate": datetime.datetime(2014, 10, 15),
        "maxDrive": 300
    },
    "chengdu": {
        "cityName": "chengdu", 
        "selectM": "PD", 
        "timePeriod": "7:30-9:0",  
        "matchM": "greedy", 
        "maxResp": 120, 
        "maxWalk": 120, 
        "mdate": datetime.datetime(2016, 11, 1),
        "maxDrive": 300
    }
}

nameMap = {
    "PD":   "AOPD",
    "NPD":  "MOPD",
    "NPND": "NOPD",
    "KMT"   : "KM" ,
    "greedy": "GR",
    "greedyT": "GRT",
    "totalScore": "Total score",
    "passengerResp": "Response time(s)",
    "walkTime": "Walking time(s)",
    "CR": "Competitive ratio",
    "tenRed": ">10%",
    "twiRed": ">20%",
    "thiRed": ">30%",
    "reduction": "Reduction ratio",
    "cityName": "cityName",
    "selectM": "selectM",
    "timePeriod": "Time period",
    "matchM": "Algorithms",
    "maxResp": "Maximum response time(s)",
    "maxWalk": "Maximum walking time(s)",
    "maxDrive": "Maximum driving time(s)",
    "mdate": "Date",
    "matchN": "Number of matched pairs",
    "DTR": "Driving time reduction(s)",
    "DTR2": ">=120s",
    "DTR4": ">=240s",
    "DTR6": ">=360s",
    "DTRR": "Driving time reduction ratio",
    "DTRR1": "Driving time reduction ratio >= 10%",
    "DTRR2": "Driving time reduction ratio >= 20%",
    "DTRR3": "Driving time reduction ratio >= 30%"
}

SPTimeMap = {
    "EMA": "Baseline", 
    "PD" : "AOPD",
    "Memory": "Memory cost (MB)"
}

maxResps = [30, 60, 90, 120, 150]
maxDrives = [120, 300, 480, 600, 900]
maxWalks = [30, 60, 90, 120, 150]
selectMs = ["PD", "NPD", "NPND"]
matchMs = ["KMT", "greedy", "greedyT"]
ylabels = ["CR", "passengerResp", "walkTime", "matchN", "totalScore"]
spYlabels = ["tenRed", "twiRed", "thiRed", "reduction", "DTRR", "DTRR1", "DTRR2", "DTRR3"]
cityNames = ["chengdu", "NYC"]
timePeriods = ["7:30-9:0", "12:0-13:30", "17:0-19:0"]
timeMaps = {
    "7:30-9:0": "7:30-8:30", 
    "12:0-13:30": "12:00-13:00", 
    "17:0-19:0": "17:00-18:00"
}
dates = {
    "chengdu": [datetime.datetime(2016, 11, date) for date in [1, 2, 3, 4, 5]],
    "NYC": [datetime.datetime(2014, 10, date) for date in [13, 14, 15, 16, 17]]
}
xlsFilePath = "../../Experiment/XLS"

dataUtil = DataInitUtil()
dataUtil.getDataAnaItems()
anaData = TimeMemUtil()


def anaAlgorithm():
    for cityName in cityNames:
        xlsfileName = os.path.join(xlsFilePath, cityName+"-"+"Algorithm.xlsx")
        write = pd.ExcelWriter(xlsfileName, mode='w', engine="openpyxl")
        for ylabel in ylabels:
            df_dict = {
                "Algorithms": [],
                "Type": [],
                nameMap[ylabel]: []
            }
            for matchM in matchMs:
                changeParams = {
                    "selectM": "PD", "matchM": matchM
                }
                cityDefault = defaultParams[cityName]
                adItem = dataUtil.getDataByInfo(cityDefault, changeParams)
                # print(adItem.turnInfo2Params())
                df_dict["Algorithms"].append(nameMap[matchM])
                df_dict["Type"].append(nameMap["PD"])
                df_dict[nameMap[ylabel]].append(adItem.anaResults[ylabel])
            df = pd.DataFrame(df_dict)
            df.to_excel(write, sheet_name=ylabel)
        write.save()

def anaDriveTime():
    for cityName in cityNames:
        xlsfileName = os.path.join(xlsFilePath, cityName+"-"+"Driving time.xlsx")
        write = pd.ExcelWriter(xlsfileName, mode='w', engine="openpyxl")
        for ylabel in ylabels:
            df_dict = {
                    nameMap["maxDrive"]: [],
                    "Type": [],
                    nameMap[ylabel]: []
            }
            for maxDrive in maxDrives:
                for selectM in selectMs:
                    changeParams = {
                        "selectM": selectM, "maxDrive": maxDrive
                    }
                    cityDefault = defaultParams[cityName]
                    adItem = dataUtil.getDataByInfo(cityDefault, changeParams)
                    df_dict[nameMap["maxDrive"]].append(str(maxDrive))
                    df_dict["Type"].append(nameMap[selectM])
                    # print(adItem.turnInfo2Params())
                    df_dict[nameMap[ylabel]].append(adItem.anaResults[ylabel])
            df = pd.DataFrame(df_dict)
            df.to_excel(write, sheet_name=ylabel)
        write.save()

def anaRespTime(): 
    for cityName in cityNames:
        xlsfileName = os.path.join(xlsFilePath, cityName+"-"+"Response time.xlsx")
        write = pd.ExcelWriter(xlsfileName, mode='w', engine="openpyxl")
        for ylabel in ylabels:
            df_dict = {
                    nameMap["maxResp"]: [],
                    "Type": [],
                    nameMap[ylabel]: []
            }
            for maxResp in maxResps:
                for selectM in selectMs:
                    changeParams = {
                        "selectM": selectM, "maxResp": maxResp
                    }
                    cityDefault = defaultParams[cityName]
                    adItem = dataUtil.getDataByInfo(cityDefault, changeParams)
                    df_dict[nameMap["maxResp"]].append(str(maxResp))
                    df_dict["Type"].append(nameMap[selectM])
                    df_dict[nameMap[ylabel]].append(adItem.anaResults[ylabel])
            df = pd.DataFrame(df_dict)
            df.to_excel(write, sheet_name=ylabel)
        write.save()

def anaDate():
    for cityName in cityNames:
        xlsfileName = os.path.join(xlsFilePath, cityName+"-"+"Date.xlsx")
        write = pd.ExcelWriter(xlsfileName, mode='w', engine="openpyxl")
        for ylabel in ylabels:
            df_dict = {
                "Date": [],
                "Type": [],
                nameMap[ylabel]: []
            }
            for date in dates[cityName]:
                for selectM in selectMs:
                    changeParams = {
                        "selectM": selectM, "mdate": date
                    }
                    cityDefault = defaultParams[cityName]
                    adItem = dataUtil.getDataByInfo(cityDefault, changeParams)
                    df_dict["Date"].append(str(date.month)+"."+str(date.day))
                    df_dict["Type"].append(nameMap[selectM])
                    df_dict[nameMap[ylabel]].append(adItem.anaResults[ylabel])
            df = pd.DataFrame(df_dict)
            df.to_excel(write, sheet_name=ylabel)
        write.save()

def anaTimePeriod():
    for cityName in cityNames:
        xlsfileName = os.path.join(xlsFilePath, cityName+"-"+"Time Period.xlsx")
        write = pd.ExcelWriter(xlsfileName, mode='w', engine="openpyxl")
        for ylabel in ylabels:
            df_dict = {
                    "Time Period": [],
                    "Type": [],
                    nameMap[ylabel]: []
            }
            for timePeriod in timePeriods:
                for selectM in selectMs:
                    changeParams = {
                        "selectM": selectM, "timePeriod": timePeriod
                    }
                    cityDefault = defaultParams[cityName]
                    adItem = dataUtil.getDataByInfo(cityDefault, changeParams)
                    df_dict["Time Period"].append(timeMaps[timePeriod])
                    df_dict["Type"].append(nameMap[selectM])
                    df_dict[nameMap[ylabel]].append(adItem.anaResults[ylabel])
            df = pd.DataFrame(df_dict)
            df.to_excel(write, sheet_name=ylabel)
        write.save()

def anaMaxWalk():
    for cityName in cityNames:
        xlsfileName = os.path.join(xlsFilePath, cityName+"-"+"walkTime.xlsx")
        write = pd.ExcelWriter(xlsfileName, mode='w', engine="openpyxl")
        for ylabel in ylabels:
            df_dict = {
                    nameMap["maxWalk"]: [],
                    "Type": [],
                    nameMap[ylabel]: []
            }
            for maxWalk in maxWalks:
                for selectM in selectMs:
                    changeParams = {
                        "selectM": selectM, "maxWalk": maxWalk
                    }
                    cityDefault = defaultParams[cityName]
                    adItem = dataUtil.getDataByInfo(cityDefault, changeParams)
                    df_dict[nameMap["maxWalk"]].append(str(maxWalk))
                    df_dict["Type"].append(nameMap[selectM])
                    df_dict[nameMap[ylabel]].append(adItem.anaResults[ylabel])
            df = pd.DataFrame(df_dict)
            df.to_excel(write, sheet_name=ylabel)
        write.save()

def anaAlgorithmSP():
    for cityName in cityNames:
        xlsfileName = os.path.join(xlsFilePath, cityName+"-"+"Algorithm.xlsx")
        write = pd.ExcelWriter(xlsfileName, mode='a', engine="openpyxl")
        for ylabel in spYlabels:
            df_dict = {
                "Algorithms": [],
                "Type": [],
                nameMap[ylabel]: []
            }
            for matchM in matchMs:
                changeParams = {
                    "selectM": "PD", "matchM": matchM
                }
                cityDefault = defaultParams[cityName]
                adItem = dataUtil.getDataByInfo(cityDefault, changeParams)
                # print(adItem.turnInfo2Params())
                df_dict["Algorithms"].append(nameMap[matchM])
                df_dict["Type"].append(nameMap["PD"])
                df_dict[nameMap[ylabel]].append(adItem.anaResults[ylabel])
            df = pd.DataFrame(df_dict)
            df.to_excel(write, sheet_name=ylabel)
        write.save()

def anaDateSP():
    for cityName in cityNames:
        xlsfileName = os.path.join(xlsFilePath, cityName+"-"+"Date.xlsx")
        write = pd.ExcelWriter(xlsfileName, mode='a', engine="openpyxl")
        for ylabel in spYlabels:
            df_dict = {
                "Date": [],
                "Type": [],
                nameMap[ylabel]: []
            }
            for date in dates[cityName]:
                for selectM in selectMs:
                    changeParams = {
                        "selectM": selectM, "mdate": date
                    }
                    cityDefault = defaultParams[cityName]
                    adItem = dataUtil.getDataByInfo(cityDefault, changeParams)
                    df_dict["Date"].append(str(date.month)+"."+str(date.day))
                    df_dict["Type"].append(nameMap[selectM])
                    df_dict[nameMap[ylabel]].append(adItem.anaResults[ylabel])
            df = pd.DataFrame(df_dict)
            df.to_excel(write, sheet_name=ylabel)
        write.save()

def anaTimePeriodSP():
    for cityName in cityNames:
        xlsfileName = os.path.join(xlsFilePath, cityName+"-"+"Time Period.xlsx")
        write = pd.ExcelWriter(xlsfileName, mode='a', engine="openpyxl")
        for ylabel in spYlabels:
            df_dict = {
                    "Time Period": [],
                    "Type": [],
                    nameMap[ylabel]: []
            }
            for timePeriod in timePeriods:
                for selectM in selectMs:
                    changeParams = {
                        "selectM": selectM, "timePeriod": timePeriod
                    }
                    cityDefault = defaultParams[cityName]
                    adItem = dataUtil.getDataByInfo(cityDefault, changeParams)
                    df_dict["Time Period"].append(timeMaps[timePeriod])
                    df_dict["Type"].append(nameMap[selectM])
                    df_dict[nameMap[ylabel]].append(adItem.anaResults[ylabel])
            df = pd.DataFrame(df_dict)
            df.to_excel(write, sheet_name=ylabel)
        write.save()

def anaMaxWalkSP():
    for cityName in cityNames:
        xlsfileName = os.path.join(xlsFilePath, cityName+"-"+"walkTime.xlsx")
        write = pd.ExcelWriter(xlsfileName, mode='a', engine="openpyxl")
        for ylabel in spYlabels:
            df_dict = {
                    nameMap["maxWalk"]: [],
                    "Type": [],
                    nameMap[ylabel]: []
            }
            for maxWalk in maxWalks:
                for selectM in selectMs:
                    changeParams = {
                        "selectM": selectM, "maxWalk": maxWalk
                    }
                    cityDefault = defaultParams[cityName]
                    adItem = dataUtil.getDataByInfo(cityDefault, changeParams)
                    df_dict[nameMap["maxWalk"]].append(str(maxWalk))
                    df_dict["Type"].append(nameMap[selectM])
                    df_dict[nameMap[ylabel]].append(adItem.anaResults[ylabel])
            df = pd.DataFrame(df_dict)
            df.to_excel(write, sheet_name=ylabel)
        write.save()

def anaDriveTimeSP():
    for cityName in cityNames:
        xlsfileName = os.path.join(xlsFilePath, cityName+"-"+"Driving time.xlsx")
        write = pd.ExcelWriter(xlsfileName, mode='a', engine="openpyxl")
        for ylabel in spYlabels:
            df_dict = {
                    nameMap["maxDrive"]: [],
                    "Type": [],
                    nameMap[ylabel]: []
            }
            for maxDrive in maxDrives:
                for selectM in selectMs:
                    changeParams = {
                        "selectM": selectM, "maxDrive": maxDrive
                    }
                    cityDefault = defaultParams[cityName]
                    adItem = dataUtil.getDataByInfo(cityDefault, changeParams)
                    df_dict[nameMap["maxDrive"]].append(str(maxDrive))
                    df_dict["Type"].append(nameMap[selectM])
                    df_dict[nameMap[ylabel]].append(adItem.anaResults[ylabel])
            df = pd.DataFrame(df_dict)
            df.to_excel(write, sheet_name=ylabel)
        write.save()

def anaRespTimeSP():
    for cityName in cityNames:
        xlsfileName = os.path.join(xlsFilePath, cityName+"-"+"Response time.xlsx")
        write = pd.ExcelWriter(xlsfileName, mode='a', engine="openpyxl")
        for ylabel in spYlabels:
            df_dict = {
                    nameMap["maxResp"]: [],
                    "Type": [],
                    nameMap[ylabel]: []
            }
            for maxResp in maxResps:
                for selectM in selectMs:
                    changeParams = {
                        "selectM": selectM, "maxResp": maxResp
                    }
                    cityDefault = defaultParams[cityName]
                    adItem = dataUtil.getDataByInfo(cityDefault, changeParams)
                    df_dict[nameMap["maxResp"]].append(str(maxResp))
                    df_dict["Type"].append(nameMap[selectM])
                    df_dict[nameMap[ylabel]].append(adItem.anaResults[ylabel])
            df = pd.DataFrame(df_dict)
            df.to_excel(write, sheet_name=ylabel)
        write.save()

def anaMemRun():
    Types = ["EMA", "PD"]
    for cityName in cityNames:
        xlsfileName = os.path.join(xlsFilePath, cityName+"-"+"walkTime.xlsx")
        write = pd.ExcelWriter(xlsfileName, mode='a', engine="openpyxl")
        for ylabel in ["Memory cost(MB)", "Running time(ms)"]:
            df_dict = {
                nameMap["maxWalk"]: [],
                "Type": [],
                ylabel: [] 
            }
            if ylabel == "Memory cost(MB)":
                for maxwalk in maxWalks:
                    for Type in Types:
                        if Type == "EMA":
                            memoryEMA = anaData[cityName][Type][maxwalk]["memory"]/1024/1024
                            memoryOrigin = anaData[cityName]["PD"][maxwalk]["memory"]/1024/1024
                            if memoryEMA > memoryOrigin:
                                memory = memoryEMA - memoryOrigin
                            else:
                                memory = memoryEMA - 2/3*memoryOrigin
                            if memory < 0:
                                memory = memoryEMA
                        else:
                            memory = anaData[cityName][Type][maxwalk]["memory"]/1024/1024
                        df_dict[nameMap["maxWalk"]].append(maxwalk)
                        df_dict["Type"].append(SPTimeMap[Type])
                        df_dict[ylabel].append(memory)
            if ylabel == "Running time(ms)":
                for maxwalk in maxWalks:
                    for Type in Types:
                        runTime = (anaData[cityName][Type][maxwalk]["task"] + anaData[cityName][Type][maxwalk]["worker"])/2
                        df_dict[nameMap["maxWalk"]].append(maxwalk)
                        df_dict["Type"].append(SPTimeMap[Type])
                        df_dict[ylabel].append(runTime)
            df = pd.DataFrame(df_dict)
            df.to_excel(write, sheet_name=ylabel)
        write.save()

def anaWalkMemRun():
    respDefault = 120
    walkDefault = 120
    driveDefault = 300
    Types = ["AOPD", "Baseline"]
    for cityName in cityNames:
        xlsfileName = os.path.join(xlsFilePath, cityName+"-"+"walkTime.xlsx")
        write = pd.ExcelWriter(xlsfileName, mode='a', engine="openpyxl")
        for ylabel in ["Memory cost(MB)", "Running time(ms)"]:
            df_dict = {
                nameMap["maxWalk"]: [],
                "Type": [],
                ylabel: [] 
            }
            if ylabel == "Memory cost(MB)":
                for maxwalk in maxWalks:
                    for Type in Types:
                        walkVisited = set()
                        for cData in anaData[cityName][Type]:
                            if cData["maxWalk"] == maxwalk and cData["maxDrive"] == driveDefault and cData["maxResp"] == respDefault and maxwalk not in walkVisited:
                                df_dict[nameMap["maxWalk"]].append(str(maxwalk))
                                df_dict["Type"].append(Type)
                                df_dict[ylabel].append(cData["Memory"]/1024/1024)
                                walkVisited.add(maxwalk)
            if ylabel == "Running time(ms)":
                for maxwalk in maxWalks:
                    for Type in Types:
                        walkVisited = set()
                        for cData in anaData[cityName][Type]:
                            if cData["maxWalk"] == maxwalk and cData["maxDrive"] == driveDefault and cData["maxResp"] == respDefault and maxwalk not in walkVisited:
                                df_dict[nameMap["maxWalk"]].append(str(maxwalk))
                                df_dict["Type"].append(Type)
                                df_dict[ylabel].append(cData["Task"]/2 + cData["Worker"]/2)
                                walkVisited.add(maxwalk)
            df = pd.DataFrame(df_dict)
            df.to_excel(write, sheet_name=ylabel)
        write.save()

def anaDriveMemRun():
    respDefault = 120
    walkDefault = 120
    driveDefault = 300
    Types = ["AOPD", "Baseline"]
    for cityName in cityNames:
        xlsfileName = os.path.join(xlsFilePath, cityName+"-"+"Driving time.xlsx")
        write = pd.ExcelWriter(xlsfileName, mode='a', engine="openpyxl")
        for ylabel in ["Memory cost(MB)", "Running time(ms)"]:
            df_dict = {
                nameMap["maxDrive"]: [],
                "Type": [],
                ylabel: [] 
            }
            if ylabel == "Memory cost(MB)":
                for maxDrive in maxDrives:
                    for Type in Types:
                        DriveVisted = set()
                        for cData in anaData[cityName][Type]:
                            if cData["maxWalk"] == walkDefault and cData["maxDrive"] == maxDrive and cData["maxResp"] == respDefault and maxDrive not in DriveVisted:
                                df_dict[nameMap["maxDrive"]].append(str(maxDrive))
                                df_dict["Type"].append(Type)
                                df_dict[ylabel].append(cData["Memory"]/1024/1024)
                                DriveVisted.add(maxDrive)
            if ylabel == "Running time(ms)":
                for maxDrive in maxDrives:
                    for Type in Types:
                        DriveVisted = set()
                        for cData in anaData[cityName][Type]:
                            if cData["maxWalk"] == walkDefault and cData["maxDrive"] == maxDrive and cData["maxResp"] == respDefault and maxDrive not in DriveVisted:
                                df_dict[nameMap["maxDrive"]].append(str(maxDrive))
                                df_dict["Type"].append(Type)
                                df_dict[ylabel].append(cData["Task"]/2 + cData["Worker"]/2)
                                DriveVisted.add(maxDrive)
            df = pd.DataFrame(df_dict)
            df.to_excel(write, sheet_name=ylabel)
        write.save()

def anaRespMemRun():
    respDefault = 120
    walkDefault = 120
    driveDefault = 300
    
    Types = ["AOPD", "Baseline"]
    for cityName in cityNames:
        xlsfileName = os.path.join(xlsFilePath, cityName+"-"+"Response time.xlsx")
        write = pd.ExcelWriter(xlsfileName, mode='a', engine="openpyxl")
        for ylabel in ["Memory cost(MB)", "Running time(ms)"]:
            df_dict = {
                nameMap["maxResp"]: [],
                "Type": [],
                ylabel: [] 
            }
            if ylabel == "Memory cost(MB)":
                for maxResp in maxResps:
                    for Type in Types:
                        RespVisted = set()
                        for cData in anaData[cityName][Type]:
                            if cData["maxWalk"] == walkDefault and cData["maxDrive"] == driveDefault and cData["maxResp"] == maxResp and maxResp not in RespVisted:
                                df_dict[nameMap["maxResp"]].append(str(maxResp))
                                df_dict["Type"].append(Type)
                                df_dict[ylabel].append(cData["Memory"]/1024/1024)
                                RespVisted.add(maxResp)
            if ylabel == "Running time(ms)":
                RespVisted = set()
                for maxResp in maxResps:
                    for Type in Types:
                        RespVisted = set()
                        for cData in anaData[cityName][Type]:
                            if cData["maxWalk"] == walkDefault and cData["maxDrive"] == driveDefault and cData["maxResp"] == maxResp and maxResp not in RespVisted:
                                df_dict[nameMap["maxResp"]].append(str(maxResp))
                                df_dict["Type"].append(Type)
                                df_dict[ylabel].append(cData["Task"]/2 + cData["Worker"]/2)
                                RespVisted.add(maxResp)
            df = pd.DataFrame(df_dict)
            df.to_excel(write, sheet_name=ylabel)
        write.save()

anaAlgorithm()
anaDriveTime()
anaRespTime()
anaDate()
anaMaxWalk()
anaTimePeriod()
anaAlgorithmSP()
anaDateSP()
anaMaxWalkSP()
anaTimePeriodSP()
anaDriveTimeSP()
anaRespTimeSP()
# anaMemRun()
anaRespMemRun()
anaWalkMemRun()
anaDriveMemRun()

print("All data generated")