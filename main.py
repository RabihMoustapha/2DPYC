# main.py – the game loop, user input, and AI timer

import pygame
import sys
import config.constants as constants
from assets.image_loader import load_images
from game.game import ChessGame
from ai.ai import get_best_move
import ui.drawing as drawing

def main():
    pygame.init()
    screen = pygame.display.set_mode((constants.WIDTH, constants.HEIGHT))
    pygame.display.set_caption("Chess – You (White) vs AI (Black)")
    clock = pygame.time.Clock()

    images = load_images()
    game = ChessGame()

    running = True
    while running:
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                running = False
            elif event.type == pygame.MOUSEBUTTONDOWN:
                game.handle_click(event.pos)

        # AI turn
        if game.turn == constants.AI_COLOR and not game.game_over:
            if game.ai_delay_start is None:
                game.ai_delay_start = pygame.time.get_ticks()
            else:
                if pygame.time.get_ticks() - game.ai_delay_start >= constants.AI_DELAY:
                    best_move = get_best_move(game)
                    if best_move:
                        game.execute_move(best_move)
                    game.ai_delay_start = None

        screen.fill((0, 0, 0))
        drawing.draw_board(screen, game)
        drawing.draw_pieces(screen, game, images)
        drawing.draw_game_over(screen, game)
        pygame.display.flip()
        clock.tick(60)

    pygame.quit()
    sys.exit()

if __name__ == "__main__":
    main()