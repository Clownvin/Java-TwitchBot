package com.github.clownvin.jtwitchbot.messaging;

import com.github.clownvin.jtwitchbot.bot.TwitchBot;
import com.github.clownvin.jtwitchbot.user.User;

/**
 * 
 * @author Calvin A basic container object containing the info from a message
 *         string.
 */
public final class Message {
    public final User user;
    public final String fullUser;
    public final String channel;
    public String message;

    /**
     * Constructor for a new Message instance.
     * 
     * @param line
     *            the line to parse for data.
     */
    public Message(final String line, final TwitchBot bot) {
	try {
	    if (line.contains("WHISPER")) {
		String[] s1 = line.split("WHISPER");
		user = bot.getChannelManager().findUser(s1[0].split("!")[0].replace(":", ""));
		fullUser = s1[0].trim().replace(":", "");
		channel = user == null ? null : user.getChannel();
		message = s1[1].replace(s1[1].split(":")[0] + ":", "");
	    } else {
		String[] s1 = line.split("PRIVMSG");
		user = bot.getChannelManager().findUser(s1[0].split("!")[0].replace(":", ""));
		fullUser = s1[0].trim().replace(":", "");
		channel = s1[1].split(":")[0].trim();
		message = s1[1].replace(channel + " :", "").trim();
	    }
	} catch (Exception e) {
	    throw e;
	}
    }

    /**
     * Constructor for a new Message instance.
     * 
     * @param user
     *            username of the sender.
     * @param channel
     *            channel of the sender.
     * @param message
     *            message contents.
     */
    public Message(final User user, final String channel, final String message) {
	this.user = user;
	this.channel = channel;
	this.message = message;
	this.fullUser = user.getUsername();
    }
}
