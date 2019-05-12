package org.riisholt.dgtdriver.moveparser;

import java.util.Map;
import java.util.HashMap;

public final class Board {
    long pawns;
    long knights;
    long bishops;
    long rooks;
    long queens;
    long kings;

    long white;
    long black;
    long occupied;

    //boolean turn;
    int epSquare;
    //long castlingRights;

    int incrementalHash;

    public Board() {
        this.pawns = 0xff00000000ff00L;
        this.knights = 0x4200000000000042L;
        this.bishops = 0x2400000000000024L;
        this.rooks = 0x8100000000000081L;
        this.queens = 0x800000000000008L;
        this.kings = 0x1000000000000010L;

        this.white = 0xffffL;
        this.black = 0xffff000000000000L;
        this.occupied = 0xffff00000000ffffL;

        //this.turn = true;
        this.epSquare = 0;
        //this.castlingRights = this.rooks;

        this.incrementalHash = ZobristHash.hashPieces(this);// ^ ZobristHash.hashTurn(this);
    }

    public Board(Board board) {
        this.pawns = board.pawns;
        this.knights = board.knights;
        this.bishops = board.bishops;
        this.rooks = board.rooks;
        this.queens = board.queens;
        this.kings = board.kings;

        this.white = board.white;
        this.black = board.black;
        this.occupied = board.occupied;

        //this.turn = board.turn;
        this.epSquare = board.epSquare;
        //this.castlingRights = board.castlingRights;

        this.incrementalHash = ZobristHash.hashPieces(this);// ^ ZobristHash.hashTurn(this);
    }

    Board(long pawns, long knights, long bishops, long rooks, long queens, long kings,
          long white, long black,
          int epSquare, long castlingRights) {
          //boolean turn, int epSquare, long castlingRights) {

        this.pawns = pawns;
        this.knights = knights;
        this.bishops = bishops;
        this.rooks = rooks;
        this.queens = queens;
        this.kings = kings;

        this.white = white;
        this.black = black;
        this.occupied = white | black;

        //this.turn = turn;
        this.epSquare = epSquare;
        //this.castlingRights = castlingRights;

        this.incrementalHash = ZobristHash.hashPieces(this);// ^ ZobristHash.hashTurn(this);
    }

    private long castlingRights() {
        long blackInitialRooks = 0x8100000000000000L;
        long whiteInitialRooks = 0x0000000000000081L;
        long blackInitialKing = 0x1000000000000000L;
        long whiteInitialKing = 0x0000000000000010L;

        /* Castling rights for each side are the rooks on their initial
         * squares, if the king is still on its initial square.
         */
        long blackCastle = (blackInitialKing & kings & black) != 0L?
                blackInitialRooks & rooks & black:
                0L;
        long whiteCastle = (whiteInitialKing & kings & white) != 0L?
                whiteInitialRooks & rooks & white:
                0L;

        return blackCastle | whiteCastle;
    }

