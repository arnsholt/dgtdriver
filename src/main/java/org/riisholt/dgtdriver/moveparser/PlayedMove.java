package org.riisholt.dgtdriver.moveparser;

import org.riisholt.dgtdriver.BWTime;

/**
 * A move played during a game.
 */
public class PlayedMove {
    /**
     * The move's representation in UCI format (for example e2e4 for e4 or
     * g8f6 for ...Nf6).
     */
    public String uci;

    /** The moves representation in standard algebraic notation. */
    public String san;

    /** The state of the clock when the move was played. */
    public BWTime clockInfo;

    /**
     * Utility class constructor.
     *
     * @param uci Move representation in UCI format
     * @param san Standard algebraic notation
     * @param clockInfo Clock state
     */
    public PlayedMove(String uci, String san, BWTime clockInfo) {
        this.uci = uci;
        this.san = san;
        this.clockInfo = clockInfo;
    }
}
