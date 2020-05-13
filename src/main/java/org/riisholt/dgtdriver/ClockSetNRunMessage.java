package org.riisholt.dgtdriver;

import java.time.Duration;

public class ClockSetNRunMessage extends DgtClockMessage {
    private Duration leftTime, rightTime;
    private boolean leftCountsUp, rightCountsUp, pause, toggleOnLever;

    public ClockSetNRunMessage(Duration leftTime, boolean leftCountsUp,
                               Duration rightTime, boolean rightCountsUp,
                               boolean pause, boolean toggleOnLever) {
        this.leftTime = leftTime;
        this.rightTime = rightTime;
        this.leftCountsUp = leftCountsUp;
        this.rightCountsUp = rightCountsUp;
        this.pause = pause;
        this.toggleOnLever = toggleOnLever;
    }

    public byte getMessageId() { return 0x0a; }
    public byte[] getMessageData() { return new byte[] {
            (byte) (leftTime.toHours() | (leftCountsUp? 0x10 : 0x00)),
            (byte) leftTime.toMinutesPart(),
            (byte) leftTime.toSecondsPart(),
            (byte) (rightTime.toHours() | (rightCountsUp? 0x10 : 0x00)),
            (byte) rightTime.toMinutesPart(),
            (byte) rightTime.toSecondsPart(),
            (byte) ((!leftCountsUp? 0x01 : 0x00)
                    | (!rightCountsUp? 0x02 : 0x00)
                    | (pause? 0x04 : 0x00)
                    | (toggleOnLever? 0x08 : 0x00))
    }; }
}
