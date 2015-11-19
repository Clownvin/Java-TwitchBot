package com.clown.bot.regex;

import java.util.regex.Pattern;

import com.clown.bot.messaging.IRCMessage;
import com.clown.bot.server.ServerConnection;

public final class BotRegex {
	// Hellos and such
	private static final Pattern GREETINGS_PATTERN = Pattern.compile(
			"((s+u+p+)|(y+o+)|(h+i+)|(h+e+l+o+)|(h+e+y+)|(hola)|(oha[iy*]o))(!|.)?(,*\\s((el)?night(hawk)?))?(!|.)?");
	private static final String[] GREETINGS = new String[] { "Hi, $USER", "Hello, $USER", "Good day, $USER",
			"Wazzup, $USER", "Hey, $USER" };

	// Goodbyes and such
	private static final Pattern FAREWELL_PATTERN = Pattern.compile("((c+y+a+)|(peace(out)+))!*");
	private static final String[] FAREWELLS = new String[] { "Cya, $USER", "Peace, $USER",
			"Thanks for watching, $USER" };

	// WAYD - What are you doing
	private static final Pattern WAYD_PATTERN = Pattern.compile(
			"((wh*at(('*r*e*)|(\\s(a*re*))*)\\s(u|y+ou*)(\\s(currently|presently)?\\s((doing*)|(up(2|to|too))|(w*riting)|(makin(g?))|(working?\\son))))|(((wh*at'*s(\\s)*)|(wh*a[sz]*))up))(\\?|.)*");
	private static final String[] WAYD_RESPONSES = new String[] { "Vavbro is currently working on $CURPROJECT." };
	private static String currentProject = "me, an IRC bot.";

	// What ide is that?
	private static final Pattern WIIT_PATTERN = Pattern
			.compile("(wh*at)\\s((ide)|(program)|(editor))\\s(is that)*\\?*");
	private static final String[] WIIT_RESPONSES = new String[] { "Eclipse: Mars" };

	// Got a mic?
	private static final Pattern GAM_PATTERN = Pattern
			.compile("(((do\\s)*((you*)|(u))\\s)*((go*t)|(ha*ve*))\\s(an*)\\s)(mic(rophone)*)\\?*");
	private static final String[] GAM_RESPONSES = new String[] {
			"Yes he does, but he doesn't like using it OR it's late and his roomate is asleep.",
			"Yes, he's got a mic, but his roomate is either asleep or it's inconvenient." };

	// Yes questions
	private static final Pattern YQ_PATTERN = Pattern
			.compile("((el)*night(hawk)*((,*\\s(a*re*)\\s((yo)*u))|(is))\\s(a\\s)*((ro)*bot)\\?*)");
	private static final String[] YQ_RESPONSES = new String[] { "Yes", "Yessir", "Affirmative", "Most definitely",
			"Indeed" };

	// CS CE Questions (ie are you strudying CS or SE as bs?)
	private static final Pattern CSCE_PATTERN = Pattern.compile(
			"(a*re*)\\s((yo)?u)\\s(taking|studying|in)\\s((c(om(p(uter)?)?)?\\s?(((s(ci(ence)?)?))|(e(ng(ineering)?)?))))\\??");

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

	public static String[] getAffirmatives() {
		return AFFIRMATIVES;
	}
	
	public static void handleRegex(ServerConnection source, IRCMessage message) {
		for (int i = 0; i < PATTERNS.length; i++) {
			if (MY_CHANNEL_ONLY[i]) {
				break;
			}
			if (PATTERNS[i].matcher(message.message.toLowerCase()).matches()
					&& tillCooldown[i] < System.currentTimeMillis()) {
				//Need to figure out how I'm going to handle sendingMessages now lol.
				source.sendMessage(message.channel, RESPONSES[i][(int) (Math.random() * RESPONSES[i].length)]
						.replace("$USER", message.user).replace("$CURPROJECT", currentProject));
				tillCooldown[i] = System.currentTimeMillis() + RESPONSE_COOLDOWNS[i];
			}
		}
	}
}
