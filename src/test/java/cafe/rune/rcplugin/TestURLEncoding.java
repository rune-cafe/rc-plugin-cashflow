package cafe.rune.rcplugin;

import org.junit.Assert;
import org.junit.Test;

public class TestURLEncoding {
    @Test
    public void testEncoding() {
        Assert.assertEquals("Zezima", RuneCafeAPI.encodeOSRSName("Zezima"));
        Assert.assertEquals("Bwana%20Rendi", RuneCafeAPI.encodeOSRSName("Bwana Rendi"));
        Assert.assertEquals("Something%20Spacey", RuneCafeAPI.encodeOSRSName("Something Spacey"));
    }
}
