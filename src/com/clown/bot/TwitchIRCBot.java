package com.clown.bot;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import com.clown.io.BasicIO;

public final class TwitchIRCBot {
	private static final String TWITCH_IRC_IP = "irc.twitch.tv";
	public static final String TWITCH_GROUP_URL = "http://tmi.twitch.tv/group/user/"; // +
																					// "channnel/chatters"
	private static final int TWITCH_IRC_PORT = 6667;

	private static final String DEFAULT_NICKNAME = "ElNighthawk";
	private static final String DEFAULT_INDENTITY = "ElNighthawk";
	private static final String DEFAULT_REALNAME = "ElNighthawk";
	private static final String[] DEFAULT_CHANNELS = { "#vavbro", "#ismailzd", "#epicmice", "#white_oak", "#prefixaut",
			"#silentdeadly96" };
	private static final String DEFAULT_OAUTH = Messages.getString("TwitchIRCBot.5");

	private static final ArrayList<String> currentChannels = new ArrayList<String>(1);
	private static final ArrayList<Color> channelColors = new ArrayList<Color>();

	// Hellos and such
	private static final Pattern GREETINGS_PATTERN = Pattern.compile(
			"((s+u+p+)|(y+o+)|(h+i+)|(h+e+l+o+)|(h+e+y+)|(hola)|(oha[iy*]o))(!|.)?(,*\\s((el)?night(hawk)?))?(!|.)?");
	private static final String[] GREETINGS = new String[] { "Hi, $USER", "Hello, $USER", "Good day, $USER",
			"Wazzup, $USER", "Hey, $USER" };

	// Goodbyes and such
	private static final Pattern FAREWELL_PATTERN = Pattern.compile("((c+y+a+)|(peace(out)+))!*"); // lool...
																									// Can't
																									// do
																									// whitespace
																									// sequence..
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

	// Test stuff
	// private static final Pattern TEST_PATTERN =
	// Pattern.compile("hey\\snoob");
	// private static final String[] TEST_RESPONSES = new String[] {"Yes."};

	// Affirmative responses.
	private static final String[] AFFIRMATIVES = new String[] { "Aye aye, capn.", "Yessir.", "Okay",
			"Sure thing, boss." };

	// Pattern/response arrays.
	private static final Pattern[] PATTERNS = { GREETINGS_PATTERN, FAREWELL_PATTERN, WAYD_PATTERN, WIIT_PATTERN,
			GAM_PATTERN, YQ_PATTERN, CSCE_PATTERN };
	private static final String[][] RESPONSES = { GREETINGS, FAREWELLS, WAYD_RESPONSES, WIIT_RESPONSES, GAM_RESPONSES,
			YQ_RESPONSES, YQ_RESPONSES };
	private static final long[] RESPONSE_COOLDOWNS = { 0, 10000, 0, 0, 0, 0, 0 };// {10000,
																					// 10000,
																					// 120000,
																					// 90000,
																					// 30000,
																					// 10000};
																					// //
																					// In
																					// milliseconds.
	private static final boolean[] MY_CHANNEL_ONLY = { false, false, true, true, true, false, false };
	private static long[] tillCooldown = new long[RESPONSE_COOLDOWNS.length];

	private static final BufferedImage bufferImage = new BufferedImage(
			(int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth()),
			(int) Toolkit.getDefaultToolkit().getScreenSize().getHeight(), BufferedImage.TYPE_INT_RGB);
	private static final Graphics bufferGraphics = bufferImage.getGraphics();
	private static final ArrayList<IRCMessage> messages = new ArrayList<IRCMessage>(400);

	private static final String[] AUTO_MESSAGES = new String[] { "Have any questions? I can try and answer them.",
			"Have a question? Want to know more? Don't be 'fraid to ask.",
			"Want to know more about what he's doing? Just ask.", "Want to recommend a song? Just type it in chat." };
	private static final long AUTO_MESSAGE_DELAY = 300000;

	// private static final ArrayList<String> chatUsers = new
	// ArrayList<String>();
	// private static final ArrayList<Integer> messageCounts = new
	// ArrayList<Integer>();

	// private static final int MESSAGES_BEFORE_MUTE = 5;
	//
	// private static final Thread countRemover = new Thread() {
	// @Override
	// public void run() {
	// while (!killIssued) {
	// try {
	// Thread.sleep(5000); // 5 Seconds
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	// messageCounts.clear();
	// chatUsers.clear();
	// }
	// }
	// };

	// Currently thinking of ways to implement chat spam filters.
	// Block messages if they contain more than x emojis
	// Messages over x characters
	// Messages with relatively low character variance.
	// Others as they come up.

	// GuardsManBob - bot ideas channel

	// TODO ADD !song and variants.

