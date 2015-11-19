package com.clown.bot.user;

public final class User {
	private final String username;
	private final UserType type;
	
	public User(final String username, UserType type) {
		this.username = username;
		this.type = type;
	}
	
	public String getUsername() {
		return username;
	}
	
	public UserType getType() {
		return type;
	}
}
