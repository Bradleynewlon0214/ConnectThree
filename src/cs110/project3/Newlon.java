//1908-110-3

/* 
 * Name: Bradley Newlon
 * Assignment: Project 3
 * Lab: CS 110 Section 12
 * Date: 12/13/19
 * 
 */

package cs110.project3;



import javafx.application.Application;
import static cs110.project3.ConnectThree.Player.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import cs110.project3.ConnectThree.Player;

/**
 * Name: Newlon
 * Purpose: Implements win checking for connect N from functions 'sumWinning' and 'getWinner'.
 * Also Implements an AI for connect N in the 'BradleyNewlonForBonus' private class 
 * @author Bradl
 *
 */
public class Newlon extends ConnectThree {
	public static void main(String[] args) {
		Application.launch(args);
	}
	
	/**
	 * Name: sumWinning
	 * Purpose: recursive function that accepts a game state, where a piece was placed, the direction to look, and the player who placed the piece.
	 * The function begins by checking the next piece in the specified direction (state[rowHint + changeRow][columnHint + changeCol]) to see if it is owned by currentPlayer.
	 * If the piece is owned by the currentPlayer, the function calls itself on this new piece with the same direction parameters and player then adds 1 to it's return value;
	 * Ultimately, the function returns the number of pieces that are right next to each other in a specified direction.
	 * 
	 * @param Player[][] state - a multidemensional array of Player's which represents the current state of the game. That is, where every piece is and who owns it. (PLAYER_X, PLAYER_O or NONE for noone)
	 * @param int rowHint - the row in which the piece was placed
	 * @param int changeRow - which direction to look in for the row
	 * @param int columnHint - the column in which the piece was placed (combined with rowHint this forms a coordinate)
	 * @param int changeCol - the direction to look in for the column (combined with changeRow this becomes either a vertical, horizontal, or a diagonal direction)
	 * @param Player currentPlayer - the current Player who owns the piece that was just placed (PLAYER_X or PLAYER_O)
	 * @return - int - the number of pieces in a row in the given direction owned by the given player. 
	 * Postconditions: The function will return zero if any of the rowHints or columnHints ever becomes out of bounds.
	 * Postconditions: The function will also return zero if the next piece does not match the current piece.
	 */
	protected int sumWinning(Player[][] state, int rowHint, int changeRow, int columnHint, int changeCol, Player currentPlayer) {
		
		if(((columnHint + changeCol) < GRID_COLUMNS & (columnHint + changeCol) >= 0) & ( (rowHint + changeRow) < GRID_ROWS & (rowHint + changeRow) >= 0 ) ) {
			if(state[rowHint + changeRow][columnHint + changeCol] == currentPlayer) {
				return 1 + sumWinning(state, rowHint + changeRow, changeRow, columnHint + changeCol, changeCol, currentPlayer);
			} else {
				return 0;
			}
		}
		return 0;

	}
	
	
	@Override
	/**
	 * Name: getWinner
	 * Purpose: function that takes in a current game state, and the location in which a piece was just placed which is used to determine who owns that piece (PLAYER_X or PLAYER_O)
	 * This method makes heavy use of the sumWinning method by providing it which directions to look for the piece that was just played.
	 * After running the sumWinning method in a direction it will compare the result to the MATCH_COUNT - 1 (minus 1 because we already have one piece)
	 * and they are equal, it will return the owner of those pieces (PLAYER_X or PLAYER_O)
	 * 
	 * @param Player[][] state - a multidemensional array of Player's which represents the current state of the game. That is, where every piece is and who owns it. (PLAYER_X, PLAYER_O or NONE for noone)
	 * @param int rowHint - the row in which the piece was placed
	 * @param int columnHint - the column in which the piece was placed (combined with rowHint this forms a coordinate)
	 * @return Player - the player who own's the pieces that form an alignment(PLAYER_X or PLAYER_O) or NONE is there is no winner
	 */
	protected Player getWinner(Player[][] state, int rowHint, int columnHint) {
		//Owner of the piece that was just played
		Player currentPlayer = state[rowHint][columnHint];

		//check for vertical win
		if(sumWinning(state, rowHint, 1, columnHint, 0, currentPlayer) == MATCH_COUNT - 1) {
			return state[rowHint][columnHint];
		}
		//check for horizontal win
		else if(sumWinning(state, rowHint, 0, columnHint, -1, currentPlayer) + sumWinning(state, rowHint, 0, columnHint, 1, currentPlayer) == MATCH_COUNT - 1) {
			return state[rowHint][columnHint];
		}

		//check for positive diagonal win
		else if(sumWinning(state, rowHint, 1, columnHint, 1, currentPlayer) + sumWinning(state, rowHint, -1, columnHint, -1, currentPlayer) == MATCH_COUNT - 1) {
			return state[rowHint][columnHint];
		}
		//check for negative diagonal win
		else if(sumWinning(state, rowHint, 1, columnHint, -1, currentPlayer) + sumWinning(state, rowHint, -1, columnHint, 1, currentPlayer)  == MATCH_COUNT - 1) {
			return state[rowHint][columnHint];
		}
		
		
		return NONE;
	}
	
