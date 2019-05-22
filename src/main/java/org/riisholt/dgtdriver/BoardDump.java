package org.riisholt.dgtdriver;

import org.riisholt.dgtdriver.game.Board;

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

    public Board board() { return board; }
}
