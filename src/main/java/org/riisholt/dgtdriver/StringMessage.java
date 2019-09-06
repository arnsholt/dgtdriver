package org.riisholt.dgtdriver;

import java.nio.charset.StandardCharsets;

/**
 * Base class for board messages transmitting strings.
 */
public class StringMessage implements DgtMessage {
    /**
     * The transmitted string.
      */
    public final String value;
    public StringMessage(byte[] data) {
            value = new String(data, StandardCharsets.US_ASCII);
    }
}
