package controllers.ai;

import controllers.PlayerController;
import gamecore.Coords;
import gamecore.GomokuBoard;
import gamecore.enums.Player;
import gamecore.enums.TileState;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Représente une IA qui cherche les coups en se positionnant sur chaque case, puis en vérifiant le contenu des 4 cases autour dans les 8 directions
 */
public class AI_Random extends PlayerController
{


	public AI_Random(int minimaxDepth)
	{

	}

	public AI_Random()
	{
		super();

	}

	public int evaluateBoard(GomokuBoard board, Player player)
	{
		GomokuBoard evaluationBoard = board.clone();
		int evaluation = 0;
		int size = GomokuBoard.size;
		TileState playerTile = player == Player.White ? TileState.White : TileState.Black;
		TileState opponentTile = player == Player.White ? TileState.Black : TileState.White;

		// Directions : droite, bas, diagonale droite-bas, diagonale gauche-bas
		int[][] directions = { {0, 1}, {1, 0}, {1, 1}, {1, -1} };

		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				for (int[] dir : directions) {
					int count = 0;
					int r = row, c = col;

					while (r >= 0 && r < size && c >= 0 && c < size && evaluationBoard.get(new Coords(r, c)) == playerTile) {
						count++;
						r += dir[0];
						c += dir[1];
					}

					if (count > 1) {
						boolean open1 = false, open2 = false;

						int beforeR = row - dir[0], beforeC = col - dir[1];
						if (beforeR >= 0 && beforeR < size && beforeC >= 0 && beforeC < size &&
								evaluationBoard.get(new Coords(beforeR, beforeC)) == TileState.Empty) {
							open1 = true;
						}
						int afterR = row + count*dir[0], afterC = col + count*dir[1];
						if (afterR >= 0 && afterR < size && afterC >= 0 && afterC < size &&
								evaluationBoard.get(new Coords(afterR, afterC)) == TileState.Empty) {
							open2 = true;
						}

						boolean open = open1 && open2;
						if (count >= 5) return Integer.MAX_VALUE; // Victoire !
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

					while (r >= 0 && r < size && c >= 0 && c < size && evaluationBoard.get(new Coords(r, c)) == opponentTile)
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

						if (count >= 5) return -Integer.MAX_VALUE;
						else if (count == 4) evaluation -= open ? 10000 : 1000;
						else if (count == 3) evaluation -= open ? 100 : 10;
						else if (count == 2) evaluation -= open ? 5 : 1;
					}
				}
			}
		}
		evaluation += detectExtendedXShape(evaluationBoard, player);
		evaluation += detectExtendedPlusShape(evaluationBoard, player);

		//TODO ajouter la méthode pour les shapes speciales de l'adversaire

		return evaluation;
	}

	private int detectExtendedXShape(GomokuBoard board, Player player) {
		int score = 0;
		int size = GomokuBoard.size;
		TileState playerTile = player == Player.White ? TileState.White : TileState.Black;

		for (int row = 2; row < size - 2; row++) {
			for (int col = 2; col < size - 2; col++) {
				Coords center = new Coords(row, col);
				if (board.get(center) == TileState.Empty) {
					// Diagonales autour du centre
					Coords[] diagonals = {
							new Coords(row - 1, col - 1),
							new Coords(row - 1, col + 1),
							new Coords(row + 1, col - 1),
							new Coords(row + 1, col + 1)
					};
					boolean isX = true;
					for (Coords d : diagonals) {
						if (board.get(d) != playerTile) {
							isX = false;
							break;
						}
					}
					// Extension sur la diagonale nord-ouest/sud-est
					boolean diagonaleExtend = board.get(new Coords(row - 2, col - 2)) == playerTile
							&& board.get(new Coords(row + 2, col + 2)) == playerTile;
					if (isX && diagonaleExtend) {
						score += 100; // Score à ajuster selon ton barème
					}
				}
			}
		}
		return score;
	}

	private int detectExtendedPlusShape(GomokuBoard board, Player player) {
		int score = 0;
		int size = GomokuBoard.size;
		TileState playerTile = player == Player.White ? TileState.White : TileState.Black;

		for (int row = 2; row < size - 2; row++) {
			for (int col = 2; col < size - 2; col++) {
				Coords center = new Coords(row, col);
				if (board.get(center) == TileState.Empty) {
					// Adjacent orthogonaux
					Coords[] adjacents = {
							new Coords(row - 1, col),
							new Coords(row + 1, col),
							new Coords(row, col - 1),
							new Coords(row, col + 1)
					};
					boolean isPlus = true;
					for (Coords a : adjacents) {
						if (board.get(a) != playerTile) {
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
					if (isPlus && (verticalExtend || horizontalExtend)) {
						score += 100; // Score à ajuster
					}
				}
			}
		}
		return score;
	}

	public Coords[] getAvailableMoves(GomokuBoard board, Player player)
	{
		Coords currentCellCoords = new Coords();

		TileState playerCellState = player == Player.White ? TileState.White : TileState.Black;

		Map<Coords, Integer> moves = new HashMap<>();

		for (currentCellCoords.row = 0; currentCellCoords.row < GomokuBoard.size; currentCellCoords.row++)
		{
			for (currentCellCoords.column = 0; currentCellCoords.column < GomokuBoard.size; currentCellCoords.column++)
			{
				if (board.get(currentCellCoords) == TileState.Empty)
				{ // Si la case est vide
					board.set(currentCellCoords, playerCellState); // Jouer le coup
					int score = evaluateBoard(board, player); // Evaluer le coup
					board.set(currentCellCoords, TileState.Empty); // Annuler le coup

					moves.put(currentCellCoords.clone(), score); // Enregistrer le coup
				}
			}
		}

		Stream<Map.Entry<Coords, Integer>> sorted = moves.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));

		return sorted.map(Map.Entry::getKey).toArray(Coords[]::new);
	}

	@Override
	public Coords play(GomokuBoard board, Player player)
	{
		return getAvailableMoves(board, player)[0];
	}
}
