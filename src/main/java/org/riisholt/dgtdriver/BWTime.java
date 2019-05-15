package org.riisholt.dgtdriver;

import java.time.Duration;

public class BWTime implements DgtMessage {
    private Duration left, right;
    private int leftFlags, rightFlags;
    private byte clockStatusFlags;

    public BWTime(byte[] data) throws DgtProtocolException {
        if(data.length != 7)
            throw new DgtProtocolException("BWTime expects exactly 7 bytes of data (got " + data.length + ")");

        rightFlags = (data[0] & 0xf0) >> 4;
        right = Duration.ZERO.plusHours(data[0] & 0x0f)
                            .plusMinutes(decodeBcd(data[1]))
                            .plusSeconds(decodeBcd(data[2]));

        leftFlags = (data[3] & 0xf0) >> 4;
        left = Duration.ZERO.plusHours(data[3] & 0x0f)
                             .plusMinutes(decodeBcd(data[4]))
                             .plusSeconds(decodeBcd(data[5]));

        clockStatusFlags = data[6];
    }

    public boolean leftFinalFlag() { return (leftFlags & 0x01) != 0; }
    public boolean leftTimePerMove() { return (leftFlags & 0x02) != 0; }
    public boolean leftFlag() { return (leftFlags & 0x04) != 0; }

    public boolean rightFinalFlag() { return (rightFlags & 0x01) != 0; }
    public boolean rightTimePerMove() { return (rightFlags & 0x02) != 0; }
    public boolean rightFlag() { return (rightFlags & 0x04) != 0; }

    public boolean clockRunning()   { return (clockStatusFlags & 0x01) != 0; }
    public boolean leftHigh()       { return (clockStatusFlags & 0x02) == 0; }
    public boolean rightHigh()      { return (clockStatusFlags & 0x02) != 0; }
    public boolean batteryLow()     { return (clockStatusFlags & 0x04) != 0; }
    public boolean leftToMove()     { return (clockStatusFlags & 0x08) != 0; }
    public boolean rightToMove()    { return (clockStatusFlags & 0x10) != 0; }
    public boolean clockConnected() { return (clockStatusFlags & 0x20) != 0; }

    public String leftTimeString() { return timeString(left); }
    public String rightTimeString() { return timeString(right); }

    private static int decodeBcd(byte b){
        return ((b & 0xf0) >> 4)*10 + (b & 0x0f);
    }

    private String timeString(Duration t) {
        long seconds = t.getSeconds();
        long hours = seconds/3600;
        seconds -= hours*3600;
        long minutes = seconds/60;
        seconds -= minutes*60;
        return String.format("%d:%02d.%02d", hours, minutes, seconds);
    }
}