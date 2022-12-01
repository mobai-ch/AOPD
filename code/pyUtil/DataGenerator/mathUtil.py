import numpy as np
import math

'''
    point: (latitude, longitude)
    lines: numpy - [start, end, start_lati, start_longi, end_lati, end_longi]
'''

def computeDistance(point, lines):
    EARTH_RADIUS = 6378.137
    (lat, lon) = point
    lines_1 = np.sqrt((lines[:, 2] - lat)**2 + (lines[:, 3]-lon)**2) * 1000
    lines_2 = np.sqrt((lines[:, 4] - lat)**2 + (lines[:, 5]-lon)**2) * 1000
    lines_3 = np.sqrt((lines[:, 4] - lines[:, 2])**2 + (lines[:, 5]-lines[:, 3])**2) * 1000
    lines_3[lines_3<=0.1] = 0.1
    avg = (lines_1 + lines_2 + lines_3)/2
    A = np.sqrt(np.abs(avg*(avg - lines_1)*(avg - lines_2)*(avg - lines_3)))
    hs = 2 * A / lines_3
    return hs
    
def computeGeoDistance(point, lines):
    EARTH_RADIUS = 6378.137
    (lat, lon) = point
    p0_lat = lat * math.pi/180
    p0_lon = lon * math.pi/180
    p1_lat = lines[:, 2] * math.pi/180
    p1_lon = lines[:, 3] * math.pi/180
    p2_lat = lines[:, 4] * math.pi/180
    p2_lon = lines[:, 5] * math.pi/180

    a = p0_lat - p1_lat
    b = p0_lon - p1_lon
    lines_1 = 2 * np.arcsin(np.sqrt(np.sin(a/2)**2+np.cos(p0_lat)*np.cos(p1_lat)*np.sin(b/2)**2)) * EARTH_RADIUS * 1000

    a = p0_lat - p2_lat
    b = p0_lon - p2_lon
    lines_2 = 2 * np.arcsin(np.sqrt(np.sin(a/2)**2+np.cos(p0_lat)*np.cos(p2_lat)*np.sin(b/2)**2)) * EARTH_RADIUS * 1000

    a = p1_lat - p2_lat
    b = p1_lon - p2_lon
    lines_3 = 2 * np.arcsin(np.sqrt(np.sin(a/2)**2+np.cos(p1_lat)*np.cos(p2_lat)*np.sin(b/2)**2)) * EARTH_RADIUS * 1000

    s = (lines_1 + lines_2 + lines_3)/2
    A = np.sqrt(s*(s-lines_1)*(s-lines_2)*(s-lines_3))
    hs = 2 * A / lines_3
    hs = np.nan_to_num(hs)

    oneLine = np.where(lines_3 != 0, 1, 0)
    zeroLine = np.where(lines_3 == 0, 1, 0)
    hs = zeroLine * lines_1 + hs * oneLine
    
    min_cmp_lines_12 = np.min(np.array([lines_1, lines_2]), axis=0)
    max_cmp_lines_12 = np.max(np.array([lines_1, lines_2]), axis=0)

    costheta = min_cmp_lines_12 ** 2 + lines_3 ** 2 - max_cmp_lines_12 ** 2
    hs_choosen = np.where(costheta >= 0, 1, 0)
    min_choosen = np.where(costheta < 0, 1, 0)
    ret = hs * hs_choosen + min_cmp_lines_12 * min_choosen
    return ret

def ComputeEuclidean(latA, lonA, latB, lonB):
    a_lat = latA * math.pi/180
    a_lon = lonA * math.pi/180
    b_lat = latB * math.pi/180
    b_lon = lonB * math.pi/180
    EARTH_RADIUS = 6378.137
    a = a_lat - b_lat
    b = a_lon - b_lon
    dis = 2 * math.asin(math.sqrt(math.sin(a/2)**2+math.cos(a_lat)*math.cos(b_lat)*math.sin(b/2)**2)) * EARTH_RADIUS * 1000
    return dis

def computeEuclideanNumpy(latA, lonA, latB, lonB):
    a_lat = latA * math.pi/180
    a_lon = lonA * math.pi/180
    b_lat = latB * math.pi/180
    b_lon = lonB * math.pi/180
    EARTH_RADIUS = 6378.137
    a = a_lat - b_lat
    b = a_lon - b_lon
    dis = 2 * np.arcsin(np.sqrt(np.sin(a/2)**2+np.cos(a_lat)*np.cos(b_lat)*np.sin(b/2)**2)) * EARTH_RADIUS * 1000
    return dis

