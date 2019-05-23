package org.riisholt.dgtdriver.moveparser;

import org.riisholt.dgtdriver.BWTime;

public class PlayedMove {
    public String uci, san;
    public BWTime clockInfo;

    public PlayedMove(String uci, String san, BWTime clockInfo) {
        this.uci = uci;
        this.san = san;
        this.clockInfo = clockInfo;
    }
}
