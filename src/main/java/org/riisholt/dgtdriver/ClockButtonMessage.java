package org.riisholt.dgtdriver;

public class ClockButtonMessage extends DgtClockMessage {
    public byte getMessageId() { return 0x08; }
    public byte[] getMessageData() { return new byte[]{}; }
}
