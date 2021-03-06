package org.riisholt.dgtdriver;

/**
 * Requests that the clock sends what button is currently pressed (if any).
 */
public class ClockButtonMessage extends DgtClockMessage {
    public byte getMessageId() { return 0x08; }
    public byte[] getMessageData() { return new byte[]{}; }
}
