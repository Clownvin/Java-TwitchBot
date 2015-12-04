package com.clown.bot.messaging;

import java.util.ArrayList;
import com.clown.bot.TwitchBot;
import com.clown.bot.messaging.commands.CommandHandler;
import com.clown.bot.regex.BotRegex;
import com.clown.bot.server.ServerConnection;
import com.clown.bot.user.User;
import com.clown.io.BasicIO;
import com.clown.util.Util;

import org.json.JSONObject;

/**
 *
 * @author Calvin A non-instantiable type that handles <code>Message</code>
 *         objects.
 */
public final class MessageHandler {
	private static String videoLink = "Not currently set";
	private static String lastFollower = "Not currently set";
	// Mabye you do something like every new person has to register first like
	// !chatter and only if he has used !chatter he can chat, way better then
	// "a" infront of everything
	private static boolean registeredOnly = false;

	private static final ArrayList<String> autoMessages = new ArrayList<String>();
	private static int counter = 0;

	/**
	 * Sends a message from the AUTO_MESSAGE list every 5 minutes. Also adds
	 * clown points to currently logged in users.
	 */
	private static final Thread AUTO_MESSAGE_THREAD = new Thread() {
		@Override
		public void run() {
			while (!TwitchBot.killIssued()) {
				try {
					Thread.sleep(3600000 / (6 * autoMessages.size()));
					for (User user : TwitchBot.getIRCConnection().getChannelManager().getChannel(TwitchBot.DEFAULT_CHANNELS[0])
							.getViewerList()) {
						user.getUserData().addPoints(1);
					}
				} catch (InterruptedException e) {
				}
				if (MessageHandler.isRegisteredOnly()) {
					TwitchBot.getIRCConnection().sendMessage(TwitchBot.DEFAULT_CHANNELS[0],
							"Registered-only chat is currently active. You must !register by whispering the command to me.");
				} else {
					TwitchBot.getIRCConnection().sendMessage(TwitchBot.DEFAULT_CHANNELS[0], autoMessages.get(counter++));
					counter %= autoMessages.size();
				}
			}
		}
	};

	private static final Thread NEW_FOLLOWER_CHECKER = new Thread() {
		@Override
		public void run() {
			while (!TwitchBot.killIssued()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					continue; // For performance reasons, if something was interrupting often, could cause lag.
				}
				if (checkForNewFollower()) {
					TwitchBot.getIRCConnection().sendMessage(TwitchBot.DEFAULT_CHANNELS[0], "Thanks for following, "+lastFollower+"!");
				}
			}
		}
	};


	//Messages should be delayed such that all messages are sent 6 times per hour (once every 10 minutes)
	static {
		AUTO_MESSAGE_THREAD.start();
		NEW_FOLLOWER_CHECKER.start();
		autoMessages.add("Want to know more about what he's doing? Just ask.");
		autoMessages.add("You can do !commands to get a list of commands.");
		autoMessages.add("Did you know: In this channel, you can play games with other users? !gameguide for details on how.");
	}

	public static boolean checkForNewFollower() {
		String jsonPage = null;
		try {
			jsonPage = BasicIO.readUrl("https://api.twitch.tv/kraken/channels/vavbro/follows?direction=DESC&limit=1");
		} catch (Exception e) {
			System.err.println("Exception reading channel userlist.");
		}
		if (jsonPage == null) {
			System.out.println("JSON was null for channel");
			return false;
		}
		String mostRecent = new JSONObject(jsonPage).getJSONArray("follows").getJSONObject(0).getJSONObject("user").getString("name");
		if (lastFollower.equalsIgnoreCase("not currently set")) {
			lastFollower = mostRecent;
			return false;
		} else if (!lastFollower.equalsIgnoreCase(mostRecent)) {
			lastFollower = mostRecent;
			return true;
		}
		return false;
	}

	public static void addAutoMessage(String autoMessage) {
		autoMessages.add(autoMessage);
	}

	public static void setVideoLink(String link) {
		videoLink = link;
	}

	public static String getVideoLink() {
		return videoLink;
	}

	public static void setRegisteredOnly(boolean state) {
		registeredOnly = state;
	}

	public static boolean isRegisteredOnly() {
		return registeredOnly;
	}

	public static void setModerate(boolean state) {
		moderateOn = state;
	}

	private static boolean moderateOn = true;

	/**
	 * First step in processing a message object from
	 * <code>ServerConnection</code> and the main entry point into this object.
	 *
	 * @param source
	 *            <code>ServerConnection</code> source.
	 * @param message
	 *            <code>Message</code> container object.
	 */
	public static void handleMessage(ServerConnection source, Message message) {
		TwitchBot.getGroupConnection().getChannelManager().forceRefresh();
		TwitchBot.getIRCConnection().getChannelManager().forceRefresh();
		if (moderateMessage(source, message)) {
			return;
		}
		System.out.println(
				"[" + message.channel.replace("#", "") + "] " + message.user + ": \"" + message.message + "\"");
		if (message.message.startsWith("!")) {
			CommandHandler.handleCommand(source, message, false);
			return;
		}
		BotRegex.handleRegex(source, message);
	}

	/**
	 * Handles whispers from a <code>ServerConnection</code>.
	 *
	 * @param source
	 *            <code>ServerConnection</code> source.
	 * @param message
	 *            <code>Message</code> container object.
	 */
	public static void handleWhisper(ServerConnection source, Message message) {
		System.out.println("[Whisper] " + message.user + ": " + message.message + "");
		if (message.message.startsWith("!")) {
			CommandHandler.handleCommand(source, message, true);
			return;
		}
	}

	// TODO Make separate states for each type of chat moderation.
	/**
	 * Moderates messages based on the amount of character variation, or if it's
	 * in all caps.
	 *
	 * @param source
	 *            <code>ServerConnection</code> source.
	 * @param message
	 *            <code>Message</code> container object.
	 */
	private static boolean moderateMessage(ServerConnection source, Message message) {
		if (registeredOnly) {
			User user = TwitchBot.getIRCConnection().getUser(TwitchBot.DEFAULT_CHANNELS[0], message.user);
			if (user != null && !user.getUserData().isRegistered()) {
				user.sendWhisper(
						"This room is currently in registered only mode. You can whisper me \"!register\" to get registered. Otherwise, you'll be banned.");
				user.addWarning();
				source.sendMessage(message.channel, ".timeout " + message.user + " " + 60);
				return true;
			}
		}
		if (message.message.length() > 5 && moderateOn) {
			char[] chars = message.message.toLowerCase().toCharArray();
			int sim = 0;
			for (int i = 0; i < chars.length; i++) {
				for (int j = 0; j < chars.length; j++) {
					if (chars[i] == chars[j]) {
						sim++;
					}
				}
				if (sim / (double) message.message.length() > .5) {
					source.sendMessage(message.channel, ".timeout " + message.user + " " + 60);
					TwitchBot.getGroupConnection().sendWhisper(message.user, "You have been muted for 60 seconds.");
					return true;
				}
				sim = 0;
			}
			if (Util.isUpperCase(message.message)) {
				source.sendMessage(message.channel, ".timeout " + message.user + " " + 60);
				TwitchBot.getGroupConnection().sendWhisper(message.user,
						"Please turn your caps lock off, " + message.user + ". Kappa");
				TwitchBot.getGroupConnection().sendWhisper(message.user, "You have been muted for 60 seconds.");
				return true;
			}
		}
		return false;
	}
}
