# drawing.py – all pygame drawing routines

import pygame
import config.constants as constants

def draw_board(screen, game):
    for r in range(constants.ROWS):
        for c in range(constants.COLS):
            color = constants.LIGHT_SQUARE if (r + c) % 2 == 0 else constants.DARK_SQUARE
            pygame.draw.rect(screen, color, (c * constants.SQUARE_SIZE,
                                             r * constants.SQUARE_SIZE,
                                             constants.SQUARE_SIZE,
                                             constants.SQUARE_SIZE))

            # Highlight selected square
            if game.selected and (r, c) == game.selected:
                s = pygame.Surface((constants.SQUARE_SIZE, constants.SQUARE_SIZE), pygame.SRCALPHA)
                s.fill((0, 100, 200, 120))
                screen.blit(s, (c * constants.SQUARE_SIZE, r * constants.SQUARE_SIZE))

            # Draw legal move dots
            if (r, c) in game.legal_moves:
                s = pygame.Surface((constants.SQUARE_SIZE, constants.SQUARE_SIZE), pygame.SRCALPHA)
                pygame.draw.circle(s, (0,0,0,100),
                                   (constants.SQUARE_SIZE // 2, constants.SQUARE_SIZE // 2),
                                   constants.SQUARE_SIZE // 6)
                screen.blit(s, (c * constants.SQUARE_SIZE, r * constants.SQUARE_SIZE))

def draw_pieces(screen, game, images):
    for r in range(constants.ROWS):
        for c in range(constants.COLS):
            piece = game.board[r][c]
            if piece:
                screen.blit(images[piece], (c * constants.SQUARE_SIZE,
                                            r * constants.SQUARE_SIZE))

def draw_game_over(screen, game):
    if game.game_over:
        font = pygame.font.Font(None, 48)
        if game.winner == 'white':
            text = "White wins by checkmate!"
        elif game.winner == 'black':
            text = "Black wins by checkmate!"
        else:
            text = "Stalemate – Draw!"
        rendered = font.render(text, True, (255, 0, 0))
        rect = rendered.get_rect(center=(constants.WIDTH // 2, constants.HEIGHT // 2))
        screen.blit(rendered, rect)