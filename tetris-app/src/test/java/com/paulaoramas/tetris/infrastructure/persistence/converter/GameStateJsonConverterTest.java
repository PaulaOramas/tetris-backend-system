package com.paulaoramas.tetris.infrastructure.persistence.converter;

import com.paulaoramas.tetris.domain.model.Board;
import com.paulaoramas.tetris.domain.model.GameState;
import com.paulaoramas.tetris.domain.model.Piece;
import com.paulaoramas.tetris.domain.model.Tetromino;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GameStateJsonConverterTest {

    private GameStateJsonConverter converter;

    @BeforeEach
    void setUp() {
        converter = new GameStateJsonConverter();
    }

    @Test
    void null_state_converts_to_null_and_vice_versa() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
        assertThat(converter.convertToEntityAttribute("")).isNull();
        assertThat(converter.convertToEntityAttribute("   ")).isNull();
    }

    @Test
    void initial_game_state_roundtrip_preserves_all_fields() {
        GameState original = GameState.initial(Tetromino.T, Tetromino.I);

        String json = converter.convertToDatabaseColumn(original);
        assertThat(json).isNotNull().contains("\"nextPiece\":\"I\"");

        GameState restored = converter.convertToEntityAttribute(json);
        assertThat(restored).isNotNull();
        assertThat(restored.score()).isEqualTo(original.score());
        assertThat(restored.level()).isEqualTo(original.level());
        assertThat(restored.linesCleared()).isEqualTo(original.linesCleared());
        assertThat(restored.nextPiece()).isEqualTo(Tetromino.I);
        assertThat(restored.activePiece().type()).isEqualTo(Tetromino.T);
        assertThat(restored.activePiece().x()).isEqualTo(original.activePiece().x());
        assertThat(restored.activePiece().y()).isEqualTo(original.activePiece().y());
        assertThat(restored.activePiece().rotation()).isEqualTo(original.activePiece().rotation());
        assertThat(restored.board()).isEqualTo(original.board());
    }

    @Test
    void advanced_game_state_with_occupied_board_and_rotated_piece_roundtrip() {
        boolean[][] grid = new boolean[Board.ROWS][Board.COLUMNS];
        grid[19][0] = true;
        grid[19][1] = true;
        grid[18][5] = true;
        Board board = Board.of(grid);

        Piece activePiece = new Piece(Tetromino.S, 4, 12, 2);
        GameState original = new GameState(board, activePiece, Tetromino.Z, 4500, 4, 32);

        String json = converter.convertToDatabaseColumn(original);
        GameState restored = converter.convertToEntityAttribute(json);

        assertThat(restored).isEqualTo(original);
        assertThat(restored.board().isOccupied(0, 19)).isTrue();
        assertThat(restored.board().isOccupied(1, 19)).isTrue();
        assertThat(restored.board().isOccupied(5, 18)).isTrue();
        assertThat(restored.board().isOccupied(2, 19)).isFalse();
    }
}
