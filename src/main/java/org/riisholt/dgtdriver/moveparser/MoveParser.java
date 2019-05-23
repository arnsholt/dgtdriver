package org.riisholt.dgtdriver.moveparser;

import org.riisholt.dgtdriver.*;
import org.riisholt.dgtdriver.game.*;

import java.util.*;

public class MoveParser {
    public interface GameCallback { void gameComplete(Game game); }

    private static Board initialPosition = new Board();
    private static Board rotatedInitialPosition;
    static {
        rotatedInitialPosition = new Board();
        rotatedInitialPosition.rotate180();
    }

    private GameCallback gameCallback;

    private Board state;
    private HashMap<ReachablePosition, ReachablePosition> positions;
    private boolean seenInitialPosition;
    private boolean rotate;
    private ReachablePosition lastReachable;

    public MoveParser(GameCallback gameCallback) {
        this.gameCallback = gameCallback;
        resetState();
    }

    private void resetState() {
        state = null;
        positions = new HashMap<>();
        seenInitialPosition = false;
        rotate = false;
        lastReachable = null;
    }

    public void gotMove(DgtMessage msg) {
        Board newState;
        if(msg instanceof BoardDump) {
            newState = ((BoardDump) msg).board();
        }
        else if(msg instanceof FieldUpdate) {
            FieldUpdate update = (FieldUpdate) msg;
            if(state == null)
                throw new IllegalArgumentException("Got FieldUpdate message before initial BoardDump.");
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
            if(lastReachable != null) {
                if(rotate)
                    ((BWTime) msg).rotate();
                lastReachable.timeInfo = (BWTime) msg;
            }
            return;
        }
        else {
            throw new RuntimeException(String.format("Unhandled message type: %s", msg.getClass().getSimpleName()));
        }

        state = newState;
        if(state == null) return;

        if(!seenInitialPosition) {
            if(newState.equalSetup(initialPosition)) {
                seenInitialPosition = true;
                lastReachable = new ReachablePosition(initialPosition, null, null);
                positions.put(lastReachable, lastReachable);
                addReachablePositions(lastReachable, positions);
            }
            else if(newState.equalSetup(rotatedInitialPosition)) {
                seenInitialPosition = true;
                lastReachable = new ReachablePosition(initialPosition, null, null);
                positions.put(lastReachable, lastReachable);
                addReachablePositions(lastReachable, positions);
                state.rotate180();
                rotate = true;
            }
            return;
        }

        ReachablePosition p = new ReachablePosition(state, null, null);
        ReachablePosition reachable = positions.get(p);
        if(reachable != null) {
            addReachablePositions(reachable, positions);
            lastReachable = reachable;
        }
        else {
            Result result = state.resultSignal();
            if(result != null) {
                gameCallback.gameComplete(currentGame(result));
                resetState();
            }
        }
    }

    public void close() {
        gameCallback.gameComplete(currentGame(null));
        resetState();
    }

    private Game currentGame(Result result) {
        Game game = new Game();
        game.moves = new ArrayList<>();
        game.result = result;
        if(lastReachable == null) return game;

        for(ReachablePosition reachable = lastReachable; reachable.from != null; reachable = reachable.from) {
            game.moves.add(0, new PlayedMove(reachable.via.uci(), moveToSan(reachable), reachable.timeInfo));
        }
        return game;
    }

    private static String moveToSan(ReachablePosition r) {
        StringBuilder sb = new StringBuilder();
        if(r.via.type == Move.CASTLING) {
            sb.append(
                    Square.file(r.via.to) == 7?
                        "O-O":
                        "O-O-O");
        }
        else {
            if (r.via.role == Role.PAWN) {
                if (r.via.capture) {
                    sb.append(files[Square.file(r.via.from)]);
                }
            }
            else {
                sb.append(r.via.role.symbol);

                /* Disambiguate the origin square if necessary.
                 *
                 * The disambiguation code has been adapted wholesale from
                 * shakmaty's San::disambiguate (https://github.com/niklasf/shakmaty/blob/master/src/san.rs).
                 *
                 * The relevant moves for disambiguation are the legal moves
                 * from the previous position that move the same piece type to
                 * the same square as the move played to reach the position.
                 * Not sure if the check for promotion is strictly necessary
                 * (since pawn moves are filtered out already), but shakmaty
                 * has the check, so better safe than sorry.
                 */
                MoveList moves = new MoveList();
                r.from.board.legalMoves(moves);
                boolean rank = false;
                boolean file = false;
                for (Move m : moves) {
                    /* We ignore moves that:
                     * - Have a different destination
                     * - Move a different piece
                     * - Promote differently (XXX: should be extraneous?)
                     */
                    if (m.to != r.via.to || m.role != r.via.role || m.promotion != r.via.promotion)
                        continue;
                    // - Come from the same square (i.e. it's the *same* move as `r.via`).
                    if (m.from == r.via.from)
                        continue;

                    if (Square.rank(m.from) == Square.rank(r.via.from) || Square.file(m.from) != Square.file(r.via.from)) {
                        file = true;
                    }
                    else {
                        rank = true;
                    }
                }
                if(file)
                    sb.append(files[Square.file(r.via.from)]);
                if(rank)
                    sb.append(ranks[Square.rank(r.via.from)]);
            }

            if (r.via.capture)
                sb.append('x');

            sb.append(squareString(r.via.to));
        }

        // Any move can be check or checkmate, so we add that last.
        if(r.board.isCheck()) {
            MoveList moves = new MoveList();
            r.board.legalMoves(moves);
            if(moves.size() > 0)
                sb.append('+');
            else
                sb.append('#');
        }

        return sb.toString();
    }

    private static String[] ranks = {"1", "2", "3", "4", "5", "6", "7", "8"};
    private static String[] files = {"a", "b", "c", "d", "e", "f", "g", "h"};
    private static String squareString(int square){
        return files[Square.file(square)] + ranks[Square.rank(square)];
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
    BWTime timeInfo;

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
        if(!(o instanceof ReachablePosition)) return false;
        return board.equalSetup(((ReachablePosition) o).board);
    }
}
