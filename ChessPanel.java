import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

public class ChessPanel extends JPanel {
    private ChessGame game;
    private Map<String, BufferedImage> images;

    public ChessPanel(ChessGame game, Map<String, BufferedImage> images) {
        this.game = game;
        this.images = images;
        setPreferredSize(new Dimension(Config.WIDTH, Config.HEIGHT));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int col = e.getX() / Config.SQUARE_SIZE;
                int row = e.getY() / Config.SQUARE_SIZE;
                game.handleClick(row, col);
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        drawBoard(g2);
        drawPieces(g2);
        drawGameOver(g2);
    }

    private void drawBoard(Graphics2D g2) {
        for (int r = 0; r < Config.ROWS; r++) {
            for (int c = 0; c < Config.COLS; c++) {
                Color color = (r + c) % 2 == 0 ? Config.LIGHT_SQUARE : Config.DARK_SQUARE;
                g2.setColor(color);
                g2.fillRect(c * Config.SQUARE_SIZE, r * Config.SQUARE_SIZE,
                        Config.SQUARE_SIZE, Config.SQUARE_SIZE);

                // Highlight selected square
                if (game.getSelected() != null && game.getSelected()[0] == r && game.getSelected()[1] == c) {
                    g2.setColor(new Color(0, 100, 200, 120));
                    g2.fillRect(c * Config.SQUARE_SIZE, r * Config.SQUARE_SIZE,
                            Config.SQUARE_SIZE, Config.SQUARE_SIZE);
                }

                // Draw legal move dots
                List<int[]> legalMoves = game.getLegalMoves();
                for (int[] lm : legalMoves) {
                    if (lm[0] == r && lm[1] == c) {
                        g2.setColor(new Color(0, 0, 0, 100));
                        int d = Config.SQUARE_SIZE / 4;
                        g2.fillOval(c * Config.SQUARE_SIZE + (Config.SQUARE_SIZE - d) / 2,
                                r * Config.SQUARE_SIZE + (Config.SQUARE_SIZE - d) / 2,
                                d, d);
                    }
                }
            }
        }
    }

    private void drawPieces(Graphics2D g2) {
        for (int r = 0; r < Config.ROWS; r++) {
            for (int c = 0; c < Config.COLS; c++) {
                String piece = game.getPieceAt(r, c);
                if (piece != null && !piece.isEmpty()) {
                    BufferedImage img = images.get(piece);
                    if (img != null) {
                        int x = c * Config.SQUARE_SIZE;
                        int y = r * Config.SQUARE_SIZE;
                        int size = Config.SQUARE_SIZE;
                        // Preserve aspect ratio
                        int w = img.getWidth();
                        int h = img.getHeight();
                        double scale = Math.min((double) size / w, (double) size / h);
                        int drawW = (int) (w * scale);
                        int drawH = (int) (h * scale);
                        int drawX = x + (size - drawW) / 2;
                        int drawY = y + (size - drawH) / 2;
                        g2.drawImage(img, drawX, drawY, drawW, drawH, null);
                    }
                }
            }
        }
    }

    private void drawGameOver(Graphics2D g2) {
        if (game.isGameOver()) {
            String text;
            if (game.getWinner().equals("white"))
                text = "White wins by checkmate!";
            else if (game.getWinner().equals("black"))
                text = "Black wins by checkmate!";
            else
                text = "Stalemate – Draw!";

            g2.setFont(new Font("Arial", Font.BOLD, 36));
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int x = (Config.WIDTH - textWidth) / 2;
            int y = Config.HEIGHT / 2;
            g2.setColor(new Color(255, 0, 0));
            g2.drawString(text, x, y);
        }
    }
}