def ComputeHeight(lat, lon, edgeMatrix):
    lat_h = lat * math.pi/180
    lon_h = lon * math.pi/180
    EARTH_RADIUS = 6378.137 * 1000

    lat_a = edgeMatrix[:, 0] * math.pi / 180 
    lon_a = edgeMatrix[:, 1] * math.pi / 180 
    lat_b = edgeMatrix[:, 2] * math.pi / 180 
    lon_b = edgeMatrix[:, 3] * math.pi / 180 

    lat_ha = lat_h - lat_a
    lon_ha = lon_h - lon_a
    lat_hb = lat_h - lat_b
    lon_hb = lon_h - lon_b
    lat_ab = lat_a - lat_b
    lon_ab = lon_a - lon_b

    ha = np.arcsin(np.sqrt(np.sin(lat_ha/2)**2+np.cos(lat_h)*np.cos(lat_a)*np.sin(lon_ha/2)**2)) * EARTH_RADIUS
    hb = np.arcsin(np.sqrt(np.sin(lat_hb/2)**2+np.cos(lat_h)*np.cos(lat_b)*np.sin(lon_hb/2)**2)) * EARTH_RADIUS
    ab = np.arcsin(np.sqrt(np.sin(lat_ab/2)**2+np.cos(lat_a)*np.cos(lat_b)*np.sin(lon_ab/2)**2)) * EARTH_RADIUS

    long_cmp, short_cmp = ha >= hb, ha < hb
    long_len = long_cmp * ha + short_cmp * hb
    short_len = long_cmp * hb + short_cmp * ha
    
    angle_cmp = short_len ** 2 + ab ** 2 - long_len ** 2
    angle_cmp_no_height = angle_cmp < 0
    angle_cmp_height = angle_cmp > 0

    avg = (ha + hb + ab)/2
    A = np.sqrt(np.abs(avg*(avg - ha)*(avg - hb)*(avg - ab)))
    h = 2 * A / ab

    zero_len = np.where(ab == 0.0, 1, 0)
    
    distances = h * angle_cmp_height + angle_cmp_no_height * short_len
    distances = (1 - zero_len) * distances + zero_len * short_len

    return distances

def computePartition(lat_h, lon_h, lat_a, lon_a, lat_b, lon_b):
    EARTH_RADIUS = 6378.137 * 1000

    lat_a = lat_a * math.pi/180
    lon_a = lon_a * math.pi/180
    lat_b = lat_b * math.pi/180
    lon_b = lon_b * math.pi/180
    lat_h = lat_h * math.pi/180
    lon_h = lon_h * math.pi/180

    lat_ha = lat_h - lat_a
    lon_ha = lon_h - lon_a
    lat_hb = lat_h - lat_b
    lon_hb = lon_h - lon_b
    lat_ab = lat_a - lat_b
    lon_ab = lon_a - lon_b

    ha = np.arcsin(np.sqrt(np.sin(lat_ha/2)**2+np.cos(lat_h)*np.cos(lat_a)*np.sin(lon_ha/2)**2)) * EARTH_RADIUS
    hb = np.arcsin(np.sqrt(np.sin(lat_hb/2)**2+np.cos(lat_h)*np.cos(lat_b)*np.sin(lon_hb/2)**2)) * EARTH_RADIUS
    ab = np.arcsin(np.sqrt(np.sin(lat_ab/2)**2+np.cos(lat_a)*np.cos(lat_b)*np.sin(lon_ab/2)**2)) * EARTH_RADIUS

    partition = (ha ** 2 + ab ** 2 - hb ** 2)/(2 * ab ** 2)

    return max(0, partition)

def computeDistances(lat_h, lon_h, verticeMatrix):
    EARTH_RADIUS = 6378.137 * 1000
    lat_a = verticeMatrix[:, 1] * math.pi/180
    lon_a = verticeMatrix[:, 2] * math.pi/180
    lat_h = lat_h * math.pi/180
    lon_h = lon_h * math.pi/180

    lat_ha = lat_h - lat_a
    lon_ha = lon_h - lon_a    

    distance = np.arcsin(np.sqrt(np.sin(lat_ha/2)**2+np.cos(lat_h)*np.cos(lat_a)*np.sin(lon_ha/2)**2)) * EARTH_RADIUS
    return distance



    









