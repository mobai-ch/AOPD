import pandas as pd
import numpy as np
import os, sys, shutil
from tqdm import tqdm

def transAllFileUnderDirectory(dirPath):
    filenames = os.listdir(dirPath)
    for filename in tqdm(filenames):
        if filename[-3:] == 'csv':
            transFileWithDPOI(filename, "../OriginGraph/dpoi.csv")

def renameAndCopy(fileName, dirPath):
    nfilename = "h_" + fileName
    shutil.copy(os.path.join(dirPath, fileName), os.path.join(dirPath, nfilename)) 
    
def transFileWithDPOI(fileName, dpoifilePath):
    driveEdges = {}
    driveEdgesLen = {}

    drive = pd.read_csv(fileName, header=None)
    renameAndCopy(fileName, "./")

    for index, row in drive.iterrows():
        sid, did = int(row[0]), int(row[1])
        if sid not in driveEdges.keys():
            driveEdges[sid] = {}
            driveEdgesLen[sid] = {}
        if did not in driveEdges[sid].keys():
            driveEdges[sid][did] = []
        driveEdgesLen[sid][did] = row[2]
    
    dpois = pd.read_csv(dpoifilePath, header=None)

    for index, row in dpois.iterrows():
        [nid, s, d, partition, lat, lon] = row
        s, d = int(s), int(d)
        if partition != 0.0 and partition != 1.0:
            driveEdges[s][d].append((partition, nid))
    
    for s in driveEdges.keys():
        for d in driveEdges[s].keys():
            if len(driveEdges[s][d]) > 0:
                info = driveEdges[s][d]
                info.sort(key=lambda tup:tup[0])
                driveEdges[s][d] = info
                currentLen = driveEdgesLen[s][d] 
                driveEdgesLen[s][driveEdges[s][d][0][1]] = currentLen * driveEdges[s][d][0][0]
                for i in range(len(driveEdges[s][d])-1):
                    ns, nd = driveEdges[s][d][i][1], driveEdges[s][d][i+1][1]
                    sp, dp = driveEdges[s][d][i][0], driveEdges[s][d][i+1][0]
                    driveEdgesLen[ns] = {}
                    driveEdgesLen[ns][nd] = currentLen * (dp-sp)
                ns, sp = driveEdges[s][d][-1][1], driveEdges[s][d][-1][0]
                driveEdgesLen[ns] = {}
                driveEdgesLen[ns][d] = currentLen * (1.0-sp)
                driveEdgesLen[s].pop(d)

    driveSave = []

    for s in driveEdgesLen.keys():
        for d in driveEdgesLen[s].keys():
            driveSave.append([str(int(s)), str(int(d)), driveEdgesLen[s][d]])

    driveSave = np.array(driveSave)
    driveSave = pd.DataFrame(driveSave, index=None)
    driveSave.to_csv(fileName, header=None, index=None)
    

