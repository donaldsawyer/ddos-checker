import org.json.JSONObject;
import org.junit.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class ApacheLogPublisherTest {

    @Test
    public void attachPublishMetadata_newMetadata()
            throws InterruptedException {
        // GIVEN //
        JSONObject jsonObject = new JSONObject("{\"id\": 1234}");
        Instant startRange, endRange;
        ApacheLogPublisher sut = new ApacheLogPublisher("fake_topic");
        Set<String> expectedMetadataKeys = new HashSet<String>();
        expectedMetadataKeys.add("publish_timestamp");
        expectedMetadataKeys.add("publish_uuid");

        // WHEN //
        startRange = Instant.now();
        Thread.sleep(1);
        sut.attachPublishMetadata(jsonObject);
        Thread.sleep(1);
        endRange = Instant.now();

        // THEN //
        JSONObject metadata = jsonObject.getJSONObject("metadata");
        assertNotNull(metadata);
        assertEquals(expectedMetadataKeys, metadata.keySet());
        Instant publishTimestamp =
                Instant.parse(metadata.get("publish_timestamp").toString());
        assertTrue(publishTimestamp.isAfter(startRange));
        assertTrue(publishTimestamp.isBefore(endRange));
        assertNotNull(metadata.get("publish_uuid"));
    }

    @Test
    public void attachPublishMetadata_overwriteMetadata()
            throws InterruptedException {
        // GIVEN //
        JSONObject jsonObject = new JSONObject(
                "{\"id\": 1234, " +
                        "\"metadata\": " +
                        "{\"publish_timestamp\":\"\", \"publish_uuid\":\"abc\"}" +
                        "}");
        Instant startRange, endRange;
        ApacheLogPublisher sut = new ApacheLogPublisher("fake_topic");
        Set<String> expectedMetadataKeys = new HashSet<String>();
        expectedMetadataKeys.add("publish_timestamp");
        expectedMetadataKeys.add("publish_uuid");

        // WHEN //
        startRange = Instant.now();
        Thread.sleep(1);
        sut.attachPublishMetadata(jsonObject);
        Thread.sleep(1);
        endRange = Instant.now();

        // THEN //
        JSONObject metadata = jsonObject.getJSONObject("metadata");
        assertNotNull(metadata);
        assertEquals(expectedMetadataKeys, metadata.keySet());
        Instant publishTimestamp =
                Instant.parse(metadata.get("publish_timestamp").toString());
        assertTrue(publishTimestamp.isAfter(startRange));
        assertTrue(publishTimestamp.isBefore(endRange));
        assertNotNull(metadata.get("publish_uuid"));
        assertNotEquals("abc", metadata.get("publish_uuid"));
    }
}