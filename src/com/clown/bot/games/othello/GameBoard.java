package com.clown.bot.games.othello;

public final class GameBoard {
    private static final byte[][] DEFAULT_GAMEBOARD = new byte[][] { { 0, 0, 0, 0, 0, 0, 0, 0 },
	    { 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 1, 2, 0, 0, 0 },
	    { 0, 0, 0, 2, 1, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0 },
	    { 0, 0, 0, 0, 0, 0, 0, 0 }, };

    private byte[][] gameboard = new byte[8][8];
    private int movesLeft = 60;

    public GameBoard() {

    }

    public boolean gameOver() {
	return !playerHasMove(Othello.BLACK) && !playerHasMove(Othello.WHITE);
    }

    public byte get(int x, int y) {
	if (x > 7 || y > 7 || x < 0 || y < 0)
	    throw new IllegalArgumentException("x and y must be within the doman of all real numbers [0, 7]");
	return gameboard[x][y];
    }

    public int getMovesLeft() {
	return movesLeft;
    }

    public byte getWinner() {
	int blackCount = 0, whiteCount = 0;
	for (byte[] row : gameboard)
	    for (byte b : row)
		if (b == Othello.BLACK)
		    blackCount++;
		else if (b == Othello.WHITE)
		    whiteCount++;
	if (whiteCount == blackCount)
	    return 0;
	return whiteCount > blackCount ? Othello.WHITE : Othello.BLACK;
    }

    public boolean place(int x, int y, byte player) {
	if (x > 7 || y > 7 || x < 0 || y < 0)
	    throw new IllegalArgumentException("x and y must be within the doman of all real numbers [0, 7]");
	if (gameboard[x][y] != 0)
	    return false;
	boolean valid = false;
	for (int x2 = x + 1, y2 = y + 1; x2 < 8 && y2 < 8; x2++, y2++) {
	    if (gameboard[x2][y2] == 0)
		break;
	    if (gameboard[x2][y2] == player && x2 != x + 1) {
		for (; x2 > x && y2 > y; x2--, y2--) {
		    gameboard[x2][y2] = player;
		}
		valid = true;
		break;
	    } else if (gameboard[x2][y2] == player && x2 == x + 1)
		break;
	}
	for (int x2 = x + 1; x2 < 8; x2++) {
	    if (gameboard[x2][y] == 0)
		break;
	    if (gameboard[x2][y] == player && x2 != x + 1) {
		for (; x2 > x; x2--) {
		    gameboard[x2][y] = player;
		}
		valid = true;
		break;
	    } else if (gameboard[x2][y] == player && x2 == x + 1)
		break;
	}
	for (int x2 = x + 1, y2 = y - 1; x2 < 8 && y2 > -1; x2++, y2--) {
	    if (gameboard[x2][y2] == 0)
		break;
	    if (gameboard[x2][y2] == player && x2 != x + 1) {
		for (; x2 > x && y2 < y; x2--, y2++) {
		    gameboard[x2][y2] = player;
		}
		valid = true;
		break;
	    } else if (gameboard[x2][y2] == player && x2 == x + 1)
		break;
	}
	for (int y2 = y - 1; y2 > -1; y2--) {
	    if (gameboard[x][y2] == 0)
		break;
	    if (gameboard[x][y2] == player && y2 != y - 1) {
		for (; y2 < y; y2++) {
		    gameboard[x][y2] = player;
		}
		valid = true;
		break;
	    } else if (gameboard[x][y2] == player && y2 == y - 1)
		break;
	}
	for (int y2 = y + 1; y2 < 8; y2++) {
	    if (gameboard[x][y2] == 0)
		break;
	    if (gameboard[x][y2] == player && y2 != y + 1) {
		for (; y2 > y; y2--) {
		    gameboard[x][y2] = player;
		}
		valid = true;
		break;
	    } else if (gameboard[x][y2] == player && y2 == y + 1)
		break;
	}
	for (int x2 = x - 1; x2 > -1; x2--) {
	    if (gameboard[x2][y] == 0)
		break;
	    if (gameboard[x2][y] == player && x2 != x - 1) {
		for (; x2 < x; x2++) {
		    gameboard[x2][y] = player;
		}
		valid = true;
		break;
	    } else if (gameboard[x2][y] == player && x2 == x - 1)
		break;
	}
	for (int x2 = x - 1, y2 = y + 1; x2 > -1 && y2 < 8; x2--, y2++) {
	    if (gameboard[x2][y2] == 0)
		break;
	    if (gameboard[x2][y2] == player && x2 != x - 1) {
		for (; x2 < x && y2 > y; x2++, y2--) {
		    gameboard[x2][y2] = player;
		}
		valid = true;
		break;
	    } else if (gameboard[x2][y2] == player && x2 == x - 1)
		break;
	}
	for (int x2 = x - 1, y2 = y - 1; x2 > -1 && y2 > -1; x2--, y2--) {
	    if (gameboard[x2][y2] == 0)
		break;
	    if (gameboard[x2][y2] == player && x2 != x - 1) {
		for (; x2 < x && y2 < y; x2++, y2++) {
		    gameboard[x2][y2] = player;
		}
		valid = true;
		break;
	    } else if (gameboard[x2][y2] == player && x2 == x - 1)
		break;
	}
	if (valid) {
	    gameboard[x][y] = player;
	    movesLeft--;
	}
	return valid;
    }

