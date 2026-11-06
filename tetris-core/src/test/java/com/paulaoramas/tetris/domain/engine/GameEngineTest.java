package com.paulaoramas.tetris.domain.engine;

import com.paulaoramas.tetris.domain.model.GameResult;
import com.paulaoramas.tetris.domain.model.GameState;
import com.paulaoramas.tetris.domain.model.Move;
import com.paulaoramas.tetris.domain.model.Piece;
import com.paulaoramas.tetris.domain.model.Tetromino;
import com.paulaoramas.tetris.domain.port.RandomPieceGenerator;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GameEngineTest {

    @Test
    void startsGameWithActiveAndNextPiece() {
        GameEngine engine = new GameEngine(generator(Tetromino.T, Tetromino.L));

        GameState state = engine.startNewGame();

        assertThat(state.activePiece()).isEqualTo(Piece.spawn(Tetromino.T));
        assertThat(state.nextPiece()).isEqualTo(Tetromino.L);
        assertThat(state.score()).isZero();
        assertThat(state.level()).isEqualTo(1);
        assertThat(state.linesCleared()).isZero();
    }

    @Test
    void movesPieceLeftWithoutMutatingPreviousState() {
        GameEngine engine = new GameEngine(generator(Tetromino.T, Tetromino.L));
        GameState initialState = engine.startNewGame();

        GameResult result = engine.applyMove(initialState, Move.LEFT);

        assertThat(result).isInstanceOf(GameResult.Ongoing.class);
        GameState movedState = ((GameResult.Ongoing) result).state();
        assertThat(movedState.activePiece().x()).isEqualTo(2);
        assertThat(initialState.activePiece().x()).isEqualTo(3);
    }

    @Test
    void hardDropLocksPieceScoresDistanceAndSpawnsNextPiece() {
        GameEngine engine = new GameEngine(generator(Tetromino.I, Tetromino.O, Tetromino.T));
        GameState initialState = engine.startNewGame();

        GameResult result = engine.applyMove(initialState, Move.HARD_DROP);

        assertThat(result).isInstanceOf(GameResult.Ongoing.class);
        GameState droppedState = ((GameResult.Ongoing) result).state();
        assertThat(droppedState.activePiece()).isEqualTo(Piece.spawn(Tetromino.O));
        assertThat(droppedState.score()).isEqualTo(36);
        assertThat(droppedState.board().isOccupied(3, 19)).isTrue();
        assertThat(droppedState.board().isOccupied(6, 19)).isTrue();
    }

    private static RandomPieceGenerator generator(Tetromino... pieces) {
        Iterator<Tetromino> sequence = List.of(pieces).iterator();
        return sequence::next;
    }
}
