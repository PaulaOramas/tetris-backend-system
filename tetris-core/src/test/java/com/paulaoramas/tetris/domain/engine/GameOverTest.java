package com.paulaoramas.tetris.domain.engine;

import com.paulaoramas.tetris.domain.model.Board;
import com.paulaoramas.tetris.domain.model.GameResult;
import com.paulaoramas.tetris.domain.model.GameState;
import com.paulaoramas.tetris.domain.model.Move;
import com.paulaoramas.tetris.domain.model.Piece;
import com.paulaoramas.tetris.domain.model.Tetromino;
import com.paulaoramas.tetris.domain.port.RandomPieceGenerator;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Specifies game-over detection: triggered when the next piece cannot be
 * placed at its spawn position after the previous piece locks.
 *
 * <p>The engine must return {@link GameResult.GameOver} (not {@code Ongoing})
 * and preserve the final score within the result for persistence.
 */
class GameOverTest {

    @Test
    void game_is_over_when_spawn_position_is_blocked_after_lock() {
        // Pre-fill the O-piece spawn area: (3,0),(4,0),(3,1),(4,1)
        // These cells will be occupied before the active piece locks.
        boolean[][] grid = new boolean[Board.ROWS][Board.COLUMNS];
        grid[0][3] = true;
        grid[0][4] = true;
        grid[1][3] = true;
        grid[1][4] = true;
        Board blockedSpawn = Board.of(grid);

        // Active T-piece far below the blocked area; nextPiece = O
        // After T locks, engine tries to spawn O at (3,0) → blocked → GameOver
        Piece activePiece = new Piece(Tetromino.T, 3, 15, 0);
        GameState state = new GameState(blockedSpawn, activePiece, Tetromino.O, 500, 2, 15);

        GameEngine engine = new GameEngine(generator(Tetromino.I));
        GameResult result = engine.applyMove(state, Move.HARD_DROP);

        assertThat(result).isInstanceOf(GameResult.GameOver.class);
    }

    @Test
    void game_over_result_preserves_final_score() {
        boolean[][] grid = new boolean[Board.ROWS][Board.COLUMNS];
        grid[0][3] = true;
        grid[0][4] = true;
        grid[1][3] = true;
        grid[1][4] = true;
        Board blockedSpawn = Board.of(grid);

        Piece activePiece = new Piece(Tetromino.T, 3, 15, 0);
        int scoreBeforeDrop = 500;
        GameState state = new GameState(blockedSpawn, activePiece, Tetromino.O, scoreBeforeDrop, 2, 15);

        GameEngine engine = new GameEngine(generator(Tetromino.I));
        GameResult result = engine.applyMove(state, Move.HARD_DROP);

        GameResult.GameOver gameOver = (GameResult.GameOver) result;
        // Final score must be ≥ initial (drop points added)
        assertThat(gameOver.finalState().score()).isGreaterThanOrEqualTo(scoreBeforeDrop);
    }

    @Test
    void game_continues_when_spawn_position_is_free() {
        // Normal scenario: board has space at the top — game should continue
        GameEngine engine = new GameEngine(generator(Tetromino.I, Tetromino.T, Tetromino.S));
        GameState state = engine.startNewGame();

        GameResult result = engine.applyMove(state, Move.HARD_DROP);

        assertThat(result).isInstanceOf(GameResult.Ongoing.class);
    }

    @Test
    void game_result_ongoing_carries_updated_state() {
        GameEngine engine = new GameEngine(generator(Tetromino.T, Tetromino.L, Tetromino.S));
        GameState initial = engine.startNewGame();

        GameResult result = engine.applyMove(initial, Move.HARD_DROP);

        GameResult.Ongoing ongoing = (GameResult.Ongoing) result;
        // New active piece should be what was nextPiece before
        assertThat(ongoing.state().activePiece().type()).isEqualTo(initial.nextPiece());
    }

    @Test
    void game_over_state_contains_locked_board_not_empty_board() {
        boolean[][] grid = new boolean[Board.ROWS][Board.COLUMNS];
        // Block O spawn area
        grid[0][3] = true; grid[0][4] = true;
        grid[1][3] = true; grid[1][4] = true;
        Board blockedSpawn = Board.of(grid);

        Piece activePiece = new Piece(Tetromino.S, 1, 17, 0);
        GameState state = new GameState(blockedSpawn, activePiece, Tetromino.O, 0, 1, 0);

        GameEngine engine = new GameEngine(generator(Tetromino.I));
        GameResult result = engine.applyMove(state, Move.HARD_DROP);

        GameResult.GameOver gameOver = (GameResult.GameOver) result;
        // The final board must have locked cells (the S-piece was locked)
        boolean hasAnyOccupied = false;
        for (int row = 0; row < Board.ROWS && !hasAnyOccupied; row++) {
            for (int col = 0; col < Board.COLUMNS && !hasAnyOccupied; col++) {
                if (gameOver.finalState().board().isOccupied(col, row)) {
                    hasAnyOccupied = true;
                }
            }
        }
        assertThat(hasAnyOccupied).isTrue();
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private static RandomPieceGenerator generator(Tetromino... pieces) {
        Iterator<Tetromino> it = List.of(pieces).iterator();
        return it::next;
    }
}
