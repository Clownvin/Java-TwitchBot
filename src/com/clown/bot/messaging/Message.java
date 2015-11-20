package com.clown.bot.messaging;

import com.clown.bot.TwitchIRCBot;

public final class Message {
	// MSG structure: ":cniht!cniht@cniht.tmi.twitch.tv PRIVMSG #dansgaming
	// :random line confirmed"
	// Consider changing.
	public final String user;
	public final String fullUser;
	public final String channel;
	public final String message;

	public Message(final String line) {
		try {
			if (line.contains("WHISPER")) {
				String[] s1 = line.split("WHISPER");
				user = s1[0].split("!")[0].replace(":", "");
				fullUser = s1[0].trim().replace(":", "");
				channel = TwitchIRCBot.DEFAULT_CHANNELS[0];
				message = s1[1].replace(s1[1].split(":")[0] + ":", "");
			} else {
				String[] s1 = line.split("PRIVMSG");
				user = s1[0].split("!")[0].replace(":", "");
				fullUser = s1[0].trim().replace(":", "");
				channel = s1[1].split(":")[0].trim();
				message = s1[1].replace(channel + " :", "").trim();
			}
		} catch (Exception e) {
			throw e;
		}
	}

	public Message(final String user, final String channel, final String message) {
		this.user = user;
		this.channel = channel;
		this.message = message;
		this.fullUser = user;
	}
}
