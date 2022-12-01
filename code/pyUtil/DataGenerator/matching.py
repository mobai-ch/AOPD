from datetime import datetime, timedelta
from dis import dis
from platform import node
from tracemalloc import start
from turtle import st
import pandas as pd
import numpy as np
import os, sys
import pickle
import argparse

from tqdm import tqdm
from mathUtil import *

trajectory_path = "./Dataset/Trajectories/"
graph_path = "./Dataset/RoadNetwork/"
outputPath = "./output/"

def read_graph(cityName):
    cityGPath = os.path.join(graph_path, cityName + ".pickle")
    G = pickle.load(open(cityGPath, "rb"))
    vertices = {}
    verticesMap = {}
    verticeMatrix = []
    num = 0
    for nodeId in G.nodes.keys():
        vertices[num] = [G.nodes[nodeId]['y'], G.nodes[nodeId]['x']]
        verticesMap[nodeId] = num
        verticeMatrix.append([num, G.nodes[nodeId]['y'], G.nodes[nodeId]['x']])
        num += 1
    edgeMatrix = []
    edges = {}
    initEdges = {}
    for nodeId in G.nodes.keys():
        edges[verticesMap[nodeId]] = {}
        initEdges[verticesMap[nodeId]] = {}
        for eNode in G[nodeId].keys():
            edges[verticesMap[nodeId]][verticesMap[eNode]] = G[nodeId][eNode][0]['length']
            initEdges[verticesMap[nodeId]][verticesMap[eNode]] = G[nodeId][eNode][0]['travel_time']
            edgeMatrix.append(vertices[verticesMap[nodeId]] + vertices[verticesMap[eNode]] + [verticesMap[nodeId], verticesMap[eNode]])
    return vertices, edges, np.array(edgeMatrix), np.array(verticeMatrix), initEdges    
 
def read_trajectories(fileName):
    gpsInfo = pd.read_csv(os.path.join(trajectory_path, fileName), header=None)
    return gpsInfo

def findNearestEdge(lat, lon, edgeMatrix):
    distances = ComputeHeight(lat, lon, edgeMatrix)
    edge_index = np.argmin(distances)
    start, end = edgeMatrix[edge_index, 4], edgeMatrix[edge_index, 5]
    return (start, end)

def findNearestEdgeV2(lat, lon, edges, vertices, verticeMatrix):
    edgeMatrix = []
    distanceA = computeDistances(lat, lon, verticeMatrix)
    vertex_index = []

    for i in range(5):
        temp_vertex_index = np.argmin(distanceA)
        distanceA[temp_vertex_index] = 99999999
        vertex_index.append(temp_vertex_index)

    for index in vertex_index:
        startID = verticeMatrix[index, 0]
        for endID in edges[startID].keys():
            edgeMatrix.append(vertices[startID] + vertices[endID] + [startID, endID])
    edgeMatrix = np.array(edgeMatrix)
    distances = ComputeHeight(lat, lon, edgeMatrix)
    edge_index = np.argmin(distances)
    start, end = int(edgeMatrix[edge_index, 4]), int(edgeMatrix[edge_index, 5])
    return (start, end)

def turnTrajectory2TravelTime(Trajectories, edgeMatrix, edges, vertices, verticeMatrix):
    retList = []
    allEdgesUpdate = {}
    for key, value in tqdm(Trajectories.items()):
        allTimes = []
        allCoors = []
        allParition = []
        allEdges = []
        trajectory_onekey = Trajectories[key]
        for (time_now, lat, lon) in trajectory_onekey:
            allTimes.append(time_now)
            (start, end) = findNearestEdgeV2(lat, lon, edges, vertices, verticeMatrix)
            # print((start, end))
            partition = computePartition(lat, lon, vertices[start][0], \
                vertices[start][1], vertices[end][0], vertices[end][1])
            allParition.append(partition)
            allEdges.append((start, end))
            allCoors.append((lat, lon))
        for i in range(len(allEdges)-5):
            availiable_flag = 1
            for k in range(4):
                if allEdges[i+k][0] != allEdges[i+k+1][0] or allEdges[i+k][1] != allEdges[i+k+1][1]:
                    availiable_flag = 0
            if availiable_flag == 1:
                timeDiff = allTimes[i+3] - allTimes[i]
                lengthDiff = abs(allParition[i+3] - allParition[i]) * edges[allEdges[i][0]][allEdges[i][1]]
                if lengthDiff > 10:
                    speed = lengthDiff/timeDiff
                    travelTime = edges[allEdges[i][0]][allEdges[i][1]]/speed
                    if allEdges[i][0] not in allEdgesUpdate:
                        allEdgesUpdate[allEdges[i][0]] = {}
                    if allEdges[i][1] not in allEdgesUpdate[allEdges[i][0]].keys():
                        allEdgesUpdate[allEdges[i][0]][allEdges[i][1]] = [0, 0]
                    allEdgesUpdate[allEdges[i][0]][allEdges[i][1]][0] += 1
                    allEdgesUpdate[allEdges[i][0]][allEdges[i][1]][1] += travelTime
    for start in allEdgesUpdate.keys():
        for end in allEdgesUpdate[start].keys():
            retList.append([str(int(start)), str(int(end)), allEdgesUpdate[start][end][1]/allEdgesUpdate[start][end][0]])
    return retList  

