package org.riisholt.dgtdriver.moveparser;

import org.riisholt.dgtdriver.*;

import java.util.*;

public class MoveParser {
    public static List<Move> parseMoves(List<DgtMessage> msgs) {
        Board state = null;
        OrderedPositionSet positions = new OrderedPositionSet();
        Board initialPosition = new Board();
        boolean seenInitialPosition = false;
        ReachablePosition lastReachable = null;
        for(DgtMessage msg: msgs) {
            Board newState = null;
            if(msg instanceof BoardDump) {
                newState = ((BoardDump) msg).board();
            }
            else if(msg instanceof FieldUpdate) {
                FieldUpdate update = (FieldUpdate) msg;
                newState = new Board(state);
                if(update.role() == null) {
                    if(newState.roleAt(update.square()) == null) {
                        throw new RuntimeException("Piece removed from empty square.");
                    }
                    newState.discard(update.square());
                }
                else {
                    newState.put(update.square(), update.color(), update.role());
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
                if(BoardSetupComparator.cmp.compare(newState, initialPosition) == 0) {
                    seenInitialPosition = true;
                    lastReachable = new ReachablePosition(initialPosition, null, null);
                    positions.add(lastReachable);
                    addReachablePositions(lastReachable, positions);
                }
                continue;
            }

            ReachablePosition reachable = positions.get(state);
            if(reachable == null) continue;
            addReachablePositions(reachable, positions);
            lastReachable = reachable;
        }

        List<Move> moves = new ArrayList<>();
        for(ReachablePosition reachable = lastReachable; reachable != null; reachable = reachable.from) {
            moves.add(0, reachable.via);
        }
        moves.remove(0);

        return moves;
    }

    private static void addReachablePositions(ReachablePosition from, OrderedPositionSet positions) {
        MoveList moves = new MoveList();
        from.board.legalMoves(moves);
        for(Move m: moves) {
            Board newBoard = new Board(from.board);
            newBoard.play(m);
            positions.add(new ReachablePosition(newBoard, from, m));
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
}

class OrderedPositionSet {
    static class Node {
        ReachablePosition position;
        Node left, right;

        Node(ReachablePosition b) {
            position = b;
        }

        void add(ReachablePosition brd) {
            int c = BoardSetupComparator.cmp.compare(brd.board, position.board);
            if(c == 0) {
                position = brd;
            }
            else if(c < 0) {
                if(left == null) {
                    left = new Node(brd);
                }
                else {
                    left.add(brd);
                }
            }
            else {
                if(right == null) {
                    right = new Node(brd);
                }
                else {
                    right.add(brd);
                }
            }
        }

        ReachablePosition get(Board b) {
            int c = BoardSetupComparator.cmp.compare(b, position.board);
            if(c == 0) {
                return position;
            }
            else if(c < 0) {
                return left == null?
                        null:
                        left.get(b);
            }
            else {
                return right == null?
                        null:
                        right.get(b);
            }
        }
    }

    Node root;

    void add(ReachablePosition b) {
        if(root == null) {
            root = new Node(b);
        }
        else {
            root.add(b);
        }
    }

    ReachablePosition get(Board b) {
        return root == null?
                null:
                root.get(b);
    }
}

class BoardSetupComparator implements Comparator<Board> {
    static BoardSetupComparator cmp = new BoardSetupComparator();
    public int compare(Board a, Board b) {
        return Comparator.comparingLong((Board pos) -> pos.pawns)
                .thenComparingLong((Board pos) -> pos.knights)
                .thenComparingLong((Board pos) -> pos.bishops)
                .thenComparingLong((Board pos) -> pos.rooks)
                .thenComparingLong((Board pos) -> pos.queens)
                .thenComparingLong((Board pos) -> pos.kings)
                .thenComparingLong((Board pos) -> pos.white)
                .thenComparingLong((Board pos) -> pos.black)
                .thenComparingLong((Board pos) -> pos.occupied)
                .compare(a, b);
    }
}
