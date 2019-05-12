package org.riisholt.dgtdriver;

import org.riisholt.dgtdriver.moveparser.Role;

public class FieldUpdate implements DgtMessage {
    private int square;
    private boolean color;
    private Role role;

    public FieldUpdate(byte[] data) throws DgtProtocolException {
        if(data.length != 2)
            throw new DgtProtocolException("Field update expects exactly two bytes of data");

        square = DgtConstants.dgtCodeToSquare(data[0]);
        color = DgtConstants.dgtCodeToColor(data[1]);
        role = DgtConstants.dgtCodeToRole(data[1]);
    }

    public int square() { return square; }
    public boolean color() { return color; }
    public Role role() { return role; }
}
