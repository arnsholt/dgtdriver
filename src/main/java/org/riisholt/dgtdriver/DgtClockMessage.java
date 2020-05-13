package org.riisholt.dgtdriver;

/**
 * Base class for messages sent from software to the clock.
 */
public abstract class DgtClockMessage {
    /**
     * Get the data payload bytes of the clock message.
     *
     * @return The data bytes to send to the clock. Must be non-null.
     */
    public abstract byte[] getMessageData();

    /**
     * Get the identifier of the message sent to the clock.
     *
     * @return The message ID
     */
    public abstract byte getMessageId();

    /**
     * Convert the message to the correct byte stream to send to the board.
     *
     * @return The bytes to send to the board
     */
    public byte[] toBytes() {
        byte[] data = getMessageData();

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
