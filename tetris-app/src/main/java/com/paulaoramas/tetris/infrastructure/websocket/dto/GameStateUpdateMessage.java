package com.paulaoramas.tetris.infrastructure.websocket.dto;

import com.paulaoramas.tetris.domain.model.Board;
import com.paulaoramas.tetris.domain.model.GameState;

import java.time.Instant;
import java.util.UUID;

/**
 * Real-time event payload broadcasted across STOMP game channels.
 */
public record GameStateUpdateMessage(
        String eventType,
        UUID gameId,
        String status,
        int score,
        int level,
        int linesCleared,
        boolean[][] board,
        PieceDto activePiece,
        String nextPiece,
        String lastMove,
        Instant timestamp
) {

    public record PieceDto(String type, int x, int y, int rotation) {}

    public static GameStateUpdateMessage of(
            String eventType,
            UUID gameId,
            String status,
            GameState state,
            String lastMove
    ) {
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

        return new GameStateUpdateMessage(
                eventType,
                gameId,
                status,
                state != null ? state.score() : 0,
                state != null ? state.level() : 1,
                state != null ? state.linesCleared() : 0,
                grid,
                pieceDto,
                nextPiece,
                lastMove,
                Instant.now()
        );
    }
}
