package org.riisholt.dgtdriver;

/**
 * TODO: Document this class.
 */
public class ClockVersionMessage extends DgtClockMessage {
    public byte getMessageId() { return 0x09; }
    public byte[] getMessageData() { return new byte[]{}; }
}
