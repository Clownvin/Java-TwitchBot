package com.github.clownvin.jtwitchbot.commands;

import com.github.clownvin.jtwitchbot.regex.Word;
import com.github.clownvin.jtwitchbot.user.User;
import com.github.clownvin.jtwitchbot.user.UserType;

public abstract class Command extends Word {
    protected final String info;

    public Command(String word, String info) {
	super(word);
	this.info = info;
    }

    @Override
    public boolean equals(Object object) {
	if (object instanceof Command) {
	    return ((Command) object).word.equals(word);
	} else {
	    return false;
	}
    }

    public String getInfo() {
	return info;
    }

    public abstract UserType getUserType();

    public abstract void handleCommand(User user, String args[], String message);

    public boolean hasAccess(User user) {
	return user.getType().equivalentTo(getUserType());
    }
}
