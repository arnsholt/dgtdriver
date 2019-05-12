package org.riisholt.dgtdriver;

public class VersionMessage implements DgtMessage {
    private byte major, minor;

    public VersionMessage(byte[] data) throws DgtProtocolException {
        if(data.length != 2)
            throw new DgtProtocolException("Version message expects exactly two bytes of data");
        major = data[0];
        minor = data[1];
    }

    public byte major() { return major; }
    public byte minor() { return minor; }
}
