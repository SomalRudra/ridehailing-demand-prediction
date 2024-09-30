package miu.edu.ridehailing;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class RideHailingDemandPrediction {

    public static void main(String[] args) throws InterruptedException {
        // Initialize Spark Configuration
        SparkConf conf = new SparkConf()
                .setAppName("RideHailingDemandPrediction")
                .setMaster("local[*]"); // Use 'local[*]' for local development

        // Initialize Spark Context
        JavaSparkContext sc = new JavaSparkContext(conf);
        sc.setLogLevel("WARN");

        // Initialize Spark Streaming Context
        JavaStreamingContext streamingContext = new JavaStreamingContext(sc, Durations.seconds(10));

        // Simulate Streaming Data Source (e.g., using a socket stream)
        JavaReceiverInputDStream<String> rideStream = streamingContext.socketTextStream("localhost", 9999);

        // Example processing: Parse the incoming stream and count demand in each location
        JavaDStream<String> rideRequests = rideStream.flatMap(line -> Arrays.asList(line.split(",")).iterator());

        JavaDStream<String> demandCount = rideRequests
                .mapToPair(request -> new Tuple2<>(request, 1))
                .reduceByKey(Integer::sum)
                .map(tuple -> "Location: " + tuple._1() + " - Demand Count: " + tuple._2());

        // Output the result to the console
        demandCount.print();

        // Start the streaming context and wait for termination
        streamingContext.start();
        streamingContext.awaitTermination();
    }
}
