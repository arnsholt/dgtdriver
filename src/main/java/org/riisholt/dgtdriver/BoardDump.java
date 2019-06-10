package org.riisholt.dgtdriver;

import org.riisholt.dgtdriver.game.Board;

/**
 * A full dump of the board state.
 */
public class BoardDump implements DgtMessage {
    private Board board;
    public BoardDump(byte[] data) throws DgtProtocolException {
        if(data.length != 64)
            throw new DgtProtocolException("BoardDump expects exactly 64 bytes of data");

        board = Board.emptyBoard();
        for(int i = 0; i < data.length; i++) {
            if(data[i] != DgtConstants.EMPTY) {
                board.put(DgtConstants.dgtCodeToSquare(i), DgtConstants.dgtCodeToColor(data[i]),
                        DgtConstants.dgtCodeToRole(data[i]));
            }
        }
    }

    /**
     * The new board state. Only the piece configuration is valid, other
     * information (turn, ep square, castling rights) are in an indeterminate
     * state.
     *
     * @return The new board state
     */
    public Board board() { return board; }
}
