import java.util.ArrayList;
import java.util.List;

public class ChessGame {
    private String[][] board;
    private String turn = "w";
    private int[] selected = null; // {row, col}
    private List<int[]> legalMoves = new ArrayList<>(); // each entry {row, col}

    // Castling rights
    private boolean wKingMoved = false;
    private boolean wRookAMoved = false; // queenside rook
    private boolean wRookHMoved = false; // kingside rook
    private boolean bKingMoved = false;
    private boolean bRookAMoved = false;
    private boolean bRookHMoved = false;

    private int[] enPassantTarget = null; // {row, col} or null
    private boolean gameOver = false;
    private String winner = null; // "white", "black", or "draw"

    public ChessGame() {
        board = new String[][] {
                { "bR", "bN", "bB", "bQ", "bK", "bB", "bN", "bR" },
                { "bP", "bP", "bP", "bP", "bP", "bP", "bP", "bP" },
                { "", "", "", "", "", "", "", "" },
                { "", "", "", "", "", "", "", "" },
                { "", "", "", "", "", "", "", "" },
                { "", "", "", "", "", "", "", "" },
                { "wP", "wP", "wP", "wP", "wP", "wP", "wP", "wP" },
                { "wR", "wN", "wB", "wQ", "wK", "wB", "wN", "wR" }
        };
    }

