package com.github.clownvin.jtwitchbot.account;

public final class TwitchAccount {
	private final String username;
	private final String oauth;
	
	public TwitchAccount(final String username, final String oauth) {
		this.username = username;
		this.oauth = oauth;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getOauth() {
		return oauth;
	}
}
