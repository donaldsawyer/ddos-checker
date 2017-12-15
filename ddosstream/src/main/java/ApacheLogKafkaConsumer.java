import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.record.Record;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.json.JSONObject;

import java.io.*;
import java.util.Collections;
import java.util.Properties;

public class ApacheLogKafkaConsumer {
    private final String BOOTSTRAP_SERVERS = "quickstart.cloudera:9092";
    private String topicName = null;
    private Consumer<String, String> kafkaConsumer = null;
    private String groupId = null;

    public ApacheLogKafkaConsumer(String topic, String consumerGroupId) {
        this.topicName = topic;
        this.groupId = consumerGroupId;

        Properties props = new Properties();
        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        props.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());

        kafkaConsumer = new KafkaConsumer<String, String>(props);
        kafkaConsumer.subscribe(Collections.singletonList(topicName));
    }

    public void listenForLogs(DdosLogProcessor ddosLogProcessor) throws InterruptedException {
        final int noRecordThreshold = 100;
        int noRecordCount = 0, recordCount = 0;

        while(true) {
            ConsumerRecords<String, String> logRecords = kafkaConsumer.poll(500);

            // on poll, if no records read, quit when logs haven't been read for noRecordCount threshold //
            if(logRecords.count() == 0) {
                if(++noRecordCount > noRecordThreshold)
                    break;
                else
                    continue;
            }
            else {
                noRecordCount = 0;
            }

            for (ConsumerRecord<String, String> record : logRecords) {
                ddosLogProcessor.processLogLine(new JSONObject(record.value()));
                recordCount++;
            }

            kafkaConsumer.commitAsync();
        }

        kafkaConsumer.close();
        System.out.println(String.format("FINISHED READING %d RECORDS", recordCount));
        System.out.println("--------------- OCTETS PUBLISHED  -------------------");
        for(String ip : ddosLogProcessor.getPublishedOctets()) {
            System.out.println(ip);
        }
    }
}
