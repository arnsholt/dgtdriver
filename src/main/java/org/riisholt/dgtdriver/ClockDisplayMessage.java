package org.riisholt.dgtdriver;

public class ClockDisplayMessage extends DgtClockMessage {
    public final SevenSegment aLocation;
    public final SevenSegment bLocation;
    public final SevenSegment cLocation;
    public final SevenSegment dLocation;
    public final SevenSegment eLocation;
    public final SevenSegment fLocation;
    public final DotsAndOnes dotsAndOnes;
    public final boolean beep;

    public ClockDisplayMessage(SevenSegment aLocation, SevenSegment bLocation, SevenSegment cLocation,
                               SevenSegment dLocation, SevenSegment eLocation, SevenSegment fLocation,
                               DotsAndOnes dotsAndOnes, boolean beep) {
        this.aLocation = aLocation;
        this.bLocation = bLocation;
        this.cLocation = cLocation;
        this.dLocation = dLocation;
        this.eLocation = eLocation;
        this.fLocation = fLocation;
        this.dotsAndOnes = dotsAndOnes;
        this.beep = beep;
    }

    public byte getMessageId() { return 0x01; }

    public byte[] getMessageData() {
        return new byte[] {
                cLocation.asByte(),
                bLocation.asByte(),
                aLocation.asByte(),
                fLocation.asByte(),
                eLocation.asByte(),
                dLocation.asByte(),
                dotsAndOnes.asByte(),
                (byte) (beep? 0x03 : 0x01)
        };
    }

    public static class SevenSegment {
        public final boolean top;
        public final boolean rightTop;
        public final boolean rightBottom;
        public final boolean bottom;
        public final boolean leftBottom;
        public final boolean leftTop;
        public final boolean center;

        public SevenSegment(boolean top, boolean rightTop, boolean rightBottom,
                            boolean bottom, boolean leftBottom, boolean leftTop, boolean center) {
            this.top = top;
            this.rightTop = rightTop;
            this.rightBottom = rightBottom;
            this.bottom = bottom;
            this.leftBottom = leftBottom;
            this.leftTop = leftTop;
            this.center = center;
        }

        byte asByte() {
            return (byte)
                    ((top? 0x01 : 0)
                    | (rightTop? 0x02 : 0)
                    | (rightBottom? 0x04 : 0)
                    | (bottom? 0x08 : 0)
                    | (leftBottom? 0x10 : 0)
                    | (leftTop? 0x20 : 0)
                    | (center? 0x40 : 0));
        }
    }

    public static class DotsAndOnes {
        public final boolean leftOne;
        public final boolean leftDot;
        public final boolean leftSemicolon;
        public final boolean rightOne;
        public final boolean rightDot;
        public final boolean rightSemicolon;

        public DotsAndOnes(boolean leftOne, boolean leftDot, boolean leftSemicolon,
                           boolean rightOne, boolean rightDot, boolean rightSemicolon) {
            this.leftOne = leftOne;
            this.leftDot = leftDot;
            this.leftSemicolon = leftSemicolon;
            this.rightOne = rightOne;
            this.rightDot = rightDot;
            this.rightSemicolon = rightSemicolon;
        }

        byte asByte() {
            return (byte)
                    ((rightDot? 0x01 : 0)
                    | (rightSemicolon? 0x02 : 0)
                    | (rightOne? 0x04 : 0)
                    | (leftDot? 0x08 : 0)
                    | (leftSemicolon? 0x10 : 0)
                    | (leftOne? 0x20 : 0));
        }
    }
}
