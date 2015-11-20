package com.clown.bot.games.othello;

public final class Move {
	private final int x, y;
	private final int num;

	public Move(int x, int y, int num) {
		this.x = x;
		this.y = y;
		this.num = num;
	}

	public int getNum() {
		return num;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
}