	private static final Thread AUTO_MESSAGE_THREAD = new Thread() {
		@Override
		public void run() {
			while (!killIssued) {
				try {
					Thread.sleep(AUTO_MESSAGE_DELAY);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				sendMessage(DEFAULT_CHANNELS[0], AUTO_MESSAGES[(int) (Math.random() * AUTO_MESSAGES.length)]);
			}
		}
	};

	private static final JFrame botFrame = new JFrame() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4983554815226772948L;

		// TODO add word wrap (oh dear, what a pain that'll be.
		@Override
		public void paint(Graphics g) {
			bufferGraphics.setColor(Color.WHITE);
			bufferGraphics.fillRect(0, 0, bufferImage.getWidth(), bufferImage.getHeight());
			int fontHeight = bufferGraphics.getFontMetrics().getHeight();
			FontMetrics metrics = bufferGraphics.getFontMetrics();
			int stringCount = botFrame.getHeight() / fontHeight;
			int idx = messages.size() - 1;
			IRCMessage message = null;
			for (int i = stringCount; i > -1 && idx > -1; i--, idx--) {
				int y = i * fontHeight;
				message = messages.get(idx);
				bufferGraphics.setColor(Color.BLACK);
				bufferGraphics.drawString("[", 10, y);
				bufferGraphics.setColor(channelColors.get(currentChannels.indexOf(message.channel)));
				bufferGraphics.drawString(message.channel, 10 + metrics.stringWidth("["), y);
				bufferGraphics.setColor(Color.BLACK);
				bufferGraphics.drawString("] " + message.user + ": " + message.message,
						10 + metrics.stringWidth("[" + message.channel), y);
			}
			g.drawImage(bufferImage, 0, 0, null);
		}
	};

	private static Socket twitchIRCSocket;
	private static OutputStream twitchIRCOutput;
	private static InputStream twitchIRCInput;

	private static boolean killIssued = false;

