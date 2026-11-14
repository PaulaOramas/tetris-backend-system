package com.paulaoramas.tetris.infrastructure.security;

import java.util.UUID;

/**
 * Lightweight principal holding authenticated user details in the security context.
 */
public record UserPrincipal(UUID id, String username, String role) {}