    public static Board emptyBoard() {
        return new Board(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    private boolean isOccupied(int square) {
        return Bitboard.contains(this.occupied, square);
    }

    void discard(int square) {
        if (!isOccupied(square)) return;
        Role role = roleAt(square);
        long mask = 1L << square;

        switch (role) {
            case PAWN: this.pawns ^= mask; break;
            case KNIGHT: this.knights ^= mask; break;
            case BISHOP: this.bishops ^= mask; break;
            case ROOK: this.rooks ^= mask; break;
            case QUEEN: this.queens ^= mask; break;
            case KING: this.kings ^= mask; break;
        }

        boolean color = whiteAt(square);
        if (color) this.white ^= mask;
        else this.black ^= mask;

        this.occupied ^= mask;
        this.incrementalHash ^= ZobristHash.hashPiece(square, color, role);
    }

    public void put(int square, boolean color, Role role) {
        discard(square);

        long mask = 1L << square;

        switch (role) {
            case PAWN: this.pawns ^= mask; break;
            case KNIGHT: this.knights ^= mask; break;
            case BISHOP: this.bishops ^= mask; break;
            case ROOK: this.rooks ^= mask; break;
            case QUEEN: this.queens ^= mask; break;
            case KING: this.kings ^= mask; break;
        }

        if (color) this.white ^= mask;
        else this.black ^= mask;

        this.occupied ^= mask;
        this.incrementalHash ^= ZobristHash.hashPiece(square, color, role);
    }

    public Role roleAt(int square) {
        if (Bitboard.contains(this.pawns, square)) return Role.PAWN;
        if (Bitboard.contains(this.knights, square)) return Role.KNIGHT;
        if (Bitboard.contains(this.bishops, square)) return Role.BISHOP;
        if (Bitboard.contains(this.rooks, square)) return Role.ROOK;
        if (Bitboard.contains(this.queens, square)) return Role.QUEEN;
        if (Bitboard.contains(this.kings, square)) return Role.KING;
        return null;
    }

    public boolean whiteAt(int square) {
        return Bitboard.contains(this.white, square);
    }

    public int zobristHash() {
        return this.incrementalHash;// ^ ZobristHash.hashCastling(this) ^ ZobristHash.hashEnPassant(this);
    }

    public Map<Integer, Piece> pieceMap() {
        HashMap<Integer, Piece> map = new HashMap<Integer, Piece>();
        long occupied = this.occupied;
        while (occupied != 0) {
            int sq = Bitboard.lsb(occupied);
            map.put(sq, new Piece(whiteAt(sq), roleAt(sq)));
            occupied &= occupied - 1L;
        }
        return map;
    }

    public void play(Move move, boolean turn) {
        this.epSquare = 0;

        switch (move.type) {
            case Move.NORMAL:
                if (move.role == Role.PAWN && Math.abs(move.from - move.to) == 16) {
                    long theirPawns = them(turn) & this.pawns;
                    if (theirPawns != 0) {
                        int sq = move.from + (turn ? 8 : -8);
                        if ((Bitboard.pawnAttacks(turn, sq) & theirPawns) != 0) {
                            this.epSquare = sq;
                        }
                    }
                }

                /*if (this.castlingRights != 0) {
                    if (move.role == Role.KING) {
                        this.castlingRights &= Bitboard.RANKS[turn ? 7 : 0];
                    } else if (move.role == Role.ROOK) {
                        this.castlingRights &= ~(1L << move.from);
                    }

                    if (move.capture) {
                        this.castlingRights &= ~(1L << move.to);
                    }
                }*/

                discard(move.from);
                put(move.to, turn, move.promotion != null ? move.promotion : move.role);
                break;

            case Move.CASTLING:
                //this.castlingRights &= Bitboard.RANKS[turn ? 7 : 0];
                int rookTo = Square.combine(move.to < move.from ? Square.D1 : Square.F1, move.to);
                int kingTo = Square.combine(move.to < move.from ? Square.C1 : Square.G1, move.from);
                discard(move.from);
                discard(move.to);
                put(rookTo, turn, Role.ROOK);
                put(kingTo, turn, Role.KING);
                break;

            case Move.EN_PASSANT:
                discard(Square.combine(move.to, move.from));
                discard(move.from);
                put(move.to, turn, Role.PAWN);
                break;
        }

        this.incrementalHash ^= ZobristHash.POLYGLOT[780];
    }

    long us(boolean turn) {
        return byColor(turn);
    }

    long them(boolean turn) {
        return byColor(!turn);
    }

    long byColor(boolean white) {
        return white ? this.white : this.black;
    }

    private int king(boolean white) {
        return Bitboard.lsb(this.kings & byColor(white));
    }

    private long sliderBlockers(int king, boolean turn) {
        long snipers = them(turn) & (
            Bitboard.rookAttacks(king, 0) & (this.rooks ^ this.queens) |
            Bitboard.bishopAttacks(king, 0) & (this.bishops ^ this.queens));

        long blockers = 0;

        while (snipers != 0) {
            int sniper = Bitboard.lsb(snipers);
            long between = Bitboard.BETWEEN[king][sniper] & this.occupied;
            if (!Bitboard.moreThanOne(between)) blockers |= between;
            snipers &= snipers - 1L;
        }

        return blockers;
    }

    public boolean isCheck(boolean turn) {
        return attacksTo(king(turn), !turn) != 0;
    }

    private long attacksTo(int sq, boolean attacker) {
        return attacksTo(sq, attacker, this.occupied);
    }

    private long attacksTo(int sq, boolean attacker, long occupied) {
        return byColor(attacker) & (
            Bitboard.rookAttacks(sq, occupied) & (this.rooks ^ this.queens) |
            Bitboard.bishopAttacks(sq, occupied) & (this.bishops ^ this.queens) |
            Bitboard.KNIGHT_ATTACKS[sq] & this.knights |
            Bitboard.KING_ATTACKS[sq] & this.kings |
            Bitboard.pawnAttacks(!attacker, sq) & this.pawns);
    }

    public void legalMoves(MoveList moves, boolean turn) {
        moves.clear();

        if (this.epSquare != 0) {
            genEnPassant(turn, moves);
        }

        int king = king(turn);
        long checkers = attacksTo(king, !turn);
        if (checkers == 0) {
            long target = ~us(turn);
            genNonKing(target, turn, moves);
            genSafeKing(king, target, turn, moves);
            genCastling(king, turn, moves);
        } else {
            genEvasions(king, checkers, turn, moves);
        }

        long blockers = sliderBlockers(king, turn);
        if (blockers != 0 || this.epSquare != 0) {
            moves.retain(m -> isSafe(king, m, blockers, turn));
        }
    }

    public boolean hasLegalEnPassant(boolean turn) {
        // Like legalMoves(), but generate only en passant captures to see if
        // there are any legal en passant moves in the position.

        if (this.epSquare == 0) return false; // shortcut

        MoveList moves = new MoveList(2);
        genEnPassant(turn, moves);

        int king = king(turn);
        long blockers = sliderBlockers(king, turn);
        return moves.anyMatch(m -> isSafe(king, m, blockers, turn));
    }

    private void genNonKing(long mask, boolean turn, MoveList moves) {
        genPawn(mask, turn, moves);

        // Knights.
        long knights = us(turn) & this.knights;
        while (knights != 0) {
            int from = Bitboard.lsb(knights);
            long targets = Bitboard.KNIGHT_ATTACKS[from] & mask;
            while (targets != 0) {
                int to = Bitboard.lsb(targets);
                moves.pushNormal(this, Role.KNIGHT, from, isOccupied(to), to);
                targets &= targets - 1L;
            }
            knights &= knights - 1L;
        }

        // Bishops.
        long bishops = us(turn) & this.bishops;
        while (bishops != 0) {
            int from = Bitboard.lsb(bishops);
            long targets = Bitboard.bishopAttacks(from, this.occupied) & mask;
            while (targets != 0) {
                int to = Bitboard.lsb(targets);
                moves.pushNormal(this, Role.BISHOP, from, isOccupied(to), to);
                targets &= targets - 1L;
            }
            bishops &= bishops - 1L;
        }

        // Rooks.
        long rooks = us(turn) & this.rooks;
        while (rooks != 0) {
            int from = Bitboard.lsb(rooks);
            long targets = Bitboard.rookAttacks(from, this.occupied) & mask;
            while (targets != 0) {
                int to = Bitboard.lsb(targets);
                moves.pushNormal(this, Role.ROOK, from, isOccupied(to), to);
                targets &= targets - 1L;
            }
            rooks &= rooks - 1L;
        }

        // Queens.
        long queens = us(turn) & this.queens;
        while (queens != 0) {
            int from = Bitboard.lsb(queens);
            long targets = Bitboard.queenAttacks(from, this.occupied) & mask;
            while (targets != 0) {
                int to = Bitboard.lsb(targets);
                moves.pushNormal(this, Role.QUEEN, from, isOccupied(to), to);
                targets &= targets - 1L;
            }
            queens &= queens - 1L;
        }
    }

    private void genSafeKing(int king, long mask, boolean turn, MoveList moves) {
        long targets = Bitboard.KING_ATTACKS[king] & mask;
        while (targets != 0) {
            int to = Bitboard.lsb(targets);
            if (attacksTo(to, !turn) == 0) {
                moves.pushNormal(this, Role.KING, king, isOccupied(to), to);
            }
            targets &= targets - 1L;
        }
    }

    private void genEvasions(int king, long checkers, boolean turn, MoveList moves) {
        // Checks by these sliding pieces can maybe be blocked.
        long sliders = checkers & (this.bishops ^ this.rooks ^ this.queens);

        // Collect attacked squares that the king can not escape to.
        long attacked = 0;
        while (sliders != 0) {
            int slider = Bitboard.lsb(sliders);
            attacked |= Bitboard.RAYS[king][slider] ^ (1L << slider);
            sliders &= sliders - 1L;
        }

        genSafeKing(king, ~us(turn) & ~attacked, turn, moves);

        if (checkers != 0 && !Bitboard.moreThanOne(checkers)) {
            int checker = Bitboard.lsb(checkers);
            long target = Bitboard.BETWEEN[king][checker] | checkers;
            genNonKing(target, turn, moves);
        }
    }

    private void genPawn(long mask, boolean turn, MoveList moves) {
        // Pawn captures (except en passant).
        long capturers = us(turn) & this.pawns;
        while (capturers != 0) {
            int from = Bitboard.lsb(capturers);
            long targets = Bitboard.pawnAttacks(turn, from) & them(turn) & mask;
            while (targets != 0) {
                int to = Bitboard.lsb(targets);
                addPawnMoves(from, true, to, turn, moves);
                targets &= targets - 1L;
            }
            capturers &= capturers - 1L;
        }

        // Normal pawn moves.
        long singleMoves =
            ~this.occupied & (turn ?
                ((this.white & this.pawns) << 8) :
                ((this.black & this.pawns) >>> 8));

        long doubleMoves =
            ~this.occupied &
            (turn ? (singleMoves << 8) : (singleMoves >>> 8)) &
            Bitboard.RANKS[turn ? 3 : 4];

        singleMoves &= mask;
        doubleMoves &= mask;

        while (singleMoves != 0) {
            int to = Bitboard.lsb(singleMoves);
            int from = to + (turn ? -8 : 8);
            addPawnMoves(from, false, to, turn, moves);
            singleMoves &= singleMoves - 1L;
        }

        while (doubleMoves != 0) {
            int to = Bitboard.lsb(doubleMoves);
            int from = to + (turn ? -16: 16);
            moves.pushNormal(this, Role.PAWN, from, false, to);
            doubleMoves &= doubleMoves - 1L;
        }
    }

    private void addPawnMoves(int from, boolean capture, int to, boolean turn, MoveList moves) {
        if (Square.rank(to) == (turn ? 7 : 0)) {
            moves.pushPromotion(this, from, capture, to, Role.QUEEN);
            moves.pushPromotion(this, from, capture, to, Role.KNIGHT);
            moves.pushPromotion(this, from, capture, to, Role.ROOK);
            moves.pushPromotion(this, from, capture, to, Role.BISHOP);
        } else {
            moves.pushNormal(this, Role.PAWN, from, capture, to);
        }
    }

    private void genEnPassant(boolean turn, MoveList moves) {
        long pawns = us(turn) & this.pawns & Bitboard.pawnAttacks(!turn, this.epSquare);
        while (pawns != 0) {
            int pawn = Bitboard.lsb(pawns);
            moves.pushEnPassant(this, pawn, this.epSquare);
            pawns &= pawns - 1L;
        }
    }

    private void genCastling(int king, boolean turn, MoveList moves) {
        long rooks = this.castlingRights() & Bitboard.RANKS[turn ? 0 : 7];
        while (rooks != 0) {
            int rook = Bitboard.lsb(rooks);
            long path = Bitboard.BETWEEN[king][rook];
            if ((path & this.occupied) == 0) {
                int kingTo = Square.combine(rook < king ? Square.C1 : Square.G1, king);
                long kingPath = Bitboard.BETWEEN[king][kingTo] | (1L << kingTo) | (1L << king);
                while (kingPath != 0) {
                    int sq = Bitboard.lsb(kingPath);
                    if (attacksTo(sq, !turn, this.occupied ^ (1L << king)) != 0) {
                        break;
                    }
                    kingPath &= kingPath - 1L;
                }
                if (kingPath == 0) moves.pushCastle(this, king, rook);
            }
            rooks &= rooks - 1L;
        }
    }

    // Used for filtering candidate moves that would leave/put the king
    // in check.
    private boolean isSafe(int king, Move move, long blockers, boolean turn) {
        switch (move.type) {
            case Move.NORMAL:
                return
                    !Bitboard.contains(us(turn) & blockers, move.from) ||
                    Square.aligned(move.from, move.to, king);

            case Move.EN_PASSANT:
                long occupied = this.occupied;
                occupied ^= (1L << move.from);
                occupied ^= (1L << Square.combine(move.to, move.from)); // captured pawn
                occupied |= (1L << move.to);
                return
                    (Bitboard.rookAttacks(king, occupied) & them(turn) & (this.rooks ^ this.queens)) == 0 &&
                    (Bitboard.bishopAttacks(king, occupied) & them(turn) & (this.bishops ^ this.queens)) == 0;

            default:
                return true;
        }
    }

    public boolean equals(Object o) {
        Board b = (Board) o;
        return b.occupied == occupied
                && b.queens == queens
                && b.pawns == pawns
                && b.rooks == rooks
                && b.bishops == bishops
                && b.kings == kings
                && b.knights == knights
                && b.white == white
                && b.black == black
                ;
    }

    public int hashCode() {
        return Long.hashCode(occupied
                ^ queens
                ^ pawns
                ^ rooks
                ^ bishops
                ^ kings
                ^ knights
                ^ white
                ^ black);
    }

    public String debugBoard() {
        StringBuilder sb = new StringBuilder();
        for(int row = 7; row >=0; row--) {
            for(int file = 0; file < 8; file++) {
                if(file > 0)
                    sb.append(' ');
                int square = row * 8 + file;
                if(isOccupied(square)) {
                    String s = roleAt(square).symbol;
                    if(s.equals("")) s = "P";
                    sb.append(whiteAt(square)? s: s.toLowerCase());
                }
                else {
                    sb.append('.');
                }
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
