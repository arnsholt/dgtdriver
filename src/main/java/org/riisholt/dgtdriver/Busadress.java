package org.riisholt.dgtdriver;

/**
 * The board's bus address.
 */
public class Busadress implements DgtMessage {
    public final int address;
    public Busadress(byte[] data) throws DgtProtocolException {
        if(data.length != 2)
            throw new DgtProtocolException("Busadress expects exactly two bytes of data");
        address = (data[0] << 7) | data[1];
    }
}
