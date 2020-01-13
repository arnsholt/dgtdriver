package org.riisholt.dgtdriver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.time.Duration;

import org.riisholt.dgtdriver.game.Role;

public class EEMoves implements DgtMessage {
    public static final byte EE_POWERUP         = 0x6a;
    public static final byte EE_EOF             = 0x6b;
    public static final byte EE_FOURROWS        = 0x6c;
    public static final byte EE_EMPTYBOARD      = 0x6d;
    public static final byte EE_DOWNLOADED      = 0x6e;
    public static final byte EE_BEGINPOS        = 0x6f;
    public static final byte EE_BEGINPOS_ROT    = 0x7a;
    public static final byte EE_START_TAG       = 0x7b;
    public static final byte EE_WATCHDOG_ACTION = 0x7c;
    public static final byte EE_FUTURE_1        = 0x7d;
    public static final byte EE_FUTURE_2        = 0x7e;
    public static final byte EE_NOP             = 0x7f;
    public static final byte EE_NOP2            = 0x00;
    // 0x40 to 0x5f: two-byte field update message
    // 0x60-69 and 0x70-0x79: three byte clock message

    public final List<EEEvent> events;

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

    public static abstract class EEEvent {}

    public static class SimpleEvent extends EEEvent {
        public final byte type;

        SimpleEvent(byte type) {
            this.type = type;
        }
    }

    public static class FieldEvent extends EEEvent {
        public final int square;
        public final Role role;

        FieldEvent(byte piece, byte field) throws DgtProtocolException {
            square = DgtConstants.dgtCodeToSquare(field);
            role = DgtConstants.dgtCodeToRole(piece);
        }
    }

    public static class ClockEvent extends EEEvent {
        public final boolean isLeft;
        public final Duration time;

        ClockEvent(boolean isLeft, byte hours, byte minutes, byte seconds) {
            this.isLeft = isLeft;
            this.time = Duration.ZERO.plusHours(hours)
                                     .plusMinutes(minutes)
                                     .plusSeconds(seconds);
        }
    }
}
