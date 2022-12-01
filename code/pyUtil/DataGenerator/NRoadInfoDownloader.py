from datetime import datetime
from itertools import count
from operator import add
import time
import osmnx as ox
import networkx as nx
import pandas as pd
import numpy as np
import pickle
import os, sys

from pandas.core.groupby.groupby import GroupByPlot

'''
['ID', 'Segment ID', 'Roadway Name', 'From', 'To', 'Direction', 'Date', '12:00-1:00 AM', '1:00-2:00AM', '2:00-3:00AM', 
'3:00-4:00AM', '4:00-5:00AM', '5:00-6:00AM', '6:00-7:00AM', '7:00-8:00AM', '8:00-9:00AM', '9:00-10:00AM', '10:00-11:00AM', 
'11:00-12:00PM', '12:00-1:00PM', '1:00-2:00PM', '2:00-3:00PM', '3:00-4:00PM', '4:00-5:00PM', '5:00-6:00PM', '6:00-7:00PM', 
'7:00-8:00PM', '8:00-9:00PM', '9:00-10:00PM', '10:00-11:00PM', '11:00-12:00AM']
'''

dataBaseDir = "../../Experiment/DataSet"
cityName = "NYC"
cityPath = os.path.join(dataBaseDir, cityName)

def GenerateNYCMap():
    DG = ox.graph_from_place(query="New York, USA", network_type="drive")
    WG = ox.graph_from_place(query="New York, USA", network_type="walk")
    df = open("drive.pickle", "wb")
    wf = open("walk.pickle", "wb")
    pickle.dump(DG, df)
    pickle.dump(WG, wf)

def AnalyzeTheDate(filepath):
    filepath = os.path.join(cityPath, filepath)
    df = pd.read_csv(filepath)
    gs = df.groupby('Date')
    count_dict = {}
    for key, group in gs:
        year = key[-4:]
        if year not in count_dict.keys():
            count_dict[year] = 0
        if year == '2014':
            print("{}:{}".format(key, len(group)))
        count_dict[year] += len(group)
    print(count_dict)
    print(df[df['Date']=='10/13/2014'])


def collateTripDataStep1(fileName):
    filepath = os.path.join(cityPath, fileName)
    tempPath = os.path.join(cityPath, "./temp/order_temp.csv")
    
    time_start = datetime(2014, 10, 13, 0, 0, 0)
    time_end   = datetime(2014, 10, 18, 0, 0, 0)

    df = pd.read_csv(filepath, index_col=' pickup_datetime')
    # print(df.columns.tolist())
    '''
    ['vendor_id', ' pickup_datetime', ' dropoff_datetime', ' passenger_count', ' trip_distance', 
    ' pickup_longitude', ' pickup_latitude', ' rate_code', ' store_and_fwd_flag', ' dropoff_longitude', 
    ' dropoff_latitude', ' payment_type', ' fare_amount', ' surcharge', ' mta_tax', ' tip_amount', 
    ' tolls_amount', ' total_amount']
    '''
    df.index = pd.DatetimeIndex(df.index)
    df = df[df.index > time_start]
    df = df[df.index < time_end]

    df.to_csv(tempPath)

def collateTripDataStep2():
    tempPath = os.path.join(cityPath, "./temp/order_temp.csv")
    df = pd.read_csv(tempPath)
    df[' pickup_datetime'] = pd.DatetimeIndex(df[' pickup_datetime']) 
    df[' dropoff_datetime'] = pd.DatetimeIndex(df[' dropoff_datetime'])
    print(df)
    for day_s in range(13, 18):
        time_start = datetime(2014, 10, day_s, 0, 0, 0)
        time_end   = datetime(2014, 10, day_s + 1, 0, 0, 0)
        filename   = './order_01-30/total_ride_request/order_{}'.format(time_start.strftime("%Y%m%d"))
        TripInDay  = os.path.join(cityPath, filename)
        dftemp = df[df[' pickup_datetime'] > time_start]
        dftemp = dftemp[dftemp[' pickup_datetime'] < time_end]

        dfSave = dftemp[['vendor_id', ' pickup_datetime', ' dropoff_datetime', ' pickup_longitude', \
            ' pickup_latitude', ' dropoff_longitude', ' dropoff_latitude', ' trip_distance']]
        
        dfSave[' pickup_datetime'] = dfSave[' pickup_datetime'].apply(lambda x:int(time.mktime(x.timetuple())))
        dfSave[' dropoff_datetime'] = dfSave[' dropoff_datetime'].apply(lambda x:int(time.mktime(x.timetuple())))

        dfSave.to_csv(TripInDay, index=False, header=None)
        print("{} finished.".format(day_s))

def AnalyzeGraph():
    filepath  = os.path.join(cityPath, "./Graph/OriginGraph")
    walkPath  = os.path.join(filepath, "walk.pickle")
    drivePath = os.path.join(filepath, "drive.pickle")
    DG = pickle.load(open(drivePath, "rb"))
    for node1 in DG.nodes.keys():
        for node2 in DG[node1].keys():
            if 'name' in DG[node1][node2][0].keys() and DG[node1][node2][0]['name'] == 'Bedford Park Boulevard':
                print(DG[node1][node2])

