package com.github.clownvin.jtwitchbot.user;

import com.github.clownvin.jtwitchbot.bot.TwitchBot;

/**
 * 
 * @author Calvin A basic container type containing information about a user.
 */
public final class User {
	private final String username;
	private final String channel;
	private final UserType type;
	private long requestDelay;
	private long commandDelay;
	private UserData userData = null;
	private int banWarning = 0;
	private final TwitchBot bot;

	public void addWarning() {
		banWarning++;
		if (banWarning == 3) {
			bot.getIrcConnection().sendMessage(channel, "/ban " + username + "");
		}
	}

	public int getWarnings() {
		return banWarning;
	}
	
	public TwitchBot getBot() {
		return bot;
	}

	/**
	 * Constructor for a new instance.
	 * 
	 * @param username
	 *            username of the user.
	 * @param type
	 *            type of user.
	 */
	public User(final String username, final String channel, final UserType type, final TwitchBot bot) {
		this.username = username;
		this.channel = channel;
		this.bot = bot;
		this.type = type;
		this.requestDelay = System.currentTimeMillis();
		this.commandDelay = System.currentTimeMillis();
	}

	/*
	 * 
	 * /** Checks whether or not the command delay has passed.
	 * 
	 * @return true if the delay has passed, false if it hasn't.
	 */
	public boolean commandDelayPassed() {
		return System.currentTimeMillis() - commandDelay > 0;
	}

	/**
	 * Allows access to the type of this user container.
	 * 
	 * @return the type of user.
	 */
	public UserType getType() {
		return type;
	}

	/**
	 * Allows access to the username of this user object.
	 * 
	 * @return the username.
	 */
	public String getUsername() {
		return username;
	}
	
	public String getChannel() {
		return channel;
	}

	/**
	 * Checks whether the delay has passed.
	 * 
	 * @return true if the delay has passed, false if it hasn't.
	 */
	public boolean requestDelayPassed() {
		return System.currentTimeMillis() - requestDelay > 0;
	}

	/**
	 * Sets the command delay.
	 * 
	 * @param delay
	 *            delay in milliseconds.
	 */
	public void setCommandDelay(long delay) {
		this.commandDelay = System.currentTimeMillis() + delay;
	}

	/**
	 * Sets the request delay.
	 * 
	 * @param delay
	 *            delay in milliseconds.
	 */
	public void setRequestDelay(long delay) {
		this.requestDelay = System.currentTimeMillis() + delay;
	}

	/**
	 * Allows access to the UserData for this user
	 * 
	 * @return the current UserData for this user.
	 */
	public UserData getUserData() {
		return userData;
	}

	/**
	 * Loads the UserData.
	 */
	public void loadUserData() {
		this.userData = UserData.loadUserData(this);
	}

	/**
	 * Saves the data for this user.
	 */
	public void save() {
		userData.saveData();
	}

	/**
	 * Sends whisper to user.
	 * 
	 * @param message
	 *            the contents of the whisper
	 */
	public void sendWhisper(String message) {
		bot.getGroupConnection().sendWhisper(this, message);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof User) {
			return ((User) other).getUsername().equals(username);
		} else {
			return false;
		}
	}
}
