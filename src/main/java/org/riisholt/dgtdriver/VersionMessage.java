package org.riisholt.dgtdriver;

public class VersionMessage implements DgtMessage {
    private byte major, minor;

    public VersionMessage(byte[] data) throws DgtProtocolException {
        if(data.length != 2)
            throw new DgtProtocolException("Version message expects exactly two bytes of data");
        major = data[0];
        minor = data[1];
    }

    /**
     * The major version number.
     *
     * @return Major version number
     */
    public byte major() { return major; }

    /**
     * The minor version number.
     *
     * @return Minor version number
     */
    public byte minor() { return minor; }
}