def GenerateNYCEnvs():
    timeS = [450, 480, 510, 720, 750, 780, 1020, 1050, 1080, 1110]
    timeP = ['7:00-8:00AM', '8:00-9:00AM', '8:00-9:00AM', '12:00-1:00PM', '12:00-1:00PM', \
        '1:00-2:00PM', '5:00-6:00PM', '5:00-6:00PM', '6:00-7:00PM', '6:00-7:00PM']
    allDates = [datetime(2014, 10, day, 0, 0, 0) for day in range(13, 18)]
    DatesC = [" {}".format(mdate.strftime("%m/%d/%Y")) for mdate in allDates]
    print(DatesC)
    vehDF = os.path.join(cityPath, "./temp/Traffic_Volume_Counts__2014-2019_.csv")
    vehDF = pd.read_csv(vehDF)
    drivePath = os.path.join(cityPath, "./Graph/OriginGraph/drive.pickle")
    DG = pickle.load(open(drivePath, "rb"))
    DG = ox.add_edge_speeds(DG)
    DG = ox.add_edge_travel_times(DG)

    roadNames = vehDF['Roadway Name'].to_list() 
    
    roads = {}
    roadsLength = {}

    for roadName in roadNames:
        roads[roadName] = 0
        roadsLength[roadName] = [[0]]

    for i in range(len(timeS)):
        for date in allDates:
            MDG = pickle.load(open(drivePath, "rb"))
            MDG = ox.add_edge_speeds(MDG)

            for roadName in roads.keys():
                pTime = timeP[i]
                tempDF = vehDF[vehDF['Roadway Name']==roadName]
                tempDF = tempDF[tempDF['Date']=="{}".format(date.strftime("%m/%d/%Y"))]
                vehNum = tempDF[pTime].sum()
                roads[roadName] = vehNum

            for node1 in MDG.nodes.keys():
                for node2 in MDG[node1].keys():
                    if 'name' in MDG[node1][node2][0].keys():
                        rName = MDG[node1][node2][0]['name']
                        if type(rName)==type('str') and rName in roadsLength.keys():
                            roadsLength[rName][0][0] = roadsLength[rName][0][0] + MDG[node1][node2][0]['length']
                            roadsLength[rName].append([node1, node2])
            
            for roadName in roadsLength.keys():
                cap = roadsLength[roadName][0][0]/2.2
                piece = (1 + 2 * (roads[roadName]/cap)) ** 2
                for k in range(1, len(roadsLength[roadName])):
                    [a, b] = roadsLength[roadName][k]
                    MDG[a][b][0]['speed_kph'] = MDG[a][b][0]['speed_kph'] / piece
                    # if piece > 1.0:
                    #     print("Road from {} to {} changed - {}".format(a, b, piece))
            
            FMDG = ox.add_edge_travel_times(MDG)
            
            fileName = os.path.join(cityPath, "./Graph/GraphWithEnv/drive_{}_{}_{}.csv".format(
                date.strftime("%Y%m%d"), timeS[i], timeS[i]+30))
            
            saveGraphAsCSV(FMDG, fileName)

            for roadName in roads.keys():
                roads[roadName] = 0
                roadsLength[roadName] = [[0]]


def GenerateOriginDG():
    DGraphPath = os.path.join(cityPath, "Graph\\OriginGraph\\drive.pickle")
    WGraphPath = os.path.join(cityPath, "Graph\\OriginGraph\\walk.pickle")
    DGraphCSV  = os.path.join(cityPath, "Graph\\OriginGraph\\drive.csv")
    WGraphCSV  = os.path.join(cityPath, "Graph\\OriginGraph\\walk.csv")
    DNodeCSV = os.path.join(cityPath, "Graph\\OriginGraph\\dnode.csv")
    WNodeCSV = os.path.join(cityPath, "Graph\\OriginGraph\\wnode.csv")

    DG = pickle.load(open(DGraphPath, "rb"))
    DG = ox.add_edge_speeds(DG)
    DG = ox.add_edge_travel_times(DG)
    WG = pickle.load(open(WGraphPath, "rb"))

    Dnodes, Wnodes = {}, {}
    DGraph, WGraph, DnodeS, WnodeS = [], [], [], []

    num = 0
    for node in DG.nodes.keys():
        Dnodes[node] = num
        DnodeS.append([num, node, DG.nodes[node]['y'], DG.nodes[node]['x']])
        num += 1

    num = 0
    for node in WG.nodes.keys():
        Wnodes[node] = num
        WnodeS.append([num, node, WG.nodes[node]['y'], WG.nodes[node]['x']])
        num += 1

    for a in DG.nodes.keys():
        for b in DG[a].keys():
            DGraph.append([Dnodes[a], Dnodes[b], DG[a][b][0]['travel_time']])

    for a in WG.nodes.keys():
        for b in WG[a].keys():
            WGraph.append([Wnodes[a], Wnodes[b], WG[a][b][0]['length']])

    pd.DataFrame(DGraph).to_csv(DGraphCSV, header=0, index=0)
    pd.DataFrame(WGraph).to_csv(WGraphCSV, header=0, index=0)
    pd.DataFrame(DnodeS).to_csv(DNodeCSV, header=0, index=0)
    pd.DataFrame(WnodeS).to_csv(WNodeCSV, header=0, index=0)

    

def saveGraphAsCSV(G, fileName):
    nodes = {}
    num = 0
    Graph = []
    for node in G.nodes.keys():
        nodes[node] = num
        num += 1
    for a in G.nodes.keys():
        for b in G[a].keys():
            Graph.append([nodes[a], nodes[b], G[a][b][0]['travel_time']])
    Graph = pd.DataFrame(Graph)
    Graph.to_csv(fileName, header=0, index=0)

if __name__ == '__main__':
    GenerateNYCMap()
    # AnalyzeTheDate("./temp/Traffic_Volume_Counts__2014-2019_.csv")
    collateTripDataStep1("yellow_tripdata_2014-10.csv")
    collateTripDataStep2()
    # AnalyzeGraph()
    GenerateNYCEnvs()
    GenerateOriginDG()