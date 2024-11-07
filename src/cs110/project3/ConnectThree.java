//1908-110-3

/* 
 * Name: Bradley Newlon
 * Assignment: Project 3
 * Lab: CS 110 Section 12
 * Date: 12/13/19
 * 
 */

package cs110.project3;




import javafx.application.*;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.geometry.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import java.net.URL;
import java.util.*;
import static cs110.project3.ConnectThree.Player.*;

public abstract class ConnectThree extends Application {
	// THESE CONSTANTS CAN BE CHANGED FOR TESTING
	
	/** The dimensions of the game grid. */
	protected static final int GRID_COLUMNS = 4, GRID_ROWS = 5;
//	protected static final int GRID_COLUMNS = 7, GRID_ROWS = 6;
	/** The number of marks in a line to win. */
	protected static final int MATCH_COUNT = 3;
//	protected static final int MATCH_COUNT = 4;
	
	/** The number of games to run when automating tests. */
	protected static final int AUTOMATED_GAMES = 100;
	
	// *******************************************
	// * DO NOT CHANGE ANYTHING BEYOND THIS LINE *
	// *******************************************
	
	// GRAPHICAL USER INTERFACE	
	
	private static final String X_CHAR = "\u274C", O_CHAR = "\u2B24";
	
	private Cell[][] cells = new Cell[GRID_ROWS][GRID_COLUMNS];
	
	private IntegerProperty elapsed;
	private ObjectProperty<Player> turn;
	private StringProperty status, aiX, aiO;
	
