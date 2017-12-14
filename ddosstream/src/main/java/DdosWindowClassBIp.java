import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DdosWindowClassBIp {
    private Instant windowStart, windowEnd;
    private Map<String, List<IpAddress>> ipAddressMap = new HashMap<String, List<IpAddress>>();

    public DdosWindowClassBIp(Instant windowStart, int windowSizeInSeconds) {
        this.windowStart = windowStart;
        this.windowEnd = windowStart.plusSeconds(windowSizeInSeconds);
    }

    public int addIpToWindow(IpAddress ipAddress, Instant messageTime) {
        String ipTwoOctets = ipAddress.getFirstTwoOctets();

        if(messageTime.equals(windowStart) ||
                messageTime.equals(windowEnd) ||
                (messageTime.isAfter(windowStart) && messageTime.isBefore(windowEnd))) {
            if(ipAddressMap.containsKey(ipTwoOctets)) { ;
                ipAddressMap.get(ipTwoOctets).add(ipAddress);
            }
            else {
                List<IpAddress> newIpList = new ArrayList<IpAddress>();
                newIpList.add(ipAddress);
                ipAddressMap.put(ipTwoOctets, newIpList);
            }
        }
        return ipAddressMap.get(ipTwoOctets).size();
    }

    public List<IpAddress> getMatchingClassBIpAddresses(IpAddress ipToMatch) {
        return ipAddressMap.get(ipToMatch.getFirstTwoOctets());
    }
}