package my.shin;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class Main {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "testKafka01:9092,testKafka02:9092,testKafka03:9092");
        props.put("acks", "1");
        props.put("compression.type", "gzip");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);
        for (int i = 1; i < 11; i++) {
            producer.send(new ProducerRecord<String, String>("ycshin-topic", "Apache kafka is a distributed streaming platform"));
        }
        producer.close();
    }
}
