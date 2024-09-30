package miu.edu.ridehailing;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class RideHailingKafkaProducer {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<String,String>(props);

        // Simulating ride-hailing data
        String[] locations = {"downtown", "airport", "suburb", "station"};
        for (int i = 0; i < 10; i++) {
            String location = locations[i % locations.length];
            producer.send(new ProducerRecord<>("ride_hailing", location));
        }
        producer.close();
    }
}
