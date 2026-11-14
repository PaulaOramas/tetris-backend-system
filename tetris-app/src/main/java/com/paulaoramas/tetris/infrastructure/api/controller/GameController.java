package com.paulaoramas.tetris.infrastructure.api.controller;

import com.paulaoramas.tetris.infrastructure.api.dto.GameResponseDto;
import com.paulaoramas.tetris.infrastructure.api.service.GamePlayService;
import com.paulaoramas.tetris.infrastructure.api.service.GameService;
import com.paulaoramas.tetris.infrastructure.security.UserPrincipal;
import com.paulaoramas.tetris.infrastructure.websocket.dto.PlayerMoveRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;
    private final GamePlayService gamePlayService;

    public GameController(GameService gameService, GamePlayService gamePlayService) {
        this.gameService = gameService;
        this.gamePlayService = gamePlayService;
    }

    @PostMapping
    public ResponseEntity<GameResponseDto> createGame(@AuthenticationPrincipal UserPrincipal principal) {
        GameResponseDto response = gameService.createGame(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameResponseDto> getGame(@PathVariable("id") UUID id) {
        GameResponseDto response = gameService.getGame(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/moves")
    public ResponseEntity<GameResponseDto> applyMove(
            @PathVariable("id") UUID id,
            @Valid @RequestBody PlayerMoveRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        GameResponseDto response = gamePlayService.applyMove(id, request.move(), principal);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<Page<GameResponseDto>> getMyHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<GameResponseDto> history = gameService.getUserGames(principal.id(), pageable);
        return ResponseEntity.ok(history);
    }
}
