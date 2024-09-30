package miu.edu.ridehailing;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.execution.columnar.BOOLEAN;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.LocationStrategies;
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
import java.util.*;

public class RideHailingKafkaConsumer {

    public static void main(String[] args) throws InterruptedException {
        // Setting up Spark Streaming configuration
        SparkConf conf = new SparkConf().setAppName("RideHailingDemandPrediction").setMaster("local[*]");
        JavaStreamingContext streamingContext = new JavaStreamingContext(conf, Durations.seconds(10));

        // Kafka parameters
        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        kafkaParams.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaParams.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaParams.put(ConsumerConfig.GROUP_ID_CONFIG, "ride-hailing-group");
        kafkaParams.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        kafkaParams.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.FALSE);

        Collection<String> topics = Arrays.asList("ride_hailing");

        // Creating Kafka stream
        JavaInputDStream<ConsumerRecord<String, String>> stream = KafkaUtils.createDirectStream(
                streamingContext,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.Subscribe(topics, kafkaParams)
        );

        // Processing the stream here
        JavaDStream<String> rideRequests = stream.map(ConsumerRecord::value);
        JavaDStream<String> demandCount = rideRequests
                .mapToPair(request -> new Tuple2<>(request, 1))
                .reduceByKey((Integer count1, Integer count2) -> (Integer) (count1 + count2))
                .map(tuple -> "Location: " + tuple._1() + " - Demand Count: " + tuple._2());

        demandCount.print();

        // Saving processed data to HBase
        demandCount.foreachRDD(rdd -> {
            rdd.foreachPartition(partition -> {
                // Setting up HBase configuration
                Configuration config = HBaseConfiguration.create();
                try (Connection connection = ConnectionFactory.createConnection(config)) {
                    Table table = connection.getTable(TableName.valueOf("ride_hailing_demand"));

                    // Iterating over each record and insert it into HBase
                    partition.forEachRemaining(record -> {
                        String[] splitRecord = record.split(" - ");
                        String location = splitRecord[0].split(": ")[1];
                        String demandCountValue = splitRecord[1].split(": ")[1];

                        // Preparing HBase 'Put' object
                        Put put = new Put(Bytes.toBytes(location)); // Row key is the location
                        put.addColumn(Bytes.toBytes("demand_data"), Bytes.toBytes("count"), Bytes.toBytes(demandCountValue));

                        // Inserting into HBase
                        try {
                            table.put(put);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    table.close();
                }
            });
        });

        // Starting streaming context
        streamingContext.start();
        streamingContext.awaitTermination();
    }
}
