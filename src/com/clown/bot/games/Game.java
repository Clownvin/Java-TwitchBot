package com.clown.bot.games;

public abstract class Game {
	protected final GameSession session;

	public Game(final GameSession session) {
		this.session = session;
	}

	public abstract boolean gameOver();

	public abstract void handleInput(String user, String message);
}
