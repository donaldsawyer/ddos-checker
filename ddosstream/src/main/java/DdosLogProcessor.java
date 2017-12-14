import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

public class DdosLogProcessor {
    private int windowSizeSeconds;
    private Set<String> kafkaMessageIds = new HashSet<String>();
    private Set<String> ipAddressesPublished = new HashSet<String>();
    private Set<String> ipOctetsPublished = new HashSet<String>();
    private Map<String, DdosWindowClassBIp> ddosWindows = new HashMap<String, DdosWindowClassBIp>();
    private long hitThreshold;


    public DdosLogProcessor(int windowSizeSeconds) {
        this.windowSizeSeconds = windowSizeSeconds;
        this.hitThreshold = 60 * windowSizeSeconds;
    }

    public void processLogLine(JSONObject logLineJson) {
        // if the message has not been processed before, then process it //
        // this removes the repeated processing of the same message, which wouldd skew ip counts //
        if(!kafkaMessageIds.contains(logLineJson.getJSONObject("metadata").getString("publish_uuid"))) {
            String logTimeString = logLineJson.getString("log_timestamp");
            Instant logTimeInstant = Instant.parse(logTimeString);
            IpAddress currentIpAddress = new IpAddress(logLineJson.getString("ip_address"));

            // if window exists, add the message to the window //
            if(ddosWindows.containsKey(logTimeString)) {
                int classBCount = ddosWindows.get(logTimeString)
                        .addIpToWindow(
                                currentIpAddress,
                                logTimeInstant);

                // if the ip count returned from ddos window exceeds threshold, log the ip address //
                if(classBCount > hitThreshold) {
                    logIpAddress(currentIpAddress, logLineJson);
                }
                else if(classBCount == hitThreshold) {
                    // publish all ip address from the class be host because they hadn't been published yet //
                    List<IpAddress> ipAddressesToRepublish =
                            ddosWindows.get(logTimeString).getMatchingClassBIpAddresses(currentIpAddress);
                    for(IpAddress ip : ipAddressesToRepublish) {
                        //System.out.println("LOG IP REPUB: " + currentIpAddress.getPaddedIpAddress());
                        logIpAddress(ip, logLineJson);
                    }
                }
            }
            else { // if window doesn't exist, create it and add to the window //
                DdosWindowClassBIp newDdosWindow = new DdosWindowClassBIp(logTimeInstant, this.windowSizeSeconds);
                ddosWindows.put(logTimeString, newDdosWindow);
            }
        } // end if kafka message not processed yet
    } // end processLogLine()

    public void logIpAddress(IpAddress ipAddress, JSONObject jsonMessage) {
        BufferedWriter ipViolationBufferedWriter = null;
        FileWriter ipViolationFileWriter = null;

        jsonMessage.put("metadata",
                jsonMessage.getJSONObject("metadata")
                        .put("violation_discover_timestamp", Instant.now().toString()));

        try {
            ipViolationFileWriter = new FileWriter("/home/cloudera/phdata/ip_violations.txt", true);
            ipViolationBufferedWriter = new BufferedWriter(ipViolationFileWriter);

            if(!ipAddressesPublished.contains(ipAddress.getPaddedIpAddress())) {
                ipViolationBufferedWriter.write(jsonMessage.toString());
                ipViolationBufferedWriter.newLine();
            }

            ipAddressesPublished.add(ipAddress.getPaddedIpAddress());
            ipOctetsPublished.add(ipAddress.getFirstTwoOctets());
        } catch(IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if(ipViolationBufferedWriter != null) {
                    ipViolationBufferedWriter.close();
                }
                if(ipViolationFileWriter != null) {
                    ipViolationFileWriter.close();
                }
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public Set<String> getPublishedOctets() {
        return ipOctetsPublished;
    }
} // end class
