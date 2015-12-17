package com.github.clownvin.jtwitchbot.account;

/**
 * 
 * @author Calvin Gene Hall
 * 
 *         This class represents the bot account info, such as username and oath
 *         code.
 *
 */
public final class TwitchAccount {
    private final String username;
    private final String oauth;

    /**
     * Creates a new immutable instance of TwitchAccount with the username and
     * oauth specified.
     * 
     * @param username
     *            username for this account
     * @param oauth
     *            oauth for this account
     */
    public TwitchAccount(final String username, final String oauth) {
	this.username = username;
	this.oauth = oauth;
    }

    /**
     * Allows access to the oauth code contained in this object.
     * 
     * @return the oauth of this object.
     */
    public String getOauth() {
	return oauth;
    }

    /**
     * Allows access to the username contained in this object.
     * 
     * @return the username of this object.
     */
    public String getUsername() {
	return username;
    }
}
