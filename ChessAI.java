import java.util.*;

public class ChessAI {

    public static Move getBestMove(ChessGame game) {
        List<Move> moves = game.allLegalMovesForColor();
        if (moves.isEmpty())
            return null;

        String currentPlayer = game.getTurn();
        Move bestMove = null;
        int bestScore = currentPlayer.equals("w") ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        boolean maximizing = currentPlayer.equals("w"); // AI is Black, but this works for any

        for (Move move : orderMoves(moves, game)) {
            ChessGame copy = cloneGame(game);
            copy.executeMove(move);
            int score = alphaBeta(copy, 2, Integer.MIN_VALUE, Integer.MAX_VALUE, !maximizing);
            if (maximizing) {
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            } else {
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            }
        }
        return bestMove;
    }

    private static int alphaBeta(ChessGame game, int depth, int alpha, int beta, boolean maximizing) {
        if (depth == 0 || game.isGameOver()) {
            return evaluateBoard(game);
        }

        List<Move> moves = game.allLegalMovesForColor();
        if (moves.isEmpty())
            return evaluateBoard(game);

        if (maximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : orderMoves(moves, game)) {
                ChessGame copy = cloneGame(game);
                copy.executeMove(move);
                int eval = alphaBeta(copy, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha)
                    break;
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move move : orderMoves(moves, game)) {
                ChessGame copy = cloneGame(game);
                copy.executeMove(move);
                int eval = alphaBeta(copy, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha)
                    break;
            }
            return minEval;
        }
    }

    private static List<Move> orderMoves(List<Move> moves, ChessGame game) {
        // Simple ordering: captures/promotions first
        moves.sort((m1, m2) -> {
            int score1 = moveScore(m1, game);
            int score2 = moveScore(m2, game);
            return Integer.compare(score2, score1); // descending
        });
        return moves;
    }

    private static int moveScore(Move move, ChessGame game) {
        if (move.promotion() != '\0')
            return 1000;
        String targetPiece = game.getPieceAt(move.toRow(), move.toCol());
        if (targetPiece != null && !targetPiece.isEmpty()) {
            int victimVal = Config.PIECE_VALUES[targetPiece.charAt(1)];
            String attackerPiece = game.getPieceAt(move.fromRow(), move.fromCol());
            int attackerVal = Config.PIECE_VALUES[attackerPiece.charAt(1)];
            return 10 * victimVal - attackerVal;
        }
        return 0;
    }

    public static int evaluateBoard(ChessGame game) {
        int score = 0;
        for (int r = 0; r < Config.ROWS; r++) {
            for (int c = 0; c < Config.COLS; c++) {
                String piece = game.getPieceAt(r, c);
                if (piece == null || piece.isEmpty())
                    continue;
                String color = ChessGame.getPieceColor(piece);
                char ptype = piece.charAt(1);
                int value = Config.PIECE_VALUES[ptype];
                // Positional bonus
                if (ptype == 'P' || ptype == 'N' || ptype == 'B' || ptype == 'R' || ptype == 'Q' || ptype == 'K') {
                    int bonus;
                    if (color.equals("w")) {
                        bonus = Config.PST[ptype][r][c];
                    } else {
                        bonus = Config.PST[ptype][7 - r][c];
                    }
                    value += bonus * 0.1; // scale down
                }
                if (color.equals("w"))
                    score += value;
                else
                    score -= value;
            }
        }
        return score;
    }

    // Deep copy of game (simplified: create new game and copy board + state)
    private static ChessGame cloneGame(ChessGame game) {
        ChessGame copy = new ChessGame();
        copy.copyStateFrom(game);
        return copy;
    }
}