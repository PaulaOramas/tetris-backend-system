package com.paulaoramas.tetris.infrastructure.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long EXPIRATION_MS = 3600000; // 1 hour

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, EXPIRATION_MS);
    }

    @Test
    void generateToken_creates_valid_token_with_correct_claims() {
        UUID userId = UUID.randomUUID();
        String username = "pro_tetris_player";
        String role = "ROLE_PLAYER";

        String token = jwtService.generateToken(userId, username, role);

        assertThat(token).isNotBlank();
        assertThat(jwtService.validateToken(token)).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo(username);
        assertThat(jwtService.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtService.extractRole(token)).isEqualTo(role);
    }

    @Test
    void validateToken_returns_false_for_tampered_token() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, "player1", "ROLE_PLAYER");
        String tamperedToken = token.substring(0, token.length() - 5) + "abcde";

        assertThat(jwtService.validateToken(tamperedToken)).isFalse();
    }

    @Test
    void validateToken_returns_false_for_expired_token() {
        JwtService shortLivedService = new JwtService(TEST_SECRET, -1000); // already expired
        String token = shortLivedService.generateToken(UUID.randomUUID(), "player1", "ROLE_PLAYER");

        assertThat(jwtService.validateToken(token)).isFalse();
    }

    @Test
    void validateToken_returns_false_for_invalid_string() {
        assertThat(jwtService.validateToken("not-a-valid-token")).isFalse();
        assertThat(jwtService.validateToken("")).isFalse();
    }
}