def transTrajectories(all_trajectory, edgeMatrix, edges, vertices, timeStamp1, timeStamp2, timeSlice, verticeMatrix):
    for timeStart in range(timeStamp1, timeStamp2, timeSlice):
        trajectory_piece = all_trajectory[(all_trajectory[2] > timeStart) & (all_trajectory[2] < timeSlice + timeStart)]
        Trajectories = {}
        gpsPieceInfo = trajectory_piece.groupby(1)
        for key, df in gpsPieceInfo:
            Trajectories[key] = []
            arr = df.to_numpy()
            Times, longitudes, latitudes = arr[:, 2], arr[:, 3], arr[:, 4]
            for i in range(Times.shape[0]):
                Trajectories[key].append((Times[i], latitudes[i], longitudes[i]))
        print("Compute trajectory from {} to {}".format(timeStart, timeStart + timeSlice))
        travelTimeList = turnTrajectory2TravelTime(Trajectories, edgeMatrix, edges, vertices, verticeMatrix)
        print("Finish computing trajectory from {} to {}".format(timeStart, timeStart + timeSlice))
        for i in range(len(travelTimeList)):
            travelTimeList[i] += [timeStart, timeStart + timeSlice]
        filePath = os.path.join(outputPath, "{}-{}.csv".format(str(int(timeStart)), str(int(timeStart + timeSlice))))
        travelTimeList = np.array(travelTimeList)
        travelTimeList = pd.DataFrame(travelTimeList)
        travelTimeList.to_csv(filePath, header=None, index=None)

def saveGraph(vertices, edges, initEdges):
    vertice_save = []
    edge_save = []
    init_edge_save = []
    for pId in vertices.keys():
        vertice_save.append([str(pId), str(vertices[pId][0]), str(vertices[pId][1])])
    for Id_a in edges.keys():
        for Id_b in edges[Id_a].keys():
            edge_save.append([str(Id_a), str(Id_b), edges[Id_a][Id_b]])
            init_edge_save.append([str(Id_a), str(Id_b), initEdges[Id_a][Id_b]])
    vertice_save = np.array(vertice_save)
    edge_save = np.array(edge_save)
    init_edge_save = np.array(init_edge_save)
    
    pd.DataFrame(vertice_save).to_csv(os.path.join(outputPath, "vertices.csv"), header=None, index=None)
    pd.DataFrame(edge_save).to_csv(os.path.join(outputPath, "edges.csv"), header=None, index=None)
    pd.DataFrame(init_edge_save).to_csv(os.path.join(outputPath, "initEdges.csv"), header=None, index=None)

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("date")
    args = parser.parse_args()
    dateTimeStr = args.date
    sTime = datetime.strptime(dateTimeStr, '%Y%m%d') + timedelta(hours=8)
    sTime = int(datetime.timestamp(sTime))
    eTime = sTime + 60 * 60 * 24
    vertices, edges, edgeMatrix, verticeMatrix, initEdges = read_graph("Chengdu")
    saveGraph(vertices, edges, initEdges)
    all_trajectory = read_trajectories("gps_{}".format(dateTimeStr))
    transTrajectories(all_trajectory, edgeMatrix, edges, vertices, sTime, eTime, 300, verticeMatrix)

if __name__ == '__main__':
    main()














