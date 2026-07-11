# game.py – chess board, moves, rules, game state

from copy import deepcopy
import config.constants as constants

class ChessGame:
    def __init__(self):
        self.board = self.create_initial_board()
        self.turn = 'w'
        self.selected = None
        self.legal_moves = []

        # Castling rights
        self.w_king_moved = False
        self.w_rook_a_moved = False   # queenside rook (col 0)
        self.w_rook_h_moved = False   # kingside rook (col 7)
        self.b_king_moved = False
        self.b_rook_a_moved = False
        self.b_rook_h_moved = False

        self.en_passant_target = None
        self.game_over = False
        self.winner = None
        self.ai_delay_start = None

    def create_initial_board(self):
        return [
            ['bR', 'bN', 'bB', 'bQ', 'bK', 'bB', 'bN', 'bR'],
            ['bP', 'bP', 'bP', 'bP', 'bP', 'bP', 'bP', 'bP'],
            ['',   '',   '',   '',   '',   '',   '',   ''],
            ['',   '',   '',   '',   '',   '',   '',   ''],
            ['',   '',   '',   '',   '',   '',   '',   ''],
            ['',   '',   '',   '',   '',   '',   '',   ''],
            ['wP', 'wP', 'wP', 'wP', 'wP', 'wP', 'wP', 'wP'],
            ['wR', 'wN', 'wB', 'wQ', 'wK', 'wB', 'wN', 'wR']
        ]

    # ---------- Move generation ----------
    def is_within_board(self, r, c):
        return 0 <= r < constants.ROWS and 0 <= c < constants.COLS

    @staticmethod
    def get_piece_color(piece):
        return piece[0] if piece else None

    def generate_moves_for_piece(self, r, c):
        """Return pseudo‑legal moves (ignoring king safety)."""
        piece = self.board[r][c]
        if not piece:
            return []
        color = self.get_piece_color(piece)
        moves = []
        ptype = piece[1]

        # --- Pawn ---
        if ptype == 'P':
            direction = -1 if color == 'w' else 1
            start_row = 6 if color == 'w' else 1
            fwd_r = r + direction
            if self.is_within_board(fwd_r, c) and self.board[fwd_r][c] == '':
                moves.append(((r, c), (fwd_r, c), None))
                if r == start_row:
                    fwd2_r = r + 2 * direction
                    if self.board[fwd2_r][c] == '':
                        moves.append(((r, c), (fwd2_r, c), None))
            for dc in (-1, 1):
                cap_r, cap_c = r + direction, c + dc
                if self.is_within_board(cap_r, cap_c):
                    target = self.board[cap_r][cap_c]
                    if target and self.get_piece_color(target) != color:
                        moves.append(((r, c), (cap_r, cap_c), None))
                    if (cap_r, cap_c) == self.en_passant_target:
                        moves.append(((r, c), (cap_r, cap_c), None))

        # --- Knight ---
        elif ptype == 'N':
            for dr, dc in [(-2,-1),(-2,1),(-1,-2),(-1,2),(1,-2),(1,2),(2,-1),(2,1)]:
                nr, nc = r + dr, c + dc
                if self.is_within_board(nr, nc):
                    target = self.board[nr][nc]
                    if not target or self.get_piece_color(target) != color:
                        moves.append(((r, c), (nr, nc), None))

        # --- Bishop ---
        elif ptype == 'B':
            for dr, dc in [(-1,-1),(-1,1),(1,-1),(1,1)]:
                for step in range(1, 8):
                    nr, nc = r + dr*step, c + dc*step
                    if not self.is_within_board(nr, nc):
                        break
                    target = self.board[nr][nc]
                    if not target:
                        moves.append(((r, c), (nr, nc), None))
                    else:
                        if self.get_piece_color(target) != color:
                            moves.append(((r, c), (nr, nc), None))
                        break

        # --- Rook ---
        elif ptype == 'R':
            for dr, dc in [(-1,0),(1,0),(0,-1),(0,1)]:
                for step in range(1, 8):
                    nr, nc = r + dr*step, c + dc*step
                    if not self.is_within_board(nr, nc):
                        break
                    target = self.board[nr][nc]
                    if not target:
                        moves.append(((r, c), (nr, nc), None))
                    else:
                        if self.get_piece_color(target) != color:
                            moves.append(((r, c), (nr, nc), None))
                        break

        # --- Queen ---
        elif ptype == 'Q':
            for dr, dc in [(-1,-1),(-1,1),(1,-1),(1,1),(-1,0),(1,0),(0,-1),(0,1)]:
                for step in range(1, 8):
                    nr, nc = r + dr*step, c + dc*step
                    if not self.is_within_board(nr, nc):
                        break
                    target = self.board[nr][nc]
                    if not target:
                        moves.append(((r, c), (nr, nc), None))
                    else:
                        if self.get_piece_color(target) != color:
                            moves.append(((r, c), (nr, nc), None))
                        break

        # --- King ---
        elif ptype == 'K':
            for dr, dc in [(-1,-1),(-1,0),(-1,1),(0,-1),(0,1),(1,-1),(1,0),(1,1)]:
                nr, nc = r + dr, c + dc
                if self.is_within_board(nr, nc):
                    target = self.board[nr][nc]
                    if not target or self.get_piece_color(target) != color:
                        moves.append(((r, c), (nr, nc), None))

            # Castling
            if color == 'w' and not self.w_king_moved:
                # kingside
                if (not self.w_rook_h_moved and
                    self.board[7][5] == '' and self.board[7][6] == '' and
                    not self.is_square_attacked(7, 4, 'b') and
                    not self.is_square_attacked(7, 5, 'b') and
                    not self.is_square_attacked(7, 6, 'b')):
                    moves.append(((7, 4), (7, 6), None))
                # queenside
                if (not self.w_rook_a_moved and
                    self.board[7][3] == '' and self.board[7][2] == '' and self.board[7][1] == '' and
                    not self.is_square_attacked(7, 4, 'b') and
                    not self.is_square_attacked(7, 3, 'b') and
                    not self.is_square_attacked(7, 2, 'b')):
                    moves.append(((7, 4), (7, 2), None))
            if color == 'b' and not self.b_king_moved:
                # kingside
                if (not self.b_rook_h_moved and
                    self.board[0][5] == '' and self.board[0][6] == '' and
                    not self.is_square_attacked(0, 4, 'w') and
                    not self.is_square_attacked(0, 5, 'w') and
                    not self.is_square_attacked(0, 6, 'w')):
                    moves.append(((0, 4), (0, 6), None))
                # queenside
                if (not self.b_rook_a_moved and
                    self.board[0][3] == '' and self.board[0][2] == '' and self.board[0][1] == '' and
                    not self.is_square_attacked(0, 4, 'w') and
                    not self.is_square_attacked(0, 3, 'w') and
                    not self.is_square_attacked(0, 2, 'w')):
                    moves.append(((0, 4), (0, 2), None))

        # Auto‑queen promotions
        final = []
        for move in moves:
            (fr, fc), (tr, tc), _ = move
            piece_moving = self.board[fr][fc]
            if piece_moving[1] == 'P':
                if (color == 'w' and tr == 0) or (color == 'b' and tr == 7):
                    final.append(((fr, fc), (tr, tc), 'Q'))
                else:
                    final.append(move)
            else:
                final.append(move)
        return final

    def is_square_attacked(self, r, c, attacker_color):
        for row in range(constants.ROWS):
            for col in range(constants.COLS):
                piece = self.board[row][col]
                if piece and self.get_piece_color(piece) == attacker_color:
                    for move in self.generate_moves_for_piece(row, col):
                        (_, (tr, tc), _) = move
                        if tr == r and tc == c:
                            return True
        return False

    def get_king_position(self, color):
        for r in range(constants.ROWS):
            for c in range(constants.COLS):
                piece = self.board[r][c]
                if piece and piece[0] == color and piece[1] == 'K':
                    return (r, c)
        return None

    def is_king_in_check(self, color):
        king_pos = self.get_king_position(color)
        if not king_pos:
            return False
        opponent = 'b' if color == 'w' else 'w'
        return self.is_square_attacked(king_pos[0], king_pos[1], opponent)

    def generate_legal_moves(self, r, c):
        pseudo = self.generate_moves_for_piece(r, c)
        legal = []
        color = self.turn
        for move in pseudo:
            board_copy = deepcopy(self.board)
            (fr, fc), (tr, tc), promo = move
            board_copy[tr][tc] = board_copy[fr][fc]
            board_copy[fr][fc] = ''

            # en passant capture
            if self.board[fr][fc][1] == 'P' and (tr, tc) == self.en_passant_target:
                board_copy[tr - (1 if color == 'w' else -1)][tc] = ''

            # castling rook
            if self.board[fr][fc][1] == 'K' and abs(tc - fc) == 2:
                if tc == 6:
                    board_copy[fr][5] = board_copy[fr][7]
                    board_copy[fr][7] = ''
                elif tc == 2:
                    board_copy[fr][3] = board_copy[fr][0]
                    board_copy[fr][0] = ''

            if promo:
                board_copy[tr][tc] = color + promo

            king_pos = None
            for row in range(constants.ROWS):
                for col in range(constants.COLS):
                    if board_copy[row][col] == color + 'K':
                        king_pos = (row, col)
                        break
                if king_pos:
                    break
            if not king_pos:
                continue

            opponent = 'b' if color == 'w' else 'w'
            attacked = False
            for row in range(constants.ROWS):
                for col in range(constants.COLS):
                    piece = board_copy[row][col]
                    if piece and piece[0] == opponent:
                        if self._piece_attacks_square(board_copy, row, col, king_pos[0], king_pos[1]):
                            attacked = True
                            break
                if attacked:
                    break
            if not attacked:
                legal.append(move)
        return legal

    def _piece_attacks_square(self, board, pr, pc, tr, tc):
        piece = board[pr][pc]
        if not piece:
            return False
        color, ptype = piece[0], piece[1]
        dr = tr - pr
        dc = tc - pc

        if ptype == 'P':
            direction = -1 if color == 'w' else 1
            return dr == direction and abs(dc) == 1
        if ptype == 'N':
            return (abs(dr), abs(dc)) in [(2,1), (1,2)]
        if ptype in ('B','R','Q'):
            if ptype == 'B' and abs(dr) != abs(dc):
                return False
            if ptype == 'R' and not (dr == 0 or dc == 0):
                return False
            if ptype == 'Q' and not (abs(dr) == abs(dc) or dr == 0 or dc == 0):
                return False
            return self._clear_path(board, pr, pc, tr, tc)
        if ptype == 'K':
            return max(abs(dr), abs(dc)) == 1
        return False

    def _clear_path(self, board, r1, c1, r2, c2):
        dr = r2 - r1
        dc = c2 - c1
        step_r = 1 if dr > 0 else (-1 if dr < 0 else 0)
        step_c = 1 if dc > 0 else (-1 if dc < 0 else 0)
        cur_r, cur_c = r1 + step_r, c1 + step_c
        while (cur_r, cur_c) != (r2, c2):
            if board[cur_r][cur_c] != '':
                return False
            cur_r += step_r
            cur_c += step_c
        return True

    def all_legal_moves_for_color(self):
        all_moves = []
        for r in range(constants.ROWS):
            for c in range(constants.COLS):
                if self.board[r][c] and self.board[r][c][0] == self.turn:
                    all_moves.extend(self.generate_legal_moves(r, c))
        return all_moves

    # ---------- Execution & state update ----------
    def execute_move(self, move):
        (fr, fc), (tr, tc), promo = move
        piece = self.board[fr][fc]
        color = self.turn

        self.board[tr][tc] = piece
        self.board[fr][fc] = ''

        # en passant capture
        if piece[1] == 'P' and (tr, tc) == self.en_passant_target:
            captured_row = tr - (1 if color == 'w' else -1)
            self.board[captured_row][tc] = ''

        # castling rook move
        if piece[1] == 'K' and abs(tc - fc) == 2:
            if tc == 6:
                self.board[fr][5] = self.board[fr][7]
                self.board[fr][7] = ''
            elif tc == 2:
                self.board[fr][3] = self.board[fr][0]
                self.board[fr][0] = ''

        if promo:
            self.board[tr][tc] = color + promo

        # Update castling rights
        if piece == 'wK':
            self.w_king_moved = True
        elif piece == 'wR' and (fr, fc) == (7, 0):
            self.w_rook_a_moved = True
        elif piece == 'wR' and (fr, fc) == (7, 7):
            self.w_rook_h_moved = True
        elif piece == 'bK':
            self.b_king_moved = True
        elif piece == 'bR' and (fr, fc) == (0, 0):
            self.b_rook_a_moved = True
        elif piece == 'bR' and (fr, fc) == (0, 7):
            self.b_rook_h_moved = True

        # en passant target
        self.en_passant_target = None
        if piece[1] == 'P' and abs(tr - fr) == 2:
            self.en_passant_target = ((fr + tr) // 2, fc)

        self.turn = 'b' if self.turn == 'w' else 'w'
        self.selected = None
        self.legal_moves = []
        self.check_game_end()

    def check_game_end(self):
        all_moves = self.all_legal_moves_for_color()
        in_check = self.is_king_in_check(self.turn)
        if not all_moves:
            self.game_over = True
            if in_check:
                self.winner = 'white' if self.turn == 'b' else 'black'
            else:
                self.winner = 'draw'

    # ---------- User input handling ----------
    def handle_click(self, pos):
        if self.game_over or self.turn == constants.AI_COLOR:
            return
        col = pos[0] // constants.SQUARE_SIZE
        row = pos[1] // constants.SQUARE_SIZE

        if self.selected is None:
            piece = self.board[row][col]
            if piece and piece[0] == self.turn:
                self.selected = (row, col)
                legal = self.generate_legal_moves(row, col)
                self.legal_moves = [(tr, tc) for (_, (tr, tc), _) in legal]
            return

        fr, fc = self.selected
        if (row, col) == (fr, fc):
            self.selected = None
            self.legal_moves = []
            return

        for move in self.generate_legal_moves(fr, fc):
            (_, (tr, tc), promo) = move
            if tr == row and tc == col:
                self.execute_move(move)
                return

        piece = self.board[row][col]
        if piece and piece[0] == self.turn:
            self.selected = (row, col)
            legal = self.generate_legal_moves(row, col)
            self.legal_moves = [(tr, tc) for (_, (tr, tc), _) in legal]
        else:
            self.selected = None
            self.legal_moves = []