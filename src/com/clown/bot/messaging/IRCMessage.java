package com.clown.bot.messaging;

public final class IRCMessage {
	//MSG structure: ":cniht!cniht@cniht.tmi.twitch.tv PRIVMSG #dansgaming :random line confirmed"
	//Consider changing.
	public final String user;
	public final String fullUser;
	public final String channel;
	public final String message;
	
	public IRCMessage(final String line) {
		try {
			String[] s1 = line.split("PRIVMSG");
			user = s1[0].split("!")[0].replace(":", "");
			fullUser = s1[0].trim().replace(":", "");
			channel = s1[1].split(":")[0].trim();
			message = s1[1].split(":")[1].trim();
		} catch (Exception e) {
			throw e;
		}
	}
	
	public IRCMessage(final String user, final String channel, final String message) {
		this.user = user;
		this.channel = channel;
		this.message = message;
		this.fullUser = user;
	}
}