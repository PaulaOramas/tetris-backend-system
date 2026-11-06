package com.paulaoramas.tetris.domain.engine;

public final class ScoringSystem {
    private static final int[] BASE_POINTS = {0, 100, 300, 500, 800};

    private ScoringSystem(){}

    public static int calculateScore (int linesCleared, int currentLevel){
        if (linesCleared < 1 || linesCleared > 4){
            return 0;
        }
        return BASE_POINTS[linesCleared] * currentLevel;
    }

    public static int calculateLevel (int totalLinesCleared){
        return totalLinesCleared / 10 + 1;
    }
}
