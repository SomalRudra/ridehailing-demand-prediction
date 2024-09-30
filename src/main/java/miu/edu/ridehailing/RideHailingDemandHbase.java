package miu.edu.ridehailing;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import scala.Tuple2;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.Arrays;

@SpringBootApplication
public class RideHailingDemandHbase {

    private static final Logger log = LoggerFactory.getLogger(RideHailingDemandHbase.class);

    public static void main(String[] args) throws InterruptedException {
        // Initializing Spark Configuration
        SparkConf conf = new SparkConf()
                .setAppName("RideHailingDemandPrediction")
                .setMaster("local[*]"); // Use 'local[*]' for local development

        // Initializing Spark Context
        JavaSparkContext sc = new JavaSparkContext(conf);
        sc.setLogLevel("WARN");

        // Initializing Spark Streaming Context
        JavaStreamingContext streamingContext = new JavaStreamingContext(sc, Durations.seconds(10));

        // Simulating Streaming Data Source (e.g., using a socket stream)
        JavaReceiverInputDStream<String> rideStream = streamingContext.socketTextStream("localhost", 9999);

        // Example processing: Parse the incoming stream and count demand in each location
        JavaDStream<String> rideRequests = rideStream.flatMap(line -> Arrays.asList(line.split(",")).iterator());

        JavaDStream<Tuple2<String, Integer>> demandCount = rideRequests
                .mapToPair(request -> new Tuple2<>(request, 1))
                .reduceByKey(Integer::sum)
                .map(tuple -> new Tuple2<>(tuple._1(), tuple._2()));

        // Saving processed data to HBase
        demandCount.foreachRDD(rdd -> {
            rdd.foreachPartition(partition -> {
                // Set up HBase configuration
                Configuration config = HBaseConfiguration.create();
                config.set("hbase.zookeeper.quorum", "localhost");
                config.set("hbase.zookeeper.property.clientPort", "2181");

                try (Connection connection = ConnectionFactory.createConnection(config)) {
                    Table table = connection.getTable(TableName.valueOf("ride_hailing_demand"));

                    // Iterate over each record and insert it into HBase
                    partition.forEachRemaining(record -> {
                        String location = record._1();
                        int demandCountValue = record._2();

                        // Prepare HBase 'Put' object
                        Put put = new Put(Bytes.toBytes(location)); // Row key is the location
                        put.addColumn(Bytes.toBytes("demand_data"), Bytes.toBytes("count"), Bytes.toBytes(demandCountValue));

                        // Insert into HBase
                        try {
                            table.put(put);
                            log.info("data put done");
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to insert record into HBase", e);
                        }
                    });
                    table.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });

        // Starting the streaming context and wait for termination
        streamingContext.start();
        streamingContext.awaitTermination();
    }
}
