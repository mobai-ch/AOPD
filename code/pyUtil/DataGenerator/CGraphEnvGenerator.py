import osmnx as ox
import networkx as nx
from osmnx import speed
from osmnx import distance
import pandas as pd
import os, sys
import pickle
from geopy.distance import geodesic
import numpy as np
import time
import matching as mc
import mathUtil as mu

class GraphEnvGenerator:
    def __init__(self, dataDir, utc) -> None:

        self.dataDir = dataDir
        self.utc = utc
    
    def ResetEnv(self, cityName):

        cityDir = os.path.join(self.dataDir, cityName)
        driveEnvFile = os.path.join(cityDir, "./Graph/OriginGraph/drive.pickle")      
        self.baseEnv = pickle.load(open(driveEnvFile, "rb"))
        self.storeData = {}
        node_dict = {}
        nodeCoor = []
        num = 0
        for node in self.baseEnv.nodes.keys():
            node_dict[num] = node
            self.storeData[node] = {}
            for key in self.baseEnv[node].keys():
                self.storeData[node][key] = [0, 0.0]
            nodeCoor.append([self.baseEnv.nodes[node]['y'], self.baseEnv.nodes[node]['x']])
            num += 1
        self.node_dict = node_dict
        self.node_coor = np.array(nodeCoor)

        print("Initialize the {} enviroment finished.".format(cityName))

    def findNearestNode(self, coor):
        coor = np.array([coor])
        temp = self.node_coor - coor
        temp = np.sum((temp * temp), axis=1)
        res  = np.argmin(temp)
        return self.node_dict[res]

    def readTraceFile(self, cityName, date):
        
        cityDir = os.path.join(self.dataDir, cityName)
        csvPart = "./gps/gps_{}".format(date)
        return pd.read_csv(os.path.join(cityDir, csvPart), header=None)


    def FindNearestEdge(self, A, B, C):
        a = B - A
        b = B - C
        a2 = np.sum(a * a, axis=1) 
        b2 = np.sum(b * b, axis=1)
        vL = a2 * b2

        vR  = np.sum(a * b, axis=1)
        vR  = vR * vR

        return np.argmin(np.sqrt((vL - vR)/a2))


    def saveTheEnv(self, cityName, date, timeS):
        for key_1 in self.storeData.keys():
            for key_2 in self.storeData[key_1].keys():
                if self.storeData[key_1][key_2][0] > 0:
                    speed = self.storeData[key_1][key_2][1]/self.storeData[key_1][key_2][0]
                    if self.baseEnv[key_1][key_2][0]['speed_kph'] > speed:
                        self.baseEnv[key_1][key_2][0]['speed_kph'] = speed
        G = ox.speed.add_edge_travel_times(self.baseEnv)
        csvFilePath = os.path.join(self.dataDir, cityName)
        csvFilePath = os.path.join(csvFilePath, "./Graph/GraphWithEnv/drive_{}_{}_{}.csv".format(date, timeS, timeS+30))
        self.ParseGraphCSV(G, csvFilePath)
        print("The file is saved as {}".format(csvFilePath))


    def ParseGraphCSV(self, G, CsvFilePath):
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
        Graph.to_csv(CsvFilePath, header=0, index=0)
        


    def GenerateEnvTime(self, cityName, date, timeS):

        cityDir = os.path.join(self.dataDir, cityName)
        self.ResetEnv(cityName)      

        mixTrace = self.readTraceFile(cityName, date)
        mixTrace = mixTrace[(((mixTrace[2] + 28800)/60)%(24*60) > timeS) & (((mixTrace[2] + 28800)/60)%(24*60) < timeS+30)]
        mixTrace.sort_values(2)
        mixTrace = mixTrace.groupby(1)
        print("Number of the trace: {}".format(len(mixTrace)))

        tempTime = 0.0
        nodenum = 0

        for key, group in mixTrace:
            time1 = time.time()
            nodeSet = set()
            EN_1, EN_2 = [], []
            END_1, END_2 = [], []
            
            for index, row in group.iterrows():
                (lat, lon) = (row[4], row[3])
                node = self.findNearestNode([lat, lon])
                nodeSet.add(node)
                for start in nodeSet:
                    for end in nodeSet:
                        if end in self.storeData[start].keys():
                            EN_1.append([self.baseEnv.nodes[start]['y'], self.baseEnv.nodes[start]['x']])
                            EN_2.append([self.baseEnv.nodes[end]['y'], self.baseEnv.nodes[end]['x']])
                            END_1.append(start)
                            END_2.append(end)
            
            EN_1 = np.array(EN_1)
            EN_2 = np.array(EN_2)

            if EN_1.shape[0] > 0:
                for i in range(len(group)-1):
                    row_1 = group.iloc[i]
                    row_2 = group.iloc[i+1]
                    (lat0, lon0, timestamp0) = (row_1[4], row_1[3], row_1[2])
                    (lat1, lon1, timestamp1) = (row_2[4], row_2[3], row_2[2])
                    speed = 3.6 * geodesic((lat0, lon0), (lat1, lon1)).m/(timestamp1 - timestamp0)
                    if speed > 0.5:
                        coor = np.array([[(lat0 + lat1)/2, (lon0 + lon1)/2]])
                        edgeNum = self.FindNearestEdge(EN_1, EN_2, coor)
                        start = END_1[edgeNum]
                        end = END_2[edgeNum]
                        self.storeData[start][end][0] += 1
                        self.storeData[start][end][1] += speed

            time2 = time.time()
            nodenum += 1
            tempTime += (time2 - time1)

            if(nodenum % 100 == 0):
                print("RunTime for {}: {}".format(nodenum, tempTime))
                tempTime = 0

        self.saveTheEnv(cityName, date, timeS)

    def GenerateEnvDate(self, cityName, date):
        all_times = [450, 480, 510, 720, 750, 780, 1020, 1050, 1080, 1110]
        for timeS in all_times:
            self.GenerateEnvTime(cityName, date, timeS)

def GenerateChengduMap():
    DG = ox.graph_from_place(query="Chengdu,Sichuan,China", network_type="drive")
    WG = ox.graph_from_place(query="Chengdu,Sichuan,China", network_type="walk")
    df = open("drive.pickle", "wb")
    wf = open("walk.pickle", "wb")
    pickle.dump(DG, df)
    pickle.dump(WG, wf)

if __name__ == '__main__':
    GenerateChengduMap()
    generator = GraphEnvGenerator("../../Experiment/DataSet/", 8)
    generator.GenerateEnvDate("chengdu", "20161101")
    generator.GenerateEnvDate("chengdu", "20161102")
    generator.GenerateEnvDate("chengdu", "20161103")
    generator.GenerateEnvDate("chengdu", "20161104")
    generator.GenerateEnvDate("chengdu", "20161105")
    generator.GenerateEnvDate("chengdu", "20161106")
    generator.GenerateEnvDate("chengdu", "20161107")
    generator.GenerateEnvDate("chengdu", "20161108")
    generator.GenerateEnvDate("chengdu", "20161109")
    generator.GenerateEnvDate("chengdu", "20161110")
