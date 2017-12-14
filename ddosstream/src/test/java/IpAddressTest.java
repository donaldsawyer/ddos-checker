import org.junit.Test;

import static org.junit.Assert.*;

public class IpAddressTest {

    @Test
    public void padIpAddress_noPaddingNeeded() {
        // GIVEN //
        String originalIpAddress = "111.222.3333.444";
        IpAddress sut = new IpAddress("111.111.111.111");

        // WHEN //
        String paddedIp = sut.padIpAddress(originalIpAddress);

        // THEN //
        assertEquals("111.222.3333.444", paddedIp);
    }

    @Test
    public void padIpAddress_alreadyPadded() {
        // GIVEN //
        String originalIpAddress = "001.012.123.444";
        IpAddress sut = new IpAddress("111.111.111.111");

        // WHEN //
        String paddedIp = sut.padIpAddress(originalIpAddress);

        // THEN //
        assertEquals("001.012.123.444", paddedIp);
    }

    @Test
    public void padIpAddress_paddingNeeded() {
        // GIVEN //
        String originalIpAddress = "0.1.12.123";
        IpAddress sut = new IpAddress("111.111.111.111");

        // WHEN //
        String paddedIp = sut.padIpAddress(originalIpAddress);

        // THEN //
        assertEquals("000.001.012.123", paddedIp);
    }

    @Test
    public void getFirstTwoOctets_zeroPadded() {
        // GIVEN //
        IpAddress sut = new IpAddress("001.012.123.444");

        // WHEN //
        String twoOctets = sut.getFirstTwoOctets();

        // THEN //
        assertEquals("001.012", twoOctets);
    }

    @Test
    public void getFirstTwoOctets_notPadded() {
        // GIVEN //
        IpAddress sut = new IpAddress("1.12.123.444");

        // WHEN //
        String twoOctets = sut.getFirstTwoOctets();

        // THEN //
        assertEquals("001.012", twoOctets);
    }
}