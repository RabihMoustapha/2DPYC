# ♟️ 2D Java Chess

A classic chess game built in Java with a Swing graphical interface.  
You play as **White** against a simple **AI opponent (Black)**, with legal move highlighting, castling, en passant, promotion, and checkmate detection.

This project is a complete rewrite of an earlier Python/Pygame version, with improved UI/UX, bug fixes, and a more maintainable object‑oriented structure.

---

## ✨ Features

- **Graphical board** (640×640) with light/dark squares
- **All standard chess rules implemented:**
  - Castling (kingside & queenside)
  - En passant captures
  - Pawn promotion (auto‑queen)
  - Check and checkmate detection
  - Stalemate draw
- **Click‑based piece movement**
  - Click a piece to see legal moves (dots)
  - Click a destination to move
- **AI opponent (Black)**
  - Minimax with alpha‑beta pruning (depth 2)
  - Positional evaluation using piece‑square tables
  - Move ordering for better pruning
- **Fixed bugs from the Python version:**
  - En passant capture now removes the correct pawn
  - No infinite recursion when checking attacks (castling vs. attack detection separated)
  - Piece images preserve aspect ratio – no distortion
- **Improved UI/UX:**
  - Selected square highlighting
  - Legal move indicators
  - Game over message
  - (Optionally extendable: coordinates, last move highlight, check highlight, etc.)

---

## 🛠️ Requirements

- **Java 17 or later** (uses records – Java 16+ required)
- No external libraries – only standard JDK

---

## 🚀 How to Run

1. **Compile the source files**  
   Place all `.java` files in the same directory (or package). Then:
   ```bash
   javac *.java
   ```

2. **Place the assets**  
   Create an `assets` folder in the same directory as your compiled classes (or in the classpath root) and put the piece PNGs inside it.  
   The expected filenames are:
   ```
   w_King.png, w_Queen.png, w_Rook.png, w_Bishop.png, w_Knight.png, w_Pawn.png
   b_King.png, b_Queen.png, b_Rook.png, b_Bishop.png, b_Knight.png, b_Pawn.png
   ```
   (You can reuse the original Itch.IO assets.)

3. **Run**
   ```bash
   java Main
   ```

---

## 🎮 Gameplay

- You control **White**; the AI controls **Black**.
- **Click** a white piece to select it – legal moves are shown as small dots on empty squares.
- **Click** a destination square to move.
- If you click another white piece, the selection changes.
- Click the selected piece again (or an illegal square) to deselect.
- The AI will automatically make its move after a short delay (500 ms).

### Special Moves

- **Castling:** Move the king two squares toward a rook; the rook jumps automatically.
- **En passant:** If a pawn moves two squares forward and lands beside an enemy pawn, the enemy pawn may capture it as if it had moved only one square.
- **Promotion:** When a pawn reaches the last rank, it is automatically promoted to a **Queen**.

---

## 📁 Project Structure

```
src/
├── ChessGame.java      – Core chess rules, move generation, board state
├── ChessAI.java        – Minimax AI with evaluation
├── ChessPanel.java     – Swing panel drawing board and pieces
├── Config.java         – Constants, colors, piece‑square tables
├── ImageLoader.java    – Loads piece images from resources
├── Main.java           – Entry point, sets up the frame and AI timer
└── Move.java           – Simple record representing a chess move
assets/                 – PNG images for all pieces
```

---

## 🧠 AI Details

- **Algorithm:** Minimax with alpha‑beta pruning (depth 2)
- **Evaluation:**
  - Material values: Pawn=1, Knight=3, Bishop=3, Rook=5, Queen=9
  - Positional bonuses from piece‑square tables
- **Move ordering:** Captures and promotions are searched first to improve pruning efficiency.

---

## 🔧 Possible Enhancements

The current implementation is clean and modular. You can easily extend it with:

- **Promotion dialog** (choose Queen, Rook, Bishop, Knight)
- **Board coordinates** (a‑h, 1‑8)
- **Last move / check highlighting**
- **Undo / redo**
- **Adjustable AI difficulty** (depth or time‑limited search)
- **Sound effects / animations**
- **Pause / restart buttons**

---

## 📝 License & Credits

- **Chess piece assets:** Original assets from Itch.IO (free assets).  
  If you use this project, please keep the original asset license in mind.
- **Code:** This project is open for learning and modification.

---

Enjoy the game! 🏆
