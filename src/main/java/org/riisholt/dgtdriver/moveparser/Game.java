package org.riisholt.dgtdriver.moveparser;

import org.riisholt.dgtdriver.game.Result;

import java.util.Collections;
import java.util.List;

/**
 * A complete game of chess.
 */
public class Game {
    /**
     * The moves played during the game. Even numbered moves (zero-indexed)
     * are white moves, while odd-numbered moves are black.
     */
    public final List<PlayedMove> moves;

    /**
     * The result of the game, if any. If parsing of the game is ended by
     * calling {@link MoveParser#endGame()} directly, this member will be
     * null.
     */
    public final Result result;

    public Game(List<PlayedMove> moves, Result result) {
        this.moves = Collections.unmodifiableList(moves);
        this.result = result;
    }

    public String uci() {
        int ply = 0;
        StringBuilder builder = new StringBuilder();
        for(PlayedMove m: moves) {
            if(ply % 2 == 0) {
                builder.append(1 + ply/2)
                        .append(". ")
                        .append(m.uci);
            }
            else {
                builder.append(' ')
                        .append(m.uci)
                        .append('\n');
            }
            ply++;
        }
        builder.append('\n');
        return builder.toString();
    }

    /**
     * The game in PGN format, possibly including clock times.
     *
     * @param includeClock Include clock times for each move, if available.
     * @return The PGN representation of the game.
     */
    public String pgn(boolean includeClock) {
        int ply = 0;
        StringBuilder sb = new StringBuilder();
        for(PlayedMove m: moves) {
            if(ply % 2 == 0) {
                sb.append(1 + ply/2)
                        .append(". ")
                        .append(m.san);
                if(includeClock && m.clockInfo != null)
                    sb.append(' ')
                      .append(String.format("{[%%clk %s]}", m.clockInfo.leftTimeString()));
            }
            else {
                sb.append(' ')
                        .append(m.san);
                if(includeClock && m.clockInfo != null)
                    sb.append(' ')
                      .append(String.format("{[%%clk %s]}", m.clockInfo.rightTimeString()));
                sb.append('\n');
            }
            ply++;
        }
        appendResult(sb);
        sb.append('\n');
        return sb.toString();
    }

    private void appendResult(StringBuilder sb) {
        if(result != null) {
            sb.append(' ')
              .append(result.resultString());
        }
    }
}
