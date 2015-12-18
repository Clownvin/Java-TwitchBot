package com.github.clownvin.jtwitchbot.regex;

/**
 *
 * @author Calvin Non-instantiable type that handles regex.
 */
public final class BotRegex {
    private static final String[] AFFIRMATIVES = new String[] { "Yessir" };

    /**
     * Allows access to the array of string affirmatives.
     *
     * @return array of affirmatives.
     */
    public static String[] getAffirmatives() {
	return AFFIRMATIVES;
    }

    public static float getPercentLikeness(String ori, String match) {
	char[] oriChar = ori.toLowerCase().toCharArray();
	char[] matchChar = match.toLowerCase().toCharArray();
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
	return val / (ori.length() < match.length() ? match.length() : ori.length());
    }
}
