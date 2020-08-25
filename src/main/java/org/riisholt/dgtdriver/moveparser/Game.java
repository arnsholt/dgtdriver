package org.riisholt.dgtdriver.moveparser;

import org.riisholt.dgtdriver.game.Result;

import java.util.Collections;
import java.util.List;

/**
 * A game of chess. Contains a list of played moves, and possibly a final
 * result.
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

    /**
     * <p>The game, as UCI formatted moves. The move list is formatted similarly
     * to a PGN or traditional print move list, with one full move per line
     * preceded by the move number, but with the moves formatted as
     * origin-destination square pairs as in the UCI protocol rather than
     * algebraic notation. If a result is recorded it is added to the final
     * line as "1-0", "0-1" or "1/2-1/2". Thus, this method returns</p>
     *
     * <pre>1. e2e4 e7e5
2. g1f3 b8c6
3. f1b5 a7a6</pre>
     *
     * <p>rather than</p>
     *
     * <pre>1. e4 e5
2. Nf3 Nc6
3. Bb5 a6</pre>
     *
     * <p>for the first few moves of the ruy Lopez.</p>
     *
     * @return The UCI moves of the game
     */
    public String uci() {
        int ply = 0;
        StringBuilder builder = new StringBuilder();
        for(PlayedMove m: moves) {
            if(ply % 2 == 0) {
                builder.append(1 + ply/2)
                        .append(". ")
                        .append(m.move.uci());
            }
            else {
                builder.append(' ')
                        .append(m.move.uci())
                        .append('\n');
            }
            ply++;
        }
        appendResult(builder);
        builder.append('\n');
        return builder.toString();
    }

    /**
     * The game in PGN format, possibly including clock times.
     *
     * @param includeClock Include clock times for each move, if available.
     * @return The PGN representation of the game.
     * @see <a href="https://www.chessclub.com/help/PGN-spec">The PGN specification</a>
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
        sb.append(' ').append(result != null? result.resultString() : "*");
    }
}
