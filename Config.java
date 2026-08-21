import java.awt.Color;

public class Config {
    public static final int WIDTH = 640;
    public static final int HEIGHT = 640;
    public static final int ROWS = 8;
    public static final int COLS = 8;
    public static final int SQUARE_SIZE = WIDTH / 8;

    public static final Color LIGHT_SQUARE = new Color(240, 217, 181);
    public static final Color DARK_SQUARE = new Color(181, 136, 99);

    public static final String AI_COLOR = "b"; // Black plays as AI
    public static final int AI_DELAY = 500; // ms delay before AI move

    public static final int[] PIECE_VALUES = new int[256]; // indexed by char

    static {
        PIECE_VALUES['K'] = 0;
        PIECE_VALUES['Q'] = 9;
        PIECE_VALUES['R'] = 5;
        PIECE_VALUES['B'] = 3;
        PIECE_VALUES['N'] = 3;
        PIECE_VALUES['P'] = 1;
    }

    // Piece-square tables (White perspective). Translate from Python.
    // I'll include them as static 3D arrays: [pieceType][row][col]
    public static final int[][][] PST = new int[256][][]; // simplified: using char key

    static {
        // Pawn
        PST['P'] = new int[][] {
                { 0, 0, 0, 0, 0, 0, 0, 0 },
                { 50, 50, 50, 50, 50, 50, 50, 50 },
                { 10, 10, 20, 30, 30, 20, 10, 10 },
                { 5, 5, 10, 25, 25, 10, 5, 5 },
                { 0, 0, 0, 20, 20, 0, 0, 0 },
                { 5, -5, -10, 0, 0, -10, -5, 5 },
                { 5, 10, 10, -20, -20, 10, 10, 5 },
                { 0, 0, 0, 0, 0, 0, 0, 0 }
        };
        // Knight
        PST['N'] = new int[][] {
                { -50, -40, -30, -30, -30, -30, -40, -50 },
                { -40, -20, 0, 0, 0, 0, -20, -40 },
                { -30, 0, 10, 15, 15, 10, 0, -30 },
                { -30, 5, 15, 20, 20, 15, 5, -30 },
                { -30, 0, 15, 20, 20, 15, 0, -30 },
                { -30, 5, 10, 15, 15, 10, 5, -30 },
                { -40, -20, 0, 5, 5, 0, -20, -40 },
                { -50, -40, -30, -30, -30, -30, -40, -50 }
        };
        // Bishop
        PST['B'] = new int[][] {
                { -20, -10, -10, -10, -10, -10, -10, -20 },
                { -10, 0, 0, 0, 0, 0, 0, -10 },
                { -10, 0, 5, 10, 10, 5, 0, -10 },
                { -10, 5, 5, 10, 10, 5, 5, -10 },
                { -10, 0, 10, 10, 10, 10, 0, -10 },
                { -10, 10, 10, 10, 10, 10, 10, -10 },
                { -10, 5, 0, 0, 0, 0, 5, -10 },
                { -20, -10, -10, -10, -10, -10, -10, -20 }
        };
        // Rook
        PST['R'] = new int[][] {
                { 0, 0, 0, 0, 0, 0, 0, 0 },
                { 5, 10, 10, 10, 10, 10, 10, 5 },
                { -5, 0, 0, 0, 0, 0, 0, -5 },
                { -5, 0, 0, 0, 0, 0, 0, -5 },
                { -5, 0, 0, 0, 0, 0, 0, -5 },
                { -5, 0, 0, 0, 0, 0, 0, -5 },
                { -5, 0, 0, 0, 0, 0, 0, -5 },
                { 0, 0, 0, 5, 5, 0, 0, 0 }
        };
        // Queen
        PST['Q'] = new int[][] {
                { -20, -10, -10, -5, -5, -10, -10, -20 },
                { -10, 0, 0, 0, 0, 0, 0, -10 },
                { -10, 0, 5, 5, 5, 5, 0, -10 },
                { -5, 0, 5, 5, 5, 5, 0, -5 },
                { 0, 0, 5, 5, 5, 5, 0, -5 },
                { -10, 5, 5, 5, 5, 5, 0, -10 },
                { -10, 0, 5, 0, 0, 0, 0, -10 },
                { -20, -10, -10, -5, -5, -10, -10, -20 }
        };
        // King
        PST['K'] = new int[][] {
                { -30, -40, -40, -50, -50, -40, -40, -30 },
                { -30, -40, -40, -50, -50, -40, -40, -30 },
                { -30, -40, -40, -50, -50, -40, -40, -30 },
                { -30, -40, -40, -50, -50, -40, -40, -30 },
                { -20, -30, -30, -40, -40, -30, -30, -20 },
                { -10, -20, -20, -20, -20, -20, -20, -10 },
                { 20, 20, 0, 0, 0, 0, 20, 20 },
                { 20, 30, 10, 0, 0, 10, 30, 20 }
        };
    }
}