package org.riisholt.dgtdriver;

/** Clear custom message from the clock display. */
public class ClockEndMessage extends DgtClockMessage {
    public byte getMessageId() { return 0x03; }
    public byte[] getMessageData() { return new byte[]{}; }
}
