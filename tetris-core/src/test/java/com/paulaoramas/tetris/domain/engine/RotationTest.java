package com.paulaoramas.tetris.domain.engine;

import com.paulaoramas.tetris.domain.model.Board;
import com.paulaoramas.tetris.domain.model.GameResult;
import com.paulaoramas.tetris.domain.model.GameState;
import com.paulaoramas.tetris.domain.model.Move;
import com.paulaoramas.tetris.domain.model.Piece;
import com.paulaoramas.tetris.domain.model.Tetromino;
import com.paulaoramas.tetris.domain.port.RandomPieceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Specifies the rotation engine and SRS (Super Rotation System) wall kick behavior.
 *
 * <p>Wall kick order per the engine implementation:
 * 1. Try the rotated position directly.
 * 2. If blocked, try kicking 1 column to the left (offset -1, 0).
 * 3. If still blocked, try kicking 1 column to the right (offset +1, 0).
 * 4. If all three fail, the rotation is rejected and state is unchanged.
 *
 * <p>TDD note: these tests (commit #5) were written BEFORE commit #6 expanded the
 * rotation implementation — they define the contract the engine must satisfy.
 */
class RotationTest {

    private GameEngine engine;

    @BeforeEach
    void setUp() {
        engine = new GameEngine(generator(Tetromino.T, Tetromino.T, Tetromino.T));
    }

    // ── Basic rotation ────────────────────────────────────────────────────

    @Test
    void piece_rotates_clockwise_when_space_is_available() {
        // T piece spawns at (3,0) with rotation 0
        GameState state = GameState.initial(Tetromino.T, Tetromino.I);
        int initialRotation = state.activePiece().rotation();

        GameResult result = engine.applyMove(state, Move.ROTATE_CW);

        GameState next = ongoing(result);
        assertThat(next.activePiece().rotation()).isEqualTo((initialRotation + 1) % 4);
        assertThat(next.activePiece().x()).isEqualTo(state.activePiece().x()); // no kick applied
    }

    @Test
    void piece_rotates_counter_clockwise_when_space_is_available() {
        GameState state = GameState.initial(Tetromino.T, Tetromino.I);
        int initialRotation = state.activePiece().rotation(); // 0

        GameResult result = engine.applyMove(state, Move.ROTATE_CCW);

        GameState next = ongoing(result);
        // CCW from 0 wraps to 3
        assertThat(next.activePiece().rotation()).isEqualTo(3);
        assertThat(next.activePiece().x()).isEqualTo(state.activePiece().x());
    }

    @Test
    void rotation_does_not_change_score_or_level() {
        GameState state = GameState.initial(Tetromino.T, Tetromino.I);
        GameState next = ongoing(engine.applyMove(state, Move.ROTATE_CW));

        assertThat(next.score()).isZero();
        assertThat(next.level()).isEqualTo(1);
    }

    // ── Wall kick: rotate against right wall → kick left ─────────────────

    @Test
    void rotation_kicks_left_when_right_wall_blocks_direct_rotation() {
        // I-piece (vertical) at x=7, rotation=1:
        //   cells at (9,y),(9,y+1),(9,y+2),(9,y+3) — column 9 (rightmost valid)
        // Rotating CW (1→2) produces horizontal I at x=7:
        //   cells at (7,y+2),(8,y+2),(9,y+2),(10,y+2) — column 10 is out of bounds
        // Kick left (x=6): cells at (6,y+2),(7,y+2),(8,y+2),(9,y+2) — all valid ✓
        Piece verticalI = new Piece(Tetromino.I, 7, 5, 1);
        GameState state = new GameState(Board.empty(), verticalI, Tetromino.T, 0, 1, 0);

        GameResult result = engine.applyMove(state, Move.ROTATE_CW);

        GameState next = ongoing(result);
        assertThat(next.activePiece().rotation()).isEqualTo(2);         // rotated
        assertThat(next.activePiece().x()).isEqualTo(6);                // kicked left by 1
    }

    // ── Wall kick: rotate against left wall → kick right ─────────────────

    @Test
    void rotation_kicks_right_when_left_wall_blocks_direct_rotation() {
        // I-piece (vertical) at x=-1, rotation=3:
        //   offset {1,0},{1,1},{1,2},{1,3} → cells at (0,y)..(0,y+3) — column 0 (leftmost valid)
        // Rotating CW (3→0) produces horizontal I at x=-1:
        //   offset {0,1},{1,1},{2,1},{3,1} → cells at (-1,y+1),(0,y+1),(1,y+1),(2,y+1)
        //   col -1 is out of bounds
        // Kick left (x=-2): also out of bounds → rejected
        // Kick right (x=0): cells at (0,y+1),(1,y+1),(2,y+1),(3,y+1) — valid ✓
        Piece verticalI = new Piece(Tetromino.I, -1, 5, 3);
        GameState state = new GameState(Board.empty(), verticalI, Tetromino.T, 0, 1, 0);

        GameResult result = engine.applyMove(state, Move.ROTATE_CW);

        GameState next = ongoing(result);
        assertThat(next.activePiece().rotation()).isEqualTo(0);        // rotated
        assertThat(next.activePiece().x()).isEqualTo(0);               // kicked right by 1
    }

    // ── Rotation rejected when all positions blocked ───────────────────────

    @Test
    void rotation_is_rejected_when_direct_and_all_kick_positions_are_blocked() {
        // T-piece at (5,10) rotating CW (0→1):
        //   Direct (x=5): one cell would land at (6,10) if blocked
        //   Kick left (x=4): one cell would land at (5,10)
        //   Kick right (x=6): one cell would land at (7,10)
        // We block all three with locked cells so no rotation is possible.
        boolean[][] grid = new boolean[Board.ROWS][Board.COLUMNS];
        // T rotation-1 offset {1,0},{1,1},{1,2},{2,1}:
        // at x=5 → (6,10),(6,11),(6,12),(7,11)   block (6,10)
        // at x=4 → (5,10),(5,11),(5,12),(6,11)   block (5,10)
        // at x=6 → (7,10),(7,11),(7,12),(8,11)   block (7,10)
        grid[10][6] = true;
        grid[10][5] = true;
        grid[10][7] = true;
        Board blockedBoard = Board.of(grid);

        Piece tPiece = new Piece(Tetromino.T, 5, 10, 0);
        GameState state = new GameState(blockedBoard, tPiece, Tetromino.T, 0, 1, 0);

        GameResult result = engine.applyMove(state, Move.ROTATE_CW);

        GameState next = ongoing(result);
        // State must be completely unchanged
        assertThat(next.activePiece().rotation()).isEqualTo(0);
        assertThat(next.activePiece().x()).isEqualTo(5);
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private static GameState ongoing(GameResult result) {
        assertThat(result).isInstanceOf(GameResult.Ongoing.class);
        return ((GameResult.Ongoing) result).state();
    }

    private static RandomPieceGenerator generator(Tetromino... pieces) {
        Iterator<Tetromino> it = List.of(pieces).iterator();
        return it::next;
    }
}
