package controllers.ai;

import controllers.PlayerController;
import gamecore.Coords;
import gamecore.GomokuBoard;
import gamecore.enums.Player;
import gamecore.enums.TileState;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente une IA qui cherche les coups en se positionnant sur chaque case, puis en vérifiant le contenu des 4 cases autour dans les 8 directions
 */
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

	public int evaluateBoard(GomokuBoard board) {
		int size = GomokuBoard.size;
		int score = 0;

		int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};

		// Évaluation des lignes
		score += evaluateLines(board, TileState.White, directions);
		score -= evaluateLines(board, TileState.Black, directions);

		score += evaluateShapes(board, TileState.White);
		score -= evaluateShapes(board, TileState.Black);

		return score;
	}

	private int evaluateLines(GomokuBoard board, TileState tile, int[][] directions) {
		int score = 0;
		int size = GomokuBoard.size;
		int center = size / 2;

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

					if (count > 1) {
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

						if (!open1 && !open2) continue;

						int baseScore = 0;
						if (count >= 5) return Integer.MAX_VALUE;
						else if (count == 4) baseScore = (open1 && open2) ? 10000 : 1000;
						else if (count == 3) baseScore = (open1 && open2) ? 100 : 30;
						else if (count == 2) baseScore = (open1 && open2) ? 5 : 2;

						//  Bonus de centralité
						int distanceToCenter = Math.abs(row - center) + Math.abs(col - center);
						int centralityBonus = Math.max(0, 10 - distanceToCenter); // max bonus = 10, diminue avec l’éloignement

						score += baseScore + centralityBonus;
					}
				}
			}
		}
		return score;
	}


	private int evaluateShapes(GomokuBoard board, TileState tile) {
		int score = 0;
		int size = GomokuBoard.size;
		int center = size / 2;

		int[][] directions = {
				{0, 1},   // ➡️
				{1, 0},   // ⬇️
				{1, 1},   // ↘
				{1, -1}   // ↙
		};

		for (int row = 1; row < size - 1; row++) {
			for (int col = 1; col < size - 1; col++) {
				Coords centerCoord = new Coords(row, col);
				if (board.get(centerCoord) != TileState.Empty) continue;

				// 🔹 Diagonales (X)
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

				// 🔹 Côtés (+)
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

				// 🔸 Côtés ouverts
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

	@Override
	public Coords play(GomokuBoard board, Player player)
	{
		return minimax(board, this.minimaxDepth, /*false, (pas sur que ce qui est après soit optimal FIXME*/(playerColor == Player.White) ? true : false, playerColor).coords;
	}

	public EvaluationVariable minimax(GomokuBoard board, int depth, boolean isMaximizingPlayer, Player player)
	{
		if (getAvailableMoves(board).length >= 224)
		{
			if (board.get(6,6).equals(TileState.Empty))
			{
				return new EvaluationVariable(new Coords(6, 6), Integer.MAX_VALUE);
			}
			else
			{
				return new EvaluationVariable(new Coords(7, 7), Integer.MAX_VALUE);
			}
		}
		// Profondeur 0 atteinte
		if (depth == 0)
		{
			return new EvaluationVariable(new Coords(), minimaxEval(board));
		}

		Coords[] moves = getAvailableMoves(board);
		Coords bestCoords = moves[(0)];       //TODO a modifier par mieux --> problème pas de la
		int bestEval = isMaximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;
		ArrayList<Coords> bestMoves = new ArrayList<Coords>();

		for (Coords move : moves)
		{
			GomokuBoard clonedBoard = board.clone();
			clonedBoard.set(move, player == Player.White ? TileState.White : TileState.Black);

			Player nextPlayer = (player == Player.White ? Player.Black : Player.White);
			EvaluationVariable childEval = minimax(clonedBoard, depth - 1, !isMaximizingPlayer, nextPlayer);
			childEval.coords = move;

//			int depthForString = 0;
//			String tabString = "";
//			while (depthForString != depth)
//			{
//				tabString += "   ";
//				depthForString++;
//			}
//			System.out.println(tabString + depth + ": " + childEval.coords + " : " + childEval.evaluationScore);

			if (isMaximizingPlayer)
			{
				if (childEval.evaluationScore > bestEval)
				{
//					System.out.println("New maximised best board : " + childEval.evaluationScore);
//					clonedBoard.print();
					bestEval = childEval.evaluationScore;
					bestMoves.clear();
					bestMoves.add(move);
				}
				else if (childEval.evaluationScore == bestEval)
				{
					bestMoves.add(move);
				}
			}
			else
			{
				if (childEval.evaluationScore < bestEval)
				{
//					System.out.println("New minimised best board : " + childEval.evaluationScore);
//					clonedBoard.print();
					bestEval = childEval.evaluationScore;
					bestCoords = childEval.coords;
				}
			}
//			System.out.println(depth + " : " +  move + " : evaluation retenue : " + bestEval);
		}
		return new EvaluationVariable(bestCoords, bestEval);
	}

	public int minimaxEval(GomokuBoard board)
	{
		return evaluateBoard(board);
	}
}
