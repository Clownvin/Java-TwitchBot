package com.clown.bot.messaging.commands;

import com.clown.bot.regex.Word;
import com.clown.bot.user.User;

public abstract class Command extends Word {
	protected final String description, info;

	public Command(String word, String description, String info) {
		super(word);
		this.description = description;
		this.info = info;
	}

	public String getDescription() {
		return description;
	}

	public String getInfo() {
		return info;
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
