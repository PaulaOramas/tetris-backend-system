package com.paulaoramas.tetris.domain.model;

/**
 * Sealed interface representing the two possible outcomes after the engine
 * processes a move: either the game continues or it ends.
 *
 * <p>Using a sealed interface instead of a nullable return or a boolean flag:
 * <ul>
 *   <li>Makes the outcome exhaustive in switch expressions — the compiler forces
 *       callers to handle both {@code Ongoing} and {@code GameOver} cases.</li>
 *   <li>Eliminates NullPointerExceptions from forgotten null checks.</li>
 *   <li>Encapsulates the final state within the result itself.</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>
 *   GameResult result = engine.apply(state, move);
 *   GameState next = switch (result) {
 *       case GameResult.Ongoing o  -> o.state();
 *       case GameResult.GameOver g -> handleGameOver(g.finalState());
 *   };
 * </pre>
 */
public sealed interface GameResult permits GameResult.Ongoing, GameResult.GameOver {

    /**
     * The game is still in progress. Contains the new state after the move.
     *
     * @param state the updated game state
     */
    record Ongoing(GameState state) implements GameResult {}

    /**
     * The game has ended. The board is full and no new piece can spawn.
     * Contains the final state for score recording purposes.
     *
     * @param finalState the last valid game state before game over
     */
    record GameOver(GameState finalState) implements GameResult {}

    // ── Convenience factory methods ───────────────────────────────────────

    static GameResult ongoing(GameState state) {
        return new Ongoing(state);
    }

    static GameResult gameOver(GameState finalState) {
        return new GameOver(finalState);
    }
}
