package com.paulaoramas.tetris.infrastructure.websocket;

import com.paulaoramas.tetris.domain.model.GameState;
import com.paulaoramas.tetris.infrastructure.websocket.dto.GameStateUpdateMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service responsible for broadcasting game events over STOMP topics.
 */
@Service
public class GameEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public GameEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastGameState(UUID gameId, String eventType, String status, GameState state, String lastMove) {
        GameStateUpdateMessage message = GameStateUpdateMessage.of(eventType, gameId, status, state, lastMove);
        messagingTemplate.convertAndSend("/topic/game/" + gameId, message);
    }
}
