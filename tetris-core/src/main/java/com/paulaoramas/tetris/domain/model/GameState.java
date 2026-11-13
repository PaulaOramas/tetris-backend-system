package com.paulaoramas.tetris.domain.model;

/**
 * Immutable snapshot of the complete game state at a point in time.
 *
 * <p>Being a {@code record} makes {@code GameState} a pure value object:
 * two states with identical fields are equal, and there is no shared mutable
 * state between game sessions. This is essential for:
 * <ul>
 *   <li>Thread safety in multiplayer sessions (no synchronization needed)</li>
 *   <li>Deterministic testing: given state S and move M, the result is always identical</li>
 *   <li>Event sourcing: the full game history can be rebuilt by replaying moves</li>
 * </ul>
 *
 * @param board       the 10×20 grid of locked cells
 * @param activePiece the currently falling piece
 * @param nextPiece   the piece shown in the "next" preview
 * @param score       accumulated score
 * @param level       current level (affects gravity speed)
 * @param linesCleared total lines cleared in this game session
 */
public record GameState(
        Board board,
        Piece activePiece,
        Tetromino nextPiece,
        int score,
        int level,
        int linesCleared
) {

    /**
     * Creates an initial game state with an empty board.
     *
     * @param firstPiece the first active piece
     * @param nextPiece  the piece queued after the first
     * @return a fresh game state at score 0, level 1
     */
    public static GameState initial(Tetromino firstPiece, Tetromino nextPiece) {
        return new GameState(
                Board.empty(),
                Piece.spawn(firstPiece),
                nextPiece,
                0,
                1,
                0
        );
    }

    /**
     * Returns a copy of this state with a different active piece.
     * Used when a piece locks and the next piece becomes active.
     */
    public GameState withActivePiece(Piece piece) {
        return new GameState(board, piece, nextPiece, score, level, linesCleared);
    }

    /** Returns a copy of this state with updated board and score after line clears. */
    public GameState withBoardAndScore(Board newBoard, int additionalScore, int additionalLines) {
        int newLinesCleared = linesCleared + additionalLines;
        int newLevel = Math.max(level, newLinesCleared / 10 + 1);
        return new GameState(newBoard, activePiece, nextPiece, score + additionalScore, newLevel, newLinesCleared);
    }

    /** Returns a copy of this state with a new next piece (after spawning a new active piece). */
    public GameState advancePiece(Tetromino newActivePieceType, Tetromino newNextPiece) {
        return new GameState(board, Piece.spawn(newActivePieceType), newNextPiece, score, level, linesCleared);
    }

    /** Returns a copy of this state with score incremented (e.g. for hard drop bonus). */
    public GameState addScore(int points) {
        return new GameState(board, activePiece, nextPiece, score + points, level, linesCleared);
    }
}
