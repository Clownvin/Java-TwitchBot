package com.clown.bot.games;

import java.util.ArrayList;

import com.clown.bot.TwitchBot;
import com.clown.bot.games.othello.Othello;
import com.clown.bot.games.tictactoe.TicTacToe;

/**
 * 
 * @author Calvin This object is a non-instantiable type that handles all
 *         current GameSession objects.
 */
public final class GameManager {
	private static final ArrayList<GameSession> sessions = new ArrayList<GameSession>();

	/**
	 * This thread removes game sessions that are "over" from the session list.
	 */
	private static final Thread GAME_CULLER = new Thread() {
		@Override
		public void run() {
			while (!TwitchBot.killIssued()) {
				try {
					Thread.sleep(15000);
				} catch (InterruptedException e) {
				}
				for (int i = 0; i < sessions.size(); i++) {
					if (sessions.get(i).getGame() != null && sessions.get(i).getGame().gameOver()) {
						sessions.remove(i--);
					}
				}
			}
		}
	};

	static {
		GAME_CULLER.start();
	}

	/**
	 * Allows for the construction of a new game session between user1 and
	 * user2, with a game specified.
	 * 
	 * @param user1
	 *            user1's name.
	 * @param user2
	 *            user2's name.
	 * @param gameType
	 *            the game to be played.
	 */
	public static void createSession(String user1, String user2, String gameType) {
		Game game = null;
		GameSession session = new GameSession(user1, user2);
		switch (gameType) {
		case "tictactoe":
			game = new TicTacToe(session);
			session.setGame(game);
			sessions.add(session);
			break;
		case "reversi":
		case "othello":
			game = new Othello(session);
			session.setGame(game);
			sessions.add(session);
			break;
		default:
			System.err.println("No case for " + gameType);
			break;
		}
	}

	/**
	 * Forces a cull by interrupting the GAME_CULLER.
	 */
	public static void forceSessionCull() {
		GAME_CULLER.interrupt();
	}

	/**
	 * Allows access to individual game sessions.
	 * 
	 * @param user
	 *            username to search sessions for.
	 * @return the session containing a user with the same name as user, or null
	 *         if there isn't one.
	 */
	public static GameSession getSession(String user) {
		for (GameSession session : sessions) {
			if (session.isPlayer(user)) {
				return session;
			}
		}
		return null;
	}

	/**
	 * Private constructor to prevent instantiation.
	 */
	private GameManager() {
		// To prevent instantiation
	}
}
