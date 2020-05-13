package org.riisholt.dgtdriver;

/**
 * TODO: Document this class.
 */
public class ClockButtonMessage extends DgtClockMessage {
    public byte getMessageId() { return 0x08; }
    public byte[] getMessageData() { return new byte[]{}; }
}
