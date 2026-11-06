package com.paulaoramas.tetris.domain.model;

/**
 * The seven standard Tetromino pieces defined by the Tetris Guideline.
 *
 * <p>Each piece stores its rotation states as a list of coordinate offsets
 * relative to a 2D pivot point. Coordinates are expressed as [col, row] pairs,
 * where col increases to the right and row increases downward.
 *
 * <p>Rotation order: 0° → 90°CW → 180° → 270°CW (index 0, 1, 2, 3).
 */
public enum Tetromino {

    /**
     * I-piece: cyan, 4 cells in a line.
     * <pre>
     * [ ][ ][ ][ ]
     * </pre>
     */
    I(new int[][][] {
        {{0,1},{1,1},{2,1},{3,1}},  // 0°
        {{2,0},{2,1},{2,2},{2,3}},  // 90°
        {{0,2},{1,2},{2,2},{3,2}},  // 180°
        {{1,0},{1,1},{1,2},{1,3}}   // 270°
    }),

    /**
     * O-piece: yellow, 2×2 square (rotation has no visual effect).
     * <pre>
     * [ ][ ]
     * [ ][ ]
     * </pre>
     */
    O(new int[][][] {
        {{0,0},{1,0},{0,1},{1,1}},
        {{0,0},{1,0},{0,1},{1,1}},
        {{0,0},{1,0},{0,1},{1,1}},
        {{0,0},{1,0},{0,1},{1,1}}
    }),

    /**
     * T-piece: purple, T-shape.
     * <pre>
     *    [ ]
     * [ ][ ][ ]
     * </pre>
     */
    T(new int[][][] {
        {{0,1},{1,1},{2,1},{1,0}},  // 0°
        {{1,0},{1,1},{1,2},{2,1}},  // 90°
        {{0,1},{1,1},{2,1},{1,2}},  // 180°
        {{1,0},{1,1},{1,2},{0,1}}   // 270°
    }),

    /**
     * S-piece: green, S-shape.
     * <pre>
     *    [ ][ ]
     * [ ][ ]
     * </pre>
     */
    S(new int[][][] {
        {{1,0},{2,0},{0,1},{1,1}},  // 0°
        {{1,0},{1,1},{2,1},{2,2}},  // 90°
        {{1,1},{2,1},{0,2},{1,2}},  // 180°
        {{0,0},{0,1},{1,1},{1,2}}   // 270°
    }),

    /**
     * Z-piece: red, Z-shape.
     * <pre>
     * [ ][ ]
     *    [ ][ ]
     * </pre>
     */
    Z(new int[][][] {
        {{0,0},{1,0},{1,1},{2,1}},  // 0°
        {{2,0},{1,1},{2,1},{1,2}},  // 90°
        {{0,1},{1,1},{1,2},{2,2}},  // 180°
        {{1,0},{0,1},{1,1},{0,2}}   // 270°
    }),

    /**
     * J-piece: blue, J-shape.
     * <pre>
     * [ ]
     * [ ][ ][ ]
     * </pre>
     */
    J(new int[][][] {
        {{0,0},{0,1},{1,1},{2,1}},  // 0°
        {{1,0},{2,0},{1,1},{1,2}},  // 90°
        {{0,1},{1,1},{2,1},{2,2}},  // 180°
        {{1,0},{1,1},{0,2},{1,2}}   // 270°
    }),

    /**
     * L-piece: orange, L-shape.
     * <pre>
     *          [ ]
     * [ ][ ][ ]
     * </pre>
     */
    L(new int[][][] {
        {{2,0},{0,1},{1,1},{2,1}},  // 0°
        {{1,0},{1,1},{1,2},{2,2}},  // 90°
        {{0,1},{1,1},{2,1},{0,2}},  // 180°
        {{0,0},{1,0},{1,1},{1,2}}   // 270°
    });

    /**
     * Rotation matrices: [rotation][cell][col/row].
     * Indexed as {@code rotations[rotationIndex][cellIndex][0=col, 1=row]}.
     */
    private final int[][][] rotations;

    Tetromino(int[][][] rotations) {
        this.rotations = rotations;
    }

    /**
     * Returns the cell coordinates for a given rotation state.
     *
     * @param rotation rotation index (0–3), wraps around automatically
     * @return 2D array of [col, row] offsets for each cell of the piece
     */
    public int[][] cellsForRotation(int rotation) {
        return rotations[Math.floorMod(rotation, 4)];
    }

    /** Total number of rotation states (always 4 per Tetris Guideline). */
    public int rotationCount() {
        return 4;
    }
}
