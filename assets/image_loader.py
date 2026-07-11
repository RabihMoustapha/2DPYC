# image_loader.py – finds and loads piece PNGs

import pygame
from pathlib import Path
import config.constants as constants

def load_images():
    """Load and scale piece images. Automatically finds the assets folder."""
    script_dir = Path(__file__).parent.resolve()
    cwd = Path(".").resolve()

    # Search possible locations for the images
    candidates = [
        script_dir / "assets",
        script_dir,
        cwd / "assets",
        cwd,
    ]
    assets_dir = None
    test_file = "w_King.png"
    for folder in candidates:
        if (folder / test_file).is_file():
            assets_dir = folder
            break

    if assets_dir is None:
        searched = "\n".join(str(d) for d in candidates)
        raise FileNotFoundError(
            f"Could not find the chess piece PNGs. Searched in:\n{searched}\n"
            "Make sure the PNG files are in an 'assets' folder next to this script."
        )

    # Full piece name → standard abbreviation
    piece_abbr = {
        'King':   'K',
        'Queen':  'Q',
        'Rook':   'R',
        'Bishop': 'B',
        'Knight': 'N',
        'Pawn':   'P'
    }

    pieces = {}
    names = ['w_King', 'w_Queen', 'w_Rook', 'w_Bishop', 'w_Knight', 'w_Pawn',
             'b_King', 'b_Queen', 'b_Rook', 'b_Bishop', 'b_Knight', 'b_Pawn']

    for name in names:
        path = assets_dir / (name + '.png')
        img = pygame.image.load(str(path))
        img = pygame.transform.scale(img, (constants.SQUARE_SIZE, constants.SQUARE_SIZE))
        color, piece_name = name.split('_')
        symbol = color + piece_abbr[piece_name]   # e.g. 'wK', 'bN'
        pieces[symbol] = img
    return pieces