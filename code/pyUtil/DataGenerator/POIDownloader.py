from heapq import merge
from lib2to3.pgen2 import driver
from operator import index
import numpy as np
import osmnx as ox
import pickle
from tqdm import tqdm
import os, sys
from mathGeo import DistanceUtil

import pandas as pd
from shapely.geometry import Point, LineString, Polygon

def downloadPOI(locationName):
    with open("{}.pickle".format(locationName), "wb") as f:
        pdf = ox.geometries_from_place(locationName, tags={"amenity": True}) 
        pickle.dump(pdf, f)

def generatePOIFile(locationName):
        pdf = pickle.load(open("{}.pickle".format(locationName), "rb"))
        poiList = []
        index_count = 10000000
        for index, row in pdf.iterrows():
            coord = []
            if type(row["geometry"]) == type(Point((0, 0))):
                coord = meanList(row["geometry"].coords[:])
            elif type(LineString([(0, 1), (2, 3), (4, 5)])) == type(row["geometry"]):
                coord = meanList(list(row["geometry"].coords))
            elif type(Polygon([(0, 0), (0, 1), (1, 1), (0, 0)])) == type(row["geometry"]):
                coord = meanList(row["geometry"].exterior.coords[:])
            else:
                poly_list = []
                for poly in list(row["geometry"].geoms):
                    poly_list += poly.exterior.coords
                coord = meanList(poly_list)
            poiList.append([str(int(index_count)), coord[1], coord[0]])
            index_count += 1
        return poiList

def meanList(polyList):
        # compute the mean value for all points in poly list
        sum_val = [0, 0]
        for poly in polyList:
            sum_val[0] += poly[0]
            sum_val[1] += poly[1]
        sum_val[0] /= len(polyList)
        sum_val[1] /= len(polyList)
        return sum_val

def mergeOriginMap():
    allEdges = {}
    allNodes = {}
    dnodeMap = {}
    wnodeMap = {}
    dnodes = pd.read_csv("dnode.csv", header=None)
    wnodes = pd.read_csv("wnode.csv", header=None)
    for index, row in dnodes.iterrows():
        nid, osmid, lat, lon = int(row[0]), int(row[1]), row[2], row[3]
        if osmid not in allNodes.keys():
            allNodes[osmid] = [-1, -1, -1, -1]
        allNodes[osmid][0] = nid
        allNodes[osmid][2] = lat
        allNodes[osmid][3] = lon
        dnodeMap[nid] = osmid
    for index, row in wnodes.iterrows():
        nid, osmid, lat, lon = int(row[0]), int(row[1]), row[2], row[3]
        if osmid not in allNodes.keys():
            allNodes[osmid] = [-1, -1, -1, -1]
        allNodes[osmid][1] = nid
        allNodes[osmid][2] = lat
        allNodes[osmid][3] = lon
        wnodeMap[nid] = osmid

    drive = pd.read_csv("drive.csv", header=None)
    walk = pd.read_csv("walk.csv", header=None)

    for index, row in drive.iterrows():
        sid, did = int(row[0]), int(row[1])
        soid = dnodeMap[sid]
        doid = dnodeMap[did]
        if soid not in allEdges.keys():
            allEdges[soid] = {}
        if doid not in allEdges[soid].keys():
            allEdges[soid][doid] = [-1, -1, -1]
        allEdges[soid][doid][0] = 1
        allEdges[soid][doid][2] = row[2]
    
    for index, row in walk.iterrows():
        sid, did = int(row[0]), int(row[1])
        soid = wnodeMap[sid]
        doid = wnodeMap[did]
        if soid not in allEdges.keys():
            allEdges[soid] = {}
        if doid not in allEdges[soid].keys():
            allEdges[soid][doid] = [-1, -1, -1]
        allEdges[soid][doid][1] = 1
        allEdges[soid][doid][2] = row[2]

    return allNodes, allEdges
    
def mapPOIs2Edge(poilist, allnodes, alledges):
    # get all poi and the mapped osm edge
    dUtil = DistanceUtil()
    edgesArr = []
    for sid in alledges.keys():
        for did in alledges[sid].keys():
            edgesArr.append(np.array([sid, did, allnodes[sid][2], allnodes[sid][3], allnodes[did][2],\
                 allnodes[did][3], alledges[sid][did][2], alledges[sid][did][2]]))
    edgesArr = np.array(edgesArr)
    dUtil.initEdges(edgesArr)
    poi2EdgeList = []
    for [pid, lat, lon] in tqdm(poilist):
        [source, destination], partition = dUtil.findEdgeWithMinDistance([lat, lon])
        poi2EdgeList.append([pid, source, destination, partition, lat, lon])
    return poi2EdgeList

def transpoi2EdgesIn(poi2EdgeList, allnodes, alledges):
    # get the poi map to pedestrian road network (mapped id) and vehicle road network respectively
    drivePOIs = []
    walkPOIs = []
    for [pid, source, destination, partition, lat, lon] in poi2EdgeList:
        # generate pois in driver and pedestrian road network 
        if alledges[source][destination][0] == 1:
            drivePOIs.append([str(pid), str(int(allnodes[source][0])), str(int(allnodes[destination][0])), partition, lat, lon])
        if alledges[source][destination][1] == 1:
            walkPOIs.append([str(pid), str(int(allnodes[source][1])), str(int(allnodes[destination][1])), partition, lat, lon])
    drivePOIs = np.array(drivePOIs)
    walkPOIs = np.array(walkPOIs)
    drivePOIs = pd.DataFrame(drivePOIs, index=None)
    drivePOIs.to_csv("dpoi.csv", header=None, index=None)
    walkPOIs = pd.DataFrame(walkPOIs, index=None)
    walkPOIs.to_csv("wpoi.csv", header=None, index=None)

if __name__ == '__main__':
    locationName = "Chengdu, Sichuan, China"
    # locationName = "New York, USA"
    if not os.path.exists("{}.pickle".format(locationName)):
        downloadPOI(locationName)
    poilist = generatePOIFile(locationName)
    allnodes, alledges = mergeOriginMap()
    poi2EdgeList = mapPOIs2Edge(poilist, allnodes, alledges)
    transpoi2EdgesIn(poi2EdgeList, allnodes, alledges)

