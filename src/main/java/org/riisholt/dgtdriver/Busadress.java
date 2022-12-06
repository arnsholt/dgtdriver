package org.riisholt.dgtdriver;

/**
 * The board's bus address.
 */
public class Busadress implements DgtMessage {
    /** The board's bus address. */
    public final int address;

    /** Construct an object from a board data payload. */
    public Busadress(byte[] data) throws DgtProtocolException {
        if(data.length != 2)
            throw new DgtProtocolException("Busadress expects exactly two bytes of data");
        address = (data[0] << 7) | data[1];
    }
}
