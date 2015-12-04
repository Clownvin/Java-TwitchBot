package com.clown.bot.regex;

import com.clown.bot.TwitchBot;
import com.clown.bot.messaging.Message;
import com.clown.bot.server.ServerConnection;
import com.clown.bot.user.User;

/**
 *
 * @author Calvin Non-instantiable type that handles regex.
 */
public final class BotRegex {
	private static boolean regexOff = false;
	private static final Expression[] EXPRESSIONS = new Expression[] {
		//GREETINGS
		new Expression("(((s+u+p+)|(y+o+)|(h+i+)|(h+e+l+o+)|(hola)|(oha[iy*]o))(!|.)?(,*\\s(((el)?night(hawk)?)|(elly)))?)") {
			@Override
			public void perform(User user) {
				TwitchBot.getIRCConnection().sendMessage(TwitchBot.DEFAULT_CHANNELS[0], "Hello, "+user.getUsername());
			}
		},
		//FAREWELLS
		new Expression("((c+y+a+)|(peace(out)?)|((good)?b+y+e+)|(goodnight))(.*)?") {
			@Override
			public void perform(User user) {
				TwitchBot.getIRCConnection().sendMessage(TwitchBot.DEFAULT_CHANNELS[0], "Goodbye, "+user.getUsername());
			}
		}

	 };
	private static final String[] AFFIRMATIVES = new String[] {"Yessir"};
	private static long[] tillCooldown = new long[EXPRESSIONS.length];

	static {
		for (int i = 0; i < tillCooldown.length; i++) {
			tillCooldown[i] = System.currentTimeMillis();
		}
	}

	/**
	 * Allows access to the array of string affirmatives.
	 *
	 * @return array of affirmatives.
	 */
	public static String[] getAffirmatives() {
		return AFFIRMATIVES;
	}

	public static float getPercentLikeness(String ori, String match) {
		char[] oriChar = ori.toCharArray();
		char[] matchChar = match.toCharArray();
		float val = 0.0f;
		int oriOff = ori.indexOf(matchChar[0]);
		if (oriOff == -1) {
			oriOff = 0;
		}
		for (int i = 0; i < match.length(); i++) {
			while (i + oriOff == ori.length()) {
				oriOff--;
			}
			if (matchChar[i] == oriChar[i + oriOff]) {
				val += 1;
			} else {
				for (int j = i - 2; j < i + 3; j++) {
					if (j > -1 && j < match.length()) {
						if (matchChar[j] == oriChar[i + oriOff]) {
							int denom = Math.abs(j - (i + oriOff)) + 1;
							val += Math.abs(1 / denom);
						}
					}
				}
			}
		}
		return (float) (val / (float) (ori.length() < match.length() ? match.length() : ori.length()));
	}

	/**
	 * Handles the message by matching it to regex patterns, and sending
	 * responses.
	 *
	 * @param source
	 *            source of the message.
	 * @param message
	 *            the message to match.
	 */
	public static void handleRegex(ServerConnection source, Message message) {
		if (regexOff) {
			return;
		}
		User user = source.getUser(TwitchBot.DEFAULT_CHANNELS[0], message.user);
		if (user == null) {
			return;
		}
		for (int i = 0; i < EXPRESSIONS.length; i++) {
			if (EXPRESSIONS[i].matches(message.message.toLowerCase())) {
				EXPRESSIONS[i].perform(user);
				return;
			}
		}
	}

	/**
	 * Prevents regex from being used.
	 *
	 * @param state
	 *            the state to set the variable to.
	 */
	public static void setRegexOff(boolean state) {
		regexOff = state;
	}
}
