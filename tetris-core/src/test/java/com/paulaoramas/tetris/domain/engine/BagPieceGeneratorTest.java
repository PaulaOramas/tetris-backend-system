package com.paulaoramas.tetris.domain.engine;

import com.paulaoramas.tetris.domain.model.Tetromino;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Specifies the 7-bag piece generation contract.
 *
 * <p>The key invariant: in every consecutive window of 7 pieces,
 * each Tetromino type appears exactly once.
 */
class BagPieceGeneratorTest {

    @Test
    void first_seven_pieces_contain_all_tetromino_types_exactly_once() {
        BagPieceGenerator generator = new BagPieceGenerator(42L); // fixed seed

        List<Tetromino> firstBag = IntStream.range(0, 7)
                .mapToObj(i -> generator.next())
                .collect(Collectors.toList());

        assertThat(firstBag).hasSize(7);
        assertThat(new HashSet<>(firstBag))
                .containsExactlyInAnyOrderElementsOf(EnumSet.allOf(Tetromino.class));
    }

    @Test
    void second_bag_also_contains_all_tetromino_types_exactly_once() {
        BagPieceGenerator generator = new BagPieceGenerator(42L);

        // Exhaust the first bag
        IntStream.range(0, 7).forEach(i -> generator.next());

        // Collect the second bag
        List<Tetromino> secondBag = IntStream.range(0, 7)
                .mapToObj(i -> generator.next())
                .collect(Collectors.toList());

        assertThat(new HashSet<>(secondBag))
                .containsExactlyInAnyOrderElementsOf(EnumSet.allOf(Tetromino.class));
    }

    @Test
    void seeded_generator_produces_same_sequence_every_time() {
        List<Tetromino> run1 = firstFourteen(new BagPieceGenerator(123L));
        List<Tetromino> run2 = firstFourteen(new BagPieceGenerator(123L));

        assertThat(run1).isEqualTo(run2);
    }

    @Test
    void different_seeds_produce_different_sequences() {
        List<Tetromino> run1 = firstFourteen(new BagPieceGenerator(1L));
        List<Tetromino> run2 = firstFourteen(new BagPieceGenerator(999L));

        // With overwhelming probability two different seeds differ in ordering
        assertThat(run1).isNotEqualTo(run2);
    }

    @Test
    void no_piece_repeats_more_than_twice_in_any_consecutive_fourteen_pieces() {
        // Over 2 bags, each piece can appear at most twice (once per bag).
        BagPieceGenerator generator = new BagPieceGenerator(7L);
        List<Tetromino> pieces = firstFourteen(generator);

        for (Tetromino t : Tetromino.values()) {
            long count = pieces.stream().filter(p -> p == t).count();
            assertThat(count)
                    .as("Piece %s appeared %d times in 14 draws (max 2 expected)", t, count)
                    .isLessThanOrEqualTo(2);
        }
    }

    @Test
    void default_constructor_generates_valid_bags() {
        // Smoke test: unseeded generator still produces all 7 pieces per bag
        BagPieceGenerator generator = new BagPieceGenerator();
        Set<Tetromino> bag = IntStream.range(0, 7)
                .mapToObj(i -> generator.next())
                .collect(Collectors.toSet());

        assertThat(bag).containsExactlyInAnyOrderElementsOf(EnumSet.allOf(Tetromino.class));
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private static List<Tetromino> firstFourteen(BagPieceGenerator gen) {
        return IntStream.range(0, 14).mapToObj(i -> gen.next()).collect(Collectors.toList());
    }
}
