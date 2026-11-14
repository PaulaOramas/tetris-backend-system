package com.paulaoramas.tetris.infrastructure.api.dto;

import java.time.Instant;
import java.util.UUID;

public record LeaderboardEntryDto(
        int rank,
        UUID gameId,
        String username,
        int score,
        int level,
        int linesCleared,
        Instant playedAt
) {}
