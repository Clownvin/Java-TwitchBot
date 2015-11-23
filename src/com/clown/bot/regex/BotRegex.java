package com.clown.bot.regex;

import java.util.regex.Pattern;

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
    private static final Pattern CONST_USER_PATTERN = Pattern.compile("(.*)?(((el)?night(hawke?))|(vav(bro)?))(.*)?",
	    Pattern.CASE_INSENSITIVE);
    // Hellos and such
    private static final Pattern GREETINGS_PATTERN = Pattern.compile(
	    "(((s+u+p+)|(y+o+)|(h+i+)|(h+e+l+o+)|(hola)|(oha[iy*]o))(!|.)?(,*\\s(((el)?night(hawk)?)|(elly)))?)",
	    Pattern.CASE_INSENSITIVE);
    private static final String[] GREETINGS = new String[] { "Hi, $USER", "Hello, $USER", "Good day, $USER",
	    "Wazzup, $USER", "Hey, $USER" };

    // Goodbyes and such
    private static final Pattern FAREWELL_PATTERN = Pattern.compile("(.*)?((c+y+a+)|(peace(out)?)|(b+y+e+))(.*)?",
	    Pattern.CASE_INSENSITIVE);
    private static final String[] FAREWELLS = new String[] { "Cya, $USER", "Peace, $USER",
	    "Thanks for watching, $USER" };

    // WAYD - What are you doing
    private static final Pattern WAYD_PATTERN = Pattern.compile(
	    "(.*)?(((wh*at(('*r*e*)|(\\s(a*re*))*)\\s(u|y+ou*)(\\s(currently|presently))?\\s((doing?)|(up(2|to|too))|(w?riting)|(makin(g?))|(working?\\son)))|(((wh?at'?s(\\s)?)|(wh?a[sz]?))up))\\??)(.*)?",
	    Pattern.CASE_INSENSITIVE);
    private static final String[] WAYD_RESPONSES = new String[] { "Vavbro is currently working on $CURPROJECT." };
    private static String currentProject = "me, an IRC bot.";

    // What ide is that?
    private static final Pattern WIIT_PATTERN = Pattern
	    .compile("((wh?at)\\s((ide)|(program)|(editor))\\s(is that)?\\??)", Pattern.CASE_INSENSITIVE);
    private static final String[] WIIT_RESPONSES = new String[] { "Eclipse: Mars" };

    // Got a mic?
    private static final Pattern GAM_PATTERN = Pattern.compile(
	    "((((do\\s)?((you?)|(u))\\s)?((go?t)|(ha?ve?))\\s(an?)\\s)(mic(rophone)?)\\??)", Pattern.CASE_INSENSITIVE);
    private static final String[] GAM_RESPONSES = new String[] { "Effectively, no. :(", "No, he doesn't.", "Nada",
	    "Unfortunately, no." };

    // Yes questions
    private static final Pattern YQ_PATTERN = Pattern.compile(
	    "(((el)*night(hawke?)*((,*\\s(a?re?)\\s((yo)?u))|(is))\\s(a\\s)?((ro)?bot)\\??)|((el)?night(hawke?)?('?s?)?\\s(is\\s)?(a)\\s((ro)*bot)(\\?|\\.)*)|((a?re?)\\s((yo)?u)\\s(a)\\s((ro)?bot),?\\s((el)?night(hawke?)?)\\??))",
	    Pattern.CASE_INSENSITIVE);
    private static final String[] YQ_RESPONSES = new String[] { "Yes", "Yes", "Mhm", "Probably... :3",
	    "Well, first let's consider a spherical bot... naw, yea I am. Kappa" };

    // CS CE Questions (ie are you strudying CS or SE as bs?)
    private static final Pattern CSCE_PATTERN = Pattern.compile(
	    "(a*re*)\\s((yo)?u)\\s(taking|studying|in)\\s((c(om(p(uter)?)?)?\\s?(((s(ci(ence)?)?))|(e(ng(ineering)?)?))))\\??",
	    Pattern.CASE_INSENSITIVE);

    // Affirmative responses.
    private static final String[] AFFIRMATIVES = new String[] { "Aye aye, capn.", "Yessir.", "Okay",
	    "Sure thing, boss." };

    // Pattern/response arrays.
    private static final Pattern[] PATTERNS = { GREETINGS_PATTERN, FAREWELL_PATTERN, WAYD_PATTERN, WIIT_PATTERN,
	    GAM_PATTERN, YQ_PATTERN, CSCE_PATTERN };
    private static final String[][] RESPONSES = { GREETINGS, FAREWELLS, WAYD_RESPONSES, WIIT_RESPONSES, GAM_RESPONSES,
	    YQ_RESPONSES, YQ_RESPONSES };

    private static final long[] RESPONSE_COOLDOWNS = { 0, 10000, 0, 0, 0, 0, 0 };
    private static final boolean[] MY_CHANNEL_ONLY = { false, false, true, true, true, false, false };
    private static long[] tillCooldown = new long[RESPONSE_COOLDOWNS.length];

    static {
	if ((PATTERNS.length != RESPONSES.length) || (RESPONSES.length != RESPONSE_COOLDOWNS.length)) {
	    System.err.println("Patterns/Responses/Cooldowns don't all have the same length!");
	    System.exit(3);
	}
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

    // TODO Nighthawk do you love me
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
	if (!regexOff) {
	    for (int i = 0; i < PATTERNS.length; i++) {
		if (MY_CHANNEL_ONLY[i] && !message.channel.equalsIgnoreCase(TwitchBot.DEFAULT_CHANNELS[0])) {
		    System.out.println("breaking.");
		    break;
		}
		if (PATTERNS[i].matcher(message.message).matches() && tillCooldown[i] < System.currentTimeMillis()) {
		    String userString = message.user;
		    if (i == 0 || i == 1) {
			outer: for (User user : source.getChannelManager().getChannel(message.channel)
				.getViewerList()) {
			    Word name = new Word(user.getUsername());
			    for (String s : message.message.split(" "))
				if (name.matches(s) && !CONST_USER_PATTERN.matcher(message.message).matches()) {
				    userString = user.getUsername();
				    break outer;
				}
			}
		    }
		    source.sendMessage(message.channel, RESPONSES[i][(int) (Math.random() * RESPONSES[i].length)]
			    .replace("$USER", userString).replace("$CURPROJECT", currentProject));
		    tillCooldown[i] = System.currentTimeMillis() + RESPONSE_COOLDOWNS[i];
		}
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