	@Override
	/*
	 * NAME: RegisterOriginalAIs
	 * Purpose: tells the game window which additional AI to show
	 */
	protected void registerOriginalAIs() {
		registerAI(new BradleyNewlonForBonus());
		registerAI(new NegaMax());
	}
	

	/**
	 * Name: BradleyNewlon
	 * Purpose: Implements an easy algorithm for playing connect N. Only contains one method that finds what it thinks is the best move (in a column)
	 * @author Bradley
	 *
	 */
	private class BradleyNewlonForBonus extends AI {
				
		@Override
		/**
		 * Name: getBestColumn
		 * Purpose: function that takes a player, which in this case is the AI, and attempts to find the best possible move it can play
		 * If the AI is the first player in connect N (PLAYER_X) then it blocks a win if one is present or completes a win if it can.
		 * Next it trys for a horizontal win by attemptting to fill up each row and while it is trying to fill up the row, it trys to play as close to the middle as possible
		 * 
		 * If the AI is the second player in connect N (PLAYER_O), then it follows the same logic as it did as first player where blocks or completes it's own win when possible.
		 * However, next it only try to play in the middle of the board or randomly if it can't
		 * 
		 * @param Player[][] state - a multidemensional array of Player's which represents the current state of the game. That is, where every piece is and who owns it. (PLAYER_X, PLAYER_O or NONE for noone)
		 * @param Player player - the player the AI will play as.
		 * @return int - the column index where the AI will play next
		 */
		public int getBestColumn(Player[][] state, Player player) {
			final Player[] players ={player, opponentOf(player)};
			for(Player p: players ) {
				for(int c: shuffledRange(0, GRID_COLUMNS - 1)) {
					final int r = free(state, c);
					if(r >= 0) {
						state[r][c] = p;
						if(getWinner(state, r, c) == p) {
							return c;
						}
						state[r][c] = NONE;
					}
				}
			}
				
			if(player == PLAYER_X) {
				//we basically want to try and fill up the bottom row with preference towards middle
				//loop through game state
				//if one of the spaces in the bottom row is empty, save it
				//pick piece closest to middle
				
				ArrayList<Integer> emptyCols = new ArrayList<Integer>();
				for(int c = 0; c < GRID_COLUMNS; c++) {
					if(state[GRID_ROWS - 1][c] == NONE) {
						emptyCols.add(c);
					}
				}
				if(!emptyCols.isEmpty()) {
					//middle space of boards that are not an odd length
					if(state[0].length % 2 == 0) {
						
						int middle = (0 + (state[0].length - 1)) / 2;
						for(int i = middle; i >= 0; i--) {
							if(state[GRID_ROWS - 1][i] == NONE) {
								return i;
							}
						}
						for(int i = middle + 1; i < GRID_COLUMNS; i++) {
							if(state[GRID_ROWS - 1][i] == NONE) {
								return i;
							}
						}
					} else {
						int middle = (0 + (state[0].length - 1)) / 2;
						for(int i = middle; i >= 0; i--) {
							if(state[GRID_ROWS - 1][i] == NONE) {
								return i;
							}
						}
						for(int i = middle; i < GRID_COLUMNS; i++) {
							if(state[GRID_ROWS - 1][i] == NONE) {
								return i;
							}
						}
					}
				} else { //if the bottom row is full (this is probably redundant code) attempt to fill up the next row in the same way
					int middle = (0 + (state[0].length - 1)) / 2; 
	
					for(int r = 0; r < GRID_ROWS; r++) {
	
						if(state[0].length % 2 == 0) {
							for(int c = middle; c >= 0; c--) {
								if(state[r][c] == NONE) {
									return c;
								}
							}
							for(int c = middle + 1; c < GRID_COLUMNS; c++) {
								if(state[r][c] == NONE) {
									return c;
								}
							}
						} else {
							for(int i = middle; i >= 0; i--) {
								if(state[r][i] == NONE) {
									return i;
								}
							}
							for(int i = middle; i < GRID_COLUMNS; i++) {
								if(state[r][i] == NONE) {
									return i;
								}
							}
						}
					}	
				}
			} else { //Logic for is the AI is PLAYER_O. basically only plays in the middle or randomly
				int middle = (0 + (state[0].length - 1)) / 2; 
				for(int r = 0; r < GRID_ROWS - 1; r++) {
					if(state[r][middle] == NONE) {
						return middle;
					} else {
						return (new Random()).getBestColumn(state, player);
					}
				}
			}
			
			
			//we never get here, but just in case
			return (new Random()).getBestColumn(state, player);
		}
		
	}

	private class NegaMax extends AI {

