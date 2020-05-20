package org.riisholt.dgtdriver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.time.Duration;

import org.riisholt.dgtdriver.game.Role;

/**
 * Moves transmitted from on-board EEPROM. DGT boards have a built-in EEPROM
 * (Electrically Erasable Programmable Read Only Memory, a kind of
 * non-volatile memory) which stores 8kB of data, allowing the recording of
 * games from a board after the fact, as long as the board was powered when
 * they were played. The memory is used as a ring buffer, so that incoming
 * data replaces the oldest data already written.
 *
 * @see <a href="https://en.wikipedia.org/wiki/EEPROM">EEPROM</a>
 * @see <a  href="https://en.wikipedia.org/wiki/Circular_buffer">Ring buffer</a>
 */
public class EEMoves implements DgtMessage {
    /**
     * When the board is powered, this tag is written (after three NOP tags).
     */
    public static final byte EE_POWERUP         = 0x6a;

    /**
     * This tag is always sent at the end of the event stream.
     */
    public static final byte EE_EOF             = 0x6b;

    /**
     * This tag is sent when the board detects that the board has eight
     * pieces on all fields of the first, second, seventh and eighth ranks,
     * but the position is <em>not</em> the canonical starting position. The
     * canonical starting position generates {@link EEMoves#EE_BEGINPOS} (or
     * {@link EEMoves#EE_BEGINPOS_ROT} if rotated).
     */
    public static final byte EE_FOURROWS        = 0x6c;

    /**
     * This tag is sent when the board is empty.
     */
    public static final byte EE_EMPTYBOARD      = 0x6d;

    /**
     * This tag marks the position of a previous download of the EEPROM.
     */
    public static final byte EE_DOWNLOADED      = 0x6e;

    /**
     * This tag is sent when the pieces on the board are in the starting position.
     */
    public static final byte EE_BEGINPOS        = 0x6f;

    /**
     * This tag is sent when the pieces on the board are in the starting
     * position, but rotated so that white is on G and H, and black on A and
     * B.
     */
    public static final byte EE_BEGINPOS_ROT    = 0x7a;

    /**
     * This tag is sent in the position marked by the bus-mode set start game
     * command (bus mode is not yet implemented by this library).
     */
    public static final byte EE_START_TAG       = 0x7b;

    /**
     * Boards equipped with a watchdog timer will have this tag written if the
     * timer fires, before the normal boot sequence.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Watchdog_timer">Watchdog timer</a>
     */
    public static final byte EE_WATCHDOG_ACTION = 0x7c;

    /**
     * Reserved for future use.
     */
    public static final byte EE_FUTURE_1        = 0x7d;

    /**
     * Reserved for future use.
     */
    public static final byte EE_FUTURE_2        = 0x7e;

    public static final byte EE_NOP             = 0x7f;
    public static final byte EE_NOP2            = 0x00;
    // 0x40 to 0x5f: two-byte field update message
    // 0x60-69 and 0x70-0x79: three byte clock message

    /**
     * The list of events received from the board.
     */
    public final List<EEEvent> events;

    /**
     * Parses a sequence of bytes received from the board into a sequence of
     * {@link EEEvent EEEvents}.
     *
     * @param data The bytes received from the board
     * @throws DgtProtocolException If a field or role code is invalid
     */
    public EEMoves(byte[] data) throws DgtProtocolException {
        ArrayList<EEEvent> events = new ArrayList<>(data.length);
        for(int i = 0; i < data.length;) {
            byte value = data[i];
            if((0x6a <= value && value <= 0x6f) || (0x7a <= value && value <= 07f) || value == 0x00) {
                events.add(new SimpleEvent(value));
                i++;
            }
            else if(0x40 <= value && value <= 0x5f) {
                /* Not sure if this is necessary, but in case we somehow get
                 * partial data at the end, break if we'd overflow.
                 */
                if(i + 1 >= data.length)
                    break;
                events.add(new FieldEvent((byte) (value & 0x0f), data[i+1]));
                i += 2;
            }
            else if((0x60 <= value && value <= 0x69) || (0x70 <= value && value <= 0x79)) {
                // As in the case above, break in case of partial trailing data
                if(i + 2 >= data.length)
                    break;
                events.add(new ClockEvent((value & 0x10) == 0x10, (byte) (value & 0x0f), data[i+1], data[i+2]));
                i += 3;
            }
            else {
                /* XXX: Unrecognized data. Increment to make sure we don't
                 * loop forever. Consider better thing to do here?
                 */
                i++;
            }
        }

        this.events = Collections.unmodifiableList(events);
    }

