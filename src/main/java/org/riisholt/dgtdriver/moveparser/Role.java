package org.riisholt.dgtdriver.moveparser;

public enum Role {
    PAWN(0, ""),
    KNIGHT(1, "N"),
    BISHOP(2, "B"),
    ROOK(3, "R"),
    QUEEN(4, "Q"),
    KING(5, "K"),
    DRAW(6, null),
    WIN(7, null);

    public final int index;
    public final String symbol;

    Role(int index, String symbol) {
        this.index = index;
        this.symbol = symbol;
    }
}
