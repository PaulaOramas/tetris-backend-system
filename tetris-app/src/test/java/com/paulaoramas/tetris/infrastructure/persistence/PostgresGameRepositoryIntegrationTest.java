package com.paulaoramas.tetris.infrastructure.persistence;

import com.paulaoramas.tetris.domain.model.Board;
import com.paulaoramas.tetris.domain.model.GameState;
import com.paulaoramas.tetris.domain.model.Piece;
import com.paulaoramas.tetris.domain.model.Tetromino;
import com.paulaoramas.tetris.domain.port.GameRepository;
import com.paulaoramas.tetris.infrastructure.persistence.entity.GameEntity;
import com.paulaoramas.tetris.infrastructure.persistence.entity.GameStatus;
import com.paulaoramas.tetris.infrastructure.persistence.repository.PostgresGameRepository;
import com.paulaoramas.tetris.infrastructure.persistence.repository.SpringDataGameRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests verifying the persistence contract between the domain and real PostgreSQL.
 *
 * <p>Tests the {@link PostgresGameRepository} adapter fulfilling the domain's {@link GameRepository} port.
 * Uses ephemeral Testcontainers PostgreSQL instance with JSONB column support and Flyway schema.
 */
class PostgresGameRepositoryIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private PostgresGameRepository gameRepository;

    @Autowired
    private SpringDataGameRepository springDataRepository;

    @Test
    void save_and_find_by_id_returns_identical_initial_game_state() {
        UUID gameId = UUID.randomUUID();
        GameState initialState = GameState.initial(Tetromino.T, Tetromino.I);

        gameRepository.save(gameId, initialState);

        Optional<GameState> retrieved = gameRepository.findById(gameId);

        assertThat(retrieved).isPresent();
        GameState state = retrieved.get();
        assertThat(state.score()).isEqualTo(0);
        assertThat(state.level()).isEqualTo(1);
        assertThat(state.linesCleared()).isEqualTo(0);
        assertThat(state.activePiece().type()).isEqualTo(Tetromino.T);
        assertThat(state.nextPiece()).isEqualTo(Tetromino.I);
        assertThat(state.board()).isEqualTo(initialState.board());
    }

    @Test
    void save_and_find_by_id_preserves_complex_game_state_with_locked_cells_and_rotation() {
        UUID gameId = UUID.randomUUID();

        // Construct a realistic mid-game board with locked cells
        boolean[][] grid = new boolean[Board.ROWS][Board.COLUMNS];
        grid[19][0] = true;
        grid[19][1] = true;
        grid[19][2] = true;
        grid[18][4] = true;
        grid[18][5] = true;
        Board boardWithLockedCells = Board.of(grid);

        Piece activePiece = new Piece(Tetromino.L, 5, 14, 3);
        GameState complexState = new GameState(
                boardWithLockedCells,
                activePiece,
                Tetromino.Z,
                14500,
                5,
                48
        );

        gameRepository.save(gameId, complexState);

        Optional<GameState> retrieved = gameRepository.findById(gameId);

        assertThat(retrieved).isPresent();
        GameState state = retrieved.get();

        assertThat(state).isEqualTo(complexState);
        assertThat(state.board().isOccupied(0, 19)).isTrue();
        assertThat(state.board().isOccupied(1, 19)).isTrue();
        assertThat(state.board().isOccupied(2, 19)).isTrue();
        assertThat(state.board().isOccupied(4, 18)).isTrue();
        assertThat(state.board().isOccupied(3, 19)).isFalse(); // unoccupied
        assertThat(state.activePiece()).isEqualTo(activePiece);
        assertThat(state.nextPiece()).isEqualTo(Tetromino.Z);
    }

    @Test
    void updating_existing_game_state_updates_record_without_creating_duplicates() {
        UUID gameId = UUID.randomUUID();
        GameState state1 = GameState.initial(Tetromino.O, Tetromino.S);
        gameRepository.save(gameId, state1);

        // Advance state
        GameState state2 = state1.addScore(400).withBoardAndScore(state1.board(), 800, 4);
        gameRepository.save(gameId, state2);

        Optional<GameState> retrieved = gameRepository.findById(gameId);
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().score()).isEqualTo(1200);
        assertThat(retrieved.get().linesCleared()).isEqualTo(4);

        // Verify entity fields are also synchronized in DB columns
        Optional<GameEntity> entityOpt = springDataRepository.findById(gameId);
        assertThat(entityOpt).isPresent();
        GameEntity entity = entityOpt.get();
        assertThat(entity.getScore()).isEqualTo(1200);
        assertThat(entity.getLinesCleared()).isEqualTo(4);
        assertThat(entity.getStatus()).isEqualTo(GameStatus.ONGOING);
    }

    @Test
    void find_by_id_returns_empty_for_non_existent_game() {
        UUID nonExistentId = UUID.randomUUID();
        Optional<GameState> result = gameRepository.findById(nonExistentId);
        assertThat(result).isEmpty();
    }
}
