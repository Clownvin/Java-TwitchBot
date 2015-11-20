package com.clown.bot.user;

public final class User {
	private final String username;
	private final UserType type;
	private long requestDelay;
	private long commandDelay;

	public User(final String username, UserType type) {
		this.username = username;
		this.type = type;
		this.requestDelay = System.currentTimeMillis();
		this.commandDelay = System.currentTimeMillis();
	}

	public boolean commandDelayPassed() {
		return System.currentTimeMillis() - commandDelay > 0;
	}

	public UserType getType() {
		return type;
	}

	public String getUsername() {
		return username;
	}

	public boolean requestDelayPassed() {
		return System.currentTimeMillis() - requestDelay > 0;
	}

	public void setCommandDelay(long delay) {
		this.commandDelay = System.currentTimeMillis() + delay;
	}

	public void setRequestDelay(long delay) {
		this.requestDelay = System.currentTimeMillis() + delay;
	}
}
