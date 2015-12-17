package com.github.clownvin.jtwitchbot.regex;

import com.github.clownvin.jtwitchbot.user.User;

public abstract class Expression extends Word {
    public Expression(String word) {
	super(word);
    }

    public abstract void perform(User user);
}
