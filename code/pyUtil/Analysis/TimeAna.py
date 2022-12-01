import pandas as pd
import numpy as np
import os, sys
import seaborn as sn
import matplotlib.pyplot as plt

originPath = "../../Experiment/ExpRecords/EndData"

def AnaTimes(cityName):
    maxWalk = [60, 120, 180, 240, 300]
    maxResp = [60, 90, 120, 150, 180]
    styles = ['PD', "EMA"]
    matchMethods = ['greedy', "greedyT", "KMT"]
    styleMap = {"PD": "Tripartite graph-cached", "EMA": "no optimization"}

    walk_runtime = {
        "Max Extra Walking Time": [],
        "Mean RunTime":  [],
        "Type": []
    }

    for walktime in maxWalk:
        for style in styles:
            for matchMethod in matchMethods:
                taskFileName = "{}_{}-{}-{}-{}_Task.json".format(cityName, matchMethod, walktime, 120, style)
                workerFileName = "{}_{}-{}-{}-{}_worker.json".format(cityName, matchMethod, walktime, 120, style)
                taskFileName = os.path.join(originPath, taskFileName)
                workerFileName = os.path.join(originPath, workerFileName)
                runtime = GetTime(taskFileName, workerFileName)
                Type = "AOPUD-{}-{}".format(matchMethod, styleMap[style])
                walk_runtime["Max Extra Walking Time"].append(walktime)
                walk_runtime["Mean RunTime"].append(runtime)
                walk_runtime["Type"].append(Type)

    xlsFilePath = os.path.join("./XLSFile", "{}_MaxWalk_runtime.xlsx".format(cityName))
    excelWriter = pd.ExcelWriter(xlsFilePath, engine="xlsxwriter")
    df = pd.DataFrame(walk_runtime)
    sn.set(font_scale=1, font='Times New Roman')
    plt.figure(figsize=(6, 5))
    sn.lineplot(data=df, x="Max Extra Walking Time", y="Mean RunTime", hue="Type", style="Type", markers=True, dashes=False)
    imgPath = os.path.join("./Image", "{}_MaxWalk_{}".format(cityName, "runtime"))
    idf = pd.DataFrame(df)
    idf.to_excel(excelWriter, sheet_name="runtime")
    plt.savefig(imgPath)  
    excelWriter.save()  

    response_runtime = {
        "Max Response Time": [],
        "Mean RunTime":  [],
        "Type": []
    }

    for resp in maxResp:
        for style in styles:
            for matchMethod in matchMethods:
                taskFileName = "{}_{}-{}-{}-{}_Task.json".format(cityName, matchMethod, 120, resp, style)
                workerFileName = "{}_{}-{}-{}-{}_worker.json".format(cityName, matchMethod, 120, resp, style)
                taskFileName = os.path.join(originPath, taskFileName)
                workerFileName = os.path.join(originPath, workerFileName)
                runtime = GetTime(taskFileName, workerFileName)
                Type = "AOPUD-{}-{}".format(matchMethod, styleMap[style])
                response_runtime["Max Response Time"].append(resp)
                response_runtime["Mean RunTime"].append(runtime)
                response_runtime["Type"].append(Type)

    xlsFilePath = os.path.join("./XLSFile", "{}_MaxResponse_runtime.xlsx".format(cityName))
    excelWriter = pd.ExcelWriter(xlsFilePath, engine="xlsxwriter")
    df = pd.DataFrame(response_runtime)
    sn.set(font_scale=1, font='Times New Roman')
    plt.figure(figsize=(6, 5))
    sn.lineplot(data=df, x="Max Response Time", y="Mean RunTime", hue="Type", style="Type", markers=True, dashes=False)
    imgPath = os.path.join("./Image", "{}_MaxResponse_{}".format(cityName, "runtime"))
    idf = pd.DataFrame(df)
    idf.to_excel(excelWriter, sheet_name="runtime")
    plt.savefig(imgPath)  
    excelWriter.save()

    

def GetTime(taskFileName, workerFileName):
    taskPD = pd.DataFrame(eval(open(taskFileName, "r").read()), index=[0])
    workerPD = pd.DataFrame(eval(open(workerFileName, "r").read()), index=[0])
    taskPD = taskPD.T
    workerPD = workerPD.T
    avgRunTime = (taskPD[0].sum() + workerPD[0].sum())/(taskPD.shape[0] + workerPD.shape[0])
    return avgRunTime

AnaTimes("chengdu")
AnaTimes("NYC")