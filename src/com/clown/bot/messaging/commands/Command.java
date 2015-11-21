package com.clown.bot.messaging.commands;

import com.clown.bot.regex.Word;
import com.clown.bot.user.User;

public abstract class Command extends Word {

	public Command(String word) {
		super(word);
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
