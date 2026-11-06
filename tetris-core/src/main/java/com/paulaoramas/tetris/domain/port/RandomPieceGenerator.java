package com.paulaoramas.tetris.domain.port;

import com.paulaoramas.tetris.domain.model.Tetromino;

/**
 * Output port: piece generation contract.
 *
 * <p>Separating piece generation into a port allows the engine to remain
 * deterministic in tests: inject a {@code FixedPieceGenerator} that returns
 * a predetermined sequence, and test complex board scenarios reproducibly.
 *
 * <p>The production implementation uses the 7-bag algorithm, while tests use
 * controlled sequences via Mockito or a simple list-based stub.
 */
public interface RandomPieceGenerator {

    /**
     * Returns the next piece type from the generation sequence.
     *
     * <p>Implementations must guarantee that the same piece type never appears
     * more than twice consecutively (7-bag algorithm fulfills this naturally).
     *
     * @return the next {@code Tetromino} to be queued as the upcoming piece
     */
    Tetromino next();
}
