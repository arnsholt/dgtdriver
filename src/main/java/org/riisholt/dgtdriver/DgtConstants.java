package org.riisholt.dgtdriver;

import org.riisholt.dgtdriver.game.Role;
import static org.riisholt.dgtdriver.game.Role.*;

/**
 * Constants used in board communication, and conversion utilities.
 */
public class DgtConstants {
    /* Messages to board that are not responded to. */
    /** Reset the board back to idle mode. Cancels any update mode requested. */
    public static final byte DGT_SEND_RESET      = 0x40;
    /** Put the board in bus mode. */
    public static final byte DGT_TO_BUSMODE      = 0x4a;
    /** Reboot the board? Poorly documented from DGT. */
    public static final byte DGT_STARTBOOTLOADER = 0x4e;

    /* Messages that generate responses. */
    /** Request the current clock state. */
    public static final byte DGT_SEND_CLK             = 0x41;
    /** Request the current board state. */
    public static final byte DGT_SEND_BRD             = 0x42;
    /** Request periodic updates. */
    public static final byte DGT_SEND_UPDATE          = 0x43;
    /** Request updates for board state. */
    public static final byte DGT_SEND_UPDATE_BRD      = 0x44;
    /** Request the board's serial number. */
    public static final byte DGT_RETURN_SERIALNR      = 0x45;
    /** Request the board's bus adress. */
    public static final byte DGT_RETURN_BUSADRES      = 0x46;
    /** Request the trademark message. */
    public static final byte DGT_SEND_TRADEMARK       = 0x47;
    /** Request transmission of moves stored in the board's EEPROM. */
    public static final byte DGT_SEND_EE_MOVES        = 0x49;
    /** Request updates when board or clock state changes. */
    public static final byte DGT_SEND_UPDATE_NICE     = 0x4b;
    /** Request battery status. */
    public static final byte DGT_SEND_BATTERY_STATUS  = 0x4c;
    /** Request the board's version information. */
    public static final byte DGT_SEND_VERSION         = 0x4d;
    /** (Draughts) Requests the state of the black squares. */
    public static final byte DGT_SEND_BRD_50B         = 0x50;
    /** (Draughts) Set the board to scan only the black squares. */
    public static final byte DGT_SCAN_50B             = 0x51;
    /** (Draughts) Requests the state of the white squares. */
    public static final byte DGT_SEND_BRD_50W         = 0x52;
    /** (Draughts) Set the board to scan only the white squares. */
    public static final byte DGT_SCAN_50W             = 0x53;
    /** (Draughts) Set the board to scan the whole board. */
    public static final byte DGT_SCAN_100             = 0x54;
    /** Request the board's long serial number. */
    public static final byte DGT_RETURN_LONG_SERIALNR = 0x55;

    /* Clock messages. */
    /** Set clock state. */
    public static final byte DGT_CLOCK_MESSAGE           = 0x2b;
    /** Change main display. */
    public static final byte DGT_CMD_CLOCK_DISPLAY       = 0x01;
    /** Change icons on clock. */
    public static final byte DGT_CMD_CLOCK_ICONS         = 0x02;
    /** End custom display. */
    public static final byte DGT_CMD_CLOCK_END           = 0x03;
    /** Request current button pressed. */
    public static final byte DGT_CMD_CLOCK_BUTTON        = 0x08;
    /** Request clock version. */
    public static final byte DGT_CMD_CLOCK_VERSION       = 0x09;
    /** Programmatically set clock times and running state. */
    public static final byte DGT_CMD_CLOCK_SETNRUN       = 0x0a;
    /** Change beep state. */
    public static final byte DGT_CMD_CLOCK_BEEP          = 0x0b;
    /** Start of clock message. */
    public static final byte DGT_CMD_CLOCK_START_MESSAGE = 0x03;
    /** End of clock message. */
    public static final byte DGT_CMD_CLOCK_END_MESSAGE   = 0x00;

    /* Messages from board. */
    /** Empty message. */
    public static final byte DGT_NONE           = 0x00;
    /** Board dump message. */
    public static final byte DGT_BOARD_DUMP     = 0x06;
    /** Clock status message. */
    public static final byte DGT_BWTIME         = 0x0d;
    /** Field change message. */
    public static final byte DGT_FIELD_UPDATE   = 0x0e;
    /** Moves from EEPROM. */
    public static final byte DGT_EE_MOVES       = 0x0f;
    /** Board bus adress. */
    public static final byte DGT_BUSADRES       = 0x10;
    /** Board serial number. */
    public static final byte DGT_SERIALNR       = 0x11;
    /** Trademark message. */
    public static final byte DGT_TRADEMARK      = 0x12;
    /** Version information. */
    public static final byte DGT_VERSION        = 0x13;
    /** (Draughts) Black square board dump. */
    public static final byte DGT_BOARD_DUMP_50B = 0x14;
    /** (Draughts) White square board dump. */
    public static final byte DGT_BOARD_DUMP_50W = 0x15;
    /** Battery status. */
    public static final byte DGT_BATTERY_STATUS = 0x20;
    /** Board long serial number. */
    public static final byte DGT_LONG_SERIALNR  = 0x22;

