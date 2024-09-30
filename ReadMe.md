# **Ride-Hailing Demand Prediction Project**

## **Table of Contents**

- [Project Overview](#project-overview)
- [Technologies Used](#technologies-used)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Project Setup](#project-setup)
- [Running the Project](#running-the-project)
- [Project Structure](#project-structure)
- [Data Flow](#data-flow)
- [Future Enhancements](#future-enhancements)
- [Contributors](#contributors)

---

## **Project Overview**

The Ride-Hailing Demand Prediction project is designed to predict ride-hailing demand in real-time using Apache Spark Streaming. The project simulates or processes real-time streaming data from ride-hailing requests, analyzes the demand, and integrates with technologies such as Kafka, HBase for efficient data storage and retrieval.

This project demonstrates how big data tools can be combined to build a scalable, real-time analytics platform for predicting ride-hailing demand in different locations.

---

## **Technologies Used**

- **Apache Spark Streaming**: Real-time data processing
- **Apache Kafka**: Data ingestion and streaming
- **HBase**: NoSQL database for storing demand prediction results
- **Java**: Programming language used for implementation
- **Maven**: Build and dependency management
- **Netcat**: Tool to simulate data streaming (for local testing)

---

## **Architecture**

The architecture consists of the following main components:

1. **Data Ingestion**: Using Kafka to simulate real-time ride-hailing requests.
2. **Real-Time Processing**: Spark Streaming processes the incoming data to predict demand.
3. **Storage**: Processed data is stored in HBase and Hive for further analysis.

---

## **Prerequisites**

Before running the project, ensure you have the following installed:

1. **Java** (JDK 11+)
2. **Apache Spark** (3.3.1 or later)
3. **Apache Kafka** (2.8.0 or later)
4. **HBase** (2.4.11 or later)
5. **Maven** (for building the project)

Additionally:
- **Netcat** (for simulating streaming data)

---

## **Project Setup**

### **Step 1: Clone the Repository**

```bash
git clone https://github.com/SomalRudra/ridehailing-demand-prediction.git
cd ridehailing-demand-prediction
```

### **Step 2: Build the Project with Maven**

```bash
mvn clean install
```

### **Step 3: Start Services**

1. **Start HBase and Hive:**
    - Ensure HBase is running: `start-hbase.sh`
    - Start Hive Metastore: `hive --service metastore`

2. **Start Kafka:**
    - Start Zookeeper: `bin/zookeeper-server-start.sh config/zookeeper.properties`
    - Start Kafka Broker: `bin/kafka-server-start.sh config/server.properties`

3. **Create Kafka Topic:**
   ```bash
   bin/kafka-topics.sh --create --topic ride_hailing --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
   ```

### **Step 4: Create Required HBase Table**

Start the HBase shell and create the table:
```bash
create 'ride_hailing_demand', 'demand_data'
```

---

## **Running the Project**

### **1. Start the Kafka Producer**
Run the `RideHailingKafkaProducer` to send simulated ride-hailing requests to the Kafka topic:
```bash
mvn exec:java -Dexec.mainClass="com.example.RideHailingKafkaProducer"
```

### **2. Start the Spark Streaming Application**
Run the `RideHailingKafkaConsumer` to process the Kafka stream and store data into HBase/Hive:
```bash
mvn exec:java -Dexec.mainClass="com.example.RideHailingKafkaConsumer"
```

### **3. Simulate Data (Using Netcat for Testing)**
If using Netcat, you can start it with:
```bash
nc -lk 9999
```
Type data such as `downtown, airport, suburb` to simulate ride requests.

---

## **Project Structure**

```
ride-hailing-demand-prediction
│
├── src
│   ├── main
│   │   ├── java
│   │   │   └── miu.edu.ridehailing
│   │   │       ├── RideHailingDemandPrediction.java
│   │   │       ├── RideHailingDemandHBase.java
│   │   │       ├── RideHailingKafkaProducer.java
│   │   │       ├── RideHailingKafkaConsumer.java
│   │   └── resources
│   └── test
├── pom.xml
└── README.md
```
- `RideHailingDemandPrediction`: Gets Realtime data from netcat & prints it
- `RideHailingHBase.java`: Stores processed data into HBase
- `RideHailingKafkaProducer.java`: Produces ride-hailing data to Kafka
- `RideHailingKafkaConsumer.java`: Consumes data from Kafka and processes it using Spark Streaming, stores in hbase


---

## **Data Flow**

1. **Kafka Producer** sends simulated ride-hailing data to the Kafka topic.
2. **Spark Streaming** consumes data from Kafka and processes ride-hailing demand.
3. **Data Storage**: Processed data is stored in HBase for querying and analysis.

---

## **Future Enhancements**

- **Integrate with Real-Time Data Sources**: Replace simulated data with real-time data from APIs like Twitter or Google Maps.
- **Predictive Analysis**: Implement advanced machine learning models to forecast demand based on historical data.
- **Visualization**: Create a real-time dashboard using tools like Grafana or Tableau to visualize ride-hailing demand.

---

## **Contributors**

- [Somal Chakraborty](https://github.com/SomalRudra/) - Project Lead

Feel free to reach out for any questions or contributions!

---

This README provides a complete guide for anyone looking to understand, set up, and run your project. Let me know if you'd like any adjustments or additional details!