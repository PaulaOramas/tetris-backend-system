package com.paulaoramas.tetris.domain.model;

/**
 * Represents the active falling piece on the board.
 *
 * <p>Immutable value object: every transformation (move, rotate) returns a new
 * {@code Piece} instance. This guarantees thread safety when multiple game sessions
 * share the engine and eliminates accidental mutation bugs.
 *
 * @param type     the Tetromino shape
 * @param x        column of the piece's pivot on the board (0 = leftmost column)
 * @param y        row of the piece's pivot on the board (0 = top row)
 * @param rotation rotation state index (0–3, clockwise)
 */
public record Piece(Tetromino type, int x, int y, int rotation) {

    /**
     * Factory method: spawns a piece at the standard spawn position.
     * Per the Tetris Guideline, pieces appear centered at the top of the board.
     *
     * @param type the Tetromino shape to spawn
     * @return a new Piece at the spawn position for a 10-wide board
     */
    public static Piece spawn(Tetromino type) {
        return new Piece(type, 3, 0, 0);
    }

    /** Returns a new Piece shifted one column to the left. */
    public Piece moveLeft() {
        return new Piece(type, x - 1, y, rotation);
    }

    /** Returns a new Piece shifted one column to the right. */
    public Piece moveRight() {
        return new Piece(type, x + 1, y, rotation);
    }

    /** Returns a new Piece shifted one row downward. */
    public Piece moveDown() {
        return new Piece(type, x, y + 1, rotation);
    }

    /**
     * Returns a new Piece rotated 90° clockwise.
     * The rotation index wraps from 3 back to 0.
     */
    public Piece rotateCW() {
        return new Piece(type, x, y, Math.floorMod(rotation + 1, 4));
    }

    /**
     * Returns a new Piece rotated 90° counter-clockwise.
     * The rotation index wraps from 0 back to 3.
     */
    public Piece rotateCCW() {
        return new Piece(type, x, y, Math.floorMod(rotation - 1, 4));
    }

    /**
     * Returns the absolute board coordinates occupied by this piece.
     *
     * <p>Each cell offset from the Tetromino's rotation matrix is added to
     * the piece's (x, y) pivot to obtain absolute board positions.
     *
     * @return array of [col, row] absolute coordinates for each cell
     */
    public int[][] absoluteCells() {
        int[][] offsets = type.cellsForRotation(rotation);
        int[][] cells = new int[offsets.length][2];
        for (int i = 0; i < offsets.length; i++) {
            cells[i][0] = x + offsets[i][0]; // absolute column
            cells[i][1] = y + offsets[i][1]; // absolute row
        }
        return cells;
    }

    /**
     * Returns a copy of this piece with a horizontal offset applied.
     * Used by the wall-kick algorithm to test alternate positions after rotation.
     *
     * @param dx column offset (negative = left, positive = right)
     * @return new Piece shifted by {@code dx} columns
     */
    public Piece withOffset(int dx, int dy) {
        return new Piece(type, x + dx, y + dy, rotation);
    }
}
