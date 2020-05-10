package org.riisholt.dgtdriver;

public class ClockBeepMessage extends DgtClockMessage {
    private byte duration;

    public ClockBeepMessage(byte duration) {
        this.duration = duration;
    }

    public byte getMessageId() { return 0x0b; }
    public byte[] getMessageData() { return new byte[]{ duration }; }
}
