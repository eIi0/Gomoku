package controllers.ai;

import controllers.PlayerController;
import gamecore.Array2D;
import gamecore.Coords;
import gamecore.GomokuBoard;
import gamecore.enums.Player;
import gamecore.enums.TileState;

import java.util.ArrayList;
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

		// Liste des directions à tester pour trouver un alignement de 5 pièces
		final int[][] DIRECTIONS_TO_CHECK = {
				{-1, -1}, // Diagonales bas-gauche haut-droite
				{-1, 0}, // Verticales
				{-1, 1}, // Diagonales haut-gauche bas-droite
				{0, 1} // Horizontales
		};

		ArrayList<Array2D> positionInBoard = evaluationBoard.findPositionInBoard();

		for(Array2D pos : positionInBoard)
		{

		}

		/*
		Idée tirée de chatGPT :
		5 alignées = ∞ (victoire)
		4 ouvertes = 10 000
		4 fermées = 1 000
		3 ouvertes = 100
		3 fermées = 10
		2 ouvertes = 5
		2 fermées = 1

		Vu en cours :
		Positions stratégiques fortes (type T, X (avec le centre vide, triangle, etc.) devraient rapporter des points elles aussi
		*/
		return 0;
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

		Stream<Map.Entry<Coords, Integer>> sorted = moves.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())); // Trier les coups par ordre de priorité décroissante

		return sorted.map(Map.Entry::getKey).toArray(Coords[]::new); // Retourner les coordonnées des coups
	}

	@Override
	public Coords play(GomokuBoard board, Player player)
	{
		// on retournelepremier coup
		return getAvailableMoves(board, player)[0];
	}
}
