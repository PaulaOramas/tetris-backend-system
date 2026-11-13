package com.paulaoramas.tetris.infrastructure.persistence.repository;

import com.paulaoramas.tetris.domain.model.GameState;
import com.paulaoramas.tetris.domain.port.GameRepository;
import com.paulaoramas.tetris.infrastructure.persistence.entity.GameEntity;
import com.paulaoramas.tetris.infrastructure.persistence.entity.GameStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Secondary adapter: implements domain output port {@link GameRepository} using Spring Data JPA.
 *
 * <p>Preserves Hexagonal Architecture:
 * - Domain port: {@code com.paulaoramas.tetris.domain.port.GameRepository}
 * - Infrastructure adapter: this class
 */
@Component
public class PostgresGameRepository implements GameRepository {

    private final SpringDataGameRepository springDataRepository;

    public PostgresGameRepository(SpringDataGameRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    @Transactional
    public void save(UUID gameId, GameState state) {
        GameEntity entity = springDataRepository.findById(gameId)
                .map(existing -> {
                    existing.setGameState(state);
                    return existing;
                })
                .orElseGet(() -> new GameEntity(gameId, null, GameStatus.ONGOING, state));

        springDataRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GameState> findById(UUID gameId) {
        return springDataRepository.findById(gameId)
                .map(GameEntity::getGameState);
    }
}
