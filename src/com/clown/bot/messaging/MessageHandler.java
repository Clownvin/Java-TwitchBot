package com.clown.bot.messaging;

import java.util.ArrayList;

import com.clown.bot.TwitchBot;
import com.clown.bot.channel.Channel;
import com.clown.bot.games.GameManager;
import com.clown.bot.games.GameSession;
import com.clown.bot.regex.BotRegex;
import com.clown.bot.server.ServerConnection;
import com.clown.bot.user.User;
import com.clown.io.Action;
import com.clown.util.Util;

/**
 * 
 * @author Calvin A non-instantiable type that handles <code>Message</code>
 *         objects.
 */
public final class MessageHandler {
	private static String videoLink = "Not currently set";

	// Unit: Milliseconds
	public static final long REQUEST_TIMEOUT = 60000; // 1 min
	private static final long REQUEST_DELAY = 10000; // 10 seconds

	private static final ArrayList<Request> pendingRequests = new ArrayList<Request>();

	private static boolean moderateOn = false;

	// We're going to assume that all request exist ONLY on the
	// DEFAULT_CHANNELS[0]

	/**
	 * A thread to cull expired requests from the request list.
	 */
	private static final Thread REQUEST_CULLER = new Thread() {
		@Override
		public void run() {
			while (!TwitchBot.killIssued()) {
				try {
					Thread.sleep(1000); // 1 second
				} catch (InterruptedException e) {
				}
				for (int i = 0; i < pendingRequests.size(); i++) {
					if (pendingRequests.get(i).timeoutReached()) {
						TwitchBot.getIRCConnection()
								.getUser(TwitchBot.DEFAULT_CHANNELS[0], pendingRequests.remove(i).getFrom())
								.setRequestDelay(REQUEST_DELAY);
						i--;
					}
				}
			}
		}
	};

	static {
		REQUEST_CULLER.start();
	}

	/**
	 * Interrupts the REQUEST_CULLER to force it to do a cull loop.
	 */
	public static void forceRequestCull() {
		REQUEST_CULLER.interrupt();
	}

	/**
	 * Allows access to requests given the username.
	 * 
	 * @param user
	 *            username to match requests against.
	 * @return the request containing the username, or null if non exist.
	 */
	private static Request getRequest(String user) {
		for (Request request : pendingRequests) {
			if (request.getFrom().equalsIgnoreCase(user) || request.getTo().equalsIgnoreCase(user)) {
				return request;
			}
		}
		return null;
	}

