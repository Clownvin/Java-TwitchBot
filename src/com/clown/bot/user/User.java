package com.clown.bot.user;

/**
 * 
 * @author Calvin
 *	A basic container type containing information about a user.
 */
public final class User {
	private final String username;
	private final UserType type;
	private long requestDelay;
	private long commandDelay;

	/**
	 * Constructor for a new instance.
	 * @param username username of the user.
	 * @param type type of user.
	 */
	public User(final String username, UserType type) {
		this.username = username;
		this.type = type;
		this.requestDelay = System.currentTimeMillis();
		this.commandDelay = System.currentTimeMillis();
	}
	
	/**
	 * Checks whether or not the command delay has passed.
	 * @return true if the delay has passed, false if it hasn't.
	 */
	public boolean commandDelayPassed() {
		return System.currentTimeMillis() - commandDelay > 0;
	}
	
	/**
	 * Allows access to the type of this user container.
	 * @return the type of user.
	 */
	public UserType getType() {
		return type;
	}

	/**
	 * Allows access to the username of this user object.
	 * @return the username.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Checks whether the delay has passed.
	 * @return true if the delay has passed, false if it hasn't.
	 */
	public boolean requestDelayPassed() {
		return System.currentTimeMillis() - requestDelay > 0;
	}

	/**
	 * Sets the command delay.
	 * @param delay delay in milliseconds.
	 */
	public void setCommandDelay(long delay) {
		this.commandDelay = System.currentTimeMillis() + delay;
	}

	/**
	 * Sets the request delay.
	 * @param delay delay in milliseconds.
	 */
	public void setRequestDelay(long delay) {
		this.requestDelay = System.currentTimeMillis() + delay;
	}
}
