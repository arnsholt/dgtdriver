package org.riisholt.dgtdriver;

public class ClockEndMessage extends DgtClockMessage {
    public byte getMessageId() { return 0x03; }
    public byte[] getMessageData() { return new byte[]{}; }
}
