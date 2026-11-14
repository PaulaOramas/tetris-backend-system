package com.paulaoramas.tetris.infrastructure.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.paulaoramas.tetris.domain.engine.GameEngine;
import com.paulaoramas.tetris.domain.model.GameState;
import com.paulaoramas.tetris.infrastructure.persistence.AbstractPostgresIntegrationTest;
import com.paulaoramas.tetris.infrastructure.persistence.entity.GameEntity;
import com.paulaoramas.tetris.infrastructure.persistence.entity.GameStatus;
import com.paulaoramas.tetris.infrastructure.persistence.entity.UserEntity;
import com.paulaoramas.tetris.infrastructure.persistence.repository.SpringDataGameRepository;
import com.paulaoramas.tetris.infrastructure.persistence.repository.SpringDataUserRepository;
import com.paulaoramas.tetris.infrastructure.security.jwt.JwtService;
import com.paulaoramas.tetris.infrastructure.websocket.dto.GameStateUpdateMessage;
import com.paulaoramas.tetris.infrastructure.websocket.dto.PlayerMoveRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketStompIntegrationTest extends AbstractPostgresIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private SpringDataGameRepository gameRepository;

    @Autowired
    private SpringDataUserRepository userRepository;

    @Autowired
    private GameEngine gameEngine;

    @Autowired
    private JwtService jwtService;

    private WebSocketStompClient stompClient;
    private UUID gameId;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(mapper);

        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(converter);

        String username = "ws_player_" + UUID.randomUUID().toString().substring(0, 8);
        UserEntity user = userRepository.save(new UserEntity(username, "pass123", "ROLE_PLAYER"));
        jwtToken = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole());

        gameId = UUID.randomUUID();
        GameState state = gameEngine.startNewGame();
        gameRepository.save(new GameEntity(gameId, user.getId(), GameStatus.ONGOING, state));
    }

    @Test
    void connect_subscribe_to_game_channel_and_receive_move_broadcast() throws Exception {
        String wsUrl = "ws://localhost:" + port + "/ws";

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + jwtToken);

        CompletableFuture<GameStateUpdateMessage> messageFuture = new CompletableFuture<>();

        StompSession session = stompClient.connectAsync(
                wsUrl,
                new WebSocketHttpHeaders(),
                connectHeaders,
                new StompSessionHandlerAdapter() {}
        ).get(5, TimeUnit.SECONDS);

        assertThat(session.isConnected()).isTrue();

        // Subscribe to real-time game event topic
        session.subscribe("/topic/game/" + gameId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return GameStateUpdateMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messageFuture.complete((GameStateUpdateMessage) payload);
            }
        });

        // Brief delay for broker subscription registration
        Thread.sleep(400);

        // Send move command via STOMP /app/game.move
        session.send("/app/game.move", new PlayerMoveRequest(gameId, "LEFT"));

        // Await broadcasted update
        GameStateUpdateMessage received = messageFuture.get(5, TimeUnit.SECONDS);

        assertThat(received).isNotNull();
        assertThat(received.gameId()).isEqualTo(gameId);
        assertThat(received.eventType()).isEqualTo("MOVE_APPLIED");
        assertThat(received.lastMove()).isEqualTo("LEFT");
        assertThat(received.activePiece().x()).isEqualTo(2); // spawn is 3, 3 - 1 = 2

        session.disconnect();
    }
}