	/**
	 * Handles commands (!command) from a <code>ServerConnection</code>.
	 * 
	 * @param source
	 *            the <code>ServerConnection</code> source.
	 * @param message
	 *            the <code>Message</code> container.
	 */
	public static void handleCommand(ServerConnection source, Message message) {
		String command = message.message.toLowerCase().split(" ")[0].trim();
		String preSplit = message.message.toLowerCase().replace(command + "", "").trim();
		String[] args = preSplit.length() > 0 ? preSplit.split(" ") : new String[0]; // Subsequent
																						// args
		User user = TwitchBot.getIRCConnection().getUser(message.channel, message.user);
		if (user == null) {
			TwitchBot.getIRCConnection().getChannelManager().forceRefresh();
			TwitchBot.getGroupConnection().sendWhisper(message.user, "Something went wrong. Try again.");
			return;
		}
		System.out.println("Command: " + command + ", arglength: " + args.length);
		switch (user.getType()) {
		case MODERATOR:
			switch (command) {
			case "!setsong":
				if (args.length == 1) {
					videoLink = args[0];
					TwitchBot.getGroupConnection().sendWhisper(message.user,
							BotRegex.getAffirmatives()[(int) (Math.random() * BotRegex.getAffirmatives().length)]);
				} else {
					TwitchBot.getGroupConnection().sendWhisper(message.user,
							"You must include a video link after the command, sir. Like: !setsong YT-Link");
				}
				break;
			case "!regexoff":
				BotRegex.setRegexOff(true);
				TwitchBot.getGroupConnection().sendWhisper(message.user,
						BotRegex.getAffirmatives()[(int) (Math.random() * BotRegex.getAffirmatives().length)]);
				break;
			case "!regexon":
				BotRegex.setRegexOff(false);
				TwitchBot.getGroupConnection().sendWhisper(message.user,
						BotRegex.getAffirmatives()[(int) (Math.random() * BotRegex.getAffirmatives().length)]);
				break;
			case "!testboard":
				TwitchBot.getGroupConnection().sendWhisper(message.user, "|X|O|X|");
				TwitchBot.getGroupConnection().sendWhisper(message.user, "|O|_|O|");
				TwitchBot.getGroupConnection().sendWhisper(message.user, "|X|O|X|");
				break;
			case "!moderateoff":
				moderateOn = false;
				TwitchBot.getGroupConnection().sendWhisper(message.user,
						BotRegex.getAffirmatives()[(int) (Math.random() * BotRegex.getAffirmatives().length)]);
				break;
			case "!moderateon":
				moderateOn = true;
				TwitchBot.getGroupConnection().sendWhisper(message.user,
						BotRegex.getAffirmatives()[(int) (Math.random() * BotRegex.getAffirmatives().length)]);
				break;
			}
		case STAFF:
		case ADMIN:
		case GLOBAL_MOD:
		case VIEWER:
			if (TwitchBot.getIRCConnection().getUser(message.channel, message.user).commandDelayPassed()) {
				TwitchBot.getIRCConnection().getUser(message.channel, message.user).setCommandDelay(5000);
				switch (command) {
				case "!invite":
				case "!request":
					if (TwitchBot.getIRCConnection().getUser(message.channel, message.user).requestDelayPassed()) {
						TwitchBot.getIRCConnection().getUser(message.channel, message.user)
								.setRequestDelay(REQUEST_DELAY);
						if (getRequest(message.user) != null) {
							TwitchBot.getGroupConnection().sendWhisper(message.user,
									"You're already involved in a request.");
							break;
						}
						if (args.length > 0) {
							System.out.println(args[0]);
							switch (args[0]) {
							case "reversi":
							case "othello":
							case "tictactoe":
								if (GameManager.getSession(message.user) != null) {
									TwitchBot.getGroupConnection().sendWhisper(message.user,
											"You're already in a game.");
									break;
								}
								if (args.length > 1) {
									if (TwitchBot.getIRCConnection().getUser(TwitchBot.DEFAULT_CHANNELS[0],
											args[1]) != null) {
										if (GameManager.getSession(message.user) != null
												|| getRequest(args[1]) != null) {
											TwitchBot.getGroupConnection().sendWhisper(message.user,
													"The other user is currently busy.");
											break;
										} else {
											pendingRequests.add(new Request(message.user, args[1], new Action() {

												@Override
												public void perform() {
													GameManager.createSession(message.user, args[1], args[0]);
												}

											}));
											TwitchBot.getGroupConnection().sendWhisper(message.user,
													"Sending request...");
											TwitchBot.getGroupConnection().sendWhisper(args[1],
													message.user + " would like to play " + args[0]
															+ " with you. Use !accept or !decline.");
										}

									} else {
										TwitchBot.getGroupConnection().sendWhisper(message.user,
												"That user isn't currently registed in my channel lists.");
									}
								} else {
									TwitchBot.getGroupConnection().sendWhisper(message.user,
											"You need to specify a user to play against.");
								}
								break;
							default:
								TwitchBot.getGroupConnection().sendWhisper(message.user,
										args[0] + " is not a game currently supported.");
								TwitchBot.getGroupConnection().sendWhisper(message.user,
										"Currently avaible games are:");
								TwitchBot.getGroupConnection().sendWhisper(message.user, "Tictactoe");
								TwitchBot.getGroupConnection().sendWhisper(message.user, "Othello (Reversi)");
							}
						} else {
							TwitchBot.getGroupConnection().sendWhisper(message.user,
									"You must specify a request. Example: \"request tictactoe <USER>\"");
						}
						break;
					}
				case "!accept":
					System.out.println("at accept");
					Request request = getRequest(message.user);
					if (request != null) {
						if (request.getTo().equalsIgnoreCase(message.user)) {
							System.out.println("Accepting request.");
							request.accept();
						} else {
							TwitchBot.getGroupConnection().sendWhisper(message.user,
									"You can accept your own invitation! :3");
						}
					} else {
						TwitchBot.getGroupConnection().sendWhisper(message.user,
								"You don't currently have a pending invitation.");
					}
					break;
				case "!decline":
					Request request1 = getRequest(message.user);
					if (request1 != null) {
						request1.deny();
					} else {
						TwitchBot.getGroupConnection().sendWhisper(message.user,
								"You don't currently have a pending invitation.");
					}
					break;
				case "!move":
					String msg = message.message.toLowerCase().replace(command + "", "").trim();
					GameSession game = GameManager.getSession(message.user);
					if (game != null) {
						game.getGame().handleInput(message.user, msg);
					} else {
						TwitchBot.getGroupConnection().sendWhisper(message.user, "You're not currently in a game.");
					}
					break;
				case "!commands":
					TwitchBot.getGroupConnection().sendWhisper(message.user,
							"Current commands: !song - gets link of song. !commands - this. !move # - makes move while in a game. !invite <game> <user> - invites user to a game. !accept - accepts invitation. !decline - declines or cancels invitation.");
					break;
				case "!song":
					TwitchBot.getGroupConnection().sendWhisper(message.user, "Current video: " + videoLink);
					break;
				}
				break;
			}
		}
	}

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
			handleCommand(source, message);
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
			handleCommand(source, message);
			return;
		}
		if (message.user.equalsIgnoreCase("vavbro")) {
			if (message.message.startsWith("regexoff")) {
				source.sendWhisper(message.user,
						BotRegex.getAffirmatives()[(int) (Math.random() * BotRegex.getAffirmatives().length)]);
			}
			if (message.message.startsWith("regexon")) {
				source.sendWhisper(message.user,
						BotRegex.getAffirmatives()[(int) (Math.random() * BotRegex.getAffirmatives().length)]);
			}
			if (message.message.startsWith("nighthawk die")) {
				source.sendWhisper(message.user,
						BotRegex.getAffirmatives()[(int) (Math.random() * BotRegex.getAffirmatives().length)]);
				TwitchBot.issueKill();
			}
			if (message.message.startsWith("set nighthawk color ")) {
				TwitchBot.getIRCConnection().sendMessage(message.channel,
						"/color " + (message.message.replace("set nighthawk color ", "")));
				source.sendWhisper(message.user,
						BotRegex.getAffirmatives()[(int) (Math.random() * BotRegex.getAffirmatives().length)]);
			}
			if (message.message.startsWith("send message this ")) {
				TwitchBot.getIRCConnection().sendMessage(message.channel,
						message.message.replace("send message this ", ""));
			}
			if (message.message.startsWith("send message all ")) {
				for (Channel channel : source.getChannelManager().getChannels()) {
					TwitchBot.getIRCConnection().sendMessage(channel.getChannel(),
							message.message.replace("send message all ", ""));
				}
			}
			if (message.message.startsWith("send message #")) {
				String channel = message.message.replace("send message ", "").split(" ")[0];
				if (source.getChannelManager().contains(channel)) {
					TwitchBot.getIRCConnection().sendMessage(channel,
							message.message.replace("send message " + channel + " ", ""));
				} else {
					source.sendWhisper(message.user, "But sir, I'm not currently in that channel.");
				}
			}
			if (message.message.startsWith("roapcsgo")) {
				TwitchBot.getIRCConnection().sendMessage(message.channel,
						"https://www.youtube.com/watch?v=mI0Unrs2prQ");
			}
			if (message.message.startsWith("join channel ")) {
				String channel = message.message.replace("join channel ", "");
				if (source.getChannelManager().addChannel(channel)) {
					TwitchBot.getIRCConnection().sendCommand("JOIN", channel);
					TwitchBot.getGroupConnection().sendCommand("JOIN", channel);
				} else {
					source.sendWhisper(message.user, "But sir, I'm already in that channel.");
				}
			}
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
						"Please turn your caps lock off, " + message.user + ". :3");
				TwitchBot.getGroupConnection().sendWhisper(message.user, "You have been muted for 60 seconds.");
				// source.sendMessage(message.channel, "Please turn your caps
				// lock off, " + message.user + ". :3");
				return true;
			}
		}
		return false;
	}
}
