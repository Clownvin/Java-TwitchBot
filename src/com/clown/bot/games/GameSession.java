package com.clown.bot.games;

public final class GameSession {
	private final String user1, user2;
	private Game game = null;

	public GameSession(String user1, String user2) {
		this.user1 = user1;
		this.user2 = user2;
	}

	public Game getGame() {
		return game;
	}

	public String getPlayer1() {
		return user1;
	}

	public String getPlayer2() {
		return user2;
	}

	public boolean isPlayer(String user) {
		return user.equalsIgnoreCase(user1) || user.equalsIgnoreCase(user2);
	}

	public void setGame(Game game) {
		this.game = game;
	}
}
