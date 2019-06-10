package org.riisholt.dgtdriver;

import java.time.Duration;

/**
 * <p>Clock state. <em>IMPORTANT:</em> The clock state operates purely in
 * terms of the <em>left</em> and <em>right</em> player; there is no inherent
 * conception of white or black (but see
 * {@link org.riisholt.dgtdriver.moveparser.PlayedMove#clockInfo}). This class
 * contains the time remaining for the left and right players (encoded as a
 * {@link Duration}), as well as status flags for each player and general
 * status information. For each player, the following flags are indicated:</p>
 *
 * <ul>
 *     <li>Is the player's flag fallen and the clock blocked at zero?</li>
 *     <li>Is the player's time per move indicator on?</li>
 *     <li>Is the flag signal indicated for the player? This is a distinct
 *     case from the first flag, as the flag is also indicated when multiple
 *     time periods are used (such as two hours for 40 moves, etc.), in which
 *     case the arbiter must ensure that enough moves are played before the
 *     flag falls.</li>
 * </ul>
 *
 * <p>The following general clock state flags are also indicated:</p>
 *
 * <ul>
 *     <li>Is a clock connected?</li>
 *     <li>Is the clock running?</li>
 *     <li>Is the tumbler high to the left?</li>
 *     <li>Is the tumbler high to the right?</li>
 *     <li>Does the clock indicate low battery?</li>
 *     <li>Is it the left player to move?</li>
 *     <li>Is it the right player to move?</li>
 * </ul>
 */
public class BWTime implements DgtMessage {
    private Duration left, right;
    private byte leftFlags, rightFlags, clockStatusFlags;

    public BWTime(byte[] data) throws DgtProtocolException {
        if(data.length != 7)
            throw new DgtProtocolException("BWTime expects exactly 7 bytes of data (got " + data.length + ")");

        rightFlags = (byte) ((data[0] & 0xf0) >> 4);
        right = Duration.ZERO.plusHours(data[0] & 0x0f)
                            .plusMinutes(decodeBcd(data[1]))
                            .plusSeconds(decodeBcd(data[2]));

        leftFlags = (byte) ((data[3] & 0xf0) >> 4);
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

    public Duration left() { return left; }
    public Duration right() { return right; }

    public void rotate() {
        byte origLeft = leftFlags;
        leftFlags = rightFlags;
        rightFlags = origLeft;

        /* The flags for left/right high, left to move, and right to move
         * depend on the orientation of the board (assuming the clock is
         * always on the same side of the board). We flip those bits by
         * XOR-ing in a one in the appropriate position.
         */
        clockStatusFlags ^= 0x02;
        clockStatusFlags ^= 0x08;
        clockStatusFlags ^= 0x10;
    }

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
