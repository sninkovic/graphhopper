package com.graphhopper.routing.ev;

import com.graphhopper.storage.IntsRef;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.graphhopper.routing.ev.IntEncodedValueImpl.isValidEncodedValue;
import static org.junit.jupiter.api.Assertions.*;

public class IntEncodedValueImplTest {

    @Test
    public void testInvalidReverseAccess() {
        IntEncodedValue prop = new IntEncodedValueImpl("test", 10, false);
        prop.init(new EncodedValue.InitializerConfig());
        try {
            prop.setInt(true, new IntsRef(1), -1);
            fail();
        } catch (Exception ex) {
        }
    }

    @Test
    public void testDirectedValue() {
        IntEncodedValue prop = new IntEncodedValueImpl("test", 10, true);
        prop.init(new EncodedValue.InitializerConfig());
        IntsRef ref = new IntsRef(1);
        prop.setInt(false, ref, 10);
        prop.setInt(true, ref, 20);
        assertEquals(10, prop.getInt(false, ref));
        assertEquals(20, prop.getInt(true, ref));
    }

    @Test
    public void multiIntsUsage() {
        IntEncodedValue prop = new IntEncodedValueImpl("test", 31, true);
        prop.init(new EncodedValue.InitializerConfig());
        IntsRef ref = new IntsRef(2);
        prop.setInt(false, ref, 10);
        prop.setInt(true, ref, 20);
        assertEquals(10, prop.getInt(false, ref));
        assertEquals(20, prop.getInt(true, ref));
    }

    @Test
    public void padding() {
        IntEncodedValue prop = new IntEncodedValueImpl("test", 30, true);
        prop.init(new EncodedValue.InitializerConfig());
        IntsRef ref = new IntsRef(2);
        prop.setInt(false, ref, 10);
        prop.setInt(true, ref, 20);
        assertEquals(10, prop.getInt(false, ref));
        assertEquals(20, prop.getInt(true, ref));
    }

    @Test
    public void maxValue() {
        IntEncodedValue prop = new IntEncodedValueImpl("test", 31, false);
        prop.init(new EncodedValue.InitializerConfig());
        IntsRef ref = new IntsRef(2);
        prop.setInt(false, ref, (1 << 31) - 1);
        assertEquals(2_147_483_647L, prop.getInt(false, ref));
    }

    @Test
    public void testSignedInt() {
        IntEncodedValue prop = new IntEncodedValueImpl("test", 31, -5, false, true);
        EncodedValue.InitializerConfig config = new EncodedValue.InitializerConfig();
        prop.init(config);

        IntsRef ref = new IntsRef(1);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            prop.setInt(false, ref, Integer.MAX_VALUE);
        });
        assertTrue(exception.getMessage().contains("test value too large for encoding"), exception.getMessage());

        prop.setInt(false, ref, -5);
        assertEquals(-5, prop.getInt(false, ref));
        assertEquals(-5, prop.getInt(false, ref));
    }

    @Test
    public void testSignedInt2() {
        IntEncodedValue prop = new IntEncodedValueImpl("test", 31, false);
        EncodedValue.InitializerConfig config = new EncodedValue.InitializerConfig();
        prop.init(config);

        IntsRef ref = new IntsRef(1);
        prop.setInt(false, ref, Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, prop.getInt(false, ref));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            prop.setInt(false, ref, -5);
        });
        assertTrue(exception.getMessage().contains("test value too small for encoding"), exception.getMessage());
    }

    @Test
    public void testNegateReverseDirection() {
        IntEncodedValue prop = new IntEncodedValueImpl("test", 5, 0, true, false);
        EncodedValue.InitializerConfig config = new EncodedValue.InitializerConfig();
        prop.init(config);

        IntsRef ref = new IntsRef(1);
        prop.setInt(false, ref, 5);
        assertEquals(5, prop.getInt(false, ref));
        assertEquals(-5, prop.getInt(true, ref));

        prop.setInt(true, ref, 2);
        assertEquals(-2, prop.getInt(false, ref));
        assertEquals(2, prop.getInt(true, ref));

        prop.setInt(false, ref, -3);
        assertEquals(-3, prop.getInt(false, ref));
        assertEquals(3, prop.getInt(true, ref));
    }

    @Test
    public void testEncodedValueName() {
        for (String str : Arrays.asList("blup_test", "test", "test12", "car_test_test")) {
            assertTrue(isValidEncodedValue(str), str);
        }

        for (String str : Arrays.asList("Test", "12test", "test|3", "car__test", "small_car$average_speed", "tes$0",
                "blup_te.st_", "car___test", "car$$access", "test{34", "truck__average_speed", "blup.test", "test,21",
                "täst", "blup.two.three", "blup..test")) {
            assertFalse(isValidEncodedValue(str), str);
        }

        for (String str : Arrays.asList("break", "switch")) {
            assertFalse(isValidEncodedValue(str), str);
        }
    }
}