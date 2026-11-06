package com.paulaoramas.tetris.domain.engine;

import com.paulaoramas.tetris.domain.model.Board;
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
    void movesPieceRight() {
        GameState state = stateWith(Tetromino.O, 3, 5, 0, Board.empty());

        GameState moved = ongoing(new GameEngine(generator())
                .applyMove(state, Move.RIGHT));

        assertThat(moved.activePiece().x()).isEqualTo(4);
    }

    @Test
    void rejectsHorizontalMovesBeyondBoardEdges() {
        GameEngine engine = new GameEngine(generator());
        GameState atLeftEdge = stateWith(Tetromino.O, 0, 5, 0, Board.empty());
        GameState atRightEdge = stateWith(Tetromino.O, 8, 5, 0, Board.empty());

        GameState leftResult = ongoing(engine.applyMove(atLeftEdge, Move.LEFT));
        GameState rightResult = ongoing(engine.applyMove(atRightEdge, Move.RIGHT));

        assertThat(leftResult.activePiece()).isEqualTo(atLeftEdge.activePiece());
        assertThat(rightResult.activePiece()).isEqualTo(atRightEdge.activePiece());
    }

    @Test
    void rejectsMoveThatWouldCollideWithLockedCell() {
        boolean[][] grid = new boolean[Board.ROWS][Board.COLUMNS];
        grid[5][4] = true;
        GameState state = stateWith(Tetromino.O, 2, 5, 0, Board.of(grid));

        GameState result = ongoing(new GameEngine(generator())
                .applyMove(state, Move.RIGHT));

        assertThat(result.activePiece()).isEqualTo(state.activePiece());
    }

    @Test
    void softDropAndGravityMovePieceDownOneRow() {
        GameEngine engine = new GameEngine(generator());
        GameState state = stateWith(Tetromino.O, 3, 5, 0, Board.empty());

        GameState softDropped = ongoing(engine.applyMove(state, Move.SOFT_DROP));
        GameState gravityMoved = ongoing(engine.applyMove(state, Move.GRAVITY));

        assertThat(softDropped.activePiece().y()).isEqualTo(6);
        assertThat(gravityMoved.activePiece().y()).isEqualTo(6);
    }

    @Test
    void softDropLocksPieceWhenItCannotMoveDownAndSpawnsNextPiece() {
        GameState state = stateWith(Tetromino.I, 3, 18, 0, Board.empty());
        GameEngine engine = new GameEngine(generator(Tetromino.T));

        GameState result = ongoing(engine.applyMove(state, Move.SOFT_DROP));

        assertThat(result.activePiece()).isEqualTo(Piece.spawn(Tetromino.O));
        assertThat(result.nextPiece()).isEqualTo(Tetromino.T);
        for (int col = 3; col <= 6; col++) {
            assertThat(result.board().isOccupied(col, 19)).isTrue();
        }
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

    @Test
    void lockingPieceClearsLineAndUpdatesScoreLinesAndLevel() {
        boolean[][] grid = new boolean[Board.ROWS][Board.COLUMNS];
        for (int col = 0; col < Board.COLUMNS; col++) {
            if (col < 3 || col > 6) {
                grid[19][col] = true;
            }
        }
        GameState state = new GameState(
                Board.of(grid),
                new Piece(Tetromino.I, 3, 18, 0),
                Tetromino.O,
                50,
                1,
                9
        );
        GameEngine engine = new GameEngine(generator(Tetromino.T));

        GameState result = ongoing(engine.applyMove(state, Move.SOFT_DROP));

        assertThat(result.score()).isEqualTo(150);
        assertThat(result.linesCleared()).isEqualTo(10);
        assertThat(result.level()).isEqualTo(2);
        assertThat(result.board().isEmptyRow(19)).isTrue();
        assertThat(result.activePiece()).isEqualTo(Piece.spawn(Tetromino.O));
    }

    private static GameState stateWith(
            Tetromino activeType,
            int x,
            int y,
            int rotation,
            Board board
    ) {
        return new GameState(board, new Piece(activeType, x, y, rotation), Tetromino.O, 0, 1, 0);
    }

    private static GameState ongoing(GameResult result) {
        assertThat(result).isInstanceOf(GameResult.Ongoing.class);
        return ((GameResult.Ongoing) result).state();
    }

    private static RandomPieceGenerator generator(Tetromino... pieces) {
        Iterator<Tetromino> sequence = List.of(pieces).iterator();
        return sequence::next;
    }
}
