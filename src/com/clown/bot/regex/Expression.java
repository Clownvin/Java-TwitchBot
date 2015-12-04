package com.clown.bot.regex;

import com.clown.bot.user.User;

public abstract class Expression extends Word {
	public Expression(String word) {
		super(word);
	}
	
	public abstract void perform(User user);
}
