import org.json.JSONObject;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.*;

public class DdosLogProcessorTest {

    @Test
    public void attachViolationMetadata() throws InterruptedException {
        // GIVEN //
        DdosLogProcessor sut = new DdosLogProcessor(1);
        JSONObject violationLogJson =
                new JSONObject("{\"somekey\":\"somevalue\",\"metadata\":{\"publish_timestamp\":\"sometimestamp\"}}");

        // WHEN //
        Instant start = Instant.now();
        sut.attachViolationMetadata(violationLogJson);
        Thread.sleep(2);
        Instant end = Instant.now();

        // THEN //
        JSONObject metadata = violationLogJson.getJSONObject("metadata");
        Instant violationTime = Instant.parse(metadata.getString("violation_discover_timestamp"));
        assertTrue(violationTime.equals(start) || violationTime.isAfter(start));
        assertTrue(violationTime.isBefore(end));
    }
}