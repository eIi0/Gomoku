/*
 * Code created by Thomas Cohen (Esisar)
 * open source code
 * */

import controllers.HumanPlayer;
import controllers.PlayerController;
import controllers.ai.AI_Elagage;
import controllers.ai.AI_Random;
import gamecore.Coords;
import gamecore.GomokuBoard;
import gamecore.enums.Player;
import gamecore.enums.TileState;
import gamecore.enums.WinnerState;

import java.util.Scanner;

public class Game
{

	/**
	 * Lancer une partie entre deux joueurs de Gomoku
	 *
	 * @param player1 Joueur 1
	 * @param player2 Joueur 2
	 * @return Vainqueur de la partie
	 */
	public static WinnerState startMatch(GomokuBoard board, PlayerController player1, PlayerController player2)
	{
		WinnerState winnerState;
		Player currentPlayer = Player.White;

		int roundCount = 0;

		long player1TotalPlayTime = 0;
		long player2TotalPlayTime = 0;
		long player1LongestPlayTime = 0;
		long player2LongestPlayTime = 0;
		long player1ShortestPlayTime = Long.MAX_VALUE;
		long player2ShortestPlayTime = Long.MAX_VALUE;

		while ((winnerState = board.getWinnerState()) == WinnerState.None)
		{ // Tant que la partie n'est pas finie
			roundCount++;

			System.out.println("Tour " + roundCount + ": " + (currentPlayer == Player.White ? "BLANC" : "NOIR"));
			long startTime = System.currentTimeMillis();
			Coords move = currentPlayer == Player.White ? player1.play(board, Player.White) : player2.play(board, Player.Black); // Obtenir le coup du joueur
			long moveDuration = System.currentTimeMillis() - startTime;

			if (currentPlayer == Player.White)
			{
				player1TotalPlayTime += moveDuration;
				player1LongestPlayTime = Math.max(player1LongestPlayTime, moveDuration);
				player1ShortestPlayTime = Math.min(player1ShortestPlayTime, moveDuration);
			}
			else
			{
				player2TotalPlayTime += moveDuration;
				player2LongestPlayTime = Math.max(player2LongestPlayTime, moveDuration);
				player2ShortestPlayTime = Math.min(player2ShortestPlayTime, moveDuration);
			}
			System.out.println(move);
			board.set(move, currentPlayer == Player.White ? TileState.White : TileState.Black); // Jouer le coup

			System.out.println("Ligne: " + move.row);
			System.out.println("Colonne: " + move.column);
			System.out.println();
			board.print();
			System.out.println();

			currentPlayer = currentPlayer == Player.White ? Player.Black : Player.White; // Changer de joueur
		}

		if (winnerState == WinnerState.Tie) System.out.println("Égalité !");
		else System.out.println("Vainqueur: " + (winnerState == WinnerState.White ? "Blanc" : "Noir"));

		double movePerPlayer = (double) (roundCount / 2);
		int player1MoveCount = (int) Math.ceil(movePerPlayer);
		int player2MoveCount = (int) Math.floor(movePerPlayer);

		System.out.println("\nStatistiques :");
		System.out.println("Blanc: " + player1MoveCount + " coups, " + (player1TotalPlayTime / player1MoveCount) + " ms/coup, " + player1LongestPlayTime + " ms (max), " + player1ShortestPlayTime + " ms (min)");
		System.out.println("Noir: " + player2MoveCount + " coups, " + (player2TotalPlayTime / player2MoveCount) + " ms/coup, " + player2LongestPlayTime + " ms (max), " + player2ShortestPlayTime + " ms (min)");

		return winnerState;
	}

	public static void main(String[] args)
	{
		GomokuBoard board = new GomokuBoard();
		System.out.println("Selection du mode de jeu : \n- Humain VS Humain (1) \n- Humain VS IA (2) \n- IA VS Humain (3) \n- IA VS IA (4)" +
				"\n- Humain VS IA Elagage (5) \n- IA Elagage VS IA Elagage (6) \n- IA Elagage VS IA Random (7)");

//		Test du plateau
//		board.set(1,1,TileState.White);
//		board.set(1,2,TileState.White);
//		board.set(1,3,TileState.White);
//		board.set(1,4,TileState.White);
//		board.set(1,5,TileState.White);
//		board.set(2,1,TileState.Black);
//		board.set(2,2,TileState.White);
//		board.set(2,3,TileState.Black);
//		board.set(2,4,TileState.Black);
//		board.set(2,5,TileState.Black);

		Scanner sc = new Scanner(System.in);
		int str = sc.nextInt();
		switch (str)
		{
			case 1:
				startMatch(board, new HumanPlayer(), new HumanPlayer());
				break;

			case 2:
				System.out.println("Profondeur calcul IA : ");
				int depthIA = sc.nextInt();
				startMatch(board, new HumanPlayer(), new AI_Random(depthIA, Player.Black));

				break;

			case 3:
				System.out.println("Profondeur calcul IA : ");
				int depthIA2 = sc.nextInt();
				startMatch(board, new AI_Random(depthIA2, Player.Black), new HumanPlayer());

				break;

			case 4:
				System.out.println("Profondeur calcul IA blanche: ");
				int depthIAWhite = sc.nextInt();
				System.out.println("Profondeur calcul IA noire: ");
				int depthIABlack = sc.nextInt();
				startMatch(board, new AI_Random(depthIAWhite, Player.White), new AI_Random(depthIABlack, Player.Black));
				break;

			case 5:
				System.out.println("Profondeur calcul IA : ");
				int depthIAElag = sc.nextInt();
				startMatch(board, new HumanPlayer(), new AI_Elagage(depthIAElag, Player.White));
				break;

			case 6:
				System.out.println("Profondeur calcul IA blanche: ");
				int depthIAElagWhite = sc.nextInt();
				System.out.println("Profondeur calcul IA noire: ");
				int depthIAElagBlack = sc.nextInt();
				startMatch(board, new AI_Elagage(depthIAElagWhite, Player.White), new AI_Elagage(depthIAElagBlack, Player.Black));
				break;

			case 7:
				System.out.println("Profondeur calcul IA blanche: ");
				int depthIAElagVsWhite = sc.nextInt();
				System.out.println("Profondeur calcul IA noire: ");
				int depthIARandomVsBlack = sc.nextInt();
				startMatch(board, new AI_Elagage(depthIAElagVsWhite, Player.White), new AI_Random(depthIARandomVsBlack, Player.Black));
				break;
		}
//            startMatch(new AI_Sweep(2), new AI_Sweep(2)); // Lancer une partie entre deux IA Sweep
//        startMatch(new AI_Star(3), new AI_Star(3)); // Lancer une partie entre deux IA Star
		//startMatch(new AI_Star(2), new AI_Sweep(2)); // Lancer une partie entre une IA Sweep et une IA Star
	}
}
