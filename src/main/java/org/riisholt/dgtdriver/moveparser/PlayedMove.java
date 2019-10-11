package org.riisholt.dgtdriver.moveparser;

import org.riisholt.dgtdriver.BWTime;
import org.riisholt.dgtdriver.game.Board;
import org.riisholt.dgtdriver.game.Move;

/**
 * A move played during a game.
 */
public class PlayedMove {
    /** The move's representation in standard algebraic notation. */
    public final String san;

    /**
     * The state of the clock when the move was played. The move parser
     * corrects for board orientation, such that the linked BWTime object
     * always has the white player on the left and black on the right.
     */
    public final BWTime clockInfo;

    /**
     * The current state of the board, after the played move. This member is a
     * copy of the board state in the move parser and can be freely modified
     * by client code if needed.
     */
    public final Board board;

    /**
     * The move played. Like the board member, this is a copy of the data from
     * the move parser and can be freely modified by client code when needed.
     */
    public final Move move;

    /**
     * Utility class constructor.
     *
     * @param san Standard algebraic notation
     * @param clockInfo Clock state
     * @param board Board state
     * @param move Move played
     */
    public PlayedMove(String san, BWTime clockInfo, Board board, Move move) {
        this.san = san;
        this.clockInfo = clockInfo;
        this.board = new Board(board);
        this.move = new Move(move);
    }
}
