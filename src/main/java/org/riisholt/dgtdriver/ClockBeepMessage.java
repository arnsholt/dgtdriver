package org.riisholt.dgtdriver;

/**
 * Turn on the clock beep for a specified duration.
 */
public class ClockBeepMessage extends DgtClockMessage {
    /** How long to turn on the beep for. */
    private byte duration;

    public ClockBeepMessage(byte duration) {
        this.duration = duration;
    }

    public byte getMessageId() { return 0x0b; }
    public byte[] getMessageData() { return new byte[]{ duration }; }
}
