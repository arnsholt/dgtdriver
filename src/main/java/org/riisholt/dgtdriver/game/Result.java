package org.riisholt.dgtdriver.game;

public enum Result {
    WHITE_WIN("1-0"),
    BLACK_WIN("0-1"),
    DRAW("1/2-1/2");

    private String resultString;
    Result(String resultString) {
        this.resultString = resultString;
    }

    public String resultString() { return resultString; }
}
