package com.clown.bot.games.tictactoe;

import com.clown.bot.TwitchBot;
import com.clown.bot.games.Game;
import com.clown.bot.games.GameSession;

public class TicTacToe extends Game {
	private static final long EXPIRE_TIME = 180000; // 3 minutes.
	private char[][] gameBoard = new char[][] { { '1', '2', '3' }, { '4', '5', '6' }, { '7', '8', '9' } };
	private final int[][][] WINS = { { { 1, 1, 1 }, { 0, 0, 0 }, { 0, 0, 0 } },
			{ { 0, 0, 0 }, { 1, 1, 1 }, { 0, 0, 0 } }, { { 0, 0, 0 }, { 0, 0, 0 }, { 1, 1, 1 } },
			{ { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } }, { { 1, 0, 0 }, { 1, 0, 0 }, { 1, 0, 0 } },
			{ { 0, 1, 0 }, { 0, 1, 0 }, { 0, 1, 0 } }, { { 0, 0, 1 }, { 0, 0, 1 }, { 0, 0, 1 } },
			{ { 0, 0, 1 }, { 0, 1, 0 }, { 1, 0, 0 } } };
	private boolean piece = false; // False = X True = O
	private boolean gameOver = false;
	private String currentPlayer;
	private long expireTime = Long.MAX_VALUE;

	public TicTacToe(GameSession session) {
		super(session);
		this.currentPlayer = Math.random() >= .5 ? session.getPlayer1() : session.getPlayer2();
		TwitchBot.getGroupConnection().sendWhisper(currentPlayer, "You are player one. Make your move.");
		sendBoard(currentPlayer);
		if (currentPlayer.equalsIgnoreCase(session.getPlayer1())) {
			TwitchBot.getGroupConnection().sendWhisper(session.getPlayer2(),
					"You are player two. Other player will move first.");
		} else {
			TwitchBot.getGroupConnection().sendWhisper(session.getPlayer1(),
					"You are player two. Other player will move first.");
		}
		resetExpireTime();
	}

	public boolean boardFull() {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (gameBoard[i][j] != 'X' && gameBoard[i][j] != 'O') {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean gameOver() {
		if (System.currentTimeMillis() > expireTime) {
			TwitchBot.getGroupConnection().sendWhisper(session.getPlayer1(),
					"Your tic-tac-toe session has expired.");
			TwitchBot.getGroupConnection().sendWhisper(session.getPlayer2(),
					"Your tic-tac-toe session has expired.");
			TwitchBot.getGroupConnection().sendWhisper(currentPlayer,
					"You have lost karma for abandoning your tic-tac-toe game.");
			if (currentPlayer.equals(session.getPlayer1())) {
				TwitchBot.getGroupConnection().sendWhisper(session.getPlayer2(),
						"You have won the match by forfeit.");
			} else {
				TwitchBot.getGroupConnection().sendWhisper(session.getPlayer1(),
						"You have won the match by forfeit.");
			}
			return true;
		}
		return gameOver;
	}

	@Override
	public void handleInput(String user, String message) {
		resetExpireTime(); // They're active.
		if (message.equalsIgnoreCase("resendboard")) {
			sendBoard(user);
			return;
		}
		// Message should the message without the command part in it. So, if
		// move x y was issued, it would just be x y
		if (user.equalsIgnoreCase(currentPlayer)) {
			if (message.length() == 1) { // 1 character in the string.
				try {
					int val = Integer.parseInt(message) - 1;
					if (val >= 0 && val < 10) {
						if (gameBoard[val / 3][val % 3] != 'O' && gameBoard[val / 3][val % 3] != 'X') {
							gameBoard[val / 3][val % 3] = piece ? 'O' : 'X';
							if (!hasWon(piece) && !boardFull()) {
								piece = !piece;
								if (currentPlayer.equals(session.getPlayer1())) {
									currentPlayer = session.getPlayer2();
								} else {
									currentPlayer = session.getPlayer1();
								}
								TwitchBot.getGroupConnection().sendWhisper(user,
										"Your turn is now over. Please wait for the other player.");
								sendBoard(currentPlayer);
							} else {
								if (hasWon(piece)) { // Winner!
									TwitchBot.getGroupConnection().sendWhisper(currentPlayer, "You won!");
									if (currentPlayer.equals(session.getPlayer1())) {
										TwitchBot.getGroupConnection().sendWhisper(session.getPlayer2(),
												"Aww, you lost. :(");
									} else {
										TwitchBot.getGroupConnection().sendWhisper(session.getPlayer1(),
												"Aww, you lost. :(");
									}
								} else { // Only other case is that board is
											// full.
									// TODO Make karma real and save it.
									TwitchBot.getGroupConnection().sendWhisper(session.getPlayer1(),
											"Game ended in a draw. You've both gained karma. :3");
									TwitchBot.getGroupConnection().sendWhisper(session.getPlayer2(),
											"Game ended in a draw. You've both gained karma. :3");
								}
								gameOver = true;
							}
						} else {
							TwitchBot.getGroupConnection().sendWhisper(user, "That tile already has a piece in it!");
						}
					} else {
						TwitchBot.getGroupConnection().sendWhisper(user,
								"Please use a number in the range of 1-9 only.");
					}
				} catch (NumberFormatException e) {
					TwitchBot.getGroupConnection().sendWhisper(user,
							"Please use a number in the range of 1-9 only. And they have to numbers DansGame.");
				}
			} else {
				TwitchBot.getGroupConnection().sendWhisper(user, "You must use the command \"!move value\".");
			}
		} else {
			TwitchBot.getGroupConnection().sendWhisper(user, "It's not your turn yet.");
		}
	}

	public boolean hasWon(boolean currPiece) {
		char currChar = currPiece ? 'O' : 'X';
		int count;
		for (int i = 0; i < WINS.length; i++) {
			count = 0;
			for (int y = 0; y < 3; y++) {
				for (int x = 0; x < 3; x++) {
					if (WINS[i][y][x] == 1 && (gameBoard[y][x] == currChar)) {
						count++;
					}
				}
			}
			if (count == 3) {
				return true;
			}
		}
		return false;
	}

	private void resetExpireTime() {
		expireTime = System.currentTimeMillis() + EXPIRE_TIME;
	}

	private void sendBoard(String user) {
		TwitchBot.getGroupConnection().sendWhisper(user,
				"|" + gameBoard[0][0] + "|" + gameBoard[0][1] + "|" + gameBoard[0][2] + "|");
		TwitchBot.getGroupConnection().sendWhisper(user,
				"|" + gameBoard[1][0] + "|" + gameBoard[1][1] + "|" + gameBoard[1][2] + "|");
		TwitchBot.getGroupConnection().sendWhisper(user,
				"|" + gameBoard[2][0] + "|" + gameBoard[2][1] + "|" + gameBoard[2][2] + "|");
	}

}
