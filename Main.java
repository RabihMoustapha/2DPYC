import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                ChessGame game = new ChessGame();
                Map<String, BufferedImage> images = ImageLoader.loadImages();
                ChessPanel panel = new ChessPanel(game, images);

                JFrame frame = new JFrame("Chess – You (White) vs AI (Black)");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.add(panel);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

                // AI timer
                Timer aiTimer = new Timer(Config.AI_DELAY, e -> {
                    if (!game.isGameOver() && game.getTurn().equals(Config.AI_COLOR)) {
                        Move best = ChessAI.getBestMove(game);
                        if (best != null) {
                            game.executeMove(best);
                            panel.repaint();
                        }
                    }
                });
                aiTimer.setRepeats(true);
                aiTimer.start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
}