    /**
     * Visit the list of events with an {@link Visitor EEMoves.Visitor}
     * instance.
     *
     * @param visitor The visitor implementation.
     */
    public void visitEvents(Visitor visitor) {
        for(EEEvent e: events) {
            if(e instanceof SimpleEvent) {
                SimpleEvent simpleEvent = (SimpleEvent) e;
                switch(simpleEvent.type) {
                    case EE_POWERUP:
                        visitor.powerup();
                        break;
                    case EE_EOF:
                        visitor.eof();
                        break;
                    case EE_FOURROWS:
                        visitor.fourRows();
                        break;
                    case EE_EMPTYBOARD:
                        visitor.emptyBoard();
                        break;
                    case EE_DOWNLOADED:
                        visitor.downloaded();
                        break;
                    case EE_BEGINPOS:
                        visitor.initialPosition(false);
                        break;
                    case EE_BEGINPOS_ROT:
                        visitor.initialPosition(true);
                        break;
                    case EE_START_TAG:
                        visitor.startTag();
                        break;
                    case EE_WATCHDOG_ACTION:
                        visitor.watchdogAction();
                        break;
                    case EE_NOP:
                    case EE_NOP2:
                        // Do nothing for NOP codes.
                        break;
                    case EE_FUTURE_1:
                        visitor.future1();
                        break;
                    case EE_FUTURE_2:
                        visitor.future2();
                        break;
                    default:
                        throw new RuntimeException(String.format("Unknown simple event code 0x%x", simpleEvent.type));
                }
            }
            else if (e instanceof ClockEvent) {
                ClockEvent clockEvent = (ClockEvent) e;
                visitor.clockUpdate(clockEvent.time, clockEvent.isLeft);
            }
            else if(e instanceof FieldEvent) {
                FieldEvent fieldEvent = (FieldEvent) e;
                visitor.fieldUpdate(fieldEvent.square, fieldEvent.role);
            }
            else {
                throw new RuntimeException(String.format("Unknown event class %s", e.getClass().getName()));
            }
        }
    }

    /** Empty class to serve as common supertype for events from EEPROM.*/
    public static abstract class EEEvent {}

    /**
     * Instances of this class are used to represent events corresponding to
     * the {@code EE_*} byte constants in {@link EEMoves}.
     */
    public static class SimpleEvent extends EEEvent {
        public final byte type;

        SimpleEvent(byte type) {
            this.type = type;
        }
    }

    /** Piece placed on or removed from a field. */
    public static class FieldEvent extends EEEvent {
        /**
         * The field updated. Ranges from 0 to 63, where 0 is A1 and 63 is H8,
         * the same coordinates as used in {@link org.riisholt.dgtdriver.game}
          */
        public final int square;

        /**
         * The kind of piece placed on the field, or {@code null} if a piece
         * was removed.
         */
        public final Role role;

        FieldEvent(byte piece, byte field) throws DgtProtocolException {
            square = DgtConstants.dgtCodeToSquare(field);
            role = DgtConstants.dgtCodeToRole(piece);
        }
    }

    /**
     * Clock update. Note that, unlike {@link BWTime} this event only contains
     * an updated time for the changed side of the clock, not both, and only
     * the time and nothing else.
     */
    public static class ClockEvent extends EEEvent {
        /** The new time left on the clock. */
        public final Duration time;

        /** {@code true} if the update applies to the left-hand side. */
        public final boolean isLeft;

        ClockEvent(boolean isLeft, byte hours, byte minutes, byte seconds) {
            this.isLeft = isLeft;
            this.time = Duration.ZERO.plusHours(hours)
                                     .plusMinutes(minutes)
                                     .plusSeconds(seconds);
        }
    }

    /**
     * <p>Visitor class for use with {@link EEMoves#visitEvents(Visitor)} with
     * empty implementations for all the methods.</p>
     *
     * <p>The methods {@link #fieldUpdate(int, Role)} and
     * {@link #clockUpdate(Duration, boolean)} are called when visiting
     * {@link FieldEvent} and {@link ClockEvent} events, respectively, while
     * the rest correspond to {@link SimpleEvent SimpleEvents} containing the
     * relevant {@code EE_*} event code; note however that
     * {@link EEMoves#EE_BEGINPOS} and {@link EEMoves#EE_BEGINPOS_ROT} have
     * been merged to a single method {@link #initialPosition(boolean)}, with
     * the parameter set to {@code true} in the rotated case.</p>
     */
    public static class Visitor {
        public void fieldUpdate(int square, Role role) {}
        public void clockUpdate(Duration timeLeft, boolean isLeft) {}
        public void powerup() {}
        public void eof() {}
        public void fourRows() {}
        public void emptyBoard() {}
        public void downloaded() {}
        public void initialPosition(boolean rotated) {}
        public void startTag() {}
        public void watchdogAction() {}
        public void future1() {}
        public void future2 () {}
    }
}
