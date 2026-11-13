package com.paulaoramas.tetris.infrastructure.persistence.entity;

import com.paulaoramas.tetris.domain.model.GameState;
import com.paulaoramas.tetris.infrastructure.persistence.converter.GameStateJsonConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "games")
public class GameEntity {

    @Id
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GameStatus status;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private int level;

    @Column(name = "lines_cleared", nullable = false)
    private int linesCleared;

    @Convert(converter = GameStateJsonConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "game_state", columnDefinition = "jsonb", nullable = false)
    private GameState gameState;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public GameEntity() {}

    public GameEntity(UUID id, UUID userId, GameStatus status, GameState gameState) {
        this.id = id;
        this.userId = userId;
        this.status = status != null ? status : GameStatus.ONGOING;
        this.gameState = gameState;
        if (gameState != null) {
            this.score = gameState.score();
            this.level = gameState.level();
            this.linesCleared = gameState.linesCleared();
        }
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        if (this.status == null) {
            this.status = GameStatus.ONGOING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
        if (this.gameState != null) {
            this.score = this.gameState.score();
            this.level = this.gameState.level();
            this.linesCleared = this.gameState.linesCleared();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLinesCleared() {
        return linesCleared;
    }

    public void setLinesCleared(int linesCleared) {
        this.linesCleared = linesCleared;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        if (gameState != null) {
            this.score = gameState.score();
            this.level = gameState.level();
            this.linesCleared = gameState.linesCleared();
        }
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
