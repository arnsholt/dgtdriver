package org.riisholt.dgtdriver;

public class Busadress implements DgtMessage {
    private int address;
    public Busadress(byte[] data) throws DgtProtocolException {
        if(data.length != 2)
            throw new DgtProtocolException("Busadress expects exactly two bytes of data");
        address = (data[0] << 7) | data[1];
    }

    public int address() { return address; }
}
