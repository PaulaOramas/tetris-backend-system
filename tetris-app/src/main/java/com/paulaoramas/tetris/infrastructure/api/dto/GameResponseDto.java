package com.paulaoramas.tetris.infrastructure.api.dto;

import com.paulaoramas.tetris.domain.model.Board;
import com.paulaoramas.tetris.domain.model.GameState;
import com.paulaoramas.tetris.infrastructure.persistence.entity.GameEntity;
import com.paulaoramas.tetris.infrastructure.persistence.entity.GameStatus;

import java.time.Instant;
import java.util.UUID;

public record GameResponseDto(
        UUID id,
        UUID userId,
        GameStatus status,
        int score,
        int level,
        int linesCleared,
        boolean[][] board,
        PieceDto activePiece,
        String nextPiece,
        Instant createdAt,
        Instant updatedAt
) {

    public record PieceDto(String type, int x, int y, int rotation) {}

    public static GameResponseDto fromEntity(GameEntity entity) {
        GameState state = entity.getGameState();
        boolean[][] grid = new boolean[Board.ROWS][Board.COLUMNS];
        PieceDto pieceDto = null;
        String nextPiece = null;

        if (state != null) {
            if (state.board() != null) {
                for (int r = 0; r < Board.ROWS; r++) {
                    for (int c = 0; c < Board.COLUMNS; c++) {
                        grid[r][c] = state.board().isOccupied(c, r);
                    }
                }
            }
            if (state.activePiece() != null) {
                pieceDto = new PieceDto(
                        state.activePiece().type().name(),
                        state.activePiece().x(),
                        state.activePiece().y(),
                        state.activePiece().rotation()
                );
            }
            if (state.nextPiece() != null) {
                nextPiece = state.nextPiece().name();
            }
        }

        return new GameResponseDto(
                entity.getId(),
                entity.getUserId(),
                entity.getStatus(),
                entity.getScore(),
                entity.getLevel(),
                entity.getLinesCleared(),
                grid,
                pieceDto,
                nextPiece,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
