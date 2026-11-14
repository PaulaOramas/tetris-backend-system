package com.paulaoramas.tetris.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "game_moves", uniqueConstraints = {
        @UniqueConstraint(name = "uq_game_move_sequence", columnNames = {"game_id", "move_number"})
})
public class GameMoveEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_id", nullable = false)
    private UUID gameId;

    @Column(name = "move_type", nullable = false, length = 20)
    private String moveType;

    @Column(name = "move_number", nullable = false)
    private int moveNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public GameMoveEntity() {}

    public GameMoveEntity(UUID gameId, String moveType, int moveNumber) {
        this.gameId = gameId;
        this.moveType = moveType;
        this.moveNumber = moveNumber;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public UUID getGameId() {
        return gameId;
    }

    public void setGameId(UUID gameId) {
        this.gameId = gameId;
    }

    public String getMoveType() {
        return moveType;
    }

    public void setMoveType(String moveType) {
        this.moveType = moveType;
    }

    public int getMoveNumber() {
        return moveNumber;
    }

    public void setMoveNumber(int moveNumber) {
        this.moveNumber = moveNumber;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
