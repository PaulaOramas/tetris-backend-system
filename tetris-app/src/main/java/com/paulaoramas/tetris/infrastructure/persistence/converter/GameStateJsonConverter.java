package com.paulaoramas.tetris.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paulaoramas.tetris.domain.model.Board;
import com.paulaoramas.tetris.domain.model.GameState;
import com.paulaoramas.tetris.domain.model.Piece;
import com.paulaoramas.tetris.domain.model.Tetromino;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class GameStateJsonConverter implements AttributeConverter<GameState, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public record PieceDto(String type, int x, int y, int rotation) {}

    public record GameStateDto(
            boolean[][] board,
            PieceDto activePiece,
            String nextPiece,
            int score,
            int level,
            int linesCleared
    ) {}

    @Override
    public String convertToDatabaseColumn(GameState state) {
        if (state == null) {
            return null;
        }
        try {
            boolean[][] grid = new boolean[Board.ROWS][Board.COLUMNS];
            if (state.board() != null) {
                for (int r = 0; r < Board.ROWS; r++) {
                    for (int c = 0; c < Board.COLUMNS; c++) {
                        grid[r][c] = state.board().isOccupied(c, r);
                    }
                }
            }

            PieceDto pieceDto = null;
            if (state.activePiece() != null) {
                pieceDto = new PieceDto(
                        state.activePiece().type().name(),
                        state.activePiece().x(),
                        state.activePiece().y(),
                        state.activePiece().rotation()
                );
            }

            String nextPiece = state.nextPiece() != null ? state.nextPiece().name() : null;

            GameStateDto dto = new GameStateDto(
                    grid,
                    pieceDto,
                    nextPiece,
                    state.score(),
                    state.level(),
                    state.linesCleared()
            );

            return OBJECT_MAPPER.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize GameState to JSON", e);
        }
    }

    @Override
    public GameState convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        try {
            GameStateDto dto = OBJECT_MAPPER.readValue(dbData, GameStateDto.class);
            Board board = dto.board() != null ? Board.of(dto.board()) : Board.empty();

            Piece piece = null;
            if (dto.activePiece() != null && dto.activePiece().type() != null) {
                piece = new Piece(
                        Tetromino.valueOf(dto.activePiece().type()),
                        dto.activePiece().x(),
                        dto.activePiece().y(),
                        dto.activePiece().rotation()
                );
            }

            Tetromino next = dto.nextPiece() != null ? Tetromino.valueOf(dto.nextPiece()) : null;

            return new GameState(board, piece, next, dto.score(), dto.level(), dto.linesCleared());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to deserialize JSON to GameState", e);
        }
    }
}
