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
    /** Time left on the left player's clock. */
    public final Duration left;
    /** Time left on the right player's clock. */
    public final Duration right;

    /**
     * Status byte for the left clock. Information <em>can</em> be extracted
     * from this byte, but more convenient is probably the helper methods
     * defined in this class.
     *
     * @see #leftFinalFlag()
     * @see #leftFlag()
     * @see #leftTimePerMove()
     */
    public final byte leftFlags;

    /**
     * Status byte for the right clock. Information <em>can</em> be extracted
     * from this byte, but more convenient is probably the helper methods
     * defined in this class.
     *
     * @see #rightFinalFlag()
     * @see #rightFlag()
     * @see #rightTimePerMove()
     */
    public final byte rightFlags;

    /**
     * Status byte for general flags. Information <em>can</em> be extracted
     * from this byte, but more convenient is probably the helper methods
     * defined in this class.
     *
     * @see #clockConnected()
     * @see #clockRunning()
     * @see #leftHigh()
     * @see #rightHigh()
     * @see #batteryLow()
     * @see #leftToMove()
     * @see #rightToMove()
     */
    public final byte clockStatusFlags;

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

    public BWTime(Duration left, byte leftFlags, Duration right, byte rightFlags, byte clockStatusFlags) {
        this.left = left;
        this.leftFlags = leftFlags;
        this.right = right;
        this.rightFlags = rightFlags;
        this.clockStatusFlags = clockStatusFlags;
    }

    /**
     * Has the left player's final flag fallen?
     *
     * @return {@code true} if the final flag has fallen
     */
    public boolean leftFinalFlag() { return (leftFlags & 0x01) != 0; }

    /**
     * Is the left player's time per move indicator on?
     *
     * @return {@code true} if the time per move indicator is on
     */
    public boolean leftTimePerMove() { return (leftFlags & 0x02) != 0; }

    /**
     * Has the left player's flag fallen?
     *
     * @return {@code true} if the flag has fallen
     */
    public boolean leftFlag() { return (leftFlags & 0x04) != 0; }

    /**
     * Has the right player's final flag fallen?
     *
     * @return {@code true} if the final flag has fallen
     */
    public boolean rightFinalFlag() { return (rightFlags & 0x01) != 0; }

    /**
     * Is the right player's time per move indicator on?
     *
     * @return {@code true} if the time per move indicator is on
     */
    public boolean rightTimePerMove() { return (rightFlags & 0x02) != 0; }

    /**
     * Has the right player's flag fallen?
     *
     * @return {@code true} if the flag has fallen
     */
    public boolean rightFlag() { return (rightFlags & 0x04) != 0; }

    /**
     * Is the clock running?
     *
     * @return {@code true} if the clock is running
     */
    public boolean clockRunning()   { return (clockStatusFlags & 0x01) != 0; }

    /**
     * Is the left side of the clock tumbler high?
     *
     * @return {@code true} if the left tumbler is high
     */
    public boolean leftHigh()       { return (clockStatusFlags & 0x02) == 0; }

    /**
     * Is the right side of the clock tumbler high?
     *
     * @return {@code true} if the right tumbler is high
     */
    public boolean rightHigh()      { return (clockStatusFlags & 0x02) != 0; }

    /**
     * Is the clock indicating low battery?
     *
     * @return {@code true} if the battery is low
     */
    public boolean batteryLow()     { return (clockStatusFlags & 0x04) != 0; }

    /**
     * Is it the left player's turn to move?
     *
     * @return {@code true} if the left player is to move
     */
    public boolean leftToMove()     { return (clockStatusFlags & 0x08) != 0; }

    /** /**
     * Is it the right player's turn to move?
     *
     * @return {@code true} if the right player is to move
     */
    public boolean rightToMove()    { return (clockStatusFlags & 0x10) != 0; }

    /**
     * Is a clock connected to the board?
     *
     * @return {@code true} if a clock is connected
     */
    public boolean clockConnected() { return (clockStatusFlags & 0x20) != 0; }

    /**
     * The time remaining on the left clock, formatted as "HH:MM:ss". The
     * minute and second fields are zero padded to always be two characters
     * wide.
     *
     * @return The left player's time
     */
    public String leftTimeString() { return timeString(left); }

    /**
     * The time remaining on the right clock, formatted as "HH:MM:ss". The
     * minute and second fields are zero padded to always be two characters
     * wide.
     *
     * @return The right player's time
     */
    public String rightTimeString() { return timeString(right); }

    /**
     * Rotate the clock info. This swaps all the position-dependent
     * information around, so that it's as if the left player is on the right
     * and vice versa. This is a helper intended for the case where the game
     * is played with white seated at the "black" side of the board, so that
     * most of the code can assume that white is always on the left and the
     * board's A1 is the game's A1.
     *
     * @return A rotated copy
     */
    public BWTime rotate() {
        /* The flags for left/right high, left to move, and right to move
         * depend on the orientation of the board (assuming the clock is
         * always on the same side of the board). We flip those bits by
         * XOR-ing in a one in the appropriate position.
         */
        byte newClockStatus = (byte) (clockStatusFlags ^ 0x1a);
        return new BWTime(right, rightFlags, left, leftFlags, newClockStatus);
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