def rebuildOriginGraph():
    dnodes = []
    wnodes = []
    driveEdges = {}
    walkEdges = {}
    driveEdgesLen = {}
    walkEdgesLen = {}

    drive = pd.read_csv("drive.csv", header=None)
    walk = pd.read_csv("walk.csv", header=None)
    dnodef = pd.read_csv("dnode.csv", header=None)
    wnodef = pd.read_csv("wnode.csv", header=None)
 
    renameAndCopy("drive.csv", "./")
    renameAndCopy("walk.csv", "./")
    renameAndCopy("dnode.csv", "./")
    renameAndCopy("wnode.csv", "./")
    
    for index, row in drive.iterrows():
        sid, did = int(row[0]), int(row[1])
        if sid not in driveEdges.keys():
            driveEdges[sid] = {}
            driveEdgesLen[sid] = {}
        if did not in driveEdges[sid].keys():
            driveEdges[sid][did] = []
        driveEdgesLen[sid][did] = row[2]

    for index, row in walk.iterrows():
        sid, did = int(row[0]), int(row[1])
        if sid not in walkEdges.keys():
            walkEdges[sid] = {}
            walkEdgesLen[sid] = {}
        if did not in walkEdges[sid].keys():
            walkEdges[sid][did] = []
        walkEdgesLen[sid][did] = row[2]
    
    for index, row in dnodef.iterrows():
        [nid, osmid, lat, lon] = int(row[0]), int(row[1]), row[2], row[3]
        dnodes.append([str(nid), str(osmid), lat, lon])
    
    for index, row in wnodef.iterrows():
        [nid, osmid, lat, lon] = int(row[0]), int(row[1]), row[2], row[3]
        wnodes.append([str(nid), str(osmid), lat, lon])
        
    dpois = pd.read_csv("dpoi.csv", header=None)
    wpois = pd.read_csv("wpoi.csv", header=None)

    for index, row in dpois.iterrows():
        [nid, s, d, partition, lat, lon] = row
        s, d = int(s), int(d)
        if partition != 0.0 and partition != 1.0:
            driveEdges[s][d].append((partition, nid))
            dnodes.append([int(nid), int(nid), lat, lon])
    
    for index, row in wpois.iterrows():
        [nid, s, d, partition, lat, lon] = row
        s, d = int(s), int(d)
        if partition != 1.0 and partition != 0.0:
            walkEdges[s][d].append((partition, nid))
            wnodes.append([int(nid), int(nid), lat, lon])
    
    for s in driveEdges.keys():
        for d in driveEdges[s].keys():
            if len(driveEdges[s][d]) > 0:
                info = driveEdges[s][d]
                info.sort(key=lambda tup:tup[0])
                driveEdges[s][d] = info
                currentLen = driveEdgesLen[s][d] 
                driveEdgesLen[s][driveEdges[s][d][0][1]] = currentLen * driveEdges[s][d][0][0]
                for i in range(len(driveEdges[s][d])-1):
                    ns, nd = driveEdges[s][d][i][1], driveEdges[s][d][i+1][1]
                    sp, dp = driveEdges[s][d][i][0], driveEdges[s][d][i+1][0]
                    driveEdgesLen[ns] = {}
                    driveEdgesLen[ns][nd] = currentLen * (dp-sp)
                ns, sp = driveEdges[s][d][-1][1], driveEdges[s][d][-1][0]
                driveEdgesLen[ns] = {}
                driveEdgesLen[ns][d] = currentLen * (1.0-sp)
                driveEdgesLen[s].pop(d)
    
    for s in walkEdges.keys():
        for d in walkEdges[s].keys():
            if len(walkEdges[s][d]) > 0:
                info = walkEdges[s][d]
                info.sort(key=lambda tup:tup[0])
                walkEdges[s][d] = info
                currentLen = walkEdgesLen[s][d]
                walkEdgesLen[s][walkEdges[s][d][0][1]] = currentLen * walkEdges[s][d][0][0]
                for i in range(len(walkEdges[s][d])-1):
                    ns, nd = walkEdges[s][d][i][1], walkEdges[s][d][i+1][1]
                    sp, dp = walkEdges[s][d][i][0], walkEdges[s][d][i+1][0]
                    walkEdgesLen[ns] = {}
                    walkEdgesLen[ns][nd] = currentLen * (dp-sp)
                ns, sp = walkEdges[s][d][-1][1], walkEdges[s][d][-1][0]
                walkEdgesLen[ns] = {}
                walkEdgesLen[ns][d] = currentLen * (1.0-sp)
                walkEdgesLen[s].pop(d)
    
    walkSave, driveSave = [], []
    for s in walkEdgesLen.keys():
        for d in walkEdgesLen[s].keys():
            walkSave.append([str(int(s)), str(int(d)), walkEdgesLen[s][d]])
    for s in driveEdgesLen.keys():
        for d in driveEdgesLen[s].keys():
            driveSave.append([str(int(s)), str(int(d)), driveEdgesLen[s][d]])

    driveSave = np.array(driveSave)
    walkSave = np.array(walkSave)
    driveSave = pd.DataFrame(driveSave, index=None)
    driveSave.to_csv("drive.csv", header=None, index=None)
    walkSave = pd.DataFrame(walkSave, index=None)
    walkSave.to_csv("walk.csv", header=None, index=None)

    dnodeSave = np.array(dnodes)
    wnodeSave = np.array(wnodes)
    dnodeSave = pd.DataFrame(dnodeSave, index=None)
    dnodeSave.to_csv("dnode.csv", header=None, index=None)
    wnodeSave = pd.DataFrame(wnodeSave, index=None)
    wnodeSave.to_csv("walk.csv", header=None, index=None)

if __name__ == '__main__':
    rebuildOriginGraph()
    # transAllFileUnderDirectory("./")


