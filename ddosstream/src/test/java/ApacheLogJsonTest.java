import org.json.JSONObject;
import org.junit.Test;

import java.text.ParseException;
import java.time.Instant;
import java.util.regex.Matcher;

import static org.junit.Assert.*;

public class ApacheLogJsonTest {

    @Test
    public void getJsonFromLogLine_validLine_returnsJson() {
        // GIVEN //
        String apacheLogLine = "200.4.91.190 - - [25/May/2015:23:11:15 +0000] \"GET / HTTP/1.0\" 200 3557 \"-\" \"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)\"";

        // WHEN //
        JSONObject sut = ApacheLogJson.getJsonFromLogLine(apacheLogLine);

        // THEN //
        assertEquals("200.4.91.190", sut.get("ip_address"));
        assertEquals("-", sut.get("remote_identity"));
        assertEquals("-", sut.get("remote_user"));
        assertEquals(Instant.parse("2015-05-25T23:11:15Z"), Instant.parse(sut.get("log_timestamp").toString()));
        assertEquals("GET / HTTP/1.0", sut.get("request_first_line"));
        assertEquals(200, sut.get("status_code"));
        assertEquals(3557, sut.get("response_size"));
        assertEquals("-", sut.get("referrer"));
        assertEquals("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)", sut.get("user_agent"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getJsonFromLogLine_invalidDate_throwParseException() {
        // GIVEN //
        String apacheLogLine = "200.4.91.190 - - [] \"GET / HTTP/1.0\" 200 3557 \"-\" \"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)\"";

        // WHEN //
        ApacheLogJson.getJsonFromLogLine(apacheLogLine);
    }

    @Test
    public void getRegexMatcherForLogLine_validLine() {
        // GIVEN //
        String apacheLogLine = "200.4.91.190 - - [25/May/2015:23:11:15 +0000] \"GET / HTTP/1.0\" 200 3557 \"-\" \"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)\"";

        // WHEN //
        Matcher sut = ApacheLogJson.getRegexMatcherForLogLine(apacheLogLine);
        sut.find();

        // THEN //
        short group = 1;
        assertEquals("200.4.91.190", sut.group(group++));
        assertEquals("-", sut.group(group++));
        assertEquals("-", sut.group(group++));
        assertEquals("25/May/2015:23:11:15 +0000", sut.group(group++));
        assertEquals("GET / HTTP/1.0", sut.group(group++));
        assertEquals("200", sut.group(group++));
        assertEquals("3557", sut.group(group++));
        assertEquals("-", sut.group(group++));
        assertEquals("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)", sut.group(group++));
    }
}