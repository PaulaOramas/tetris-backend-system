package com.paulaoramas.tetris.infrastructure.websocket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PlayerMoveRequest(
        @NotNull(message = "Game ID is required")
        UUID gameId,

        @NotBlank(message = "Move type is required (e.g. LEFT, RIGHT, SOFT_DROP, HARD_DROP, ROTATE_CLOCKWISE, ROTATE_COUNTER_CLOCKWISE)")
        String move
) {}
