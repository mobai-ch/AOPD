# Efficient Optimal PickUp and DropOff Points Selection (Experiment manual) - V1.0

Our program can be divided into three parts: 
1. Data Generation
2. Java Program Execution
3. Experimental result analysis

## 1. DataSet Generation
Generate the data in NYC and Chengdu to execute our program. Because the dataset is too large, we would offer the url to download the trajectory and taxi queries, and the Road network can be downloaded with the OSMnx. Final data would be generated with the following steps.

### Detailed Data Generation

a) Download all taxi Requests and trajectories from the url that offered by Didi GAIA dataset originally (Any usage please communicate with Didi company).

[https://outreach.didichuxing.com/](https://outreach.didichuxing.com/)

which contains ['order_01-30.zip', 'gps_01-10.zip', 'gps_11-20.zip', 'gps_21-30.zip']

b) Download the NYC taxi Requsts in the NYC official website, and download the traffic volume data of the NYC in NYC official website.

NYC Trip Record Data: 

[https://www1.nyc.gov/site/tlc/about/tlc-trip-record-data.page](https://www1.nyc.gov/site/tlc/about/tlc-trip-record-data.page)

NYC Traffic volume: 

[https://data.cityofnewyork.us/Transportation/Traffic-Volume-Counts-2014-2019-/ertz-hr4r/data](https://data.cityofnewyork.us/Transportation/Traffic-Volume-Counts-2014-2019-/ertz-hr4r/data)


c) Move the Raw data of taxi Requests and trajectories into a specific path with the following steps

```bash
mkdir ./Experiment/Dataset/chengdu/gps
unzip gps_01-10.zip
mv /gps_01-10.zip/* ./Experiment/DataSet/chengdu/gps/
mkdir ./Experiment/DataSet/chengdu/order_01-30
unzip order_01-30
mv order_01-30 ./Experiment/DataSet/chengdu
# same approach to unzip files gps_11-20 and gps_21-30

mkdir ./Experiment/DataSet/NYC/temp
mv yellow_tripdata_2014-10.csv ./Experiment/DataSet/NYC     # taxi
mkdir ./Experiment/DataSet/NYC/order_01-30/totaltotal_ride_request
mv Traffic_Volume_Counts__2014-2019_.csv ./Experiment/DataSet/NYC/temp/ # traffic flow
```

d) Download the Road network from NYC and Chengdu, and mapping the travel time in different time to the road network.

```bash
cd ./pyUtil/DataGenerator
python3 NRoadInfoDownloader.py
python3 CGraphEnvGenerator.py
```

e) Download the POI data from NYC and Chengdu, record the nearest edge for each POI and save.

We first move the "mathGeo.py", "POIDownloader.py" and "StorePOI.py" under ./pyUtil/DataGenerator into "./Experiment/DataSet/chengdu(or NYC)/Graph/OriginGraph" and execute the following command.

```bash
python3 POIDownloader.py
python3 StorePOI.py
```

Then, move the "StorePOI.py" to "./Experiment/DataSet/NYC(or chengdu)/Graph/GraphWithEnv/" and change the last line of the file as "transAllFileUnderDirectory("./")"

```bash
python3 StorePOI.py
```


## 2. Java Program Execution
a) Download and install the IDEA intellij from the following website and install: [https://www.jetbrains.com/idea/download/#section=windows](https://www.jetbrains.com/idea/download/#section=windows)

b) Download the jdk-8u341 from the oracle official website and install: [https://www.oracle.com/java/technologies/downloads/#java8-windows](https://www.oracle.com/java/technologies/downloads/#java8-windows)

c) Create a Dataset directory and move the dataset downloaded (or generated) to it.

```bash
mkdir Experiment
mkdir ./Experiment/DataSet
mv dataset.tar.gz ./Experiment/DataSet
cd ./Experiment/DataSet
tar -xvzf dataset.tar.gz 
mv ./dataset/* ./
rm -rf ./dataset  
```

After that, Chengdu and NYC datasets are loaded. 

d) Create a temporal result directory in a specific path to save the records generated during program execution.

```bash
mkdir Experiement   # create if not created before
mkdir ./Experiment/ExpRecords
mkdir ./Experiment/ExpRecords/EndData
```

e) Replace the directory path with the directories created above (absolute path) in "/RMather/src/main/java/ExpSpace.java".

```bash
"/data/research/crowdsourcing/DataSet/" -> "/xxx/Experiment/DataSet"
"/data/research/crowdsourcing/ExpRecords/EndData" -> "/xxx/Experiment/ExpRecords/EndData"
```

f) Run the java main class ExpSpace.java in IDEA and wait (Have to update the maven to wait all package downloaded).

```xml
<dependencies>
    <dependency>
        <groupId>com.opencsv</groupId>
        <artifactId>opencsv</artifactId>
        <version>5.0</version>
    </dependency>

    <dependency>
        <groupId>org.meteothink</groupId>
        <artifactId>wContour</artifactId>
        <version>1.6.1</version>
    </dependency>

    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>fastjson</artifactId>
        <version>1.2.76</version>
    </dependency>
</dependencies>
```

## 3. Experimental result analysis

a) Process all experimental records and generate the experimental result in .xlsx format.

```bash
cd ./pyUtil/Analysis
python3 Analyzer2.py    # Information except running time
python3 TimeAna.py      # Running time
```

b) Select the result related to MOPD-greedy(with pick-up and drop-off points) and NPOD-greedy in directory "./Experiment/XLS", and input them into Data.py, run draw.py to plot the experimental result.

```bash
# Record the data with the "NOPD" (no pick-up and drop-off points)
# Record the data with the "no optimization" (Exhaustive)
# Record the data with the "MOPD" (pick-up an drop-off points)
# Record the data with the "graph-cached" (virtual graph)

# change the values in Data.py with the data recorded

python3 draw.py     # Plot the experimental result
```

c) Or if you just want analyze the result with specified time, maximum walking distance, maximum driving time and more, you can  use the following script by fine-tuning the parameters in main function.

```bash
python3 sExpResultAna.py
```

Python package requirements

```bash
1. osmnx >= 1.1.2
2. pandas >= 1.4.0
3. torch >= 1.11.0 
4. seaborn >= 0.11.2
7. geopy >= 2.2.0
8. geopandas >= 0.10.2
```

#### Hint
You may be confused as to why the trip time reduction of vehicle is even smaller. This is because the paper introduce a more regular situation but we have considered more complicated situations in our experiments.
For a system without pick-up/drop-off points that does not consider the differences between vehicule and pedestrian road networks, they map the pedestrian location onto the nearest vehicular road point. But in the real situation, pedestrians are going to walk to pick-up point and get off from the drop-off point anyway.
This leads to a smaller driver trip reduction rate than the passenger trip reduction rate instead, and the actual situation is more complex unfavorable to our experimental results, but we finally adopted the scheme in our experiments in order to maintain consistency with the real world.