    /* Piece codes for chess. */
    /** Empty square. */
    public static final byte EMPTY   = 0x00;
    /** White pawn. */
    public static final byte WPAWN   = 0x01;
    /** White rook. */
    public static final byte WROOK   = 0x02;
    /** White knight. */
    public static final byte WKNIGHT = 0x03;
    /** White bishop. */
    public static final byte WBISHOP = 0x04;
    /** White king. */
    public static final byte WKING   = 0x05;
    /** White queen. */
    public static final byte WQUEEN  = 0x06;
    /** Black pawn. */
    public static final byte BPAWN   = 0x07;
    /** Black rook. */
    public static final byte BROOK   = 0x08;
    /** Black knight. */
    public static final byte BKNIGHT = 0x09;
    /** Black bishop. */
    public static final byte BBISHOP = 0x0a;
    /** Black king. */
    public static final byte BKING   = 0x0b;
    /** Black queen. */
    public static final byte BQUEEN  = 0x0c;
    /** Special piece to signal draw. */
    public static final byte PIECE1  = 0x0d;  /* Magic piece: Draw */
    /** Special piece to signal white win. */
    public static final byte PIECE2  = 0x0e;  /* Magic piece: White win */
    /** Special piece to signal black win. */
    public static final byte PIECE3  = 0x0f;  /* Magic piece: Black win */

    /* Piece codes for draughts. */
    /** White draughts piece. */
    public static final byte WDISK  = 0x01;
    /** Black draughts piece. */
    public static final byte BDISK  = 0x04;
    /** White draughts crown. */
    public static final byte WCROWN = 0x07;
    /** Black draughts crown. */
    public static final byte BCROWN = 0x0a;

    /**
     * Converts DGT square code to the coordinate system used by the game code
     * in {@link org.riisholt.dgtdriver.game}.
     *
     * @param dgtCode A dgt square code from 0 to 63.
     * @return A square code in the coordinate system used by {@link
     * org.riisholt.dgtdriver.game.Board}
     * @throws DgtProtocolException If square code is outside of [0,63].
     */
    public static int dgtCodeToSquare(int dgtCode) throws DgtProtocolException {
        /* The DGT board numbers squares back to front, left to right, as
         * viewed by white. Thus A8 is 0, B8 is 1, A7 is8, and so on. */
        if(dgtCode >= 64 || dgtCode < 0)
            throw new DgtProtocolException(String.format("Invalid square code %d", dgtCode));
        int file = dgtCode % 8;
        int rank = 7 - (dgtCode / 8);
        return rank*8 + file;
    }

    /**
     * Converts a DGT piece code to game code. The DGT piece codes contain
     * both piece and colour in a single code, whereas the game code uses
     * separate variables for the role and colour attributes.
     *
     * @param dgtCode A DGT piece code.
     * @return Corresponding game code colour. True for white, false for black
     * @throws DgtProtocolException If the input code is invalid.
     */
    public static boolean dgtCodeToColor(int dgtCode) throws DgtProtocolException {
        if(dgtCode == EMPTY)
            return true; // XXX: Empty arbitrarily decreed to be white.
        else if(dgtCode >= WPAWN && dgtCode < BPAWN)
            return true;
        else if(dgtCode >= BPAWN && dgtCode < PIECE1)
            return false;
        // XXX: Make draws arbitratrily white, because we don't have null to work with.
        else if(dgtCode == PIECE1)
            return true;
        else if(dgtCode == PIECE2)
            return true;
        else if(dgtCode == PIECE3)
            return false;
        else
            throw new DgtProtocolException(String.format("Invalid piece code 0x%x", dgtCode));
    }

    /**
     * Converts a DGT piece code to game code role.
     *
     * @param dgtCode A DGT piece code.
     * @return Corresponding game code role
     * @throws DgtProtocolException If the input code is invalid.
     */
    public static Role dgtCodeToRole(int dgtCode) throws DgtProtocolException {
        switch(dgtCode) {
            case EMPTY:
                return null;
            case WPAWN:
            case BPAWN:
                return PAWN;
            case WKNIGHT:
            case BKNIGHT:
                return KNIGHT;
            case WBISHOP:
            case BBISHOP:
                return BISHOP;
            case WROOK:
            case BROOK:
                return ROOK;
            case WQUEEN:
            case BQUEEN:
                return QUEEN;
            case WKING:
            case BKING:
                return KING;
            // TODO: Handle PIECE1-3 for win/draw signaling.
            default:
                throw new DgtProtocolException(String.format("Invalid piece code %x", dgtCode));
        }
    }
}
