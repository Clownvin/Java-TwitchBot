package com.github.clownvin.jtwitchbot.messaging;

import com.clown.util.Util;
import com.github.clownvin.jtwitchbot.bot.TwitchBot;
import com.github.clownvin.jtwitchbot.modules.ModuleManager;

/**
 *
 * @author Calvin A non-instantiable type that handles <code>Message</code>
 *         objects.
 */
public final class MessageHandler {
	// Mabye you do something like every new person has to register first like
	// !chatter and only if he has used !chatter he can chat, way better then
	// "a" infront of everything

	private final TwitchBot bot;
	
	public MessageHandler(final TwitchBot bot) {
		this.bot = bot;
	}

	/**
	 * First step in processing a message object from
	 * <code>ServerConnection</code> and the main entry point into this object.
	 *
	 * @param message
	 *            <code>Message</code> container object.
	 */
	public void handleMessage(Message message) {
		bot.getChannelManager().forceRefresh();
		if (moderateMessage(message)) {
			return;
		}
		System.out.println(
				"[" + message.channel.replace("#", "") + "] " + message.user + ": \"" + message.message + "\"");
		bot.addMessage(message);
		if (message.message.startsWith("!")) {
			bot.getChannelManager().getChannel(message.channel).getCommandHandler().handleCommand(message, false);
			return;
		}
		ModuleManager.onMessage(message);
	}

	/**
	 * Handles whispers from a <code>ServerConnection</code>.
	 *
	 * @param message
	 *            <code>Message</code> container object.
	 */
	public void handleWhisper(Message message) {
		System.out.println("[Whisper] " + message.user + ": " + message.message + "");
		if (message.message.startsWith("!")) {
			bot.getChannelManager().getChannel(message.channel).getCommandHandler().handleCommand(message, true);
			return;
		}
		bot.addMessage(new Message(message.user, "Whisper", message.message));
		ModuleManager.onWhisper(message);
	}

	/**
	 * Moderates messages based on the amount of character variation, or if it's
	 * in all caps.
	 *
	 * @param message
	 *            <code>Message</code> container object.
	 */
	private boolean moderateMessage(Message message) {
		if (message.user == null) {
			bot.getChannelManager().forceRefresh();
			return false;
		}
		if (bot.getChannelManager().getChannel(message.channel).isRegisteredOnly() && !message.user.getUserData().isRegistered()) {
			message.user.sendWhisper(
					"This channel is currently in registered only mode. You can whisper me \"!register\" to get registered. Otherwise, you'll be banned.");
			message.user.addWarning();
			bot.getIrcConnection().sendMessage(message.channel, ".timeout " + message.user + " " + 60);
			return true;
		}
		if (message.message.length() > 5 && message.user.getBot().getChannelManager().getChannel(message.user.getChannel()).getModerateOn()) {
			char[] chars = message.message.toLowerCase().toCharArray();
			int sim = 0;
			for (int i = 0; i < chars.length; i++) {
				for (int j = 0; j < chars.length; j++) {
					if (chars[i] == chars[j] && j != i) {
						sim++;
					}
				}
				if (sim / (double) message.message.length() > .5) {
					bot.getIrcConnection().sendMessage(message.channel, "/timeout " + message.user + " " + 60);
					bot.getGroupConnection().sendWhisper(message.user, "You have been muted for 60 seconds.");
					return true;
				}
				sim = 0;
			}
			if (Util.isUpperCase(message.message)) {
				bot.getIrcConnection().sendMessage(message.channel, "/timeout " + message.user + " " + 60);
				bot.getGroupConnection().sendWhisper(message.user,
						"Please turn your caps lock off, " + message.user + ". Kappa");
				bot.getGroupConnection().sendWhisper(message.user, "You have been muted for 60 seconds.");
				return true;
			}
		}
		return false;
	}
}