	@Override
	public final void start(final Stage stage) {
        stage.setTitle("Connect Three by Bradley Newlon for bonus");
        
		registerAI(new Human());
		registerAI(new Random());
		registerAI(new Naive());
		registerOriginalAIs();
		
		final BorderPane pane = new BorderPane(); {
			pane.getStyleClass().add("pane");
        	 
			final MenuBar bar = new MenuBar(); {
        		pane.setTop(bar);
        		
        		final Menu game = new Menu("_Game"); {
        			bar.getMenus().add(game);
        			
        			final MenuItem newgame = new MenuItem("_New Game");
        			newgame.setAccelerator(KeyCombination.keyCombination("SHORTCUT+N"));
        			newgame.setOnAction(event -> newGame());
        			game.getItems().add(newgame);
    				
        			final MenuItem exit = new MenuItem("E_xit");
        			exit.setAccelerator(KeyCombination.keyCombination("ESC"));
    				exit.setOnAction(event -> Platform.exit());
    				game.getItems().add(exit);
        		}
        		
        		final Menu testing = new Menu("_Testing"); {
        			bar.getMenus().add(testing);

        			final MenuItem auto = new MenuItem(String.format("_Automate %,d Games", AUTOMATED_GAMES));
        			auto.setAccelerator(KeyCombination.keyCombination("SHORTCUT+A"));
        			auto.setOnAction(event -> newGame(AUTOMATED_GAMES));
        			testing.getItems().add(auto);
        			
        			final MenuItem rest = new MenuItem("_Reset Win History");
        			rest.setAccelerator(KeyCombination.keyCombination("SHORTCUT+R"));
        			rest.setOnAction(event -> resetHistory());
        			testing.getItems().add(rest);
        			
        			final MenuItem history = new MenuItem("Output Win _History");
        			history.setAccelerator(KeyCombination.keyCombination("SHORTCUT+H"));
        			history.setOnAction(event -> outputHistory());
        			testing.getItems().add(history);
        		}
        	} 
        	
			final GridPane grid = new GridPane(); {
    			pane.setCenter(grid);
    			
    			grid.setPadding(new Insets(15));
    			grid.setHgap(15);
    			grid.setVgap(15);
  
				final RowConstraints rcons = new RowConstraints();
				rcons.setPercentHeight(1.0/GRID_ROWS*100);
				rcons.setVgrow(Priority.ALWAYS);
    			for (int r = 1; r <= GRID_ROWS; r++)
    				grid.getRowConstraints().add(rcons);
    			
				final ColumnConstraints ccons = new ColumnConstraints();
				ccons.setPercentWidth(1.0/GRID_COLUMNS*100);
				ccons.setHgrow(Priority.ALWAYS);
    			for (int c = 1; c <= GRID_COLUMNS; c++) 
    				grid.getColumnConstraints().add(ccons);
    	         
        		for (int r = 0; r < GRID_ROWS; r++) {
        			for (int c = 0; c < GRID_COLUMNS; c++) {
        				final int column = c;
        				final Button button = new Button();
        				button.setOnMouseClicked(event -> playInColumn(column));
        				button.getStyleClass().add("cell-none");
        				button.styleProperty().bind(Bindings.concat("-fx-alignment: center;", "-fx-font-size: ", button.heightProperty().divide(1.5).asString(), "px;"));
        				button.setMinSize(15, 15);
        				button.setPrefSize(45, 45);
        				button.setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        				grid.add(button, c, r);
        				cells[r][c] = new Cell(button);
        			}
        		}
        	}
        	
        	final GridPane dashboard = new GridPane(); {
    			pane.setBottom(dashboard);
    			pane.getBottom().setStyle("-fx-font-size: 1.5em;");
        		
    			dashboard.setPadding(new Insets(0, 15, 15, 15));
    			dashboard.setHgap(15);
    			
				final ColumnConstraints dcons = new ColumnConstraints();
				dcons.setPercentWidth(1.0/3*100);
				dcons.setHgrow(Priority.ALWAYS);
    			for (int c = 1; c <= 3; c++)
    				dashboard.getColumnConstraints().add(dcons);
    			
    			final ChoiceBox<String> ctrlX = new ChoiceBox<>();
    			final ChoiceBox<String> ctrlO = new ChoiceBox<>();
    			
    			for (ChoiceBox<String> cb: List.of(ctrlX, ctrlO)) {
    				cb.getItems().addAll(AIs.keySet());
    				cb.getSelectionModel().selectFirst();
    				cb.setStyle("-fx-background-radius: 0;");
    				cb.setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    				cb.getSelectionModel().selectedItemProperty().addListener((selected, oldValue, newValue) -> {
    					resetHistory();
    					playTurn();
    				});  
    			}
    			
				ctrlX.getStyleClass().addAll("cell-playerx");
				aiX = new SimpleStringProperty();
				aiX.bind(ctrlX.getSelectionModel().selectedItemProperty());
				dashboard.add(ctrlX, 0, 0);
				
				ctrlO.getStyleClass().addAll("cell-playero");
				aiO = new SimpleStringProperty();
				aiO.bind(ctrlO.getSelectionModel().selectedItemProperty());
				dashboard.add(ctrlO, 2, 0);
				
    			final Label winner = new Label();
    			status = new SimpleStringProperty();
    			elapsed = new SimpleIntegerProperty();
    			winner.textProperty().bind(status);
    			winner.setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    			winner.setStyle("-fx-alignment: center;");
				dashboard.add(winner, 1, 0);
				
				turn = new SimpleObjectProperty<Player>();
				turn.addListener(new ChangeListener<>() {
					@Override
					public void changed(ObservableValue<? extends Player> observable, Player oldValue, Player newValue) {
						if (newValue == PLAYER_X)
							ctrlX.getStyleClass().remove("cell-fade");
						else
							ctrlX.getStyleClass().add("cell-fade");
						
						if (newValue == PLAYER_O)
							ctrlO.getStyleClass().remove("cell-fade");
						else
							ctrlO.getStyleClass().add("cell-fade");
					}
				});
        	}
        } 

		resetHistory();
		newGame();
		
		final Scene scene = new Scene(pane);
		final URL css = ConnectThree.class.getResource("style.css");
		if (css == null) {
			System.err.println("Stylesheet resource is missing: src/cs110/project3/style.css");
			System.exit(1);
		}
		scene.getStylesheets().add(css.toExternalForm());
		stage.setScene(scene);
		
		stage.setResizable(false);
		stage.setOnShown(event -> {
	    	final Screen screen = Screen.getPrimary();
	        final double ratio = (screen.getVisualBounds().getHeight() * .85) / stage.getHeight();
	        stage.setHeight(stage.getHeight() * ratio);
	        stage.setWidth(stage.getWidth() * ratio);
	        stage.setX((screen.getVisualBounds().getWidth() - stage.getWidth()) / 2); 
	        stage.setY((screen.getVisualBounds().getHeight() - stage.getHeight()) / 2);  
		});
		
    	stage.show();
	}
	
