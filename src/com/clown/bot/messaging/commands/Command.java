package com.clown.bot.messaging.commands;

import com.clown.bot.regex.Word;
import com.clown.bot.user.User;

public abstract class Command extends Word {
	protected final String info;

	public Command(String word, String info) {
		super(word);
		this.info = info;
	}

	public String getInfo() {
		return info;
	}

	public boolean hasAccess(User user) {
		return true; // Return true by default. Some commands may override.
	}

	public abstract void handleCommand(User user, String args[], String message);

	@Override
	public boolean equals(Object object) {
		if (object instanceof Command) {
			return ((Command) object).word.equals(word);
		} else {
			return false;
		}
	}
}
