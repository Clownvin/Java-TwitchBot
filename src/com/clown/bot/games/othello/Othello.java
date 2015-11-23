package com.clown.bot.games.othello;

import java.util.ArrayList;

import com.clown.bot.TwitchBot;
import com.clown.bot.games.Game;
import com.clown.bot.games.GameSession;

public final class Othello extends Game {

    public static final byte BLACK = 1, WHITE = 2;
    private static final char BLACK_CHAR = 'X', WHITE_CHAR = 'O';
    private byte currentByte = BLACK;
    private final GameBoard gameBoard = new GameBoard();

    private final ArrayList<Move> availableMoves = new ArrayList<Move>();

    public Othello(GameSession session) {
	super(session);
	gameBoard.reset();
	this.currentPlayer = Math.random() >= .5 ? session.getPlayer1() : session.getPlayer2();
	currentPlayer.sendWhisper("You are player one. Make your move.");
	sendBoard(currentPlayer.getUsername());
	if (currentPlayer.getUsername().equalsIgnoreCase(session.getPlayer1().getUsername())) {
	    session.getPlayer2().sendWhisper("You are player two. Other player will move first.");
	} else {
	    session.getPlayer1().sendWhisper("You are player two. Other player will move first.");
	}
	session.resetExpireTime();
    }

    @Override
    public boolean gameOver() {
	return gameOver;
    }

    @Override
    public void handleInput(String user, String message) { // They're active.
	if (message.equalsIgnoreCase("resendboard")) {
	    sendBoard(user);
	    return;
	}
	session.resetExpireTime();
	// Message should the message without the command part in it. So, if
	// move x y was issued, it would just be x y
	//TODO Refactor
	if (user.equalsIgnoreCase(currentPlayer.getUsername())) {
	    if (message.length() == 1) { // 1 character in the string.
		try {
		    int val = Integer.parseInt(message) - 1;
		    if (val >= 0 && val < availableMoves.size()) {
			Move move = availableMoves.get(val);
			gameBoard.place(move.getX(), move.getY(), currentByte);
			if (!gameBoard.gameOver()) {
			    if (gameBoard.playerHasMove((currentByte == BLACK ? WHITE : BLACK))) {
				currentByte = currentByte == BLACK ? WHITE : BLACK;
				if (currentPlayer.equals(session.getPlayer1())) {
				    currentPlayer = session.getPlayer2();
				} else {
				    currentPlayer = session.getPlayer1();
				}
				TwitchBot.getGroupConnection().sendWhisper(user,
					"Your turn is now over. Please wait for the other player.");
				sendBoard(currentPlayer.getUsername());
			    } else {
				if (currentPlayer.equals(session.getPlayer1())) {
				    sendBoard(session.getPlayer2().getUsername());
				    session.getPlayer2().sendWhisper(
					    "It seems you don't have any moves, so the other player will go again.");
				    sendBoard(session.getPlayer1().getUsername());
				    session.getPlayer1()
					    .sendWhisper("The other player has no moves, so you may move again.");
				} else {
				    sendBoard(session.getPlayer1().getUsername());
				    session.getPlayer1().sendWhisper(
					    "It seems you don't have any moves, so the other player will go again.");
				    sendBoard(session.getPlayer2().getUsername());
				    session.getPlayer2()
					    .sendWhisper("The other player has no moves, so you may move again.");
				}
			    }
			} else {
			    sendBoard(session.getPlayer1().getUsername());
			    sendBoard(session.getPlayer2().getUsername());
			    if (hasWon(currentByte)) { // Winner!
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
			    } else { // Only other case is that board is
				     // full.
				     // TODO Make karma real and save it.
				session.getPlayer1().sendWhisper("Game ended in a draw. You've both gained karma. :3");
				session.getPlayer2().sendWhisper("Game ended in a draw. You've both gained karma. :3");
				session.getPlayer1().getUserData().addKarma(50);
				session.getPlayer2().getUserData().addKarma(50);
			    }
			    gameOver = true;
			}
		    } else {
			TwitchBot.getGroupConnection().sendWhisper(user,
				"Please use a number in the range of 1-" + availableMoves.size() + " only.");
		    }
		} catch (NumberFormatException e) {
		    TwitchBot.getGroupConnection().sendWhisper(user, "Please use a number in the range of 1-"
			    + availableMoves.size() + " only. And they have to numbers DansGame.");
		}
	    } else {
		TwitchBot.getGroupConnection().sendWhisper(user, "You must use the command \"!move value\".");
	    }
	} else {
	    TwitchBot.getGroupConnection().sendWhisper(user, "It's not your turn yet.");
	}

    }

    public boolean hasWon(byte player) {
	return gameBoard.getWinner() == player;
    }

    public void refreshAvailableMoves() {
	availableMoves.clear();
	int moveNum = 0;
	for (int x = 0; x < 8; x++) {
	    for (int y = 0; y < 8; y++) {
		if (gameBoard.validMove(x, y, currentByte)) {
		    availableMoves.add(new Move(x, y, ++moveNum));
		}
	    }
	}
	System.out.println("Num avail moves: " + moveNum);
    }

    // ffffff88 and ffffff91
    @Override
    protected void sendBoard(String user) {
	refreshAvailableMoves();
	for (int x = 0; x < 8; x++) {
	    String line = "|";
	    outer: for (int y = 0; y < 8; y++) {
		for (Move move : availableMoves) {
		    if (x == move.getX() && y == move.getY()) {
			line += move.getNum() + move.getNum() > 9 ? "|" : "_|";
			continue outer;
		    }
		}
		line += (gameBoard.get(x, y) == 1 ? "" + BLACK_CHAR + BLACK_CHAR
			: gameBoard.get(x, y) == 0 ? "__" : "" + WHITE_CHAR + WHITE_CHAR) + "|";
	    }
	    TwitchBot.getGroupConnection().sendWhisper(user, line);
	}
    }

}
