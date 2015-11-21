package com.clown.bot.games;

/**
 * 
 * @author Calvin
 *	A container object containing the two users playing the game, and the game object itself.
 */
public final class GameSession {
	private final String user1, user2;
	private Game game = null;

	/**
	 * Constructor for a new game session.
	 * @param user1 user1's name.
	 * @param user2 user2's name.
	 */
	public GameSession(String user1, String user2) {
		this.user1 = user1;
		this.user2 = user2;
	}

	/**
	 * Allows access to the game object.
	 * @return the game object.
	 */
	public Game getGame() {
		return game;
	}

	/**
	 * Allows access to the user1's string name.
	 * @return user1's string name.
	 */
	public String getPlayer1() {
		return user1;
	}

	/**
	 * Allows access to the user2's string name.
	 * @return user2's string name.
	 */
	public String getPlayer2() {
		return user2;
	}

	/**
	 * Checks whether or not the username provided is one of the two players.
	 * @param user username to check against.
	 * @return true if the username provided is a player, false if they aren't.
	 */
	public boolean isPlayer(String user) {
		return user.equalsIgnoreCase(user1) || user.equalsIgnoreCase(user2);
	}

	/**
	 * Sets the current game object to the one provided.
	 * @param game the game object to set.
	 */
	public void setGame(Game game) {
		this.game = game;
	}
}