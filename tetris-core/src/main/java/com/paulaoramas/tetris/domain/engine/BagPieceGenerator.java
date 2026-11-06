package com.paulaoramas.tetris.domain.engine;

import com.paulaoramas.tetris.domain.model.Tetromino;
import com.paulaoramas.tetris.domain.port.RandomPieceGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Official Tetris Guideline 7-bag piece generator.
 *
 * <p>Guarantees that each of the 7 Tetromino types appears exactly once per cycle (bag).
 * This prevents long droughts and limits repetition to at most 2 consecutive identical
 * pieces (last piece of one bag + first piece of the next bag).
 *
 * <p>Use the seeded constructor ({@link #BagPieceGenerator(long)}) for deterministic
 * sequences in tests or replays.
 */
public class BagPieceGenerator implements RandomPieceGenerator {

    private final List<Tetromino> bag = new ArrayList<>();
    private final Random random;

    /** Creates a generator with an unpredictable sequence (production use). */
    public BagPieceGenerator() {
        this.random = new Random();
    }

    /**
     * Creates a generator with a fixed seed for reproducible sequences.
     * Useful in integration tests and game replays.
     *
     * @param seed deterministic seed for the internal RNG
     */
    public BagPieceGenerator(long seed) {
        this.random = new Random(seed);
    }

    @Override
    public Tetromino next() {
        if (bag.isEmpty()) {
            refillAndShuffle();
        }
        return bag.remove(bag.size() - 1);
    }

    private void refillAndShuffle() {
        bag.addAll(Arrays.asList(Tetromino.values()));
        Collections.shuffle(bag, random);
    }
}
