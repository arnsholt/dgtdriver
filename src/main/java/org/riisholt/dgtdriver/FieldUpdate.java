package org.riisholt.dgtdriver;

import org.riisholt.dgtdriver.game.Role;

/**
 * Update of the state of a single square. Note that in the case where a piece
 * A standing on a square is replaced by another piece B, the board may
 * generate a single message (piece B on square) or two messages (square
 * empty, then piece B on square) depending on the how the physical act of
 * replacing the pieces on the board intersects with the scanning of the
 * board.
 */
public class FieldUpdate implements DgtMessage {
    /**
     * The square updated. The codes are in the coordinates used by {@link
     * org.riisholt.dgtdriver.game.Board} (a1=0, h1=7, a2=8, ..., h8=63), not
     * the somewhat idiosyncratic coordinates transmitted by the board (a8=0,
     * b8=1, h8=7, ... h1=63).
     */
    public final int square;

    /**
     * Is the piece placed on the square white?
     */
    public final boolean color;

    /**
     * The piece type placed on the square. If a square is now empty, null is returned.
     */
    public final Role role;

    public FieldUpdate(byte[] data) throws DgtProtocolException {
        if(data.length != 2)
            throw new DgtProtocolException("Field update expects exactly two bytes of data");

        square = DgtConstants.dgtCodeToSquare(data[0]);
        color = DgtConstants.dgtCodeToColor(data[1]);
        role = DgtConstants.dgtCodeToRole(data[1]);
    }
}
