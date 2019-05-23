package org.riisholt.dgtdriver.moveparser;

import org.riisholt.dgtdriver.game.Result;

import java.util.List;

public class Game {
    public List<PlayedMove> moves;
    public Result result;

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

    public String pgn(boolean includeClock) {
        int ply = 0;
        StringBuilder sb = new StringBuilder();
        for(PlayedMove m: moves) {
            if(ply % 2 == 0) {
                sb.append(1 + ply/2)
                        .append(". ")
                        .append(m.san);
                if(includeClock)
                    sb.append(' ')
                      .append(String.format("{[%%clk %s]}", m.clockInfo.leftTimeString()));
            }
            else {
                sb.append(' ')
                        .append(m.san);
                if(includeClock)
                    sb.append(' ')
                      .append(String.format("{[%%clk %s]}", m.clockInfo.rightTimeString()));
                sb.append('\n');
            }
            ply++;
        }
        appendResult(sb);
        return sb.toString();
    }

    private void appendResult(StringBuilder sb) {
        if(result != null) {
            sb.append(' ')
              .append(result.resultString());
        }
    }
}
