package com.clown.bot.games;

/**
 * 
 * @author Calvin
 *	This object represents the basic structure needed for all chat games.
 */
public abstract class Game {
	protected final GameSession session;
	
	/**
	 * Constructor for a new game
	 * @param session session containing the users.
	 */
	public Game(final GameSession session) {
		this.session = session;
	}

	/**
	 * Tests whether or not the game is over.
	 * @return true if the game is over, false if it isn't.
	 */
	public abstract boolean gameOver();

	/**
	 * Handles input from a user.
	 * @param user user sending the input.
	 * @param message message containing input.
	 */
	public abstract void handleInput(String user, String message);
}
