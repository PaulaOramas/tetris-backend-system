package com.paulaoramas.tetris.infrastructure.websocket;

import com.paulaoramas.tetris.infrastructure.api.dto.GameResponseDto;
import com.paulaoramas.tetris.infrastructure.api.service.GamePlayService;
import com.paulaoramas.tetris.infrastructure.security.UserPrincipal;
import com.paulaoramas.tetris.infrastructure.websocket.dto.PlayerMoveRequest;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
public class GameWebSocketController {

    private final GamePlayService gamePlayService;

    public GameWebSocketController(GamePlayService gamePlayService) {
        this.gamePlayService = gamePlayService;
    }

    /**
     * Handles incoming player moves via STOMP: destination /app/game.move
     * The updated state is broadcasted to subscribed clients on /topic/game/{gameId}.
     */
    @MessageMapping("/game.move")
    public GameResponseDto handlePlayerMove(
            @Payload PlayerMoveRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return gamePlayService.applyMove(request.gameId(), request.move(), principal);
    }
}
