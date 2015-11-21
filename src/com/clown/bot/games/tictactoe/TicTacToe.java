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
	private long expireTime = Long.MAX_VALUE;

	public TicTacToe(GameSession session) {
		super(session);
		this.currentPlayer = Math.random() >= .5 ? session.getPlayer1() : session.getPlayer2();
		currentPlayer.sendWhisper("You are player one. Make your move.");
		sendBoard(currentPlayer.getUsername());
		if (currentPlayer.getUsername().equalsIgnoreCase(session.getPlayer1().getUsername())) {
			session.getPlayer2().sendWhisper("You are player two. Other player will move first.");
		} else {
			session.getPlayer1().sendWhisper("You are player two. Other player will move first.");
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
			session.getPlayer1().sendWhisper("Your tic-tac-toe session has expired.");
			session.getPlayer2().sendWhisper("Your tic-tac-toe session has expired.");
			currentPlayer.sendWhisper("You have lost karma for abandoning your tic-tac-toe game.");
			currentPlayer.getUserData().addKarma(-100);
			if (currentPlayer.equals(session.getPlayer1())) {
				session.getPlayer2().sendWhisper("You have won the match by forfeit, but only gain karma.");
				session.getPlayer2().getUserData().addKarma(25);
			} else {
				session.getPlayer1().sendWhisper("You have won the match by forfeit, but only gain karma.");
				session.getPlayer2().getUserData().addKarma(25);
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
		if (user.equalsIgnoreCase(currentPlayer.getUsername())) {
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
								sendBoard(currentPlayer.getUsername());
							} else {
								if (hasWon(piece)) { // Winner!
									currentPlayer.sendWhisper("You won!");
									currentPlayer.getUserData().addKarma(25);
									currentPlayer.getUserData().addPoints(1);
									if (currentPlayer.equals(session.getPlayer1())) {
										session.getPlayer2().sendWhisper("Aww, you lost. :(");
										session.getPlayer2().getUserData().addKarma(25);
									} else {
										session.getPlayer1().sendWhisper("Aww, you lost. :(");
										session.getPlayer1().getUserData().addKarma(25);
									}
								} else {
									session.getPlayer1().getUserData().addKarma(50);
									session.getPlayer2().getUserData().addKarma(50);
									session.getPlayer1().sendWhisper("Game ended in a draw. You've both gained karma. :3");
									session.getPlayer2().sendWhisper("Game ended in a draw. You've both gained karma. :3");
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

	@Override
	protected void sendBoard(String user) {
		TwitchBot.getGroupConnection().sendWhisper(user,
				"|" + gameBoard[0][0] + "|" + gameBoard[0][1] + "|" + gameBoard[0][2] + "|");
		TwitchBot.getGroupConnection().sendWhisper(user,
				"|" + gameBoard[1][0] + "|" + gameBoard[1][1] + "|" + gameBoard[1][2] + "|");
		TwitchBot.getGroupConnection().sendWhisper(user,
				"|" + gameBoard[2][0] + "|" + gameBoard[2][1] + "|" + gameBoard[2][2] + "|");
	}

}
