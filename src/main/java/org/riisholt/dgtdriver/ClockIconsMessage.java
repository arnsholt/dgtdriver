package org.riisholt.dgtdriver;

public class ClockIconsMessage extends DgtClockMessage {
    private Icons left, right;
    private GeneralIcons general;

    public ClockIconsMessage(Icons left, Icons right, GeneralIcons general) {
        this.left = left;
        this.right = right;
        this.general = general;
    }

    public byte getMessageId() { return 0x02; }

    public byte[] getMessageData() {
        return new byte[]{
            left.byte1(),
            right.byte1(),
            left.byte2(),
            right.byte2(),
            general.value(),
            0x00,
            0x00,
            0x00,
        };
    }

    public static class Icons {
        public final boolean time, fisch, delay, hglass, upcnt, byo, end,
               period1, period2, period3, period4, period5, flag;

        public Icons(boolean time, boolean fisch, boolean delay, boolean
                hglass, boolean upcnt, boolean byo, boolean end, boolean
                period1, boolean period2, boolean period3, boolean period4,
                boolean period5, boolean flag) {
            this.time = time;
            this.fisch = fisch;
            this.delay = delay;
            this.hglass = hglass;
            this.upcnt = upcnt;
            this.byo = byo;
            this.end = end;
            this.period1 = period1;
            this.period2 = period2;
            this.period3 = period3;
            this.period4 = period4;
            this.period5 = period5;
            this.flag = flag;
        }

        private byte byte1() {
            return (byte) ((time?   0x01 : 0)
                         | (fisch?  0x02 : 0)
                         | (delay?  0x04 : 0)
                         | (hglass? 0x08 : 0)
                         | (upcnt?  0x10 : 0)
                         | (byo?    0x20 : 0)
                         | (end?    0x40 : 0));
        }

        private byte byte2() {
            return (byte) ((period1? 0x01 : 0)
                         | (period2? 0x02 : 0)
                         | (period3? 0x04 : 0)
                         | (period4? 0x08 : 0)
                         | (period5? 0x10 : 0)
                         | (flag?    0x20 : 0));
        }
    }

    public static class GeneralIcons {
        public final boolean clear, sound, blackWhite, whiteBlack, bat, remain;

        public GeneralIcons(boolean clear, boolean sound, boolean blackWhite,
                boolean whiteBlack, boolean bat, boolean remain) {
            this.clear = clear;
            this.sound = sound;
            this.blackWhite = blackWhite;
            this.whiteBlack = whiteBlack;
            this.bat = bat;
            this.remain = remain;
        }

        private byte value() {
            return (byte) ((clear? 0x01 : 0)
                         | (sound? 0x02 : 0)
                         | (blackWhite? 0x04 : 0)
                         | (whiteBlack? 0x08 : 0)
                         | (bat? 0x10 : 0)
                         | (remain? 0x40 : 0)
                         );
        }
    }
}
