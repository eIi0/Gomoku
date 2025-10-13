package controllers.ai;

import controllers.PlayerController;
import gamecore.Coords;
import gamecore.GomokuBoard;
import gamecore.enums.Player;
import gamecore.enums.TileState;
import gamecore.enums.WinnerState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AI_Elagage extends PlayerController {

	private final Random rnd = new Random();
	private long nodesVisited = 0;
	private long nodesPruned = 0;
	private int minimaxDepth;
	private Player playerColor;

	public AI_Elagage(int minimaxDepth, Player playerColor) {
		this.minimaxDepth = minimaxDepth;
		this.playerColor = playerColor;
	}

	public AI_Elagage() {
		super();
	}

	public Coords[] getAvailableMoves(GomokuBoard board) {
		List<Coords> moves = new ArrayList<>();
		for (int row = 0; row < GomokuBoard.size; row++) {
			for (int col = 0; col < GomokuBoard.size; col++) {
				Coords c = new Coords(row, col);
				if (board.get(c) == TileState.Empty) moves.add(c);
			}
		}
		return moves.toArray(new Coords[0]);
	}

	// --- Fonction principale minimax avec élagage alpha-beta ---
	public EvaluationVariable minimaxAlphaBeta(GomokuBoard board, int depth, boolean isMaximizingPlayer, Player currentPlayer, int alpha, int beta) {
		nodesVisited++;

		// 1️⃣ Vérifie la victoire
		WinnerState winner = board.getWinnerState();
		if (winner == WinnerState.Black || winner == WinnerState.White) {
			int score = (winner == WinnerState.Black) ? 1000000 : -1000000;
			if (playerColor == Player.White) score = -score;
			return new EvaluationVariable(new Coords(), score);
		}

		// 2️⃣ Profondeur maximale
		if (depth == 0) {
			return new EvaluationVariable(new Coords(), evaluateBoard(board));
		}

		// 3️⃣ Coups disponibles
		Coords[] moves = getAvailableMoves(board);
		if (moves.length == 0) return new EvaluationVariable(new Coords(), 0);

		List<Coords> bestMoves = new ArrayList<>();
		int bestEval = isMaximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;

		for (int i = 0; i < moves.length; i++) {
			Coords move = moves[i];
			GomokuBoard clonedBoard = board.clone();
			TileState currentTile = (currentPlayer == Player.White) ? TileState.White : TileState.Black;
			clonedBoard.set(move, currentTile);

			// 4️⃣ Détecter victoire immédiate pour le joueur actuel
			WinnerState immediateWinner = clonedBoard.getWinnerState();
			if (immediateWinner == WinnerState.Black || immediateWinner == WinnerState.White) {
				int immediateScore = (immediateWinner == WinnerState.Black) ? 1000000 : -1000000;
				if (playerColor == Player.White) immediateScore = -immediateScore;
				return new EvaluationVariable(move, immediateScore);
			}

			Player nextPlayer = (currentPlayer == Player.White) ? Player.Black : Player.White;

			EvaluationVariable childEval = minimaxAlphaBeta(
					clonedBoard, depth - 1, !isMaximizingPlayer, nextPlayer, alpha, beta);

			int childScore = childEval.evaluationScore;

			if (isMaximizingPlayer) {
				if (childScore > bestEval) {
					bestEval = childScore;
					bestMoves.clear();
					bestMoves.add(move);
				} else if (childScore == bestEval) {
					bestMoves.add(move);
				}

				alpha = Math.max(alpha, bestEval);
				if (alpha >= beta) {
					nodesPruned += (moves.length - i - 1);
					break;
				}
			} else {
				if (childScore < bestEval) {
					bestEval = childScore;
					bestMoves.clear();
					bestMoves.add(move);
				} else if (childScore == bestEval) {
					bestMoves.add(move);
				}

				beta = Math.min(beta, bestEval);
				if (beta <= alpha) {
					nodesPruned += (moves.length - i - 1);
					break;
				}
			}
		}

		Coords chosen = bestMoves.get(rnd.nextInt(bestMoves.size()));
		return new EvaluationVariable(chosen, bestEval);
	}

	public Coords play(GomokuBoard board, Player player) {
		nodesVisited = 0;
		nodesPruned = 0;

		// 1️⃣ Vérifie victoire ou blocage immédiat avant minimax
		TileState myTile = (playerColor == Player.White) ? TileState.White : TileState.Black;
		TileState oppTile = (playerColor == Player.White) ? TileState.Black : TileState.White;

		for (Coords move : getAvailableMoves(board)) {
			GomokuBoard clone = board.clone();
			clone.set(move, myTile);
			if (clone.getWinnerState().name().equalsIgnoreCase(playerColor.name())) {
				return move; // victoire immédiate
			}
		}

		for (Coords move : getAvailableMoves(board)) {
			GomokuBoard clone = board.clone();
			clone.set(move, oppTile);
			if (clone.getWinnerState().name().equalsIgnoreCase(
					(playerColor == Player.White) ? "Black" : "White")) {
				return move; // bloque l’adversaire
			}
		}

		boolean isMaximizing = true; // L’IA maximise toujours son score
		EvaluationVariable bestMove = minimaxAlphaBeta(
				board, minimaxDepth, isMaximizing, playerColor, Integer.MIN_VALUE, Integer.MAX_VALUE);

		System.out.println("AI_Elagage depth=" + minimaxDepth +
				" nodesVisited=" + nodesVisited +
				" nodesPruned=" + nodesPruned +
				" chosen=" + bestMove.coords +
				" score=" + bestMove.evaluationScore);

		return bestMove.coords;
	}

	// --- Évaluation du plateau ---
	public int evaluateBoard(GomokuBoard board) {
		int evaluation = 0;
		int size = GomokuBoard.size;
		TileState myTile = (playerColor == Player.White) ? TileState.White : TileState.Black;
		TileState oppTile = (playerColor == Player.White) ? TileState.Black : TileState.White;
		int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};

		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				for (int[] dir : directions) {
					evaluation += evaluateLine(board, row, col, dir, myTile);
					evaluation -= evaluateLine(board, row, col, dir, oppTile);
				}
			}
		}
		return evaluation;
	}

	private int evaluateLine(GomokuBoard board, int row, int col, int[] dir, TileState tile) {
		int size = GomokuBoard.size;
		int count = 0;
		int r = row, c = col;

		while (r >= 0 && r < size && c >= 0 && c < size && board.get(new Coords(r, c)) == tile) {
			count++;
			r += dir[0];
			c += dir[1];
		}

		if (count <= 1) return 0;

		boolean open1 = false, open2 = false;
		int beforeR = row - dir[0], beforeC = col - dir[1];
		if (beforeR >= 0 && beforeR < size && beforeC >= 0 && beforeC < size &&
				board.get(new Coords(beforeR, beforeC)) == TileState.Empty) open1 = true;

		int afterR = row + count * dir[0], afterC = col + count * dir[1];
		if (afterR >= 0 && afterR < size && afterC >= 0 && afterC < size &&
				board.get(new Coords(afterR, afterC)) == TileState.Empty) open2 = true;

		boolean open = open1 && open2;

		if (count >= 5) return 1000000;
		else if (count == 4) return open ? 10000 : 5000;
		else if (count == 3) return open ? 500 : 100;
		else if (count == 2) return open ? 50 : 10;
		return 0;
	}
}
