import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ImageLoader {
    public static Map<String, BufferedImage> loadImages() throws Exception {
        Map<String, BufferedImage> pieces = new HashMap<>();
        String[] names = { "w_King", "w_Queen", "w_Rook", "w_Bishop", "w_Knight", "w_Pawn",
                "b_King", "b_Queen", "b_Rook", "b_Bishop", "b_Knight", "b_Pawn" };
        Map<String, String> abbr = Map.of(
                "King", "K", "Queen", "Q", "Rook", "R", "Bishop", "B", "Knight", "N", "Pawn", "P");

        for (String name : names) {
            String path = "/assets/" + name + ".png";
            try (InputStream is = ImageLoader.class.getResourceAsStream(path)) {
                if (is == null)
                    throw new RuntimeException("Missing resource: " + path);
                BufferedImage img = ImageIO.read(is);
                String[] parts = name.split("_");
                String symbol = parts[0] + abbr.get(parts[1]);
                pieces.put(symbol, img);
            }
        }
        return pieces;
    }
}