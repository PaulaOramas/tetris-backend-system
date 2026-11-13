package com.paulaoramas.tetris.domain.engine;

import com.paulaoramas.tetris.domain.model.Board;
import com.paulaoramas.tetris.domain.model.GameResult;
import com.paulaoramas.tetris.domain.model.GameState;
import com.paulaoramas.tetris.domain.model.Move;
import com.paulaoramas.tetris.domain.model.Piece;
import com.paulaoramas.tetris.domain.model.Tetromino;
import com.paulaoramas.tetris.domain.port.RandomPieceGenerator;

public class GameEngine {

    private static final int HARD_DROP_POINTS_PER_ROW = 2;

    private final RandomPieceGenerator pieceGenerator;

    public GameEngine(RandomPieceGenerator pieceGenerator) {
        this.pieceGenerator = pieceGenerator;
    }

    public GameState startNewGame() {
        Tetromino current = pieceGenerator.next();
        Tetromino next = pieceGenerator.next();
        return GameState.initial(current, next);
    }

    public GameResult applyMove(GameState state, Move move) {
        return switch (move) {
            case LEFT -> GameResult.ongoing(tryMove(state, state.activePiece().moveLeft()));
            case RIGHT -> GameResult.ongoing(tryMove(state, state.activePiece().moveRight()));
            case SOFT_DROP, GRAVITY -> stepDown(state);
            case HARD_DROP -> executeHardDrop(state);
            case ROTATE_CW -> GameResult.ongoing(tryRotate(state, true));
            case ROTATE_CCW -> GameResult.ongoing(tryRotate(state, false));
        };
    }

    public GameResult apply(GameState state, Move move) {
        return applyMove(state, move);
    }

    private GameState tryMove(GameState state, Piece candidate) {
        if (state.board().isValidPosition(candidate)) {
            return state.withActivePiece(candidate);
        }
        return state;
    }

    private GameState tryRotate(GameState state, boolean clockwise) {
        Piece rotated = clockwise
                ? state.activePiece().rotateCW()
                : state.activePiece().rotateCCW();

        if (state.board().isValidPosition(rotated)) {
            return state.withActivePiece(rotated);
        }

        Piece kickLeft = rotated.withOffset(-1, 0);
        if (state.board().isValidPosition(kickLeft)) {
            return state.withActivePiece(kickLeft);
        }

        Piece kickRight = rotated.withOffset(1, 0);
        if (state.board().isValidPosition(kickRight)) {
            return state.withActivePiece(kickRight);
        }

        return state;
    }

    private GameResult stepDown(GameState state) {
        Piece candidate = state.activePiece().moveDown();
        if (state.board().isValidPosition(candidate)) {
            return GameResult.ongoing(state.withActivePiece(candidate));
        }
        return lockPieceAndSpawnNext(state);
    }

    private GameResult executeHardDrop(GameState state) {
        Piece landedPiece = state.activePiece();
        int rowsDropped = 0;
        Piece candidate = landedPiece.moveDown();

        while (state.board().isValidPosition(candidate)) {
            landedPiece = candidate;
            rowsDropped++;
            candidate = landedPiece.moveDown();
        }

        GameState landedState = state.withActivePiece(landedPiece)
                .addScore(rowsDropped * HARD_DROP_POINTS_PER_ROW);
        return lockPieceAndSpawnNext(landedState);
    }

    private GameResult lockPieceAndSpawnNext(GameState state) {
        Board.ClearResult clearResult = state.board()
                .lockPiece(state.activePiece())
                .clearFullRows();
        int clearedLines = clearResult.cleared();
        int addedScore = ScoringSystem.calculateScore(clearedLines, state.level());
        GameState updatedState = state.withBoardAndScore(
                clearResult.board(),
                addedScore,
                clearedLines
        );

        Piece nextPiece = Piece.spawn(state.nextPiece());
        if (!updatedState.board().isValidPosition(nextPiece)) {
            return GameResult.gameOver(updatedState);
        }

        Tetromino futurePiece = pieceGenerator.next();
        return GameResult.ongoing(updatedState.advancePiece(state.nextPiece(), futurePiece));
    }
}
