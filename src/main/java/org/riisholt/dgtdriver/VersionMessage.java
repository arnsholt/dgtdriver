package org.riisholt.dgtdriver;

public class VersionMessage implements DgtMessage {
    /**
     * The major version number.
     */
    public final byte major;

    /**
     * The minor version number.
     */
    public final byte minor;

    public VersionMessage(byte[] data) throws DgtProtocolException {
        if(data.length != 2)
            throw new DgtProtocolException("Version message expects exactly two bytes of data");
        major = data[0];
        minor = data[1];
    }
}
