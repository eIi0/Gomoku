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

	public AI_Random(int minimaxDepth, GomokuBoard board, Player playerColor)
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
		GomokuBoard evaluationBoard = board.clone();
		int evaluation = 0;
		int size = GomokuBoard.size;
		TileState whiteTileState = TileState.White;
		TileState blackTileState = TileState.Black;

		// Directions : droite, bas, diagonale droite-bas, diagonale gauche-bas
		int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};

		for (int row = 0; row < size; row++)
		{
			for (int col = 0; col < size; col++)
			{
				for (int[] dir : directions)
				{
					int count = 0;
					int r = row, c = col;

					while (r >= 0 && r < size && c >= 0 && c < size && evaluationBoard.get(new Coords(r, c)) == whiteTileState)
					{
						count++;
						r += dir[0];
						c += dir[1];
					}

					if (count > 1)
					{
						boolean open1 = false, open2 = false;

						int beforeR = row - dir[0], beforeC = col - dir[1];
						if (beforeR >= 0 && beforeR < size && beforeC >= 0 && beforeC < size &&
								evaluationBoard.get(new Coords(beforeR, beforeC)) == TileState.Empty)
						{
							open1 = true;
						}
						int afterR = row + count * dir[0], afterC = col + count * dir[1];
						if (afterR >= 0 && afterR < size && afterC >= 0 && afterC < size &&
								evaluationBoard.get(new Coords(afterR, afterC)) == TileState.Empty)
						{
							open2 = true;
						}

						boolean open = open1 && open2;
						if (count >= 5) return Integer.MAX_VALUE;
						else if (count == 4) evaluation += open ? 10000 : 1000;
						else if (count == 3) evaluation += open ? 100 : 10;
						else if (count == 2) evaluation += open ? 5 : 1;
					}
				}
			}
		}

		for (int row = 0; row < size; row++)
		{
			for (int col = 0; col < size; col++)
			{
				for (int[] dir : directions)
				{
					int count = 0;
					int r = row, c = col;

					while (r >= 0 && r < size && c >= 0 && c < size && evaluationBoard.get(new Coords(r, c)) == blackTileState)
					{
						count++;
						r += dir[0];
						c += dir[1];
					}

					if (count > 1)
					{
						boolean open1 = false, open2 = false;
						int beforeR = row - dir[0], beforeC = col - dir[1];
						if (beforeR >= 0 && beforeR < size && beforeC >= 0 && beforeC < size &&
								evaluationBoard.get(new Coords(beforeR, beforeC)) == TileState.Empty)
						{
							open1 = true;
						}
						int afterR = row + count * dir[0], afterC = col + count * dir[1];
						if (afterR >= 0 && afterR < size && afterC >= 0 && afterC < size &&
								evaluationBoard.get(new Coords(afterR, afterC)) == TileState.Empty)
						{
							open2 = true;
						}
						boolean open = open1 && open2;

						if (count >= 5) return Integer.MIN_VALUE;
						else if (count == 4) evaluation -= open ? 10000 : 1000;
						else if (count == 3) evaluation -= open ? 100 : 10;
						else if (count == 2) evaluation -= open ? 5 : 1;

					}
				}
			}
		}
