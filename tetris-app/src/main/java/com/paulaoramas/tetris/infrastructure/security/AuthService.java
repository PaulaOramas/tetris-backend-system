package com.paulaoramas.tetris.infrastructure.security;

import com.paulaoramas.tetris.infrastructure.api.dto.AuthResponse;
import com.paulaoramas.tetris.infrastructure.api.dto.LoginRequest;
import com.paulaoramas.tetris.infrastructure.api.dto.RegisterRequest;
import com.paulaoramas.tetris.infrastructure.persistence.entity.UserEntity;
import com.paulaoramas.tetris.infrastructure.persistence.repository.SpringDataUserRepository;
import com.paulaoramas.tetris.infrastructure.security.jwt.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final SpringDataUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            SpringDataUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken");
        }

        String hashedPassword = passwordEncoder.encode(request.password());
        UserEntity user = new UserEntity(request.username(), hashedPassword, "ROLE_PLAYER");
        UserEntity saved = userRepository.save(user);

        String token = jwtService.generateToken(saved.getId(), saved.getUsername(), saved.getRole());
        return new AuthResponse(token, saved.getUsername(), saved.getId());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        String token = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getUsername(), user.getId());
    }
}
