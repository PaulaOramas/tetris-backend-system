package com.paulaoramas.tetris.infrastructure.api.dto;

import java.util.UUID;

public record AuthResponse(
        String token,
        String username,
        UUID userId
) {}
