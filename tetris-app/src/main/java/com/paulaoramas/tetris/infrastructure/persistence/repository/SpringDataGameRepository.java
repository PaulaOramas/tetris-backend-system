package com.paulaoramas.tetris.infrastructure.persistence.repository;

import com.paulaoramas.tetris.infrastructure.persistence.entity.GameEntity;
import com.paulaoramas.tetris.infrastructure.persistence.entity.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataGameRepository extends JpaRepository<GameEntity, UUID> {

    List<GameEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<GameEntity> findTop10ByStatusOrderByScoreDesc(GameStatus status);
}
