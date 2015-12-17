package com.github.clownvin.jtwitchbot.user;

import java.awt.Color;

import com.github.clownvin.jtwitchbot.bot.TwitchBot;

/**
 * 
 * @author Calvin A basic container type containing information about a user.
 */
public final class User {
    private final String username;
    private final String channel;
    private final UserType type;
    private long commandDelay;
    private UserData userData = null;
    private int banWarning = 0;
    private final TwitchBot bot;
    private final Color color;

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
	this.commandDelay = System.currentTimeMillis();
	this.color = new Color((int) (0xFF * Math.random()), (int) (0xFF * Math.random()),
		(int) (0xFF * Math.random()));
    }

    public void addWarning() {
	banWarning++;
	if (banWarning == 3) {
	    bot.getIrcConnection().sendMessage(channel, "/ban " + username + "");
	}
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

    @Override
    public boolean equals(Object other) {
	if (other instanceof User) {
	    return ((User) other).getUsername().equals(username);
	} else {
	    return false;
	}
    }

    public TwitchBot getBot() {
	return bot;
    }

    public String getChannel() {
	return channel;
    }

    public Color getColor() {
	return color;
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
     * Allows access to the UserData for this user
     * 
     * @return the current UserData for this user.
     */
    public UserData getUserData() {
	return userData;
    }

    /**
     * Allows access to the username of this user object.
     * 
     * @return the username.
     */
    public String getUsername() {
	return username;
    }

    public int getWarnings() {
	return banWarning;
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

    /**
     * Sets the command delay.
     * 
     * @param delay
     *            delay in milliseconds.
     */
    public void setCommandDelay(long delay) {
	this.commandDelay = System.currentTimeMillis() + delay;
    }

    @Override
    public String toString() {
	return username;
    }
}
