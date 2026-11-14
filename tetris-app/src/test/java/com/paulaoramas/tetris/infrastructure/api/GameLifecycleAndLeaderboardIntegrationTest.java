package com.paulaoramas.tetris.infrastructure.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paulaoramas.tetris.domain.engine.GameEngine;
import com.paulaoramas.tetris.domain.model.GameState;
import com.paulaoramas.tetris.infrastructure.api.dto.AuthResponse;
import com.paulaoramas.tetris.infrastructure.api.dto.RegisterRequest;
import com.paulaoramas.tetris.infrastructure.persistence.AbstractPostgresIntegrationTest;
import com.paulaoramas.tetris.infrastructure.persistence.entity.GameEntity;
import com.paulaoramas.tetris.infrastructure.persistence.entity.GameStatus;
import com.paulaoramas.tetris.infrastructure.persistence.repository.SpringDataGameRepository;
import com.paulaoramas.tetris.infrastructure.security.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class GameLifecycleAndLeaderboardIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private SpringDataGameRepository gameRepository;

    @Autowired
    private GameEngine gameEngine;

    private String userToken;
    private UUID userId;

    @BeforeEach
    void setUp() {
        String uniqueUsername = "player_" + UUID.randomUUID().toString().substring(0, 8);
        AuthResponse auth = authService.register(new RegisterRequest(uniqueUsername, "securePassword123"));
        this.userToken = auth.token();
        this.userId = auth.userId();
    }

    @Test
    void unauthenticated_request_to_create_game_is_forbidden() throws Exception {
        mockMvc.perform(post("/api/games"))
                .andExpect(status().isForbidden());
    }

    @Test
    void authenticated_user_can_create_game_and_fetch_it_by_id() throws Exception {
        // 1. Create a game
        MvcResult createResult = mockMvc.perform(post("/api/games")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.status").value("ONGOING"))
                .andExpect(jsonPath("$.score").value(0))
                .andExpect(jsonPath("$.level").value(1))
                .andExpect(jsonPath("$.linesCleared").value(0))
                .andExpect(jsonPath("$.board", hasSize(20)))
                .andExpect(jsonPath("$.activePiece", notNullValue()))
                .andExpect(jsonPath("$.nextPiece", notNullValue()))
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        String gameId = objectMapper.readTree(responseBody).get("id").asText();

        // 2. Fetch the created game by ID
        mockMvc.perform(get("/api/games/" + gameId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(gameId))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.status").value("ONGOING"));
    }

    @Test
    void fetching_non_existent_game_returns_404_not_found() throws Exception {
        mockMvc.perform(get("/api/games/" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void leaderboard_is_publicly_accessible_without_auth_and_sorted_by_score() throws Exception {
        GameState state1 = gameEngine.startNewGame().addScore(5000);
        GameState state2 = gameEngine.startNewGame().addScore(15000);

        gameRepository.save(new GameEntity(UUID.randomUUID(), userId, GameStatus.GAME_OVER, state1));
        gameRepository.save(new GameEntity(UUID.randomUUID(), userId, GameStatus.GAME_OVER, state2));

        // Public GET request without Authorization header
        mockMvc.perform(get("/api/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].score", greaterThanOrEqualTo(15000)))
                .andExpect(jsonPath("$[0].rank").value(1));
    }

    @Test
    void authenticated_user_can_query_their_paginated_game_history() throws Exception {
        // Create 2 games for this user
        GameState state = gameEngine.startNewGame();
        gameRepository.save(new GameEntity(UUID.randomUUID(), userId, GameStatus.ONGOING, state));
        gameRepository.save(new GameEntity(UUID.randomUUID(), userId, GameStatus.GAME_OVER, state.addScore(800)));

        mockMvc.perform(get("/api/games/history?page=0&size=10")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(2)));
    }
}
