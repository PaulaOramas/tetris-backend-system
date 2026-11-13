package com.paulaoramas.tetris.domain.model;

import java.util.Arrays;

/**
 * The 10×20 Tetris board grid.
 *
 * <p>Internally represented as a 2D boolean array: {@code true} = cell occupied.
 * The board is mutable internally but exposed immutably — all public methods that
 * modify the board return a new {@code Board} instance, preserving the immutability
 * contract of {@code GameState}.
 *
 * <p>Coordinate system:
 * <ul>
 *   <li>Column (x): 0 = leftmost, 9 = rightmost</li>
 *   <li>Row (y): 0 = top, 19 = bottom (floor)</li>
 * </ul>
 */
public final class Board {

    public static final int COLUMNS = 10;
    public static final int ROWS = 20;

    /** grid[row][col] — true if the cell is occupied by a locked piece. */
    private final boolean[][] grid;

    private Board(boolean[][] grid) {
        this.grid = grid;
    }

    /** Creates a fresh empty board (all cells free). */
    public static Board empty() {
        return new Board(new boolean[ROWS][COLUMNS]);
    }

    /**
     * Creates a Board from an existing grid (deep-copies for immutability).
     *
     * @param grid source grid [row][col], must be ROWS×COLUMNS
     */
    public static Board of(boolean[][] grid) {
        boolean[][] copy = new boolean[ROWS][COLUMNS];
        for (int r = 0; r < ROWS; r++) {
            copy[r] = Arrays.copyOf(grid[r], COLUMNS);
        }
        return new Board(copy);
    }

    // ── Query methods ─────────────────────────────────────────────────────

    /** Returns {@code true} if the cell at (col, row) is occupied. */
    public boolean isOccupied(int col, int row) {
        return grid[row][col];
    }

    /** Returns {@code true} if the row is fully occupied (ready to clear). */
    public boolean isFullRow(int row) {
        for (int col = 0; col < COLUMNS; col++) {
            if (!grid[row][col]) return false;
        }
        return true;
    }

    /** Returns {@code true} if every cell in the row is free. */
    public boolean isEmptyRow(int row) {
        for (int col = 0; col < COLUMNS; col++) {
            if (grid[row][col]) return false;
        }
        return true;
    }

    /**
     * Checks whether a piece can be placed at its current position without
     * colliding with occupied cells or going out of bounds.
     *
     * @param piece the piece to validate
     * @return {@code true} if the position is valid (no collision, in bounds)
     */
    public boolean isValidPosition(Piece piece) {
        for (int[] cell : piece.absoluteCells()) {
            int col = cell[0];
            int row = cell[1];
            if (col < 0 || col >= COLUMNS) return false;    // out of bounds horizontally
            if (row < 0 || row >= ROWS) return false;        // out of bounds vertically
            if (grid[row][col]) return false;                // collision with locked piece
        }
        return true;
    }

    // ── Mutation methods (return new Board) ───────────────────────────────

    /**
     * Returns a new Board with the piece's cells locked in place.
     * Called when a piece can no longer move downward.
     *
     * @param piece the piece to lock onto the board
     * @return new Board with the piece cells marked as occupied
     */
    public Board lockPiece(Piece piece) {
        boolean[][] newGrid = deepCopy(grid);
        for (int[] cell : piece.absoluteCells()) {
            newGrid[cell[1]][cell[0]] = true;
        }
        return new Board(newGrid);
    }

    /**
     * Clears all full rows and shifts remaining rows down.
     *
     * @return a {@link ClearResult} containing the new board and the number of rows cleared
     */
    public ClearResult clearFullRows() {
        boolean[][] newGrid = new boolean[ROWS][COLUMNS];
        int writeRow = ROWS - 1; // start writing from the bottom
        int clearedCount = 0;

        for (int readRow = ROWS - 1; readRow >= 0; readRow--) {
            if (isFullRow(readRow)) {
                clearedCount++;
            } else {
                newGrid[writeRow] = Arrays.copyOf(grid[readRow], COLUMNS);
                writeRow--;
            }
        }
        return new ClearResult(new Board(newGrid), clearedCount);
    }

    // ── Internal helpers ──────────────────────────────────────────────────

    private static boolean[][] deepCopy(boolean[][] source) {
        boolean[][] copy = new boolean[ROWS][COLUMNS];
        for (int r = 0; r < ROWS; r++) {
            copy[r] = Arrays.copyOf(source[r], COLUMNS);
        }
        return copy;
    }

    // ── Nested result type ────────────────────────────────────────────────

    /**
     * Result of {@link #clearFullRows()}.
     *
     * @param board   the new board after clearing
     * @param cleared number of rows that were cleared (0–4)
     */
    public record ClearResult(Board board, int cleared) {}

    // ── equals / hashCode / toString ──────────────────────────────────────

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Board other)) return false;
        return Arrays.deepEquals(grid, other.grid);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(grid);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (boolean[] row : grid) {
            for (boolean cell : row) {
                sb.append(cell ? "█" : "·");
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
