import org.json.*;

import java.text.ParseException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.IllegalFormatException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class
ApacheLogJson {

    public static JSONObject getJsonFromLogLine(String apacheLogLine) {
        JSONObject jsonLogLine = new JSONObject();

        /*
        assertEquals("200.4.91.190", sut.get("ip_address"));
        assertEquals("-", sut.get("remote_identity"));
        assertEquals("-", sut.get("remote_user"));
        assertEquals(Instant.parse("2015-05-25T23:11:15Z"), Instant.parse(sut.get("log_timestamp").toString()));
        assertEquals("\"GET / HTTP/1.0\"", sut.get("request_first_line"));
        assertEquals(200, sut.get("status_code"));
        assertEquals(3557, sut.get("response_size"));
        assertEquals("\"-\"", sut.get("referrer"));
        assertEquals("\"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)\"", sut.get("user_agent"));
         */
        Matcher regexMatcher = getRegexMatcherForLogLine(apacheLogLine);
        regexMatcher.find();
        if(!regexMatcher.matches())
            throw new IllegalArgumentException(String.format("Unable to parse log line: %s", apacheLogLine));

        short group = 1;
        jsonLogLine.put("ip_address", regexMatcher.group(group++));
        jsonLogLine.put("remote_identity", regexMatcher.group(group++));
        jsonLogLine.put("remote_user", regexMatcher.group(group++));
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z");
        //dateTimeFormatter.parse(regexMatcher.group(group++));
        jsonLogLine.put("log_timestamp", Instant.from(dateTimeFormatter.parse(regexMatcher.group(group++))));
        jsonLogLine.put("request_first_line", regexMatcher.group(group++));
        jsonLogLine.put("status_code", Integer.parseInt(regexMatcher.group(group++)));
        jsonLogLine.put("response_size", Integer.parseInt(regexMatcher.group(group++)));
        jsonLogLine.put("referrer", regexMatcher.group(group++));
        jsonLogLine.put("user_agent", regexMatcher.group(group++));

        return jsonLogLine;
    }

    public static Matcher getRegexMatcherForLogLine(String logLine) {
//        String logPattern = "^(\\S+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(\\S+) (\\S+) (\\S+)\" (\\d{3}) (\\d+) \"(\\S+)\" \"(\\S+)\"";;
        String logPattern = "^([\\d.]+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(.+?)\" (\\d{3}) (\\d+) \"([^\"]+)\" \"([^\"]+)\"";
        Pattern regexPattern = Pattern.compile(logPattern);

        Matcher m = regexPattern.matcher(logLine);

        return regexPattern.matcher(logLine);
    }
}
