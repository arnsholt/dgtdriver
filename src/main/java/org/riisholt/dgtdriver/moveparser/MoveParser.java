package org.riisholt.dgtdriver.moveparser;

import org.riisholt.dgtdriver.*;
import org.riisholt.dgtdriver.game.*;

import java.util.*;

/**
 * <p>A class to parse raw DgtMessage events into a game of chess. This class
 * is similar in style to DgtDriver, but is intended to wrap it and rather
 * than emitting raw board events emit completed games as they are observed.
 * Game completion can be signalled both on the board, or programmatically:
 * on the board by placing both kings on the central four squares (on white
 * squares for a white win, black squares for a black win, and on opposite
 * colours for a draw), or programmatically by calling {@link #endGame()}.</p>
 *
 * <p>Of the various messages sent by the board, the parsing logic uses three
 * to keep track of games: {@link org.riisholt.dgtdriver.BoardDump},
 * {@link org.riisholt.dgtdriver.FieldUpdate}, and
 * {@link org.riisholt.dgtdriver.BWTime}. The first two to track the pieces on
 * the board, and the last to track the clock state. Since FieldUpdate
 * messages are impossible to interpret correctly without already knowing the
 * board state, it is required that a BoardDump message is received before any
 * FieldUpdate messages. A basic usage of this class should therefore look
 * like this:</p>
 *
 * <pre>
 * MoveParser.GameCallback gameCallback = ...; // Your game callback here.
 * MoveParser parser = new MoveParser(gameCallback);
 * DgtDriver driver = new DgtDriver(parser::gotMessage, writeCallback);
 * driver.reset();
 * driver.board();
 * driver.clock();
 * driver.updateNice();
 * // Write data from serial connection with driver.gotBytes(...);
 * </pre>
 *
 * @author Arne Skjærholt
 * @see Game
 * @see <a href="https://github.com/arnsholt/dgtpgn/">org.riisholt.dgtpgn</a>
 */
public class MoveParser {
    public interface GameCallback { void gameComplete(Game game); }

    private static Board initialPosition = new Board();
    private static Board rotatedInitialPosition;
    static {
        rotatedInitialPosition = new Board();
        rotatedInitialPosition.rotate180();
    }

    private GameCallback gameCallback;

    private Board boardState;
    private HashMap<ReachablePosition, ReachablePosition> positions;
    private boolean seenInitialPosition;
    private boolean rotate;
    private ReachablePosition lastReachable;

    /**
     * Class constructor.
     *
     * @param gameCallback The callback to invoke when a complete game has
     *                     been parsed.
     */
    public MoveParser(GameCallback gameCallback) {
        this.gameCallback = gameCallback;
        boardState = null;
        resetState();
    }

    private void resetState() {
        positions = new HashMap<>();
        seenInitialPosition = false;
        rotate = false;
        lastReachable = null;
    }

    /**
     * Handles a message from the board. If the message results in a board
     * state signalling a result (both kings in the central four squares), a
     * game is emitted to the gameCallback parameter supplied to the
     * constructor.
     *
     * @param msg The message received
     * @throws IllegalArgumentException if a FieldUpdate message is received
     *                                  before a BoardUpdate message.
     */
    public void gotMessage(DgtMessage msg) {
        if(msg instanceof BoardDump) {
            handleUpdate(((BoardDump) msg).board);
        }
        else if(msg instanceof FieldUpdate) {
            FieldUpdate update = (FieldUpdate) msg;
            if(boardState == null)
                throw new IllegalArgumentException("Got FieldUpdate message before initial BoardDump.");
            Board newState = new Board(boardState);
            int square = rotate?
                    // Rotation trick from https://www.chessprogramming.org/Flipping_Mirroring_and_Rotating#Rotationby180degrees
                    update.square ^ 63:
                    update.square;
            if(update.role == null) {
                if(newState.roleAt(square) == null) {
                    throw new RuntimeException("Piece removed from empty square.");
                }
                newState.discard(square);
            }
            else {
                newState.put(square, update.color, update.role);
            }
            handleUpdate(newState);
        }
        else if(msg instanceof  BWTime) {
            if(lastReachable != null) {
                if(rotate)
                    msg = ((BWTime) msg).rotate();
                lastReachable.timeInfo = (BWTime) msg;
            }
        }
    }

    private void handleUpdate(Board newState) {
        boardState = newState;
        if(boardState == null) return;

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
                boardState.rotate180();
                rotate = true;
            }
            return;
        }

        ReachablePosition p = new ReachablePosition(boardState, null, null);
        ReachablePosition reachable = positions.get(p);
        if(reachable != null) {
            addReachablePositions(reachable, positions);
            lastReachable = reachable;
        }
        else {
            Result result = boardState.resultSignal();
            if(result != null) {
                gameCallback.gameComplete(currentGame(result));
                resetState();
            }
        }
    }

    /**
     * Ends the game currently in progress, if any. Invoking this method
     * causes the gameComplete callback to be be invoked with the recorded
     * game and the recording state to be reset; if no moves have been
     * recorded, nothing happens. If a game is emitted, its {@link Game#result
     * result} member will be null.
     */
    public void endGame() {
        if(lastReachable != null) {
            gameCallback.gameComplete(currentGame(null));
            resetState();
        }
    }

    /**
     * Get the moves of the currently in progress game.
     *
     * @param result The result to attach to the output {@link Game}
     * @return A {@link Game} containing the in-progress game
     */
    public Game currentGame(Result result) {
        if(lastReachable == null) return new Game(new ArrayList<>(), null);

        ArrayList<PlayedMove> moves = new ArrayList<>();

        for(ReachablePosition reachable = lastReachable; reachable.from != null; reachable = reachable.from) {
            moves.add(0, new PlayedMove(
                    moveToSan(reachable),
                    reachable.timeInfo,
                    reachable.board,
                    reachable.via));
        }

        return new Game(moves, result);
    }

    private static String moveToSan(ReachablePosition r) {
        StringBuilder sb = new StringBuilder();
        // Castling is easy; we just have to check whether it's long or short.
        if(r.via.type == Move.CASTLING) {
            sb.append(
                    Square.file(r.via.to) == 7?
                        "O-O":
                        "O-O-O");
        }
        /* All non-castling moves have the format of possible prefix, a
         * literal "x" in the case of a capture, and finally the target
         * square. */
        else {
            if (r.via.role == Role.PAWN) {
                // For pawn captures, the prefix is the origin file.
                if (r.via.capture) {
                    sb.append(files[Square.file(r.via.from)]);
                }
            }
            else {
                /* Non-pawn moves always have a prefix. First is the piece
                 * code, then possibly a disambiguation of the origin square. */
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
                     * - Come from the same square (i.e. it's the *same* move as `r.via`).
                     */
                    if (m.to != r.via.to || m.role != r.via.role || m.promotion != r.via.promotion || m.from == r.via.from)
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

    private static class ReachablePosition {
        Board board;
        ReachablePosition from;
        Move via;
        BWTime timeInfo;

        ReachablePosition(Board b, ReachablePosition f, Move v) {
            board = b;
            from = f;
            via = v;
        }

        /* hashCode() can't use board.incrementalHash, since that includes the
         * turn member in the hash computation, which messes things up since we
         * don't track turn in the board setup. */
        public int hashCode() { return ZobristHash.hashPieces(board); }
        public boolean equals(Object o) {
            if(!(o instanceof ReachablePosition)) return false;
            return board.equalSetup(((ReachablePosition) o).board);
        }
    }
}
