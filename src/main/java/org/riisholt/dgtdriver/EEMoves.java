package org.riisholt.dgtdriver;

public class EEMoves implements DgtMessage {
    public static final byte EE_POWERUP         = 0x6a;
    public static final byte EE_EOF             = 0x6b;
    public static final byte EE_FOURROWS        = 0x6c;
    public static final byte EE_EMPTYBOARD      = 0x6d;
    public static final byte EE_DOWNLOADED      = 0x6e;
    public static final byte EE_BEGINPOS        = 0x6f;
    public static final byte EE_BEGINPOS_ROT    = 0x7a;
    public static final byte EE_START_TAG       = 0x7b;
    public static final byte EE_WATCHDOG_ACTION = 0x7c;
    public static final byte EE_FUTURE_1        = 0x7d;
    public static final byte EE_FUTURE_2        = 0x7e;
    public static final byte EE_NOP             = 0x7f;
    public static final byte EE_NOP2            = 0x00;
    // 0x40 to 0x5f: two-byte field update message
    // 0x60-69 and 0x70-0x79: three byte clock message

    public EEMoves(byte[] data) {
        // TODO: Figure out best representation of data and parse it.
    }
}