    // Getters used by UI
    public String getTurn() {
        return turn;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public String getWinner() {
        return winner;
    }

    public int[] getSelected() {
        return selected;
    }

    public List<int[]> getLegalMoves() {
        return legalMoves;
    }

    public String getPieceAt(int row, int col) {
        return board[row][col];
    }

    // Helper: extract color from piece string like "wK" -> "w"
    public static String getPieceColor(String piece) {
        if (piece == null || piece.isEmpty())
            return null;
        return piece.substring(0, 1);
    }

    // Check if coordinates are inside the board
    private boolean isWithinBoard(int r, int c) {
        return r >= 0 && r < Config.ROWS && c >= 0 && c < Config.COLS;
    }

    // Generate pseudo-legal moves for a piece (includeCastling controls castling
    // generation)
    public List<Move> generatePseudoLegalMoves(int r, int c, boolean includeCastling) {
        List<Move> moves = new ArrayList<>();
        String piece = board[r][c];
        if (piece == null || piece.isEmpty())
            return moves;
        String color = getPieceColor(piece);
        char ptype = piece.charAt(1);

        // Pawn
        if (ptype == 'P') {
            int direction = color.equals("w") ? -1 : 1;
            int startRow = color.equals("w") ? 6 : 1;

            // Forward one
            int fwdR = r + direction;
            if (isWithinBoard(fwdR, c) && board[fwdR][c].isEmpty()) {
                moves.add(new Move(r, c, fwdR, c, '\0'));
                // Forward two from start
                if (r == startRow) {
                    int fwd2R = r + 2 * direction;
                    if (board[fwd2R][c].isEmpty()) {
                        moves.add(new Move(r, c, fwd2R, c, '\0'));
                    }
                }
            }

            // Captures
            for (int dc : new int[] { -1, 1 }) {
                int capR = r + direction;
                int capC = c + dc;
                if (isWithinBoard(capR, capC)) {
                    String target = board[capR][capC];
                    if (!target.isEmpty() && !getPieceColor(target).equals(color)) {
                        moves.add(new Move(r, c, capR, capC, '\0'));
                    }
                    // En passant
                    if (enPassantTarget != null && enPassantTarget[0] == capR && enPassantTarget[1] == capC) {
                        moves.add(new Move(r, c, capR, capC, '\0'));
                    }
                }
            }
        }

        // Knight
        else if (ptype == 'N') {
            int[][] offsets = { { -2, -1 }, { -2, 1 }, { -1, -2 }, { -1, 2 }, { 1, -2 }, { 1, 2 }, { 2, -1 },
                    { 2, 1 } };
            for (int[] off : offsets) {
                int nr = r + off[0], nc = c + off[1];
                if (isWithinBoard(nr, nc)) {
                    String target = board[nr][nc];
                    if (target.isEmpty() || !getPieceColor(target).equals(color)) {
                        moves.add(new Move(r, c, nr, nc, '\0'));
                    }
                }
            }
        }

        // Bishop, Rook, Queen (sliding pieces)
        else if (ptype == 'B' || ptype == 'R' || ptype == 'Q') {
            int[][] directions;
            if (ptype == 'B')
                directions = new int[][] { { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 } };
            else if (ptype == 'R')
                directions = new int[][] { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
            else
                directions = new int[][] { { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 }, { -1, 0 }, { 1, 0 }, { 0, -1 },
                        { 0, 1 } };

            for (int[] dir : directions) {
                for (int step = 1; step < 8; step++) {
                    int nr = r + dir[0] * step;
                    int nc = c + dir[1] * step;
                    if (!isWithinBoard(nr, nc))
                        break;
                    String target = board[nr][nc];
                    if (target.isEmpty()) {
                        moves.add(new Move(r, c, nr, nc, '\0'));
                    } else {
                        if (!getPieceColor(target).equals(color)) {
                            moves.add(new Move(r, c, nr, nc, '\0'));
                        }
                        break;
                    }
                }
            }
        }

        // King
        else if (ptype == 'K') {
            int[][] offsets = { { -1, -1 }, { -1, 0 }, { -1, 1 }, { 0, -1 }, { 0, 1 }, { 1, -1 }, { 1, 0 }, { 1, 1 } };
            for (int[] off : offsets) {
                int nr = r + off[0], nc = c + off[1];
                if (isWithinBoard(nr, nc)) {
                    String target = board[nr][nc];
                    if (target.isEmpty() || !getPieceColor(target).equals(color)) {
                        moves.add(new Move(r, c, nr, nc, '\0'));
                    }
                }
            }

            // Castling
            if (includeCastling) {
                if (color.equals("w") && !wKingMoved) {
                    // Kingside
                    if (!wRookHMoved &&
                            board[7][5].isEmpty() && board[7][6].isEmpty() &&
                            !isSquareAttacked(7, 4, "b") &&
                            !isSquareAttacked(7, 5, "b") &&
                            !isSquareAttacked(7, 6, "b")) {
                        moves.add(new Move(7, 4, 7, 6, '\0'));
                    }
                    // Queenside
                    if (!wRookAMoved &&
                            board[7][3].isEmpty() && board[7][2].isEmpty() && board[7][1].isEmpty() &&
                            !isSquareAttacked(7, 4, "b") &&
                            !isSquareAttacked(7, 3, "b") &&
                            !isSquareAttacked(7, 2, "b")) {
                        moves.add(new Move(7, 4, 7, 2, '\0'));
                    }
                }
                if (color.equals("b") && !bKingMoved) {
                    // Kingside
                    if (!bRookHMoved &&
                            board[0][5].isEmpty() && board[0][6].isEmpty() &&
                            !isSquareAttacked(0, 4, "w") &&
                            !isSquareAttacked(0, 5, "w") &&
                            !isSquareAttacked(0, 6, "w")) {
                        moves.add(new Move(0, 4, 0, 6, '\0'));
                    }
                    // Queenside
                    if (!bRookAMoved &&
                            board[0][3].isEmpty() && board[0][2].isEmpty() && board[0][1].isEmpty() &&
                            !isSquareAttacked(0, 4, "w") &&
                            !isSquareAttacked(0, 3, "w") &&
                            !isSquareAttacked(0, 2, "w")) {
                        moves.add(new Move(0, 4, 0, 2, '\0'));
                    }
                }
            }
        }

        // Auto-queen for promotion
        List<Move> finalMoves = new ArrayList<>();
        for (Move move : moves) {
            String movingPiece = board[move.fromRow()][move.fromCol()];
            char movingType = movingPiece.charAt(1);
            if (movingType == 'P') {
                if ((color.equals("w") && move.toRow() == 0) ||
                        (color.equals("b") && move.toRow() == 7)) {
                    finalMoves.add(new Move(move.fromRow(), move.fromCol(), move.toRow(), move.toCol(), 'Q'));
                } else {
                    finalMoves.add(move);
                }
            } else {
                finalMoves.add(move);
            }
        }
        return finalMoves;
    }

    // Check if a square is attacked by any piece of the given color.
    // This method does NOT generate castling moves -> avoids recursion.
    public boolean isSquareAttacked(int r, int c, String attackerColor) {
        for (int row = 0; row < Config.ROWS; row++) {
            for (int col = 0; col < Config.COLS; col++) {
                String piece = board[row][col];
                if (!piece.isEmpty() && getPieceColor(piece).equals(attackerColor)) {
                    if (pieceAttacksSquare(row, col, r, c)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Direct attack check for a single piece (no castling, no move generation)
    private boolean pieceAttacksSquare(int pr, int pc, int tr, int tc) {
        String piece = board[pr][pc];
        if (piece.isEmpty())
            return false;
        String color = getPieceColor(piece);
        char ptype = piece.charAt(1);
        int dr = tr - pr;
        int dc = tc - pc;

        switch (ptype) {
            case 'P': {
                int direction = color.equals("w") ? -1 : 1;
                return dr == direction && Math.abs(dc) == 1;
            }
            case 'N':
                return (Math.abs(dr) == 2 && Math.abs(dc) == 1) ||
                        (Math.abs(dr) == 1 && Math.abs(dc) == 2);
            case 'B':
                if (Math.abs(dr) != Math.abs(dc))
                    return false;
                return clearPath(pr, pc, tr, tc);
            case 'R':
                if (dr != 0 && dc != 0)
                    return false;
                return clearPath(pr, pc, tr, tc);
            case 'Q':
                if (!(Math.abs(dr) == Math.abs(dc) || dr == 0 || dc == 0))
                    return false;
                return clearPath(pr, pc, tr, tc);
            case 'K':
                return Math.max(Math.abs(dr), Math.abs(dc)) == 1;
            default:
                return false;
        }
    }

    // Check if path between (r1,c1) and (r2,c2) is clear (exclusive)
    private boolean clearPath(int r1, int c1, int r2, int c2) {
        int dr = Integer.signum(r2 - r1);
        int dc = Integer.signum(c2 - c1);
        int curR = r1 + dr;
        int curC = c1 + dc;
        while (curR != r2 || curC != c2) {
            if (!board[curR][curC].isEmpty()) {
                return false;
            }
            curR += dr;
            curC += dc;
        }
        return true;
    }

    // Get king position for a color
    private int[] getKingPosition(String color) {
        for (int r = 0; r < Config.ROWS; r++) {
            for (int c = 0; c < Config.COLS; c++) {
                if (board[r][c].equals(color + "K")) {
                    return new int[] { r, c };
                }
            }
        }
        return null;
    }

    // Is the current player's king in check?
    public boolean isKingInCheck(String color) {
        int[] kingPos = getKingPosition(color);
        if (kingPos == null)
            return false;
        String opponent = color.equals("w") ? "b" : "w";
        return isSquareAttacked(kingPos[0], kingPos[1], opponent);
    }

    // Generate legal moves for a piece at (r,c) by filtering pseudo-legal moves
    public List<Move> generateLegalMoves(int r, int c) {
        List<Move> pseudo = generatePseudoLegalMoves(r, c, true);
        List<Move> legal = new ArrayList<>();
        String color = turn;

        for (Move move : pseudo) {
            String[][] boardCopy = cloneBoard();
            makeMoveOnBoard(boardCopy, move, color);
            // After move, check if own king is in check
            int[] kingPos = findKing(boardCopy, color);
            if (kingPos == null)
                continue;

            String opponent = color.equals("w") ? "b" : "w";
            boolean attacked = isSquareAttackedOnBoard(boardCopy, kingPos[0], kingPos[1], opponent);
            if (!attacked) {
                legal.add(move);
            }
        }
        return legal;
    }

    // Clone the board
    private String[][] cloneBoard() {
        String[][] copy = new String[Config.ROWS][Config.COLS];
        for (int r = 0; r < Config.ROWS; r++) {
            System.arraycopy(board[r], 0, copy[r], 0, Config.COLS);
        }
        return copy;
    }

    // Apply a move to a given board (used for simulation)
    private void makeMoveOnBoard(String[][] b, Move move, String color) {
        int fr = move.fromRow(), fc = move.fromCol();
        int tr = move.toRow(), tc = move.toCol();
        char promo = move.promotion();

        String piece = b[fr][fc];
        b[tr][tc] = piece;
        b[fr][fc] = "";

        // En passant capture
        if (piece.charAt(1) == 'P' && enPassantTarget != null &&
                tr == enPassantTarget[0] && tc == enPassantTarget[1]) {
            int capturedRow = (color.equals("w")) ? tr + 1 : tr - 1;
            b[capturedRow][tc] = "";
        }

        // Castling rook move
        if (piece.charAt(1) == 'K' && Math.abs(tc - fc) == 2) {
            if (tc == 6) { // kingside
                b[fr][5] = b[fr][7];
                b[fr][7] = "";
            } else if (tc == 2) { // queenside
                b[fr][3] = b[fr][0];
                b[fr][0] = "";
            }
        }

        // Promotion
        if (promo != '\0') {
            b[tr][tc] = color + promo;
        }
    }

    // Find king on a given board
    private int[] findKing(String[][] b, String color) {
        for (int r = 0; r < Config.ROWS; r++) {
            for (int c = 0; c < Config.COLS; c++) {
                if (b[r][c].equals(color + "K")) {
                    return new int[] { r, c };
                }
            }
        }
        return null;
    }

    // Check if square is attacked on a given board (used for legal move filtering)
    private boolean isSquareAttackedOnBoard(String[][] b, int r, int c, String attackerColor) {
        // Temporarily replace the board and call isSquareAttacked
        String[][] original = board;
        board = b;
        boolean result = isSquareAttacked(r, c, attackerColor);
        board = original;
        return result;
    }

    // Generate all legal moves for current player
    public List<Move> allLegalMovesForColor() {
        List<Move> all = new ArrayList<>();
        for (int r = 0; r < Config.ROWS; r++) {
            for (int c = 0; c < Config.COLS; c++) {
                if (!board[r][c].isEmpty() && getPieceColor(board[r][c]).equals(turn)) {
                    all.addAll(generateLegalMoves(r, c));
                }
            }
        }
        return all;
    }

    // Execute a move on the actual board
    public void executeMove(Move move) {
        int fr = move.fromRow(), fc = move.fromCol();
        int tr = move.toRow(), tc = move.toCol();
        char promo = move.promotion();
        String piece = board[fr][fc];
        String color = turn;

        // Basic move
        board[tr][tc] = piece;
        board[fr][fc] = "";

        // En passant capture
        if (piece.charAt(1) == 'P' && enPassantTarget != null &&
                tr == enPassantTarget[0] && tc == enPassantTarget[1]) {
            int capturedRow = (color.equals("w")) ? tr + 1 : tr - 1;
            board[capturedRow][tc] = "";
        }

        // Castling rook move
        if (piece.charAt(1) == 'K' && Math.abs(tc - fc) == 2) {
            if (tc == 6) { // kingside
                board[fr][5] = board[fr][7];
                board[fr][7] = "";
            } else if (tc == 2) { // queenside
                board[fr][3] = board[fr][0];
                board[fr][0] = "";
            }
        }

        // Promotion
        if (promo != '\0') {
            board[tr][tc] = color + promo;
        }

        // Update castling rights
        if (piece.equals("wK"))
            wKingMoved = true;
        else if (piece.equals("wR") && fr == 7 && fc == 0)
            wRookAMoved = true;
        else if (piece.equals("wR") && fr == 7 && fc == 7)
            wRookHMoved = true;
        else if (piece.equals("bK"))
            bKingMoved = true;
        else if (piece.equals("bR") && fr == 0 && fc == 0)
            bRookAMoved = true;
        else if (piece.equals("bR") && fr == 0 && fc == 7)
            bRookHMoved = true;

        // Update en passant target
        enPassantTarget = null;
        if (piece.charAt(1) == 'P' && Math.abs(tr - fr) == 2) {
            enPassantTarget = new int[] { (fr + tr) / 2, fc };
        }

        // Switch turn
        turn = (turn.equals("w")) ? "b" : "w";
        selected = null;
        legalMoves.clear();

        // Check game end
        checkGameEnd();
    }

    private void checkGameEnd() {
        List<Move> allMoves = allLegalMovesForColor();
        boolean inCheck = isKingInCheck(turn);
        if (allMoves.isEmpty()) {
            gameOver = true;
            if (inCheck) {
                winner = turn.equals("b") ? "white" : "black";
            } else {
                winner = "draw";
            }
        }
    }

    // Handle a click on the board (row, col)
    public void handleClick(int row, int col) {
        if (gameOver || turn.equals(Config.AI_COLOR))
            return;

        if (selected == null) {
            String piece = board[row][col];
            if (!piece.isEmpty() && getPieceColor(piece).equals(turn)) {
                selected = new int[] { row, col };
                legalMoves = new ArrayList<>();
                for (Move m : generateLegalMoves(row, col)) {
                    legalMoves.add(new int[] { m.toRow(), m.toCol() });
                }
            }
            return;
        }

        int fr = selected[0], fc = selected[1];
        if (row == fr && col == fc) {
            selected = null;
            legalMoves.clear();
            return;
        }

        for (Move m : generateLegalMoves(fr, fc)) {
            if (m.toRow() == row && m.toCol() == col) {
                executeMove(m);
                return;
            }
        }

        // If clicking another own piece, select it instead
        String piece = board[row][col];
        if (!piece.isEmpty() && getPieceColor(piece).equals(turn)) {
            selected = new int[] { row, col };
            legalMoves = new ArrayList<>();
            for (Move m : generateLegalMoves(row, col)) {
                legalMoves.add(new int[] { m.toRow(), m.toCol() });
            }
        } else {
            selected = null;
            legalMoves.clear();
        }
    }

    public void copyStateFrom(ChessGame other) {
        for (int r = 0; r < Config.ROWS; r++) {
            System.arraycopy(other.board[r], 0, this.board[r], 0, Config.COLS);
        }
        this.turn = other.turn;
        this.selected = (other.selected == null) ? null : new int[] { other.selected[0], other.selected[1] };
        this.legalMoves = new ArrayList<>();
        for (int[] lm : other.legalMoves) {
            this.legalMoves.add(new int[] { lm[0], lm[1] });
        }
        this.wKingMoved = other.wKingMoved;
        this.wRookAMoved = other.wRookAMoved;
        this.wRookHMoved = other.wRookHMoved;
        this.bKingMoved = other.bKingMoved;
        this.bRookAMoved = other.bRookAMoved;
        this.bRookHMoved = other.bRookHMoved;
        this.enPassantTarget = (other.enPassantTarget == null) ? null
                : new int[] { other.enPassantTarget[0], other.enPassantTarget[1] };
        this.gameOver = other.gameOver;
        this.winner = other.winner;
    }
}