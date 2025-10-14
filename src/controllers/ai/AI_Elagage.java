package controllers.ai;

import controllers.PlayerController;
import gamecore.Coords;
import gamecore.GomokuBoard;
import gamecore.enums.Player;
import gamecore.enums.TileState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AI_Elagage extends PlayerController
{

	private final Random rnd = new Random();
	private long nodesVisited = 0;
	private long nodesPruned = 0;
	private int minimaxDepth;
	private Player playerColor;

	public AI_Elagage(int minimaxDepth, Player playerColor)
	{
		this.minimaxDepth = minimaxDepth;
		this.playerColor = playerColor;
	}

	public AI_Elagage()
	{
		super();
	}

	public Coords[] getAvailableMoves(GomokuBoard board)
	{
		List<Coords> moves = new ArrayList<>();

		for (int row = 0; row < GomokuBoard.size; row++)
		{
			for (int col = 0; col < GomokuBoard.size; col++)
			{
				Coords c = new Coords(row, col);
				if (board.get(c) == TileState.Empty)
				{
					moves.add(c);
				}
			}
		}

		return moves.toArray(new Coords[0]);
	}

	public EvaluationVariable minimaxAlphaBeta(GomokuBoard board, int depth, boolean isMaximizingPlayer, Player player, int alpha, int beta)
	{
		nodesVisited++;

		if (getAvailableMoves(board).length >= 224)
		{
			if (board.get(6, 6).equals(TileState.Empty))
			{
				return new EvaluationVariable(new Coords(6, 6), Integer.MAX_VALUE);
			}
			else
			{
				return new EvaluationVariable(new Coords(7, 7), Integer.MAX_VALUE);
			}
		}

		if (depth == 0)
		{
			int eval = minimaxEvalRoot(board);
			return new EvaluationVariable(new Coords(), eval);
		}

		Coords[] moves = getAvailableMoves(board);
		List<Coords> bestMoves = new ArrayList<>();
		int bestEval = isMaximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;

		for (int i = 0; i < moves.length; i++)
		{
			Coords move = moves[i];

			GomokuBoard clonedBoard = board.clone();
			clonedBoard.set(move, player == Player.White ? TileState.White : TileState.Black);

			Player nextPlayer = (player == Player.White ? Player.Black : Player.White);

			EvaluationVariable childEval = minimaxAlphaBeta(clonedBoard, depth - 1, !isMaximizingPlayer, nextPlayer, alpha, beta);
			int childScore = childEval.evaluationScore;

			if (isMaximizingPlayer)
			{
				if (childScore > bestEval)
				{
					bestEval = childScore;
					bestMoves.clear();
					bestMoves.add(move);
				}
				else if (childScore == bestEval)
				{
					bestMoves.add(move);
				}

				// mettre à jour alpha et tester la coupe
				alpha = Math.max(alpha, bestEval);
				if (alpha >= beta)
				{
					nodesPruned += (moves.length - i - 1);
					break;
				}
			}
			else
			{
				if (childScore < bestEval)
				{
					bestEval = childScore;
					bestMoves.clear();
					bestMoves.add(move);
				}
				else if (childScore == bestEval)
				{
					bestMoves.add(move);
				}

				// mettre à jour beta et tester la coupe
				beta = Math.min(beta, bestEval);
				if (beta <= alpha)
				{
					nodesPruned += (moves.length - i - 1);
					break;
				}
			}
		}

		Coords chosen = bestMoves.get(0);
		if (bestMoves.size() > 1)
		{
			chosen = bestMoves.get(rnd.nextInt(bestMoves.size()));
		}

		return new EvaluationVariable(chosen, bestEval);
	}

	public int minimaxEvalRoot(GomokuBoard board)
	{
		return evaluateBoard(board);
	}

	public Coords play(GomokuBoard board, Player player)
	{
		nodesVisited = 0;
		nodesPruned = 0;

		EvaluationVariable meilleurCoup = minimaxAlphaBeta(board, this.minimaxDepth, true, this.playerColor, Integer.MIN_VALUE, Integer.MAX_VALUE);
		System.out.println("AI_Elagage depth=" + this.minimaxDepth + " nodesVisited=" + nodesVisited + " nodesPruned=" + nodesPruned + " chosen=" + meilleurCoup.coords + " score=" + meilleurCoup.evaluationScore);

		return meilleurCoup.coords;
	}


	public int evaluateBoard(GomokuBoard board) {
		int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
		int score = 0;

		// Score pour chaque joueur
		score += evaluateLines(board, TileState.White, directions);
		score -= evaluateLines(board, TileState.Black, directions);

		return score;
	}

	private int evaluateLines(GomokuBoard board, TileState tile, int[][] directions) {
		int score = 0;
		int size = GomokuBoard.size;
		int center = size / 2;

		// Définir le carré central 5x5
		int centerStart = center - 2; // start=5 pour size=15
		int centerEnd = center + 2;   // end=9 pour size=15

		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				for (int[] dir : directions) {
					int count = 0;
					int r = row, c = col;

					while (r >= 0 && r < size && c >= 0 && c < size && board.get(new Coords(r, c)) == tile) {
						count++;
						r += dir[0];
						c += dir[1];
					}

					if (count > 0) {
						boolean open1 = false, open2 = false;

						int beforeR = row - dir[0], beforeC = col - dir[1];
						if (beforeR >= 0 && beforeR < size && beforeC >= 0 && beforeC < size &&
								board.get(new Coords(beforeR, beforeC)) == TileState.Empty) {
							open1 = true;
						}

						int afterR = row + count * dir[0], afterC = col + count * dir[1];
						if (afterR >= 0 && afterR < size && afterC >= 0 && afterC < size &&
								board.get(new Coords(afterR, afterC)) == TileState.Empty) {
							open2 = true;
						}

						if (!open1 && !open2 && count < 5) continue;

						int baseScore = switch (count) {
							case 5 -> Integer.MAX_VALUE; // victoire
							case 4 -> (open1 && open2) ? 10000 : 5000;
							case 3 -> (open1 && open2) ? 500 : 100;
							case 2 -> (open1 && open2) ? 50 : 10;
							default -> 1; // 1 pion seul
						};

						// Bonus centralité avec carré 5x5
						int centralityBonus;
						if (row >= centerStart && row <= centerEnd && col >= centerStart && col <= centerEnd) {
							centralityBonus = 10; // carré central
						} else {
							int distance = Math.max(Math.abs(row - center), Math.abs(col - center));
							centralityBonus = Math.max(0, 10 - distance); // décroissance rapide
						}

						score += baseScore + centralityBonus;
					}
				}
			}
		}
		return score;
	}
}