    private final class Cell {
    	private Button button;
    	public Player player;
    	
    	public Cell(Button button) {
    		this.button = button;
    		this.player = NONE;
    	}
    	
    	private final void setPlayer(Player player) {
    		if (player == PLAYER_X) {
    			this.player = PLAYER_X;
        		button.getStyleClass().clear();
        		button.getStyleClass().add("cell-playerx");
        		button.setText(X_CHAR);
    		}
    		else if (player == PLAYER_O) {
    			this.player = PLAYER_O;
        		button.getStyleClass().clear();
        		button.getStyleClass().add("cell-playero");
        		button.setText(O_CHAR);
    		}
    		else {
	    		this.player = NONE;
	    		button.getStyleClass().clear();
	    		button.getStyleClass().add("cell-none");
	    		button.setText("");
    		}
    	}
    }
    
	// TESTING SUITE

	private int winCountX, winCountO, drawCount;
	
	private final void resetHistory() {
		winCountX = 0;
		winCountO = 0;
		drawCount = 0;
	}
	
	private final void outputHistory() {
    	final int totalGames = winCountX + winCountO + drawCount;
    	if (totalGames > 0)
	    	System.out.printf("%,d Games: X (%s) wins %.1f%%, O (%s) wins %.1f%%, draw %.1f%%\n",
	    		totalGames,
	    		aiX.get(),
	    		100 * (double) winCountX / totalGames,
	    		aiO.get(),
	    		100 * (double) winCountO / totalGames,
	    		100 * (double) drawCount / totalGames
	    	);
    	else System.out.println("No win history recorded");
	}
	
    // GAME LOGIC
    
    /**
     * Represents Player X, Player O, or neither.
     */
    public enum Player {PLAYER_X, PLAYER_O, NONE}
	
	private final void newGame(int repetitions) {
		for (int r = 1; r <= repetitions; r++)
			newGame();
	}
	
	private final void newGame() {
		for (int i = 0; i < GRID_ROWS; i++)
			for (int j = 0; j < GRID_COLUMNS; j++)
				cells[i][j].setPlayer(NONE);
		
		elapsed.set(0);
		turn.set(PLAYER_X);
		status.set("TURN " + (elapsed.get() + 1));
		playTurn();
	}
    
    private final void playTurn() {
    	if (turn.get() != NONE) {
    		final String name = turn.get() == PLAYER_X ? aiX.get() : aiO.get();
	    	final int choice = AIs.get(name).getBestColumn(state(), turn.get());
	    	if (choice != -1) 
	    		playInColumn(choice);
    	}
    }
	
	/** 
	 * The total number of cells in the game grid. 
	 */
	protected static final int CELL_COUNT = GRID_COLUMNS * GRID_ROWS;
    
    private final void playInColumn(int column) {
    	if (turn.get() != NONE) {
			final int row = free(state(), column);
			if (row >= 0) {
				final Cell target = cells[row][column];
				if (target.player == NONE) {
					target.setPlayer(turn.get());
					elapsed.set(elapsed.get() + 1);
					
					final Player winner = getWinner(state(), row, column);
					if (winner == PLAYER_X) {
						status.set(X_CHAR + " WINS");
						turn.set(NONE);
						winCountX++;
					}
					else if (winner == PLAYER_O) {
						status.set(O_CHAR + " WINS");
						turn.set(NONE);
						winCountO++;
					}
					else if (elapsed.get() == CELL_COUNT) {
						status.set("DRAW");
						turn.set(NONE);
						drawCount++;
					}
					else {
						status.set("TURN " + (elapsed.get() + 1));
						turn.set(opponentOf(turn.get()));
		    			playTurn();
					}
				}
			}
    	}
    }
    
	/**
	 * Determines any winner of a game state.
	 * 
	 * @param state any game state
	 * @param rowHint the row index for a played cell, if useful
	 * @param columnHint the column index for a played cell, if useful
	 * @return the winning player, if any
	 */
	protected abstract Player getWinner(Player[][] state, int rowHint, int columnHint);
	
    // HELPER ACCESSORS
    
