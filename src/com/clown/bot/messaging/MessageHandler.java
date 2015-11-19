package com.clown.bot.messaging;

import com.clown.bot.TwitchIRCBot;
import com.clown.bot.channel.Channel;
import com.clown.bot.channel.ChannelManager;
import com.clown.bot.regex.BotRegex;
import com.clown.bot.server.ServerConnection;
import com.clown.bot.user.User;
import com.clown.util.Util;

public final class MessageHandler {
	
	public static void handleIRCMessage(ServerConnection source, IRCMessage message) {
		System.out.println(
				"[" + message.channel.replace("#", "") + "] " + message.user + ": \"" + message.message + "\"");
		ChannelManager.forceRefresh();
		if (moderateMessage(source, message)) {
			return;
		}
		if (message.message.startsWith("!")) {
			String command = message.message.replace("!", "");
			switch (command) {
			case "commands":
				source.sendMessage(message.channel, "commands is currently the only command.");
				break;
			}
			return;
		}
		if (message.user.equalsIgnoreCase("vavbro")) {
			if (message.message.startsWith("listchannels")) {
				for (Channel channel : ChannelManager.getChannels()) {
					System.out.println("Channel: " + channel.getChannel());
					for (User user : channel.getViewerList()) {
						System.out.println(user.getUsername() + ",  " + user.getType());
					}
				}
			}
			if (message.message.startsWith("nighthawk die")) {
				source.sendMessage(message.channel, BotRegex.getAffirmatives()[(int) (Math.random() * BotRegex.getAffirmatives().length)]);
				TwitchIRCBot.issueKill();
			}
			if (message.message.startsWith("set nighthawk color ")) {
				source.sendMessage(message.channel, "/color " + (message.message.replace("set nighthawk color ", "")));
				source.sendMessage(message.channel, BotRegex.getAffirmatives()[(int) (Math.random() * BotRegex.getAffirmatives().length)]);
			}
			if (message.message.startsWith("send message this ")) {
				source.sendMessage(message.channel, message.message.replace("send message this ", ""));
			}
			if (message.message.startsWith("send message all ")) {
				for (Channel channel : ChannelManager.getChannels()) {
					source.sendMessage(channel.getChannel(), message.message.replace("send message all ", ""));
				}
			}
			if (message.message.startsWith("send message #")) {
				String channel = message.message.replace("send message ", "").split(" ")[0];
				if (ChannelManager.contains(channel)) {
					source.sendMessage(channel, message.message.replace("send message " + channel + " ", ""));
				} else {
					source.sendMessage(message.channel, "But sir, I'm not currently in that channel.");
				}
			}

			if (message.message.startsWith("join channel ")) {
				String channel = message.message.replace("join channel ", "");
				if (ChannelManager.addChannel(channel)) {
					source.sendCommand("JOIN", channel);
				} else {
					source.sendMessage(message.channel, "But sir, I'm already in that channel.");
				}
			}
			return; // I don't want it to respond to me.
		}
		BotRegex.handleRegex(source, message);
	}
	
	private static boolean moderateMessage(ServerConnection source, IRCMessage message) {
		if (message.message.length() > 5) {
			char[] chars = message.message.toLowerCase().toCharArray();
			int sim = 0;
			for (int i = 0; i < chars.length; i++) {
				for (int j = 0; j < chars.length; j++) {
					if (chars[i] == chars[j]) {
						sim++;
					}
				}
				if ((double) (sim / (double) message.message.length()) > .5) {
					source.sendMessage(message.channel, ".timeout " + message.user + " " + 60);
					return true;
				}
				sim = 0;
			}
			if (Util.isUpperCase(message.message)) {
				source.sendMessage(message.channel, ".timeout " + message.user + " " + 60);
				source.sendMessage(message.channel, "Please turn your caps lock off, " + message.user + ". :3");
				return true;
			}
		}
		return false;
	}
}
