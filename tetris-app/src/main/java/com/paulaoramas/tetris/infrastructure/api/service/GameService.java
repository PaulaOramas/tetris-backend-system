package com.paulaoramas.tetris.infrastructure.api.service;

import com.paulaoramas.tetris.domain.engine.GameEngine;
import com.paulaoramas.tetris.domain.model.GameState;
import com.paulaoramas.tetris.infrastructure.api.dto.GameResponseDto;
import com.paulaoramas.tetris.infrastructure.api.dto.LeaderboardEntryDto;
import com.paulaoramas.tetris.infrastructure.persistence.entity.GameEntity;
import com.paulaoramas.tetris.infrastructure.persistence.entity.GameStatus;
import com.paulaoramas.tetris.infrastructure.persistence.entity.UserEntity;
import com.paulaoramas.tetris.infrastructure.persistence.repository.SpringDataGameRepository;
import com.paulaoramas.tetris.infrastructure.persistence.repository.SpringDataUserRepository;
import com.paulaoramas.tetris.infrastructure.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GameService {

    private final GameEngine gameEngine;
    private final SpringDataGameRepository gameRepository;
    private final SpringDataUserRepository userRepository;

    public GameService(
            GameEngine gameEngine,
            SpringDataGameRepository gameRepository,
            SpringDataUserRepository userRepository
    ) {
        this.gameEngine = gameEngine;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public GameResponseDto createGame(UserPrincipal principal) {
        UUID gameId = UUID.randomUUID();
        GameState initialState = gameEngine.startNewGame();

        UUID userId = principal != null ? principal.id() : null;
        GameEntity entity = new GameEntity(gameId, userId, GameStatus.ONGOING, initialState);
        GameEntity saved = gameRepository.save(entity);

        return GameResponseDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public GameResponseDto getGame(UUID gameId) {
        GameEntity entity = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found: " + gameId));
        return GameResponseDto.fromEntity(entity);
    }

    @Transactional(readOnly = true)
    public Page<GameResponseDto> getUserGames(UUID userId, Pageable pageable) {
        return gameRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(GameResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryDto> getLeaderboard() {
        List<GameEntity> topGames = gameRepository.findTop10ByOrderByScoreDesc();

        List<UUID> userIds = topGames.stream()
                .map(GameEntity::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<UUID, String> usernameMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, UserEntity::getUsername));

        List<LeaderboardEntryDto> entries = new ArrayList<>();
        int rank = 1;
        for (GameEntity game : topGames) {
            String username = game.getUserId() != null
                    ? usernameMap.getOrDefault(game.getUserId(), "Anonymous")
                    : "Anonymous";

            entries.add(new LeaderboardEntryDto(
                    rank++,
                    game.getId(),
                    username,
                    game.getScore(),
                    game.getLevel(),
                    game.getLinesCleared(),
                    game.getCreatedAt()
            ));
        }

        return entries;
    }
}
