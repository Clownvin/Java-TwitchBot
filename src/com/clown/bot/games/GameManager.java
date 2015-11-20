package com.clown.bot.games;

import java.util.ArrayList;

import com.clown.bot.TwitchIRCBot;
import com.clown.bot.games.othello.Othello;

public final class GameManager {
	private static final ArrayList<GameSession> sessions = new ArrayList<GameSession>();

	private static final Thread GAME_CULLER = new Thread() {
		@Override
		public void run() {
			while (!TwitchIRCBot.killIssued()) {
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

	public static void forceSessionCull() {
		GAME_CULLER.interrupt();
	}

	public static GameSession getSession(String user) {
		for (GameSession session : sessions) {
			if (session.isPlayer(user)) {
				return session;
			}
		}
		return null;
	}

	private GameManager() {
		// To prevent instantiation
	}
}
