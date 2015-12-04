package com.clown.bot;

import java.io.IOException;
import java.net.UnknownHostException;

import com.clown.bot.channel.Channel;
import com.clown.bot.server.ServerConnection;

/**
 * 
 * @author Calvin The main class. Despite being the main, it basically just
 *         provides an entry point into the program, and other threads take over
 *         all the heavy lifting.
 */
public final class TwitchBot {
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

	private static ServerConnection ircConnection;
	private static ServerConnection groupConnection;

	private static boolean killIssued = false;

	/**
	 * Returns the <code>ServerConnection</code> for the group IRC.
	 * 
	 * @return the groupConnection object.
	 */
	public static ServerConnection getGroupConnection() {
		return groupConnection;
	}

	/**
	 * Returns the <code>ServerConnection</code> for the chat IRC.
	 * 
	 * @return the ircConnection object.
	 */
	public static ServerConnection getIRCConnection() {
		return ircConnection;
	}

	/**
	 * Sets killIssued to true, which will cause all thread loops to terminate
	 * after their current cycle.
	 */
	public static void issueKill() {
		killIssued = true;
	}

	/**
	 * Allows other objects to see the state of killIssued.
	 * 
	 * @return the value of killIssued.
	 */
	public static boolean killIssued() {
		return killIssued;
	}

	/**
	 * Instantiates the ircConnection and groupConnection objects, and adds a
	 * shutdown hook to send a message to all the channels. Once ircConnection
	 * and groupConnection are instantiated, the automatically call their own
	 * start method, and take over almost all the work of the program.
	 * 
	 * @param args
	 *            main args.
	 * @throws UnknownHostException
	 *             if there was an error connecting to the host.
	 * @throws IOException
	 *             if an IOException was thrown for any reason.
	 */
	public static void main(String[] args) throws UnknownHostException, IOException {
		try {
			ircConnection = new ServerConnection(TWITCH_IRC_IP, TWITCH_IRC_PORT);
			groupConnection = new ServerConnection(TWITCH_GROUP_IP, TWITCH_GROUP_PORT);
			for (int i = 0; i < DEFAULT_CHANNELS.length; i++) {
				ircConnection.joinChannel(DEFAULT_CHANNELS[i]);
				groupConnection.joinChannel(DEFAULT_CHANNELS[i]);
				ircConnection.sendMessage(DEFAULT_CHANNELS[i], "Hi!");
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
					ircConnection.sendMessage(channel.getChannel(), "I'll be back... Kappa");
				}
			}

		}));
	}
}
