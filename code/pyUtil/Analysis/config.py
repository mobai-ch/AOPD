cityNames = ["chengdu", "NYC"]
ylabels = ["totalScore", "passengerResp", "walkTime", "CR",\
    "matchN", "tenRed", "twiRed", "thiRed", "reduction", "DTR", "DTRR", "DTRR1", "DTRR2", "DTRR3"]
xlabels = ["Date", "Maximum extra walking time", "Time period", ""]
slabels = ["totalScore", "passengerResp", "walkTime", "CR", "matchN", "reduction", "DTRR"]
specialLabel = ["reduction", "DTRR"]

namesDict = {
    "totalScore": "Total score",  
    "passengerResp": "Response time (s)", 
    "walkTime": "Walking time (s)", 
    "CR": "Competitive ratio",
    "matchN": "The number of matched pairs", 
    "reduction": "Travel time reduction ratio (%)",
    "DTRR": "Driving time reduction ratio (%)",
    "Date": "Date",
    "Maximum extra walking time": "Maximum walking time (s)",
    "Maximum response time": "Maximum response time (s)",
    "Time period": "Time period",
    "Maximum driving time": "Maximum driving time (s)"
}

nameMap = {
    "PD":   "OTOPD",
    "NPD":  "OPDM",
    "NPND": "OTNPD",
    "KMT"   : "KM" ,
    "greedy": "GR",
    "greedyT": "GRT",
    "totalScore": "Total score",
    "passengerResp": "Average response time",
    "walkTime": "Average walking time",
    "CR": "Competitive ratio",
    "zeroRed": ">0%",
    "tenRed": ">10%",
    "twiRed": ">20%",
    "thiRed": ">30%",
    "reduction": "Reduction ratio",
    "cityName": "City name",
    "selectM": "selectM",
    "timePeriod": "Time period",
    "matchM": "matchM",
    "maxResp": "Maximum response time",
    "maxWalk": "Maximum walking time",
    "mdate": "Date",
    "matchN": "The number of matches",
    "DTR": "Driving time reduction (s)",
    "DTR2": ">=120s",
    "DTR4": ">=240s",
    "DTR6": ">=360s",
    "DTRR": "Driving time reduction ratio",
    "DTRR1": "Driving time reduction ratio >= 10%",
    "DTRR2": "Driving time reduction ratio >= 20%",
    "DTRR3": "Driving time reduction ratio >= 30%",
}

matchMethods = ["KM", "GRT", "GR"]
maxWalkTime = [30, 60, 90, 120, 150]
timePeriod = []