	/**
	 * Retrieves a copy of the current game state.
	 * The copy can be mutated with no consequences.
	 * 
	 * @return a copy of the game state
	 */
	protected final Player[][] state() {
    	final Player[][] state = new Player[GRID_ROWS][GRID_COLUMNS];
    	for (int r = 0; r < GRID_ROWS; r++)
    		for (int c = 0; c < GRID_COLUMNS; c++)
    			state[r][c] = cells[r][c].player;
    	return state;
    }
    
    /**
     * Determines the uppermost empty position in
     * the given column of the given game state,
     * or -1 if the column is full.
     * 
     * @param state a game state
     * @param column a column index
     * @return the empty position or -1
     */
    protected final int free(Player[][] state, int column) {
    	for (int r = GRID_ROWS-1; r >= 0; r--)
    		if (state[r][column] == NONE)
    			return r;
    	return -1;
    }
    
    /**
     * Determines the opponent of a player, if any.
     * 
     * @param player a given player
     * @return the opponent of the given player
     */
    protected final Player opponentOf(Player player) {
		switch (player) {
			case PLAYER_X:
				return PLAYER_O;
			case PLAYER_O:
				return PLAYER_X;    
			default:
				return NONE;
		}
    }
    
    /**
     * Returns a list of integers in the given range.
     * The range can be ascending or descending.
     * 
     * @param from the starting integer, inclusive
     * @param to the ending integer, inclusive
     * @return the list of integers
     */
    protected final List<Integer> range(int from, int to) {
    	final List<Integer> result = new ArrayList<>();
    	if (from <= to)
	    	for (int i = from; i <= to; i++)
	    		result.add(i);
    	else
	    	for (int i = from; i >= to; i--)
	    		result.add(i);
    	return result;
    }
    
    /**
     * Returns a list of integers in the given range,
     * shuffled randomly.
     * 
     * @param from the starting integer, inclusive
     * @param to the ending integer, inclusive
     * @return the shuffled list of integers
     */
    protected final List<Integer> shuffledRange(int from, int to) {
    	final List<Integer> result = range(from, to);
    	Collections.shuffle(result);
    	return result;
    }
    
	// ARTIFICIAL INTELLIGENCE
    
	/**
	 * Defines the AI protocols.
	 */
	protected abstract class AI {
		/**
		 * Determines the best column to play in.
		 * 
		 * @param state a game state to play in
		 * @param player a player to play in a column
		 * @return the column the AI decides
		 */
		public abstract int getBestColumn(Player[][] state, Player player);
		
		/**
		 * Returns the friendly name of this AI.
		 * 
		 * @return this AI's friendly name
		 */
		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}
	}

	private final static Map<String, AI> AIs = new LinkedHashMap<>();
	
	/**
	 * Used to register your original AI(s) using {@link #registerAI}.
	 */
	protected abstract void registerOriginalAIs();
	
	/**
	 * Adds a custom AI to the drop-down menus.
	 * 
	 * @param ai an AI instance
	 */
	protected final void registerAI(AI ai) {
		if (AIs.containsKey(ai.toString()))
			throw new IllegalStateException("AI name " + ai.toString() + " is already registered");
		
		AIs.put(ai.toString(), ai);
	}
	
	/** 
	 * This is a dummy AI that never decides a column,
	 * requiring a human player to select a column.
	 */
	protected final class Human extends AI {
		@Override
		public int getBestColumn(Player[][] state, Player player) {
			return -1; // decides no column
		}
	}
	
	/** 
	 * This AI picks a random free column
	 * without any strategy.
	 */
	protected final class Random extends AI {
		@Override
		public int getBestColumn(Player[][] state, Player player) {
			for (int c: shuffledRange(0, GRID_COLUMNS-1))
				if (free(state, c) >= 0)
					return c;
			return -1;
		}
	}
	
	/** 
	 * This AI picks a column it can win in, or else
	 * it picks a column the opponent can win in to
	 * block them. Otherwise, it plays as randomly.
	 */
	protected final class Naive extends AI {
		@Override
		public int getBestColumn(Player[][] state, Player player) {
			final Player[] players = {player, opponentOf(player)};
			for (Player p: players) {
				for (int c: shuffledRange(0, GRID_COLUMNS-1)) {
					final int r = free(state, c);
					if (r >= 0) {
						state[r][c] = p;
						if (getWinner(state, r, c) == p)
							return c;
						state[r][c] = NONE;
					}
				}
			}
			return (new Random()).getBestColumn(state, player);
		}
	}
}
