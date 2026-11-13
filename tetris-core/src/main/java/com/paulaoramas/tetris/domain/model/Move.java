package com.paulaoramas.tetris.domain.model;

/**
 * All possible player actions that can be applied to the game state.
 *
 * <p>The engine receives a {@code Move} and a {@code GameState} and produces
 * a new {@code GameState}. Having moves as an enum makes the action space
 * exhaustive and type-safe — the compiler catches missing cases in switch expressions.
 */
public enum Move {

    /** Shift the active piece one column to the left. */
    LEFT,

    /** Shift the active piece one column to the right. */
    RIGHT,

    /** Shift the active piece one row downward (player-initiated). */
    SOFT_DROP,

    /**
     * Instantly drop the active piece to its lowest valid position and lock it.
     * Awards 2 points per row traveled (Tetris Guideline scoring).
     */
    HARD_DROP,

    /** Rotate the active piece 90° clockwise. */
    ROTATE_CW,

    /** Rotate the active piece 90° counter-clockwise. */
    ROTATE_CCW,

    /**
     * Automatic gravity tick — the game loop moves the piece down one row
     * without player input. Differs from SOFT_DROP: does not award drop points.
     */
    GRAVITY
}