		@Override
		public int getBestColumn(Player[][] state, Player player) {
			
			//loop through possible columns to place a piece in, score each column, store score and column in hashmap where score is the key, then pick the highest score and return the corresponding column
			HashMap<Integer, Integer> bestScores = new HashMap<Integer, Integer>();
			for(int c: shuffledRange(0, GRID_COLUMNS - 1)) {
				final int r = free(state, c);
				if(r >= 0) {
					state[r][c] = player;
					int score = negamax(state, 12, r, c, player);
					bestScores.put(score, c);
					state[r][c] = NONE;
				}
			}
			int maxKey = Collections.max(bestScores.keySet());
			int move = bestScores.get(maxKey);
//			System.out.println("Max score was: " + maxKey + " from possible choices: " + bestScores.toString());
//			System.out.println("The chosen move was: " + move);
			return move;
		}
		
		public int scorePos(Player[][] state, int rowHint, int columnHint, Player currentPlayer) {
			
			int score = 0;
			
			//this portion does ray tracing method you mentioned in class where given a potential piece it adds up the number of free spaces in each direction
			int vertical = sumWinning(state, rowHint, -1, columnHint, 0, NONE);
			int horizontal = sumWinning(state, rowHint, 0, columnHint, -1, NONE) + sumWinning(state, rowHint, 0, columnHint, 1, NONE);
			int diagonalUp = sumWinning(state, rowHint, 1, columnHint, 1, NONE) + sumWinning(state, rowHint, -1, columnHint, -1, NONE);
			int diagonalDown = sumWinning(state, rowHint, 1, columnHint, -1, NONE) + sumWinning(state, rowHint, -1, columnHint, 1, NONE);
			
			int freeSpaces = horizontal + vertical + diagonalUp + diagonalDown;
			int takenSpaces = GRID_ROWS * GRID_COLUMNS;
			score = takenSpaces - freeSpaces; 
	
			//this portion checks the board for any two-in-a-rows by the current player and I a chose an arbitrary number to add to the score for each direction.
			//I changed the win checking to output which type of win occured then ran Naive vs Naive 100,000 times and saw that vertical and horizontal wins occured more, so I weighted those by more
//			if(sumWinning(state, rowHint, 1, columnHint, 0, currentPlayer) == MATCH_COUNT - 2) {
//				score += 200;
//			} else if(sumWinning(state, rowHint, 0, columnHint, -1, currentPlayer) + sumWinning(state, rowHint, 0, columnHint, 1, currentPlayer) == MATCH_COUNT - 2) {
//				score += 200;
//			} else if(sumWinning(state, rowHint, 1, columnHint, 1, currentPlayer) + sumWinning(state, rowHint, -1, columnHint, -1, currentPlayer) == MATCH_COUNT - 2) {
//				score += 200;
//			} else if(sumWinning(state, rowHint, 1, columnHint, -1, currentPlayer) + sumWinning(state, rowHint, -1, columnHint, 1, currentPlayer)  == MATCH_COUNT - 2) {
//				score += 200;
//			}
			
			if(getWinner(state, rowHint, columnHint) == currentPlayer) {
				score += 1000;
			}
			
			
			return score;
		}
		
		public void printState(Player[][] state) {
			for(Player[] p: state){
				System.out.println(Arrays.toString(p));
			}
		}
		
		//checks a given state and if there are no more free spaces, returns true
		public boolean isDraw(Player[][] state) {
			int count = 0;
			for(int c = 0; c < GRID_COLUMNS - 1; c++) {
				final int r = free(state, c);
				if(r == -1) {
					count++;
				}
			}
			
			if(count == GRID_COLUMNS - 1)
				return true;
			else
				return false;
			
			
		}
		//okay before, i was looking for best score in this method and those parts are now commented out
		//apparently, since i have the getBestColumn method, I should be looking for the worst move here
		//I don't understand why but thats what works
		// advice taken from this post: (https://stackoverflow.com/questions/30202563/tic-tac-toe-negamax-implementation)
		public int negamax(Player[][] state, int depth, int rowHint, int columnHint, Player player) {
			
			if(depth == 0 | getWinner(state, rowHint, columnHint) == player) //if we're at the max depth, score and return current posistion 
				return scorePos(state, rowHint, columnHint, player);
			
			if(isDraw(state)) //if posistion is a draw, return 0
				return 0;
			
//			int best = -1000; //lower bound of score
			int worst = 1000; //upper bound of score
			
			for(int c: shuffledRange(0, GRID_COLUMNS - 1)) { //looping through possible moves
				final int r = free(state, c);
				if(r >= 0) {
					state[r][c] = opponentOf(player);
					int score = -negamax(state, depth -1, r, c, opponentOf(player));
					state[r][c] = NONE;
					if(score < worst) worst = score;
				}
			}
			//System.out.println(worst);
			return worst;
			
		}
		
	}
}
