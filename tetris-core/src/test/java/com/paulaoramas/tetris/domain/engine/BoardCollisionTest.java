package com.paulaoramas.tetris.domain.engine;

import com.paulaoramas.tetris.domain.model.Board;
import com.paulaoramas.tetris.domain.model.Piece;
import com.paulaoramas.tetris.domain.model.Tetromino;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Specifies boundary and collision constraints for the Board.
 *
 * <p>These are the foundation of the whole engine: if isValidPosition() is wrong,
 * every move and rotation decision above it will be wrong too.
 * Tests are written against Board directly (pure unit, no engine needed).
 */
class BoardCollisionTest {

    // ── Left boundary ─────────────────────────────────────────────────────

    @Test
    void piece_at_column_zero_is_valid() {
        // O-piece at x=0: cells at (0,10),(1,10),(0,11),(1,11) — all within bounds
        Piece piece = new Piece(Tetromino.O, 0, 10, 0);
        assertThat(Board.empty().isValidPosition(piece)).isTrue();
    }

    @Test
    void piece_cannot_cross_left_wall() {
        // O-piece at x=-1: leftmost cell lands at column -1 — out of bounds
        Piece piece = new Piece(Tetromino.O, -1, 10, 0);
        assertThat(Board.empty().isValidPosition(piece)).isFalse();
    }

    @Test
    void moving_left_at_left_boundary_produces_invalid_position() {
        Piece atWall = new Piece(Tetromino.O, 0, 10, 0);
        assertThat(Board.empty().isValidPosition(atWall.moveLeft())).isFalse();
    }

    // ── Right boundary ────────────────────────────────────────────────────

    @Test
    void piece_at_rightmost_valid_column_is_valid() {
        // O-piece at x=8: cells at cols 8,9 — both within the 0-9 range
        Piece piece = new Piece(Tetromino.O, 8, 10, 0);
        assertThat(Board.empty().isValidPosition(piece)).isTrue();
    }

    @Test
    void piece_cannot_cross_right_wall() {
        // O-piece at x=9: cells at cols 9,10 — column 10 is out of bounds
        Piece piece = new Piece(Tetromino.O, 9, 10, 0);
        assertThat(Board.empty().isValidPosition(piece)).isFalse();
    }

    @Test
    void moving_right_at_right_boundary_produces_invalid_position() {
        Piece atWall = new Piece(Tetromino.O, 8, 10, 0);
        assertThat(Board.empty().isValidPosition(atWall.moveRight())).isFalse();
    }

    // ── Floor boundary ────────────────────────────────────────────────────

    @Test
    void piece_touching_floor_is_valid() {
        // I-piece rotation-0 at y=18: cells land at row 19 (last valid row)
        // I rotation-0 offsets: {0,1},{1,1},{2,1},{3,1}  → cells at y+1
        Piece piece = new Piece(Tetromino.I, 0, 18, 0);
        assertThat(Board.empty().isValidPosition(piece)).isTrue();
    }

    @Test
    void piece_cannot_pass_through_floor() {
        // I-piece at y=19: cells would land at row 20 — out of bounds
        Piece piece = new Piece(Tetromino.I, 0, 19, 0);
        assertThat(Board.empty().isValidPosition(piece)).isFalse();
    }

    @Test
    void moving_down_at_floor_produces_invalid_position() {
        Piece atFloor = new Piece(Tetromino.I, 0, 18, 0);
        assertThat(Board.empty().isValidPosition(atFloor.moveDown())).isFalse();
    }

    // ── Collision with locked cells ───────────────────────────────────────

    @Test
    void piece_overlapping_locked_cell_is_invalid() {
        // Lock O-piece at (4,18): occupies cells (4,18),(5,18),(4,19),(5,19)
        Board board = Board.empty().lockPiece(new Piece(Tetromino.O, 4, 18, 0));

        // Another O-piece at (4,17): cells (4,17),(5,17),(4,18),(5,18)
        // → (4,18) and (5,18) are occupied
        Piece falling = new Piece(Tetromino.O, 4, 17, 0);
        assertThat(board.isValidPosition(falling)).isFalse();
    }

    @Test
    void piece_directly_above_locked_cell_is_valid() {
        Board board = Board.empty().lockPiece(new Piece(Tetromino.O, 4, 18, 0));

        // O-piece at (4,16): cells (4,16),(5,16),(4,17),(5,17) — all free
        Piece falling = new Piece(Tetromino.O, 4, 16, 0);
        assertThat(board.isValidPosition(falling)).isTrue();
    }

    // ── Board state queries ───────────────────────────────────────────────

    @Test
    void locked_cells_are_marked_occupied() {
        Piece piece = new Piece(Tetromino.O, 2, 5, 0);
        Board board = Board.empty().lockPiece(piece);

        // O rotation-0 offsets: {0,0},{1,0},{0,1},{1,1}
        assertThat(board.isOccupied(2, 5)).isTrue();
        assertThat(board.isOccupied(3, 5)).isTrue();
        assertThat(board.isOccupied(2, 6)).isTrue();
        assertThat(board.isOccupied(3, 6)).isTrue();
        // Surrounding cells remain free
        assertThat(board.isOccupied(1, 5)).isFalse();
        assertThat(board.isOccupied(4, 5)).isFalse();
    }

    @Test
    void is_full_row_true_when_all_columns_occupied() {
        boolean[][] grid = new boolean[Board.ROWS][Board.COLUMNS];
        Arrays.fill(grid[19], true); // fill the bottom row completely
        Board board = Board.of(grid);

        assertThat(board.isFullRow(19)).isTrue();
        assertThat(board.isFullRow(18)).isFalse(); // adjacent row is empty
    }

    @Test
    void is_empty_row_true_when_no_columns_occupied() {
        boolean[][] grid = new boolean[Board.ROWS][Board.COLUMNS];
        grid[10][5] = true; // only one cell occupied
        Board board = Board.of(grid);

        assertThat(board.isEmptyRow(0)).isTrue();  // completely empty row
        assertThat(board.isEmptyRow(10)).isFalse(); // has one occupied cell
    }

    @Test
    void board_of_creates_independent_copy_of_source_grid() {
        boolean[][] grid = new boolean[Board.ROWS][Board.COLUMNS];
        grid[5][5] = true;
        Board board = Board.of(grid);

        // Mutating the source grid must not affect the board
        grid[5][5] = false;
        assertThat(board.isOccupied(5, 5)).isTrue();
    }

    @Test
    void two_boards_with_identical_grids_are_equal() {
        boolean[][] grid = new boolean[Board.ROWS][Board.COLUMNS];
        grid[0][0] = true;
        Board a = Board.of(grid);
        Board b = Board.of(grid);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void board_to_string_contains_block_char_for_occupied_cells() {
        Board board = Board.empty().lockPiece(new Piece(Tetromino.O, 0, 0, 0));
        // O at (0,0): cells (0,0),(1,0),(0,1),(1,1)
        String repr = board.toString();
        assertThat(repr).contains("█");
        assertThat(repr).contains("·");
    }
}
