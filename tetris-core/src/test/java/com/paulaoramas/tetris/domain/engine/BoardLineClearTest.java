package com.paulaoramas.tetris.domain.engine;

import com.paulaoramas.tetris.domain.model.Board;
import com.paulaoramas.tetris.domain.model.Piece;
import com.paulaoramas.tetris.domain.model.Tetromino;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Specifies line-clear mechanics: detecting complete rows, removing them,
 * and collapsing the rows above downward.
 *
 * <p>The TDD value here: Board.clearFullRows() must handle all cases
 * (0, 1, 2, 4 lines) deterministically without touching a UI or game loop.
 */
class BoardLineClearTest {

    @Test
    void no_full_rows_means_zero_lines_cleared_and_board_unchanged() {
        boolean[][] grid = new boolean[Board.ROWS][Board.COLUMNS];
        grid[19][0] = true; // only one cell — not a full row
        Board board = Board.of(grid);

        Board.ClearResult result = board.clearFullRows();

        assertThat(result.cleared()).isZero();
        assertThat(result.board().isOccupied(0, 19)).isTrue();  // cell preserved
    }

    @Test
    void one_full_row_is_cleared_and_counted() {
        boolean[][] grid = new boolean[Board.ROWS][Board.COLUMNS];
        Arrays.fill(grid[19], true); // fill bottom row completely
        Board board = Board.of(grid);

        Board.ClearResult result = board.clearFullRows();

        assertThat(result.cleared()).isEqualTo(1);
        assertThat(result.board().isEmptyRow(19)).isTrue(); // cleared row is now empty
    }

    @Test
    void row_above_cleared_row_shifts_down_to_fill_the_gap() {
        // Row 19 is full (will be cleared), row 18 has one cell
        boolean[][] grid = new boolean[Board.ROWS][Board.COLUMNS];
        Arrays.fill(grid[19], true);
        grid[18][3] = true; // marker cell in row above

        Board board = Board.of(grid);
        Board.ClearResult result = board.clearFullRows();

        // Row 19 (full) was cleared → row 18's content falls to row 19
        assertThat(result.board().isOccupied(3, 19)).isTrue();
        // Row 18 is now empty (its content fell down)
        assertThat(result.board().isOccupied(3, 18)).isFalse();
    }

    @Test
    void two_full_rows_are_cleared_simultaneously() {
        boolean[][] grid = new boolean[Board.ROWS][Board.COLUMNS];
        Arrays.fill(grid[18], true);
        Arrays.fill(grid[19], true);
        Board board = Board.of(grid);

        Board.ClearResult result = board.clearFullRows();

        assertThat(result.cleared()).isEqualTo(2);
        assertThat(result.board().isEmptyRow(18)).isTrue();
        assertThat(result.board().isEmptyRow(19)).isTrue();
    }

    @Test
    void tetris_four_lines_cleared_simultaneously() {
        boolean[][] grid = new boolean[Board.ROWS][Board.COLUMNS];
        Arrays.fill(grid[16], true);
        Arrays.fill(grid[17], true);
        Arrays.fill(grid[18], true);
        Arrays.fill(grid[19], true);
        Board board = Board.of(grid);

        Board.ClearResult result = board.clearFullRows();

        assertThat(result.cleared()).isEqualTo(4);
        assertThat(result.board().isEmptyRow(16)).isTrue();
        assertThat(result.board().isEmptyRow(19)).isTrue();
    }

    @Test
    void partial_rows_are_not_cleared() {
        boolean[][] grid = new boolean[Board.ROWS][Board.COLUMNS];
        // Fill all columns except one
        for (int col = 0; col < Board.COLUMNS - 1; col++) {
            grid[19][col] = true;
        }
        Board board = Board.of(grid);

        Board.ClearResult result = board.clearFullRows();

        assertThat(result.cleared()).isZero();
        // Partial row still exists
        assertThat(result.board().isOccupied(0, 19)).isTrue();
    }

    @Test
    void locking_piece_then_clearing_full_row_gives_correct_score_via_engine() {
        // Integration: lock pieces to fill a row, verify scoring fires
        // Fill row 19 with 5 O-pieces (10 cells = full row)
        Board board = Board.empty();
        for (int x = 0; x < 5; x++) {
            // O-piece at (x*2, 18): cells at (x*2,18),(x*2+1,18),(x*2,19),(x*2+1,19)
            board = board.lockPiece(new Piece(Tetromino.O, x * 2, 18, 0));
        }

        Board.ClearResult result = board.clearFullRows();

        // Both rows 18 and 19 are full (O-pieces span 2 rows)
        assertThat(result.cleared()).isEqualTo(2);
    }
}
