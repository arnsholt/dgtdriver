package org.riisholt.dgtdriver;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class BWTimeTest {
    @Test
    void testRotation() {
        BWTime orig = new BWTime(
                Duration.ZERO.plusMinutes(5),
                (byte) 0x04,
                Duration.ZERO.plusMinutes(3),
                (byte) 0x00,
                (byte) 0x08);
        BWTime rotated = orig.rotate();

        assertEquals(orig.left, rotated.right);
        assertEquals(orig.right, rotated.left);
        assertTrue(orig.leftFlag());
        assertFalse(orig.rightFlag());
        assertFalse(rotated.leftFlag());
        assertTrue(rotated.rightFlag());

        assertTrue(orig.leftHigh());
        assertTrue(orig.leftToMove());
        assertFalse(orig.rightHigh());
        assertFalse(orig.rightToMove());

        assertFalse(rotated.leftHigh());
        assertFalse(rotated.leftToMove());
        assertTrue(rotated.rightHigh());
        assertTrue(rotated.rightToMove());
    }
}
