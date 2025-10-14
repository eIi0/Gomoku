package controllers.ai;

import controllers.PlayerController;
import gamecore.Coords;
import gamecore.GomokuBoard;
import gamecore.enums.Player;
import gamecore.enums.TileState;

import java.util.ArrayList;
import java.util.List;

public class AI_Random extends PlayerController
{

	private int minimaxDepth;
	private Player playerColor;

	public AI_Random(int minimaxDepth, Player playerColor)
	{
		this.minimaxDepth = minimaxDepth;
		this.playerColor = playerColor;
	}

	public AI_Random()
	{
		super();

	}


	public int evaluateBoard(GomokuBoard board)
	{
		int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
		int score = 0;

		// Score pour chaque joueur
		score += evaluateLines(board, TileState.White, directions);
		score -= evaluateLines(board, TileState.Black, directions);

		return score;
	}

	private int evaluateLines(GomokuBoard board, TileState tile, int[][] directions)
	{
		int score = 0;
		int size = GomokuBoard.size;
		int center = size / 2;

		// Définir le carré central 5x5
		int centerStart = center - 2; // start=5 pour size=15
		int centerEnd = center + 2;   // end=9 pour size=15

		for (int row = 0; row < size; row++)
		{
			for (int col = 0; col < size; col++)
			{
				for (int[] dir : directions)
				{
					int count = 0;
					int r = row, c = col;

					while (r >= 0 && r < size && c >= 0 && c < size && board.get(new Coords(r, c)) == tile)
					{
						count++;
						r += dir[0];
						c += dir[1];
					}

					if (count > 0)
					{
						boolean open1 = false, open2 = false;

						int beforeR = row - dir[0], beforeC = col - dir[1];
						if (beforeR >= 0 && beforeR < size && beforeC >= 0 && beforeC < size &&
								board.get(new Coords(beforeR, beforeC)) == TileState.Empty)
						{
							open1 = true;
						}

						int afterR = row + count * dir[0], afterC = col + count * dir[1];
						if (afterR >= 0 && afterR < size && afterC >= 0 && afterC < size &&
								board.get(new Coords(afterR, afterC)) == TileState.Empty)
						{
							open2 = true;
						}

						if (!open1 && !open2 && count < 5) continue;

						// Attribution du score selon la longueur de la ligne
						int baseScore = switch (count)
						{
							case 5 -> Integer.MAX_VALUE; // victoire
							case 4 -> (open1 && open2) ? 10000 : 5000;
							case 3 -> (open1 && open2) ? 500 : 100;
							case 2 -> (open1 && open2) ? 50 : 10;
							default -> 1; // 1 pion seul
						};

						// Bonus centralité avec carré 5x5
						int centralityBonus;
						if (row >= centerStart && row <= centerEnd && col >= centerStart && col <= centerEnd)
						{
							centralityBonus = 10; // carré central
						}
						else
						{
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

	/*private int evaluateShapes(GomokuBoard board, TileState tile) {
		int score = 0;
		int size = GomokuBoard.size;
		int center = size / 2;

		int[][] directions = {
				{0, 1},   // ➡
				{1, 0},   // ⬇
				{1, 1},   // ↘
				{1, -1}   // ↙
		};

		for (int row = 1; row < size - 1; row++) {
			for (int col = 1; col < size - 1; col++) {
				Coords centerCoord = new Coords(row, col);
				if (board.get(centerCoord) != TileState.Empty) continue;

				//  Diagonales (X)
				Coords[] diagonals = {
						new Coords(row - 1, col - 1),
						new Coords(row - 1, col + 1),
						new Coords(row + 1, col - 1),
						new Coords(row + 1, col + 1)
				};
				boolean isX = true;
				for (Coords d : diagonals) {
					if (board.get(d) != tile) {
						isX = false;
						break;
					}
				}

				//  Côtés (+)
				Coords[] sides = {
						new Coords(row - 1, col),
						new Coords(row + 1, col),
						new Coords(row, col - 1),
						new Coords(row, col + 1)
				};
				boolean isPlus = true;
				for (Coords s : sides) {
					if (board.get(s) != tile) {
						isPlus = false;
						break;
					}
				}

				//  Côtés ouverts
				int openSides = 0;
				for (Coords s : sides) {
					if (board.get(s) == TileState.Empty) {
						openSides++;
					}
				}
				if (openSides == 0) continue;

				// 🔸 Bonus centralité
				int distanceToCenter = Math.abs(row - center) + Math.abs(col - center);
				int centralityBonus = Math.max(0, 10 - distanceToCenter);

				// 🔸 Score selon forme
				int baseShapeScore = 0;
				if (isX && isPlus) baseShapeScore = 100 + (openSides * 10);
				else if (isX) baseShapeScore = switch (openSides) {
					case 1 -> 20;
					case 2 -> 40;
					case 3 -> 60;
					default -> 70;
				};
				else if (isPlus) baseShapeScore = switch (openSides) {
					case 1 -> 15;
					case 2 -> 35;
					case 3 -> 55;
					default -> 60;
				};

				// 🔹 T
				boolean isT = false;
				for (int i = 0; i < 4; i++) {
					Coords arm1 = sides[i];
					Coords arm2 = sides[(i + 1) % 4];
					Coords arm3 = sides[(i + 3) % 4];
					if (board.get(arm1) == tile && board.get(arm2) == tile && board.get(arm3) == tile) {
						isT = true;
						break;
					}
				}
				if (isT) baseShapeScore += switch (openSides) {
					case 1 -> 20;
					case 2 -> 40;
					case 3 -> 60;
					default -> 70;
				};

				// 🔹 L
				boolean isL = false;
				Coords[][] lShapes = {
						{sides[0], sides[2]}, {sides[0], sides[3]},
						{sides[1], sides[2]}, {sides[1], sides[3]}
				};
				for (Coords[] pair : lShapes) {
					if (board.get(pair[0]) == tile && board.get(pair[1]) == tile) {
						isL = true;
						break;
					}
				}
				if (isL) baseShapeScore += switch (openSides) {
					case 1 -> 10;
					case 2 -> 25;
					case 3 -> 40;
					default -> 50;
				};

				// 🔍 Analyse locale : lignes partant du centre
				int localThreatBonus = 0;
				for (int[] dir : directions) {
					int r = row + dir[0], c = col + dir[1];
					int count = 0;
					while (r >= 0 && r < size && c >= 0 && c < size && board.get(new Coords(r, c)) == tile) {
						count++;
						r += dir[0];
						c += dir[1];
					}
					if (count == 2) localThreatBonus += 10;
					else if (count == 3) localThreatBonus += 30;
				}

				score += baseShapeScore + centralityBonus + localThreatBonus;
			}
		}

		return score;
	}*/

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

	@Override
	public Coords play(GomokuBoard board, Player player)
	{
		TileState myTile = (playerColor == Player.White) ? TileState.White : TileState.Black;
		TileState opponentTile = (playerColor == Player.White) ? TileState.Black : TileState.White;
		Coords[] moves = getAvailableMoves(board);

		for (Coords move : moves)
		{
			GomokuBoard clone = board.clone();
			clone.set(move, myTile);
			if (clone.getWinnerState().name().equalsIgnoreCase(playerColor.name()))
			{
				return move; // victoire directe
			}
		}

		for (Coords move : moves)
		{
			GomokuBoard clone = board.clone();
			clone.set(move, opponentTile);
			if (clone.getWinnerState().name().equalsIgnoreCase(
					(playerColor == Player.White) ? "Black" : "White"))
			{
				return move; // bloque l’adversaire
			}
		}

		return minimax(board, this.minimaxDepth, playerColor == Player.White, playerColor).coords;
	}


	public EvaluationVariable minimax(GomokuBoard board, int depth, boolean isMaximizingPlayer, Player player)
	{
		if (getAvailableMoves(board).length >= 224)
		{
			if (board.get(6, 6).equals(TileState.Empty))
				return new EvaluationVariable(new Coords(6, 6), Integer.MAX_VALUE);
			else
				return new EvaluationVariable(new Coords(7, 7), Integer.MAX_VALUE);
		}

		if (depth == 0)
		{
			return new EvaluationVariable(new Coords(), minimaxEval(board));
		}

		Coords[] moves = getAvailableMoves(board);
		Coords bestCoords = moves[0];
		int bestEval = isMaximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;

		for (Coords move : moves)
		{
			GomokuBoard clonedBoard = board.clone();
			TileState myTile = (player == Player.White) ? TileState.White : TileState.Black;

			clonedBoard.set(move, myTile);
			Player nextPlayer = (player == Player.White) ? Player.Black : Player.White;
			EvaluationVariable childEval = minimax(clonedBoard, depth - 1, !isMaximizingPlayer, nextPlayer);
			childEval.coords = move;

			int finalEval = childEval.evaluationScore;

			if (isMaximizingPlayer)
			{
				if (finalEval > bestEval)
				{
					bestEval = finalEval;
					bestCoords = move;
				}
			}
			else
			{
				if (finalEval < bestEval)
				{
					bestEval = finalEval;
					bestCoords = move;
				}
			}
		}
		return new EvaluationVariable(bestCoords, bestEval);
	}


	public int minimaxEval(GomokuBoard board)
	{
		return evaluateBoard(board);
	}
}
