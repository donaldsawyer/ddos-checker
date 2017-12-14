import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.JSONObject;
import scala.util.parsing.json.JSON;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ApacheLogPublisher {
    private Properties kafkaProperties = new Properties();
    private String apacheLogFilename = null;
    private KafkaProducer<String, String> logKafkaProducer = null;
    private String kafkaTopicName = null;

    public ApacheLogPublisher(String kafkaTopic) {
        this.kafkaTopicName = kafkaTopic;

        kafkaProperties.put("bootstrap.servers", "quickstart.cloudera:9092");
        kafkaProperties.put(
                "key.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProperties.put(
                "value.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");

        logKafkaProducer = new KafkaProducer<String, String>(kafkaProperties);
    }

    public void publishJsonToKafka(JSONObject jsonData)
            throws ExecutionException, InterruptedException {
        ProducerRecord<String, String> record =
                new ProducerRecord<>(
                        kafkaTopicName,
                        jsonData.getJSONObject("metadata").getString("publish_uuid"),
                        jsonData.toString());
        logKafkaProducer.send(record).get();
    }

    public int publishFileDataToKafka(String apacheLogFilename) {
        int publishedRecordCount = 0;

        File logFile = new File(apacheLogFilename);
        try(FileReader logReader = new FileReader(logFile)) {
            BufferedReader bufferedReader = new BufferedReader(logReader);
            String line;

            while((line = bufferedReader.readLine()) != null) {
                publishJsonToKafka(
                        attachPublishMetadata(
                                ApacheLogJson.getJsonFromLogLine(line)));
                publishedRecordCount++;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return publishedRecordCount;
    }

    public JSONObject attachPublishMetadata(JSONObject jsonObject) {
        Map<String, String> publishMetadata = new HashMap<String, String>();
        publishMetadata.put("publish_timestamp", Instant.now().toString());
        publishMetadata.put("publish_uuid", UUID.randomUUID().toString());

        jsonObject.put("metadata", publishMetadata);

        return jsonObject;
    }

}
