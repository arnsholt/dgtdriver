package org.riisholt.dgtdriver;

import java.util.Arrays;

import static org.riisholt.dgtdriver.DgtConstants.*;

public class DgtDriver {
    public interface ReadCallback { void gotMessage(DgtMessage msg); }
    public interface WriteCallback { void write(byte[] bytes); }

    private static final String TAG = "DgtDriver";
    private ReadCallback readCallback;
    private WriteCallback writeCallback;
    private byte[] buffer = new byte[128];
    private int position = 0;

    public DgtDriver(ReadCallback read, WriteCallback write) {
        readCallback = read;
        writeCallback = write;
    }

    public void reset()           { writeByte(DGT_SEND_RESET); }
    public void toBusmode()       { writeByte(DGT_TO_BUSMODE); }
    public void startBootloader() { writeByte(DGT_STARTBOOTLOADER); }
    public void clock()           { writeByte(DGT_SEND_CLK); }
    public void board()           { writeByte(DGT_SEND_BRD); }
    public void update()          { writeByte(DGT_SEND_UPDATE); }
    public void updateBoard()     { writeByte(DGT_SEND_UPDATE_BRD); }
    public void serialnr()        { writeByte(DGT_RETURN_SERIALNR); }
    public void busadress()       { writeByte(DGT_RETURN_BUSADRES); }
    public void trademark()       { writeByte(DGT_SEND_TRADEMARK); }
    public void eeMoves()         { writeByte(DGT_SEND_EE_MOVES); }
    public void updateNice()      { writeByte(DGT_SEND_UPDATE_NICE); }
    public void batteryStatus()   { writeByte(DGT_SEND_BATTERY_STATUS); }
    public void version()         { writeByte(DGT_SEND_VERSION); }
    public void board50b()        { writeByte(DGT_SEND_BRD_50B); }
    public void scan50b()         { writeByte(DGT_SCAN_50B); }
    public void board50w()        { writeByte(DGT_SEND_BRD_50W); }
    public void scan50w()         { writeByte(DGT_SCAN_50W); }
    public void scan100()         { writeByte(DGT_SCAN_100); }
    public void longSerialnr()    { writeByte(DGT_RETURN_LONG_SERIALNR); }
    // TODO: Clock commands.

    public void gotBytes(byte[] bytes) {
        if(position + bytes.length > buffer.length)
            buffer = Arrays.copyOf(buffer, buffer.length + 128);
        System.arraycopy(bytes, 0, buffer, position, bytes.length);
        position += bytes.length;
        tryEmitMessage();
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
