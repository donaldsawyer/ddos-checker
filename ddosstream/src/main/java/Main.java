import kafka.admin.AdminClient;
import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.json.JSONObject;

import java.io.*;
import java.time.Instant;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String [] args) throws ExecutionException, InterruptedException {
        // setup program state //
        File logFile = new File("/home/cloudera/phdata/apache-access-log.txt");
        String topicName = "apache_log";
        ApacheLogPublisher alp = new ApacheLogPublisher(topicName);
        ApacheLogKafkaConsumer alc =
                new ApacheLogKafkaConsumer(topicName, "test-group");
        DdosLogProcessor ddosLogProcessor = new DdosLogProcessor(1);

        // recreate the topic fresh //
        setupKafkaTopic(topicName);

        // THREAD TO START THE KAFKA PRODUCER //
        new Thread(() -> {
            alp.publishFileDataToKafka(logFile.getAbsolutePath());
        }).start();

        startProcessingFromKafka(alc, ddosLogProcessor);
    }

    private static void startProcessingFromKafka(ApacheLogKafkaConsumer alc, DdosLogProcessor ddosLogProcessor) {
        // START THE KAFKA LISTENER //
        try {
            alc.listenForLogs(ddosLogProcessor);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void setupKafkaTopic(String topicName) {
        // WIPE OUT EXISTING TOPIC AND RECREATE IT //
        String zookeeperConnectionString = "quickstart.cloudera:2181";
        int sessionTimeoutMilliseconds = 10000;
        int connectionTimeoutMilliseconds = 5000;
        int partitions = 1, replication = 1;
        ZkClient zkClient = new ZkClient(
                zookeeperConnectionString,
                sessionTimeoutMilliseconds,
                connectionTimeoutMilliseconds,
                ZKStringSerializer$.MODULE$);
        boolean isSecureCluster = false;
        ZkUtils zkUtils = new ZkUtils(
                zkClient,
                new ZkConnection(zookeeperConnectionString),
                isSecureCluster);
        Properties topicConfig = new Properties();
        try {
            AdminUtils.deleteTopic(zkUtils, topicName);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        AdminUtils.createTopic(
                zkUtils,
                topicName,
                partitions,
                replication,
                topicConfig,
                RackAwareMode.Enforced$.MODULE$);
        zkClient.close();
    }
}
