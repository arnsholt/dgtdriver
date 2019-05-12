package org.riisholt.dgtdriver;

import java.nio.charset.StandardCharsets;

public class StringMessage implements DgtMessage {
    private String value;
    public StringMessage(byte[] data) {
            value = new String(data, StandardCharsets.US_ASCII);
    }

    public String value() { return value; }
}
