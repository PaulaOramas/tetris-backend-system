package com.paulaoramas.tetris.infrastructure.config;

import com.paulaoramas.tetris.domain.engine.BagPieceGenerator;
import com.paulaoramas.tetris.domain.engine.GameEngine;
import com.paulaoramas.tetris.domain.port.RandomPieceGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameEngineConfig {

    @Bean
    public RandomPieceGenerator randomPieceGenerator() {
        return new BagPieceGenerator();
    }

    @Bean
    public GameEngine gameEngine(RandomPieceGenerator randomPieceGenerator) {
        return new GameEngine(randomPieceGenerator);
    }
}