	static {
		AUTO_MESSAGE_THREAD.start();
		botFrame.setSize((int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2),
				(int) Toolkit.getDefaultToolkit().getScreenSize().getHeight());
		botFrame.setVisible(true);
		botFrame.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_ESCAPE:
					for (String channel : currentChannels)
						sendMessage(channel, "Looks like my time is up! :(");
					System.exit(0);
					break;
				}
			}
		});
		if ((PATTERNS.length != RESPONSES.length) || (RESPONSES.length != RESPONSE_COOLDOWNS.length)) {
			System.err.println("Patterns/Responses/Cooldowns don't all have the same length!");
			System.exit(3);
		}
		for (int i = 0; i < tillCooldown.length; i++) {
			tillCooldown[i] = System.currentTimeMillis();
		}
		try {
			twitchIRCSocket = new Socket(TWITCH_IRC_IP, TWITCH_IRC_PORT);
			twitchIRCOutput = twitchIRCSocket.getOutputStream();
			twitchIRCInput = twitchIRCSocket.getInputStream();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(2);
		}
	}

	private static boolean addChannelToList(String channel) {
		if (currentChannels.contains(channel)) {
			System.out.println("Channel already exists in list.");
			return false;
		}
		currentChannels.add(channel);
		channelColors.add(new Color((int) (Math.random() * 0xFFFFFF)));
		return true;
	}

	private static void establishConnection() {
		sendCommand("PASS", DEFAULT_OAUTH);
		sendCommand("NICK", DEFAULT_NICKNAME);
		sendCommand("USER", String.format("%s %s bla :%s", DEFAULT_INDENTITY, TWITCH_IRC_IP, DEFAULT_REALNAME));
		for (String channel : DEFAULT_CHANNELS) {
			sendCommand("JOIN", channel);
			// sendMessage(channel, "Hello!");
			addChannelToList(channel);
		}
	}

	public static void main(String[] args) throws UnknownHostException, IOException {
		establishConnection();
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			@Override
			public void run() {
				for (String channel : currentChannels) {
					sendMessage(channel, "Cya later!");
				}
			}

		}));
		
		ArrayList<String> lines = new ArrayList<String>();
		while (!killIssued) {
			String line = String.valueOf(BasicIO.readLine(twitchIRCInput));
			lines.add(line);
			handleLine(line, DEFAULT_CHANNELS[0]); // Assume it's mine. (if it's
													// message, wont matter)
		}
		BufferedWriter writer = new BufferedWriter(new FileWriter("IRCFeed.txt"));
		for (int i = 0; i < lines.size(); i++) {
			writer.write(lines.get(i));
			writer.newLine();
		}
		writer.close();
		botFrame.setVisible(false);
		botFrame.dispose();
		Runtime.getRuntime().exit(0);
	}

	private static void handleLine(String line, String channel) {
		if (line.contains("PING :")) {
			System.out.println("Sending pong response.");
			sendCommand("PONG", ":" + line.replace("PING :", ""));
			return;
		}
		if (line.contains(" PRIVMSG #")) {
			handleIRCMessage(new IRCMessage(line));
			return;
		}
		System.out.println(line);
	}

	private static boolean moderateMessage(IRCMessage message) {
		if (message.message.length() > 5) {
			char[] chars = message.message.toLowerCase().toCharArray(); // So that upper case and lower case characters count as the same.
			int sim = 0;
			for (int i = 0; i < chars.length; i++) {
				for (int j = 0; j < chars.length; j++) {
					if (chars[i] == chars[j]) {
						sim++;
					}
				}
				if ((double) (sim / (double)message.message.length()) > .5) {
					sendMessage(message.channel, ".timeout " + message.user + " " + 60);
					return true;
				}
				sim = 0;
			}
			if (isUpperCase(message.message)) {
				sendMessage(message.channel, ".timeout " + message.user + " " + 60);
				sendMessage(message.channel, "Please turn your caps lock off, "+message.user+". :3");
				return true;
			}
		}
		return false;
	}
	
	private static boolean isUpperCase(String s) {
        // Boolean value to ensure that an all numeric string does not trigger the shouting functions
        boolean includesLetter = false;
        // Loop through each character in the string individually
        for(int i = 0; i < s.length(); i++) {
            // If there's at least one letter then the string could qualify as being a 'shout'
            if(Character.isLetter(s.charAt(i))) includesLetter = true;
            // Any lower case letters immediately disqualifies the string, return immediately instead of continuing the loop
            if(Character.isLowerCase(s.charAt(i))) return false;
        }
        // If there's at least one letter in the string return true, otherwise disqualify it
        if(includesLetter) return true;
        else return false;
    }

	private static void handleIRCMessage(IRCMessage message) {
		System.out.println(
				"[" + message.channel.replace("#", "") + "] " + message.user + ": \"" + message.message + "\"");
		messages.add(message);
		if (messages.size() >= 400) {
			for (int i = 0; i < 100; i++) {
				messages.remove(i);
			}
		}
		botFrame.repaint();
		if (moderateMessage(message)) {
			return; // It's been viewed as innapropriate, no need to continue
					// wasting processing on a clown.
		}
		if (message.message.startsWith("!")) { // Commands
			String command = message.message.replace("!", "");
			switch (command) {
			case "commands":
				sendMessage(message.channel, "commands is currently the only command.");
				break;
			}
			return;
		}
		if (message.user.equalsIgnoreCase("vavbro")) {
			if (message.message.startsWith("set project")) {
				currentProject = message.message.replace("set project ", "");
				sendMessage(message.channel, AFFIRMATIVES[(int) (Math.random() * AFFIRMATIVES.length)]);
			}
			if (message.message.startsWith("nighthawk die")) {
				sendMessage(message.channel, AFFIRMATIVES[(int) (Math.random() * AFFIRMATIVES.length)]);
				killIssued = true;
			}
			if (message.message.startsWith("set nighthawk color ")) {
				sendMessage(message.channel, "/color " + (message.message.replace("set nighthawk color ", "")));
				sendMessage(message.channel, AFFIRMATIVES[(int) (Math.random() * AFFIRMATIVES.length)]);
			}
			if (message.message.startsWith("send message this ")) {
				sendMessage(message.channel, message.message.replace("send message this ", ""));
			}
			if (message.message.startsWith("send message all ")) {
				for (String channel : currentChannels) {
					sendMessage(channel, message.message.replace("send message all ", ""));
				}
			}
			// Currently broke for some reason.
			if (message.message.startsWith("send message #")) {
				String channel = message.message.replace("send message ", "").split(" ")[0];
				if (currentChannels.contains(channel)) {
					sendMessage(channel, message.message.replace("send message " + channel + " ", ""));
				} else {
					sendMessage(message.channel, "But sir, I'm not currently in that channel.");
				}
			}

			if (message.message.startsWith("join channel ")) {
				String channel = message.message.replace("join channel ", "");
				if (addChannelToList(channel)) {
					sendCommand("JOIN", channel);
				} else {
					sendMessage(message.channel, "But sir, I'm already in that channel.");
				}
			}
			return; // I don't want it to respond to me.
		}
		for (int i = 0; i < PATTERNS.length; i++) {
			if (MY_CHANNEL_ONLY[i]) {
				break;
			}
			// Use cooldown unless they just joined room not too long ago. Say..
			// 2 minutes.
			if (PATTERNS[i].matcher(message.message.toLowerCase()).matches()
					&& tillCooldown[i] < System.currentTimeMillis()) {
				sendMessage(message.channel, RESPONSES[i][(int) (Math.random() * RESPONSES[i].length)]
						.replace("$USER", message.user).replace("$CURPROJECT", currentProject));
				tillCooldown[i] = System.currentTimeMillis() + RESPONSE_COOLDOWNS[i];
			}
		}
	}

	private static boolean sendMessage(String channel, String message) {
		System.out.println("Sending message: " + message + " to channel: " + channel);
		messages.add(new IRCMessage(DEFAULT_NICKNAME, channel, message));
		if (messages.size() >= 400) {
			for (int i = 0; i < 100; i++) {
				messages.remove(i);
			}
		}
		try {
			twitchIRCOutput.write(toBytes("PRIVMSG " + channel + " :" + message + "\r\n"));
			twitchIRCOutput.flush();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private static boolean sendCommand(String command, String message) {
		try {
			twitchIRCOutput.write(toBytes(command + " " + message + "\r\n"));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private static byte[] toBytes(String string) {
		char[] stringChars = string.toCharArray();
		byte[] bytes = new byte[stringChars.length];
		for (int i = 0; i < stringChars.length; i++) {
			bytes[i] = (byte) stringChars[i];
		}
		return bytes;
	}
}
