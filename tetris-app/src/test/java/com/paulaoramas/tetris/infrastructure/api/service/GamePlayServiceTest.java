package com.paulaoramas.tetris.infrastructure.api.service;

import com.paulaoramas.tetris.domain.engine.BagPieceGenerator;
import com.paulaoramas.tetris.domain.engine.GameEngine;
import com.paulaoramas.tetris.domain.model.GameState;
import com.paulaoramas.tetris.infrastructure.api.dto.GameResponseDto;
import com.paulaoramas.tetris.infrastructure.persistence.AbstractPostgresIntegrationTest;
import com.paulaoramas.tetris.infrastructure.persistence.entity.GameEntity;
import com.paulaoramas.tetris.infrastructure.persistence.entity.GameMoveEntity;
import com.paulaoramas.tetris.infrastructure.persistence.entity.GameStatus;
import com.paulaoramas.tetris.infrastructure.persistence.repository.SpringDataGameMoveRepository;
import com.paulaoramas.tetris.infrastructure.persistence.repository.SpringDataGameRepository;
import com.paulaoramas.tetris.infrastructure.security.UserPrincipal;
import com.paulaoramas.tetris.infrastructure.websocket.GameEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

class GamePlayServiceTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private GamePlayService gamePlayService;

    @Autowired
    private SpringDataGameRepository gameRepository;

    @Autowired
    private SpringDataGameMoveRepository gameMoveRepository;

    @Autowired
    private com.paulaoramas.tetris.infrastructure.persistence.repository.SpringDataUserRepository userRepository;

    @Autowired
    private GameEngine gameEngine;

    @SpyBean
    private GameEventPublisher eventPublisher;

    private UUID gameId;
    private UserPrincipal player;

    @BeforeEach
    void setUp() {
        gameId = UUID.randomUUID();
        String username = "gamer_" + UUID.randomUUID().toString().substring(0, 8);
        var userEntity = userRepository.save(new com.paulaoramas.tetris.infrastructure.persistence.entity.UserEntity(username, "hash123", "ROLE_PLAYER"));
        player = new UserPrincipal(userEntity.getId(), username, "ROLE_PLAYER");

        GameState state = gameEngine.startNewGame();
        gameRepository.save(new GameEntity(gameId, player.id(), GameStatus.ONGOING, state));
    }

    @Test
    void applying_valid_moves_updates_state_and_records_audit_trail() {
        // Move LEFT
        GameResponseDto leftResult = gamePlayService.applyMove(gameId, "LEFT", player);
        assertThat(leftResult.activePiece().x()).isEqualTo(2); // spawn was 3, 3 - 1 = 2

        // Move RIGHT
        GameResponseDto rightResult = gamePlayService.applyMove(gameId, "RIGHT", player);
        assertThat(rightResult.activePiece().x()).isEqualTo(3);

        // Move HARD_DROP
        GameResponseDto dropResult = gamePlayService.applyMove(gameId, "HARD_DROP", player);
        assertThat(dropResult.score()).isGreaterThan(0);

        // Verify audit trail in game_moves table
        List<GameMoveEntity> moves = gameMoveRepository.findByGameIdOrderByMoveNumberAsc(gameId);
        assertThat(moves).hasSize(3);
        assertThat(moves.get(0).getMoveType()).isEqualTo("LEFT");
        assertThat(moves.get(0).getMoveNumber()).isEqualTo(1);
        assertThat(moves.get(1).getMoveType()).isEqualTo("RIGHT");
        assertThat(moves.get(1).getMoveNumber()).isEqualTo(2);
        assertThat(moves.get(2).getMoveType()).isEqualTo("HARD_DROP");
        assertThat(moves.get(2).getMoveNumber()).isEqualTo(3);

        // Verify event broadcast was triggered
        verify(eventPublisher).broadcastGameState(eq(gameId), eq("MOVE_APPLIED"), eq("ONGOING"), any(), eq("HARD_DROP"));
    }

    @Test
    void invalid_move_type_throws_bad_request() {
        assertThatThrownBy(() -> gamePlayService.applyMove(gameId, "SUPER_JUMP", player))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid move type");
    }

    @Test
    void applying_move_to_non_existent_game_throws_not_found() {
        UUID unknownId = UUID.randomUUID();
        assertThatThrownBy(() -> gamePlayService.applyMove(unknownId, "LEFT", player))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Game not found");
    }

    @Test
    void applying_move_to_finished_game_throws_bad_request() {
        GameEntity entity = gameRepository.findById(gameId).orElseThrow();
        entity.setStatus(GameStatus.GAME_OVER);
        gameRepository.save(entity);

        assertThatThrownBy(() -> gamePlayService.applyMove(gameId, "LEFT", player))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already over");
    }
}
