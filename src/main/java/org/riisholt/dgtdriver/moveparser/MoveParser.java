package org.riisholt.dgtdriver.moveparser;

import org.riisholt.dgtdriver.*;

import java.util.*;

public class MoveParser {
    public static List<Move> parseMoves(List<DgtMessage> msgs) {
        /*
        * XXX: Implementing this logic probably needs to be done i several
        * smaller bites, since it's quite complicated. I think a general
        * progression should be something like:
        *
        * 1: Assume pieces are arranged with white pieces on first and second
        * ranks (by the board's reckoning), no extra setup needed. Ignore the
        * clock initially. Assume recording ends in the final position.
        *
        * 2: Assume pieces are correctly arranged in the initial state, but
        * allow for white to be on either end of the board.
        *
        * 3: Allow for some initial piece shuffling before an initial position
        * (normal or flipped) before play starts. May be as simple as scanning
        * the list of board states for initial positions and trying to parse
        * from all of them?
        *
        * 4: Allow for piece shuffling at the end of the game, and parse
        * king placements to read results.
        *
        * 5: Handle the clock, tagging moves with clock times.
        *
        * Clock handling may or may not be convenient to handle somewhere
        * other than way at the end.
        *
        * Decoding will probably initially assume that there's a unique path
        * from the final position to the initial position. However repeated
        * positions are likely to break that assumption and has to be handled.
        * Some kind of tie-break when a position is reachable from several
        * other positions? The Carlsen-Karjakin "Through the Rapids" game may
        * be a suitable initial test.
        */

        Board state = null;
        ArrayList<Board> states = new ArrayList<>();
        HashMap<Board, List<Integer>> boardToIndex = new HashMap<>();
        for(DgtMessage msg: msgs) {
            if(msg.getClass() == BoardDump.class) {
                state = ((BoardDump) msg).board();
                states.add(new Board(state));
                System.out.format("Board dump.\n%d:\n%s\n", states.size() - 1, states.get(states.size() - 1).debugBoard());
            }
            else if(msg.getClass() == FieldUpdate.class) {
                FieldUpdate u = (FieldUpdate) msg;
                if(u.role() == null) {
                    if(state.roleAt(u.square()) == null)
                        throw new RuntimeException("Piece removed from empty square.");
                    state.discard(u.square());
                }
                else {
                    if(state.roleAt(u.square()) != null)
                        // If a piece is captured in between scans, we only get a message placing the capturing piece.
                        state.discard(u.square());
                        //throw new RuntimeException("Piece placed on non-empty square.");
                    state.put(u.square(), u.color(), u.role());
                }
                Board newState = new Board(state);
                states.add(newState);
                if(!boardToIndex.containsKey(newState))
                    boardToIndex.put(newState, new ArrayList<>());
                boardToIndex.get(newState).add(states.size() - 1);
                System.out.format("Field update %s@%d.\n%d:\n%s\n", u.role(), u.square(), states.size() - 1, states.get(states.size() - 1).debugBoard());
            }
            else if(msg.getClass() == BWTime.class) {
                // TODO
            }
            else {
                throw new RuntimeException(String.format("Unhandled message type %s", msg.getClass().getSimpleName()));
            }
        }

        if(!states.get(0).equals(new Board())) {
            throw new RuntimeException("First state is not starting position.");
        }

        /* These two arrays could be a single 2D array, but it's easier to
         * fill two separate arrasy with Arrays.fill(). The arrays are of the
         * same length as our list of observed positions, and a value j >= 0
         * at position i indicates that the position is reachable from
         * position j, with white to play or black to play depending on the
         * array. whiteTrellis[0] is special-cased to also be 0.
         */
        int[] whiteTrellis = new int[states.size()];
        int[] blackTrellis = new int[states.size()];
        Arrays.fill(whiteTrellis, -1);
        Arrays.fill(blackTrellis, -1);
        whiteTrellis[0] = 0;
        MoveList legalMoves = new MoveList();
        for(int i = 0; i < states.size(); i++) {
            Board cur = states.get(i);
            if(whiteTrellis[i] >= 0) {
                cur.legalMoves(legalMoves, true);
                for (Move m : legalMoves) {
                    Board newState = new Board(cur);
                    newState.play(m, true);
                    for(int j: boardToIndex.getOrDefault(newState, Collections.emptyList())) {
                        if(j < i) continue; // Ignore backwards possibilities.
                        if(blackTrellis[j] >= 0)
                            throw new RuntimeException(String.format("Position %d already reachable from %d", j , blackTrellis[j]));
                        blackTrellis[j] = i;
                    }
                }
            }

            if(blackTrellis[i] >= 0) {
                cur.legalMoves(legalMoves, false);
                for (Move m : legalMoves) {
                    Board newState = new Board(cur);
                    newState.play(m, false);
                    for(int j: boardToIndex.getOrDefault(newState, Collections.emptyList())) {
                        if(j < i) continue; // Ignore backwards possibilities.
                        if(whiteTrellis[j] >= 0)
                            throw new RuntimeException(String.format("Position %d already reachable from %d", j , whiteTrellis[j]));
                        whiteTrellis[j] = i;
                    }
                }
            }
        }

        if(whiteTrellis[whiteTrellis.length - 1] == -1 && blackTrellis[blackTrellis.length - 1] == -1) {
            int whiteLast = lastNonNeg(whiteTrellis);
            int blackLast = lastNonNeg(blackTrellis);
            throw new RuntimeException(String.format("No path to end. Last white to move %d, black to move %d", whiteLast, blackLast));
        }

        ArrayList<Move> moves = new ArrayList<>();
        for(Board cur: states) {
            Move m = new Move();
            m.type = Move.NORMAL;
            m.from = 12;
            m.to = 28;
            moves.add(m);
        }

        return moves;
    }

    private static int lastNonNeg(int[] a) {
        for(int i = a.length - 1; i >= 0; i--) {
            if(a[i] >= 0)
                return i;
        }
        return -1;
    }
}
