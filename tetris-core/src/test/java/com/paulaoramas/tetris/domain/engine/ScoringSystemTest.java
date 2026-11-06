package com.paulaoramas.tetris.domain.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Specifies the Nintendo scoring system for line clears.
 *
 * <p>Official Nintendo scoring (level multiplied):
 * <ul>
 *   <li>0 lines → 0 points</li>
 *   <li>1 line  → 100 × level</li>
 *   <li>2 lines → 300 × level</li>
 *   <li>3 lines → 500 × level</li>
 *   <li>4 lines → 800 × level (Tetris!)</li>
 * </ul>
 */
class ScoringSystemTest {

    @Test
    void zero_lines_cleared_always_gives_zero_points() {
        assertThat(ScoringSystem.calculateScore(0, 1)).isZero();
        assertThat(ScoringSystem.calculateScore(0, 5)).isZero();
        assertThat(ScoringSystem.calculateScore(0, 10)).isZero();
    }

    @ParameterizedTest(name = "{0} lines at level 1 = {1} points")
    @CsvSource({
            "1, 100",
            "2, 300",
            "3, 500",
            "4, 800"
    })
    void single_clear_scores_correct_base_points_at_level_one(int lines, int expected) {
        assertThat(ScoringSystem.calculateScore(lines, 1)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{0} lines at level {1} = {2} points")
    @CsvSource({
            "1, 2, 200",
            "2, 3, 900",
            "4, 5, 4000",
            "3, 10, 5000"
    })
    void score_is_multiplied_by_current_level(int lines, int level, int expected) {
        assertThat(ScoringSystem.calculateScore(lines, level)).isEqualTo(expected);
    }

    @Test
    void tetris_four_lines_at_level_one_awards_800_points() {
        // Tetris is the highest single-clear reward
        assertThat(ScoringSystem.calculateScore(4, 1)).isEqualTo(800);
    }

    @Test
    void level_starts_at_one_with_zero_lines_cleared() {
        assertThat(ScoringSystem.calculateLevel(0)).isEqualTo(1);
    }

    @Test
    void level_increases_every_ten_lines_cleared() {
        assertThat(ScoringSystem.calculateLevel(9)).isEqualTo(1);
        assertThat(ScoringSystem.calculateLevel(10)).isEqualTo(2);
        assertThat(ScoringSystem.calculateLevel(19)).isEqualTo(2);
        assertThat(ScoringSystem.calculateLevel(20)).isEqualTo(3);
    }

    @Test
    void level_at_forty_lines_is_five() {
        assertThat(ScoringSystem.calculateLevel(40)).isEqualTo(5);
    }
}
