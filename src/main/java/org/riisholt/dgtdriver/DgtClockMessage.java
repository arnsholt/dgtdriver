package org.riisholt.dgtdriver;

public abstract class DgtClockMessage {
    public abstract byte[] getData();
    public abstract byte getMessageId();

    public byte[] toBytes() throws DgtProtocolException {
        byte[] data = getData();

        byte[] bytes = new byte[data.length + 5];

        bytes[0] = DgtConstants.DGT_CLOCK_MESSAGE;
        bytes[1] = (byte) (data.length + 3);
        bytes[2] = DgtConstants.DGT_CMD_CLOCK_START_MESSAGE;
        bytes[3] = getMessageId();

        bytes[data.length + 4] = DgtConstants.DGT_CMD_CLOCK_END_MESSAGE;


        System.arraycopy(data, 0, bytes, 4, data.length);

        return bytes;
    }
}