//		evaluation += detectExtendedXShape(evaluationBoard, whiteTileState);
//		evaluation -= detectExtendedXShape(evaluationBoard, blackTileState);
//		evaluation += detectExtendedPlusShape(evaluationBoard, whiteTileState);
//		evaluation -= detectExtendedPlusShape(evaluationBoard, blackTileState);

		//TODO ajouter la méthode pour les shapes speciales de l'adversaire

		return evaluation;
	}

	private int detectExtendedXShape(GomokuBoard board, TileState playerTile)
	{
		int score = 0;
		int size = GomokuBoard.size;

		for (int row = 2; row < size - 2; row++)
		{
			for (int col = 2; col < size - 2; col++)
			{
				Coords center = new Coords(row, col);
				if (board.get(center) == TileState.Empty)
				{
					// Diagonales autour du centre
					Coords[] diagonals = {
							new Coords(row - 1, col - 1),
							new Coords(row - 1, col + 1),
							new Coords(row + 1, col - 1),
							new Coords(row + 1, col + 1)
					};
					boolean isX = true;
					for (Coords d : diagonals)
					{
						if (board.get(d) != playerTile)
						{
							isX = false;
							break;
						}
					}
					// Extension sur la diagonale nord-ouest/sud-est
					boolean diagonaleExtend = board.get(new Coords(row - 2, col - 2)) == playerTile
							&& board.get(new Coords(row + 2, col + 2)) == playerTile;
					if (isX && diagonaleExtend)
					{
						score += 100;
					}
				}
			}
		}
		return score;
	}

	private int detectExtendedPlusShape(GomokuBoard board, TileState playerTile)
	{
		int score = 0;
		int size = GomokuBoard.size;

		for (int row = 2; row < size - 2; row++)
		{
			for (int col = 2; col < size - 2; col++)
			{
				Coords center = new Coords(row, col);
				if (board.get(center) == TileState.Empty)
				{
					// Adjacent orthogonaux
					Coords[] adjacents = {
							new Coords(row - 1, col),
							new Coords(row + 1, col),
							new Coords(row, col - 1),
							new Coords(row, col + 1)
					};
					boolean isPlus = true;
					for (Coords a : adjacents)
					{
						if (board.get(a) != playerTile)
						{
							isPlus = false;
							break;
						}
					}
					// Extension verticale
					boolean verticalExtend = board.get(new Coords(row - 2, col)) == playerTile
							&& board.get(new Coords(row + 2, col)) == playerTile;
					// Extension horizontale
					boolean horizontalExtend = board.get(new Coords(row, col - 2)) == playerTile
							&& board.get(new Coords(row, col + 2)) == playerTile;
					if (isPlus && (verticalExtend || horizontalExtend))
					{
						score += 100;
					}
				}
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
		return minimax(board, this.minimaxDepth, true, playerColor).coords;
	}

	public EvaluationVariable minimax(GomokuBoard board, int depth, boolean isMaximizingPlayer, Player player)
	{
		if (getAvailableMoves(board).length == 224)
		{
			if (board.areCoordsValid(6, 6))
			{
				return new EvaluationVariable(new Coords(6, 6), Integer.MAX_VALUE);
			}
			else
			{
				return new EvaluationVariable(new Coords(5, 6), Integer.MAX_VALUE);
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

//		System.out.println("-------------------------------------- new move --------------------------------------");
//		board.print();

		for (Coords move : moves)
		{
			GomokuBoard clonedBoard = board.clone();
			clonedBoard.set(move, player == Player.White ? TileState.White : TileState.Black);
			int depthForString = 0;
			String tabString = "";
			while (depthForString != depth)
			{
				tabString += "   ";
				depthForString++;
			}

			Player nextPlayer = (player == Player.White ? Player.Black : Player.White);
			EvaluationVariable childEval = minimax(clonedBoard, depth - 1, !isMaximizingPlayer, nextPlayer);
			childEval.coords = move;
//			System.out.println(tabString + depth + ": " + childEval.coords + " : " + childEval.evaluationScore);

			if (isMaximizingPlayer)
			{
				if (childEval.evaluationScore > bestEval)
				{
					System.out.println("New maximised best board : " + childEval.evaluationScore);
					clonedBoard.print();
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
				System.out.println(Math.min(childEval.evaluationScore, bestEval));

				if (childEval.evaluationScore == -4)
				{
					System.out.println("TROUVÉE SOLUTION");
				}

				if (childEval.evaluationScore < bestEval)
				{
					System.out.println("New minimised best board : " + childEval.evaluationScore);
					clonedBoard.print();
					bestEval = childEval.evaluationScore;
					bestCoords = childEval.coords;
				}
			}
			System.out.println(depth + " : " +  move + " : evaluation retenue : " + bestEval);
		}
		return new EvaluationVariable(bestCoords, bestEval);
	}

	public int minimaxEval(GomokuBoard board)
	{
		return evaluateBoard(board);
	}
}
