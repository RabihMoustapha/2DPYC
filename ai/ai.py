# ai.py – evaluation and minimax bot

from copy import deepcopy
from config import constants
from game.game import ChessGame

def evaluate_board(game):
    """Score from White's perspective (positive = good for White)."""
    score = 0
    for r in range(constants.ROWS):
        for c in range(constants.COLS):
            piece = game.board[r][c]
            if not piece:
                continue
            color, ptype = piece[0], piece[1]
            value = constants.PIECE_VALUES[ptype]
            # positional bonus
            if ptype in constants.PST:
                if color == 'w':
                    bonus = constants.PST[ptype][r][c]
                else:
                    bonus = constants.PST[ptype][7 - r][c]
                value += bonus * 0.1
            if color == 'w':
                score += value
            else:
                score -= value
    return score

def order_moves(moves, game):
    """Sort moves: captures/promotions first (better pruning)."""
    def priority(move):
        (_, (tr, tc), promo) = move
        if promo:
            return 1000
        target = game.board[tr][tc]
        if target:
            victim_val = constants.PIECE_VALUES[target[1]]
            attacker = game.board[move[0][0]][move[0][1]]
            attacker_val = constants.PIECE_VALUES[attacker[1]]
            return 10 * victim_val - attacker_val
        return 0
    return sorted(moves, key=priority, reverse=True)

def alpha_beta(game, depth, alpha, beta, maximizing_player):
    if depth == 0 or game.game_over:
        return evaluate_board(game)

    if maximizing_player:
        max_eval = float('-inf')
        moves = game.all_legal_moves_for_color()
        if not moves:
            return evaluate_board(game)
        for move in order_moves(moves, game):
            new_game = deepcopy(game)
            new_game.execute_move(move)
            eval = alpha_beta(new_game, depth - 1, alpha, beta, False)
            max_eval = max(max_eval, eval)
            alpha = max(alpha, eval)
            if beta <= alpha:
                break
        return max_eval
    else:
        min_eval = float('inf')
        moves = game.all_legal_moves_for_color()
        if not moves:
            return evaluate_board(game)
        for move in order_moves(moves, game):
            new_game = deepcopy(game)
            new_game.execute_move(move)
            eval = alpha_beta(new_game, depth - 1, alpha, beta, True)
            min_eval = min(min_eval, eval)
            beta = min(beta, eval)
            if beta <= alpha:
                break
        return min_eval

def get_best_move(game):
    """Return the best legal move for the current player (depth 2)."""
    best_move = None
    if game.turn == 'w':
        best_score = float('-inf')
        next_max = False
    else:
        best_score = float('inf')
        next_max = True

    moves = game.all_legal_moves_for_color()
    if not moves:
        return None
    for move in order_moves(moves, game):
        new_game = deepcopy(game)
        new_game.execute_move(move)
        score = alpha_beta(new_game, 2, float('-inf'), float('inf'), next_max)
        if game.turn == 'w':
            if score > best_score:
                best_score = score
                best_move = move
        else:
            if score < best_score:
                best_score = score
                best_move = move
    return best_move