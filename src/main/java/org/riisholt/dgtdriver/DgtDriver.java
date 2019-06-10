package org.riisholt.dgtdriver;

import java.util.Arrays;

import static org.riisholt.dgtdriver.DgtConstants.*;

/**
 * <p>A class to interact with DGT chess boards. This class only implements
 * the wire protocol used by the DGT boards, requiring the user to provide a
 * callback function to write bytes to the serial connection and communicating
 * the bytes received from the serial port with {@link #gotBytes(byte[])}.
 * Incoming bytes are processed, and as complete messages are received the
 * read callback is invoked. A basic use of the driver would look like this:</p>
 *
 * <pre>
 * DgtDriver.ReadCallback readCallback = ...; // Your read callback here.
 * DgtDriver.WriteCallback writeCallback = ...; // Your write handler here.
 * DgtDriver driver = new DgtDriver(readCallback,  writeCallback);
 * driver.reset();
 * driver.board();
 * driver.clock();
 * driver.updateNice();
 * byte[] buffer = new byte[128];
 * java.io.InputStream is = ...; // Or some other way of reading bytes.
 * while(true) {
 *     int read = is.read(buffer);
 *     if(read == -1)
 *         break; // End-of-file
 *     // Arrays.copyOf to only send received bytes to the driver.
 *     driver.gotBytes(java.util.Arrays.copyOf(buffer, read));
 * }
 * </pre>
 *
 * <p></p>For reference, the serial format used for the serial communication
 * is 9600  baud, 8 data bits, 1 stop bit, no parity, no flow control.</p>
 *
 * @author Arne Skjærholt
 * @see org.riisholt.dgtdriver.moveparser.MoveParser
 */
public class DgtDriver {
    public interface ReadCallback { void gotMessage(DgtMessage msg); }
    public interface WriteCallback { void write(byte[] bytes); }

    private static final String TAG = "DgtDriver";
    private ReadCallback readCallback;
    private WriteCallback writeCallback;
    private byte[] buffer = new byte[128];
    private int position = 0;

    /**
     * Class constructor.
     *
     * @param read Callback invoked by {@link #gotBytes(byte[])} when a
     *             complete message is received.
     * @param write Callback invoked to write bytes to the serial connection.
     */
    public DgtDriver(ReadCallback read, WriteCallback write) {
        readCallback = read;
        writeCallback = write;
    }

    /** Puts the board in idle mode. */
    public void reset() { writeByte(DGT_SEND_RESET); }

    /** Puts the board in bus mode. */
    public void toBusmode() { writeByte(DGT_TO_BUSMODE); }

    /**
     * Starts the board's bootloader. I have been unable to find documentation
     * of this beyond "Makes a long jump to the FC00 boot loader code. Start
     * FLIP now", so the exact purpose is unclear.
     */
    public void startBootloader() { writeByte(DGT_STARTBOOTLOADER); }

    /**
     * Requests the board to send the current clock state. Results in a {@link
     * BWTime} message.
     */
    public void clock() { writeByte(DGT_SEND_CLK); }

    /**
     * Requests the board to send the full board state. Results in a {@link
     * BoardDump} message.
     */
    public void board() { writeByte(DGT_SEND_BRD); }

    /**
     * Puts the board in update mode. The board will send {@link FieldUpdate}
     * and {@link BWTime} messages as long as it is in update mode.
     */
    public void update() { writeByte(DGT_SEND_UPDATE); }

    /**
     * Puts the board in nice update mode. The board will send {@link
     * FieldUpdate} and {@link BWTime} messages, as in update mode, but time
     * updates will only be sent when the clock changes.
     */
    public void updateNice() { writeByte(DGT_SEND_UPDATE_NICE); }

    /**
     * Puts the board in board update mode. The board will send {@link
     * FieldUpdate} messages as long as it is in board update mode.
     */
    public void updateBoard() { writeByte(DGT_SEND_UPDATE_BRD); }

    /**
     * Requests the board to send its serial number. Results in a {@link
     * SerialnrMessage} message.
     */
    public void serialnr() { writeByte(DGT_RETURN_SERIALNR); }

    /**
     * Requests the board to send its long serial number. Results in a {@link
     * LongSerialnrMessage}.
     */
    public void longSerialnr() { writeByte(DGT_RETURN_LONG_SERIALNR); }

    /**
     * Requests the board to send its board address. Results in a {@link
     * Busadress} message.
     */
    public void busadress() { writeByte(DGT_RETURN_BUSADRES); }

    /**
     * Requests the board to send its trademark message. Results in a {@link
     * TrademarkMessage} message.
     */
    public void trademark() { writeByte(DGT_SEND_TRADEMARK); }

    /**
     * Requests the board to send the moves stored in its EEPROM. The response
     * to this command is not yet handled by the driver.
     */
    public void eeMoves() { writeByte(DGT_SEND_EE_MOVES); }

