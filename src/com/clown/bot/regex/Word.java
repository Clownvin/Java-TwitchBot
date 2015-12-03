package com.clown.bot.regex;

import java.util.regex.Pattern;

public class Word {
	protected final String word;
	protected final Pattern regex;

	public Word(final String word) {
		this.word = word;
		this.regex = Pattern.compile(word);
	}

	public Word(final String word, final String regex) {
		this.word = word;
		this.regex = Pattern.compile(regex);
	}

	public boolean matches(String s) {
		return regex.matcher(s).matches()
				|| BotRegex.getPercentLikeness(word, s) > ((s.length() - 1.2f) / (float) s.length());
	}

	public String getWord() {
		return word;
	}

	@Override
	public String toString() {
		return regex.toString();
	}
}
