import org.apache.commons.lang.StringUtils;

public class IpAddress {
    private String originalIpAddress = null;
    private String paddedIpAddress = null;

    public IpAddress(String ipAddress) {
        this.originalIpAddress = ipAddress;

        // pad all elements of the ip address //
        paddedIpAddress = padIpAddress(ipAddress);
    }

    public String getFirstTwoOctets() {
        return this.paddedIpAddress.substring(0, 7);
    }

    public String padIpAddress(String ipAddress) {
        String paddedIpAddress = null;
        String [] ipParts = ipAddress.split("\\.");

        return String.format("%s.%s.%s.%s",
                StringUtils.leftPad(ipParts[0], 3, "0"),
                StringUtils.leftPad(ipParts[1], 3, "0"),
                StringUtils.leftPad(ipParts[2], 3, "0"),
                StringUtils.leftPad(ipParts[3], 3, "0"));
    }

    public String getOriginalIpAddress() {
        return originalIpAddress;
    }

    public String getPaddedIpAddress() {
        return paddedIpAddress;
    }
}
