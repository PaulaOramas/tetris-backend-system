package com.paulaoramas.tetris.infrastructure.api.controller;

import com.paulaoramas.tetris.infrastructure.api.dto.LeaderboardEntryDto;
import com.paulaoramas.tetris.infrastructure.api.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final GameService gameService;

    public LeaderboardController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public ResponseEntity<List<LeaderboardEntryDto>> getLeaderboard() {
        List<LeaderboardEntryDto> leaderboard = gameService.getLeaderboard();
        return ResponseEntity.ok(leaderboard);
    }
}
