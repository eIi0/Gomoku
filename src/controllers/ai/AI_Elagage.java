package controllers.ai;

import gamecore.Coords;
import gamecore.GomokuBoard;
import gamecore.enums.Player;
import gamecore.enums.TileState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AI_Elagage {

	private final Random rnd = new Random();
	private long nodesVisited = 0;
	private long nodesPruned = 0;
	private  int minimaxDepth;
	private Player playerColor;

	public AI_Elagage(int minimaxDepth, GomokuBoard board, Player playerColor) {
		this.minimaxDepth = minimaxDepth;
		this.playerColor = playerColor;
	}

	public AI_Elagage() {
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

	/**
	 * Minimax avec élagage alpha-beta.
	 * @param board plateau courant
	 * @param depth profondeur restante
	 * @param isMaximizingPlayer true si nœud maximisant
	 * @param player joueur devant jouer dans ce nœud
	 * @param alpha borne alpha
	 * @param beta borne beta
	 * @return EvaluationVariable contenant coords (meilleur coup) et evaluationScore
	 */
	public EvaluationVariable minimaxAlphaBeta(GomokuBoard board, int depth, boolean isMaximizingPlayer, Player player, int alpha, int beta) {
		nodesVisited++;

		if (depth == 0) {
			int eval = minimaxEvalRoot(board);
			return new EvaluationVariable(new Coords(), eval);
		}

		Coords[] moves = getAvailableMoves(board);
		List<Coords> bestMoves = new ArrayList<>();
		int bestEval = isMaximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;

		for (int i = 0; i < moves.length; i++) {
			Coords move = moves[i];

			GomokuBoard clonedBoard = board.clone();
			clonedBoard.set(move, player == Player.White ? TileState.White : TileState.Black);

			Player nextPlayer = (player == Player.White ? Player.Black : Player.White);

			EvaluationVariable childEval = minimaxAlphaBeta(clonedBoard, depth - 1, !isMaximizingPlayer, nextPlayer, alpha, beta);
			int childScore = childEval.evaluationScore;

			if (isMaximizingPlayer) {
				if (childScore > bestEval) {
					bestEval = childScore;
					bestMoves.clear();
					bestMoves.add(move);
				} else if (childScore == bestEval) {
					bestMoves.add(move);
				}

				// mettre à jour alpha et tester la coupe
				alpha = Math.max(alpha, bestEval);
				if (alpha >= beta) {
					// coupe possible : on arrête d'explorer les autres fils
					// Estimation : on coupe le reste de la liste des moves
					nodesPruned += (moves.length - i - 1);
					break;
				}
			} else { // minimizing node
				if (childScore < bestEval) {
					bestEval = childScore;
					bestMoves.clear();
					bestMoves.add(move);
				} else if (childScore == bestEval) {
					bestMoves.add(move);
				}

				// mettre à jour beta et tester la coupe
				beta = Math.min(beta, bestEval);
				if (beta <= alpha) {
					nodesPruned += (moves.length - i - 1);
					break;
				}
			}
		}

		// Choisir un coup parmi les meilleurs (random pour casser la répétition)
		Coords chosen = bestMoves.get(0);
		if (bestMoves.size() > 1) {
			chosen = bestMoves.get(rnd.nextInt(bestMoves.size()));
		}

		return new EvaluationVariable(chosen, bestEval);
	}

	public int minimaxEvalRoot(GomokuBoard board) {
		return evaluateBoard(board);
	}

	public Coords play(GomokuBoard board, Player player) {
		// reset stats
		nodesVisited = 0;
		nodesPruned = 0;

		EvaluationVariable meilleurCoup = minimaxAlphaBeta(board, this.minimaxDepth, true, this.playerColor, Integer.MIN_VALUE, Integer.MAX_VALUE);
		System.out.println("AI_Elagage depth=" + this.minimaxDepth + " nodesVisited=" + nodesVisited + " nodesPruned=" + nodesPruned + " chosen=" + meilleurCoup.coords + " score=" + meilleurCoup.evaluationScore);

		return meilleurCoup.coords;
	}




	//TODO mettre a jour avec Malak
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

}