package com.clown.bot.messaging;

import com.clown.bot.TwitchBot;
import com.clown.bot.messaging.commands.CommandHandler;
import com.clown.bot.regex.BotRegex;
import com.clown.bot.server.ServerConnection;
import com.clown.util.Util;

/**
 * 
 * @author Calvin A non-instantiable type that handles <code>Message</code>
 *         objects.
 */
public final class MessageHandler {
	private static String videoLink = "Not currently set";
	
	public static void setVideoLink(String link) {
		videoLink = link;
	}
	
	public static String getVideoLink() {
		return videoLink;
	}
	
	public static void setModerate(boolean state) {
		moderateOn = state;
	}

	private static boolean moderateOn = false;

	

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
		System.out.println(
				"[" + message.channel.replace("#", "") + "] " + message.user + ": \"" + message.message + "\"");
		TwitchBot.getGroupConnection().getChannelManager().forceRefresh();
		TwitchBot.getIRCConnection().getChannelManager().forceRefresh();
		if (moderateMessage(source, message)) {
			return;
		}
		if (message.message.startsWith("!")) {
			CommandHandler.handleCommand(source, message);
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
			CommandHandler.handleCommand(source, message);
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
