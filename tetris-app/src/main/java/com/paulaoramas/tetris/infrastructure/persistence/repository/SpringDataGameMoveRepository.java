package com.paulaoramas.tetris.infrastructure.persistence.repository;

import com.paulaoramas.tetris.infrastructure.persistence.entity.GameMoveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataGameMoveRepository extends JpaRepository<GameMoveEntity, Long> {

    long countByGameId(UUID gameId);

    List<GameMoveEntity> findByGameIdOrderByMoveNumberAsc(UUID gameId);
}
