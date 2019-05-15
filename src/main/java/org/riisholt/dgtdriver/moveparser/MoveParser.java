package org.riisholt.dgtdriver.moveparser;

import org.riisholt.dgtdriver.*;

import java.util.*;

public class MoveParser {
    public static List<Move> parseMoves(List<DgtMessage> msgs) {
        Board initialPosition = new Board();
        Board rotatedInitialPosition = new Board(initialPosition);
        rotatedInitialPosition.rotate180();

        Board state = null;
        HashMap<ReachablePosition, ReachablePosition> positions = new HashMap<>();
        boolean seenInitialPosition = false;
        boolean rotate = false;
        ReachablePosition lastReachable = null;

        for(DgtMessage msg: msgs) {
            Board newState = null;
            if(msg instanceof BoardDump) {
                newState = ((BoardDump) msg).board();
            }
            else if(msg instanceof FieldUpdate) {
                FieldUpdate update = (FieldUpdate) msg;
                newState = new Board(state);
                int square = rotate?
                        // Rotation trick from https://www.chessprogramming.org/Flipping_Mirroring_and_Rotating#Rotationby180degrees
                        update.square() ^ 63:
                        update.square();
                if(update.role() == null) {
                    if(newState.roleAt(square) == null) {
                        throw new RuntimeException("Piece removed from empty square.");
                    }
                    newState.discard(square);
                }
                else {
                    newState.put(square, update.color(), update.role());
                }
            }
            else if(msg instanceof  BWTime) {
                // TODO
                continue;
            }
            else {
                throw new RuntimeException(String.format("Unhandled message type: %s", msg.getClass().getSimpleName()));
            }

            state = newState;
            if(state == null) continue;

            if(!seenInitialPosition) {
                if(ReachablePosition.samePosition(newState, initialPosition)) {
                    seenInitialPosition = true;
                    lastReachable = new ReachablePosition(initialPosition, null, null);
                    positions.put(lastReachable, lastReachable);
                    addReachablePositions(lastReachable, positions);
                }
                else if(ReachablePosition.samePosition(newState, rotatedInitialPosition)) {
                    seenInitialPosition = true;
                    lastReachable = new ReachablePosition(initialPosition, null, null);
                    positions.put(lastReachable, lastReachable);
                    addReachablePositions(lastReachable, positions);
                    state.rotate180();
                    rotate = true;
                }
                continue;
            }

            ReachablePosition p = new ReachablePosition(state, null, null);
            ReachablePosition reachable = positions.get(p);
            if(reachable == null) continue;
            addReachablePositions(reachable, positions);
            lastReachable = reachable;
        }

        /* XXX: Using an ArrayList here is probably not optimal. Depending on
         * exactly how ArrayList works it can be kinda bad or not too bad, but
         * either using building the ArrayList backwards and reversing it or
         * just using a LinkedList is probably better. */
        List<Move> moves = new ArrayList<>();
        if(lastReachable == null) return moves;

        for(ReachablePosition reachable = lastReachable; reachable.from != null; reachable = reachable.from) {
            moves.add(0, reachable.via);
        }

        return moves;
    }

    private static void addReachablePositions(ReachablePosition from, Map<ReachablePosition, ReachablePosition> positions) {
        MoveList moves = new MoveList();
        from.board.legalMoves(moves);
        for(Move m: moves) {
            Board newBoard = new Board(from.board);
            newBoard.play(m);
            ReachablePosition reachable = new ReachablePosition(newBoard, from, m);
            positions.put(reachable, reachable);
        }
    }
}

class ReachablePosition {
    Board board;
    ReachablePosition from;
    Move via;

    ReachablePosition(Board b, ReachablePosition f, Move v) {
        board = b;
        from = f;
        via = v;
    }

    /* hashCode() can't use board.incrementalHash, since that includes a the
     * turn member in the hash computation, which messes things up since we
     * don't track turn in the board setup setup. */
    public int hashCode() { return ZobristHash.hashPieces(board); }
    public boolean equals(Object o) {
        return samePosition(board, ((ReachablePosition) o).board);
    }


    static boolean samePosition(Board a, Board b) {
        return a.pawns == b.pawns
            && a.knights == b.knights
            && a.bishops == b.bishops
            && a.rooks == b.rooks
            && a.queens == b.queens
            && a.kings == b.kings
            && a.white == b.white
            && a.black == b.black
            && a.occupied == b.occupied;
    }
}