    public boolean playerHasMove(byte player) {
	for (int x = 0; x < 8; x++) {
	    for (int y = 0; y < 8; y++) {
		if (validMove(x, y, player)) {
		    return true;
		}
	    }
	}
	return false;
    }

    public void reset() {
	movesLeft = 60;
	for (int x = 0; x < 8; x++) {
	    for (int y = 0; y < 8; y++) {
		gameboard[x][y] = DEFAULT_GAMEBOARD[x][y];
	    }
	}
    }

    public boolean validMove(int x, int y, byte player) {
	if (x > 7 || y > 7 || x < 0 || y < 0)
	    throw new IllegalArgumentException("x and y must be within the doman of all real numbers [0, 7]");
	if (gameboard[x][y] != 0)
	    return false;
	for (int x2 = x + 1, y2 = y + 1; x2 < 8 && y2 < 8; x2++, y2++) {
	    if (gameboard[x2][y2] == 0)
		break;
	    if (gameboard[x2][y2] == player && x2 != x + 1)
		return true;
	    else if (gameboard[x2][y2] == player && x2 == x + 1)
		break;
	}
	for (int x2 = x + 1; x2 < 8; x2++) {
	    if (gameboard[x2][y] == 0)
		break;
	    if (gameboard[x2][y] == player && x2 != x + 1)
		return true;
	    else if (gameboard[x2][y] == player && x2 == x + 1)
		break;
	}
	for (int x2 = x + 1, y2 = y - 1; x2 < 8 && y2 > -1; x2++, y2--) {
	    if (gameboard[x2][y2] == 0)
		break;
	    if (gameboard[x2][y2] == player && x2 != x + 1)
		return true;
	    else if (gameboard[x2][y2] == player && x2 == x + 1)
		break;
	}
	for (int y2 = y - 1; y2 > -1; y2--) {
	    if (gameboard[x][y2] == 0)
		break;
	    if (gameboard[x][y2] == player && y2 != y - 1)
		return true;
	    else if (gameboard[x][y2] == player && y2 == y - 1)
		break;
	}
	for (int y2 = y + 1; y2 < 8; y2++) {
	    if (gameboard[x][y2] == 0)
		break;
	    if (gameboard[x][y2] == player && y2 != y + 1)
		return true;
	    else if (gameboard[x][y2] == player && y2 == y + 1)
		break;
	}
	for (int x2 = x - 1; x2 > -1; x2--) {
	    if (gameboard[x2][y] == 0)
		break;
	    if (gameboard[x2][y] == player && x2 != x - 1)
		return true;
	    else if (gameboard[x2][y] == player && x2 == x - 1)
		break;
	}
	for (int x2 = x - 1, y2 = y + 1; x2 > -1 && y2 < 8; x2--, y2++) {
	    if (gameboard[x2][y2] == 0)
		break;
	    if (gameboard[x2][y2] == player && x2 != x - 1)
		return true;
	    else if (gameboard[x2][y2] == player && x2 == x - 1)
		break;
	}
	for (int x2 = x - 1, y2 = y - 1; x2 > -1 && y2 > -1; x2--, y2--) {
	    if (gameboard[x2][y2] == 0)
		break;
	    if (gameboard[x2][y2] == player && x2 != x - 1)
		return true;
	    else if (gameboard[x2][y2] == player && x2 == x - 1)
		break;
	}
	return false;
    }
}