    /**
     * Requests the board to send the status of its battery. The response to
     * this command is not yet handled by the driver.
     */
    public void batteryStatus() { writeByte(DGT_SEND_BATTERY_STATUS); }

    /**
     * Requests the board to send its version. Results in a {@link
     * VersionMessage}.
     */
    public void version() { writeByte(DGT_SEND_VERSION); }

    /* Commented out: commands for DGT draughts boards
    public void board50b()        { writeByte(DGT_SEND_BRD_50B); }
    public void scan50b()         { writeByte(DGT_SCAN_50B); }
    public void board50w()        { writeByte(DGT_SEND_BRD_50W); }
    public void scan50w()         { writeByte(DGT_SCAN_50W); }
    public void scan100()         { writeByte(DGT_SCAN_100); }*/
    // TODO: Clock commands.

    /**
     * Sends received bytes to the driver. Any complete messages parsed will
     * be emitted via the read callback passed to the constructor, and any
     * trailing partial message will be buffered and combined with the next
     * chunk of bytes received.
     *
     * @param bytes The bytes received
     */
    public void gotBytes(byte[] bytes) {
        // Make sure there's room in the buffer for the incoming data.
        if(position + bytes.length > buffer.length)
            buffer = Arrays.copyOf(buffer, buffer.length + bytes.length);

        // Add data to buffer, and process any complete messages.
        System.arraycopy(bytes, 0, buffer, position, bytes.length);
        position += bytes.length;
        tryEmitMessage();

        /* Keep the buffer reasonably sized if possible. Apart from the
         * DGT_EE_MOVES message, which dumps moves stored in EEPROM, the
         * largest message is DGT_BOARD_DUMP at 67 bytes, so 128 bytes should
         * be ample space.
         */
        if(buffer.length > 128 && position < 128)
            buffer = Arrays.copyOf(buffer, 128);
    }

    private void tryEmitMessage() {
        // A full message is always at least 3 bytes.
        while(position >= 3) {
            byte message = buffer[0];
            byte sizeMsb = buffer[1];
            byte sizeLsb = buffer[2];

            if((message & 0x80) == 0) {
                //Log.e(TAG, "Bad command byte, high bit zero");
                scrollBadBytes(1);
            }
            if((sizeMsb & 0x80) != 0) {
                //Log.e(TAG, "Bad size MSB, high bit not zero");
                scrollBadBytes(2);
            }
            if((sizeLsb & 0x80) != 0) {
                //Log.e(TAG, "Bad size LSB, high bit not zero");
                scrollBadBytes(3);
            }

            int messageLen = (sizeMsb << 7) | sizeLsb;

            // The full data hasn't been received yet.
            if(messageLen > position)
                return;

            DgtMessage msg = null;
            try {
                byte[] data = messageLen > 3?
                        Arrays.copyOfRange(buffer, 3, messageLen):
                        new byte[]{};
                System.arraycopy(buffer, messageLen, buffer, 0, position - messageLen);
                position -= messageLen;

                switch (message & 0x7f) {
                    case DGT_NONE:
                        break;
                    case DGT_BOARD_DUMP:
                        msg = new BoardDump(data);
                        break;
                    case DGT_BWTIME:
                        msg = new BWTime(data);
                        break;
                    case DGT_FIELD_UPDATE:
                        msg = new FieldUpdate(data);
                        break;
                    case DGT_EE_MOVES:
                        // TODO
                        return;
                    case DGT_BUSADRES:
                        msg = new Busadress(data);
                        break;
                    case DGT_SERIALNR:
                        msg = new SerialnrMessage(data);
                        break;
                    case DGT_TRADEMARK:
                        msg = new TrademarkMessage(data);
                        break;
                    case DGT_VERSION:
                        msg = new VersionMessage(data);
                        break;
                    case DGT_BOARD_DUMP_50B:
                        // TODO
                        return;
                    case DGT_BOARD_DUMP_50W:
                        // TODO
                        return;
                    case DGT_LONG_SERIALNR:
                        msg = new LongSerialnrMessage(data);
                        break;
                    default:
                        throw new DgtProtocolException(String.format("Unknown message id %x from board", message & 0x7f));
                }
            }
            catch(Exception e) {
                //Log.e(TAG, String.format("Exception (%s) while parsing message: %s",
                //        e.getClass().getCanonicalName(), e.getMessage()), e);
                continue;
            }

            if(msg != null)
                readCallback.gotMessage(msg);
        }
    }

    private void scrollBadBytes(int start) {
        int good;
        for(good = start; good < position;good++) {
            if((buffer[good] & 0x80) != 0)
                break;
        }
        System.arraycopy(buffer, good, buffer, 0, position-good);
        position -= good;
    }

    private void writeByte(byte b) { writeCallback.write(new byte[]{b}); }
}
