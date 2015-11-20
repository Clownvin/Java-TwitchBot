package com.clown.bot;

import java.io.IOException;
import java.net.UnknownHostException;

import com.clown.bot.channel.Channel;
import com.clown.bot.server.ServerConnection;

public final class TwitchIRCBot {
	private static final String TWITCH_IRC_IP = "irc.twitch.tv";
	private static final String TWITCH_GROUP_IP = "192.16.64.180";
	public static final String TWITCH_GROUP_URL = "http://tmi.twitch.tv/group/user/"; // +
																						// "/chatters"
	private static final int TWITCH_GROUP_PORT = 443;
	private static final int TWITCH_IRC_PORT = 6667;

	public static final String DEFAULT_NICKNAME = "ElNighthawk";
	public static final String DEFAULT_INDENTITY = "ElNighthawk";
	public static final String DEFAULT_REALNAME = "ElNighthawk";
	public static final String[] DEFAULT_CHANNELS = { "#vavbro" };
	public static final String DEFAULT_OAUTH = Messages.getString("TwitchIRCBot.5");

	private static final String[] AUTO_MESSAGES = new String[] { "Have any questions? I can try and answer them.",
			"Have a question? Want to know more? Don't be 'fraid to ask.",
			"Want to know more about what he's doing? Just ask.", "Want to recommend a song? Just type it in chat.",
			"You can do !commands to get a list of commands.", "Use !commands to get a list of commands you can use.",
			"Did you know: In this channel, you can play games with other users? !commands for details on how.",
			"You can play games with the other users! !commands to learn how." };
	private static final long AUTO_MESSAGE_DELAY = 300000;

	private static ServerConnection ircConnection;
	private static ServerConnection groupConnection;

	private static final Thread AUTO_MESSAGE_THREAD = new Thread() {
		@Override
		public void run() {
			while (!killIssued) {
				try {
					Thread.sleep(AUTO_MESSAGE_DELAY);
				} catch (InterruptedException e) {
				}
				ircConnection.sendMessage(DEFAULT_CHANNELS[0],
						AUTO_MESSAGES[(int) (Math.random() * AUTO_MESSAGES.length)]);
			}
		}
	};

	static {
		AUTO_MESSAGE_THREAD.start();
	}

	private static boolean killIssued = false;

	public static ServerConnection getGroupConnection() {
		return groupConnection;
	}

	public static ServerConnection getIRCConnection() {
		return ircConnection;
	}

	public static void issueKill() {
		killIssued = true;
	}

	public static boolean killIssued() {
		return killIssued;
	}

	public static void main(String[] args) throws UnknownHostException, IOException {
		try {
			ircConnection = new ServerConnection(TWITCH_IRC_IP, TWITCH_IRC_PORT);
			groupConnection = new ServerConnection(TWITCH_GROUP_IP, TWITCH_GROUP_PORT);
			for (int i = 0; i < DEFAULT_CHANNELS.length; i++) {
				ircConnection.joinChannel(DEFAULT_CHANNELS[i]);
				groupConnection.joinChannel(DEFAULT_CHANNELS[i]);
			}
			groupConnection.sendCommand("CAP REQ", ":twitch.tv/commands");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			@Override
			public void run() {
				for (Channel channel : ircConnection.getChannelManager().getChannels()) {
					ircConnection.sendMessage(channel.getChannel(), "Cya later!");
				}
			}

		}));
	}
}
