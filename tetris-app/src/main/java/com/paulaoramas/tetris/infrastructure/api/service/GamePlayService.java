package com.paulaoramas.tetris.infrastructure.api.service;

import com.paulaoramas.tetris.domain.engine.GameEngine;
import com.paulaoramas.tetris.domain.model.GameResult;
import com.paulaoramas.tetris.domain.model.Move;
import com.paulaoramas.tetris.infrastructure.api.dto.GameResponseDto;
import com.paulaoramas.tetris.infrastructure.persistence.entity.GameEntity;
import com.paulaoramas.tetris.infrastructure.persistence.entity.GameMoveEntity;
import com.paulaoramas.tetris.infrastructure.persistence.entity.GameStatus;
import com.paulaoramas.tetris.infrastructure.persistence.repository.SpringDataGameMoveRepository;
import com.paulaoramas.tetris.infrastructure.persistence.repository.SpringDataGameRepository;
import com.paulaoramas.tetris.infrastructure.security.UserPrincipal;
import com.paulaoramas.tetris.infrastructure.websocket.GameEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class GamePlayService {

    private final GameEngine gameEngine;
    private final SpringDataGameRepository gameRepository;
    private final SpringDataGameMoveRepository gameMoveRepository;
    private final GameEventPublisher eventPublisher;

    public GamePlayService(
            GameEngine gameEngine,
            SpringDataGameRepository gameRepository,
            SpringDataGameMoveRepository gameMoveRepository,
            GameEventPublisher eventPublisher
    ) {
        this.gameEngine = gameEngine;
        this.gameRepository = gameRepository;
        this.gameMoveRepository = gameMoveRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public GameResponseDto applyMove(UUID gameId, String moveName, UserPrincipal principal) {
        Move move;
        try {
            move = Move.valueOf(moveName.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid move type: " + moveName);
        }

        GameEntity entity = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found: " + gameId));

        if (entity.getStatus() == GameStatus.GAME_OVER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot apply move: game is already over");
        }

        GameResult result = gameEngine.applyMove(entity.getGameState(), move);

        // Record move audit trail
        int moveNumber = (int) gameMoveRepository.countByGameId(gameId) + 1;
        gameMoveRepository.save(new GameMoveEntity(gameId, move.name(), moveNumber));

        if (result instanceof GameResult.Ongoing ongoing) {
            entity.setGameState(ongoing.state());
            GameEntity updated = gameRepository.save(entity);

            eventPublisher.broadcastGameState(
                    gameId,
                    "MOVE_APPLIED",
                    GameStatus.ONGOING.name(),
                    ongoing.state(),
                    move.name()
            );

            return GameResponseDto.fromEntity(updated);
        } else if (result instanceof GameResult.GameOver gameOver) {
            entity.setStatus(GameStatus.GAME_OVER);
            entity.setGameState(gameOver.finalState());
            GameEntity finished = gameRepository.save(entity);

            eventPublisher.broadcastGameState(
                    gameId,
                    "GAME_OVER",
                    GameStatus.GAME_OVER.name(),
                    gameOver.finalState(),
                    move.name()
            );

            return GameResponseDto.fromEntity(finished);
        }

        throw new IllegalStateException("Unhandled game result: " + result);
    }
}
