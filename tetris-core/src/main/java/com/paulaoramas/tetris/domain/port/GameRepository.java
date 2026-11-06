package com.paulaoramas.tetris.domain.port;

import com.paulaoramas.tetris.domain.model.GameState;

import java.util.Optional;
import java.util.UUID;

/**
 * Output port: persistence contract for game sessions.
 *
 * <p>Defined by the domain, implemented by the infrastructure layer (JPA adapter).
 * The domain never imports Spring, JPA or SQL — it only depends on this interface.
 *
 * <p>This follows the Dependency Inversion Principle: the high-level domain module
 * dictates the contract, and the low-level persistence module fulfills it.
 */
public interface GameRepository {

    /**
     * Persists a game state, creating a new record or updating an existing one.
     *
     * @param gameId game session identifier
     * @param state  the current game state to persist
     */
    void save(UUID gameId, GameState state);

    /**
     * Retrieves a game state by its identifier.
     *
     * @param gameId game session identifier
     * @return the stored game state, or empty if not found
     */
    Optional<GameState> findById(UUID gameId);
}
