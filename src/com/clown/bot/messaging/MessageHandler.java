package com.clown.bot.messaging;

import java.util.ArrayList;

import com.clown.bot.TwitchIRCBot;
import com.clown.bot.channel.Channel;
import com.clown.bot.games.GameManager;
import com.clown.bot.games.GameSession;
import com.clown.bot.regex.BotRegex;
import com.clown.bot.server.ServerConnection;
import com.clown.bot.user.User;
import com.clown.io.Action;
import com.clown.util.Util;

public final class MessageHandler {
	private static String videoLink = "Not currently set";

	// Unit: Milliseconds
	public static final long REQUEST_TIMEOUT = 60000; // 1 min
	private static final long REQUEST_DELAY = 10000; // 10 seconds

	private static final ArrayList<Request> pendingRequests = new ArrayList<Request>();

	private static boolean moderateOn = false;

	// We're going to assume that all request exist ONLY on the
	// DEFAULT_CHANNELS[0]

	private static final Thread REQUEST_CULLER = new Thread() {
		@Override
		public void run() {
			while (!TwitchIRCBot.killIssued()) {
				try {
					Thread.sleep(1000); // 1 second
				} catch (InterruptedException e) {
				}
				for (int i = 0; i < pendingRequests.size(); i++) {
					if (pendingRequests.get(i).timeoutReached()) {
						TwitchIRCBot.getIRCConnection()
								.getUser(TwitchIRCBot.DEFAULT_CHANNELS[0], pendingRequests.remove(i).getFrom())
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

	public static void forceRequestCull() {
		REQUEST_CULLER.interrupt();
	}

	private static Request getRequest(String user) {
		for (Request request : pendingRequests) {
			if (request.getFrom().equalsIgnoreCase(user) || request.getTo().equalsIgnoreCase(user)) {
				return request;
			}
		}
		return null;
	}

	// Command ideas//
	/******************************
	 * !upvote user : Needs way of making sure members don't abuse. !downvote
	 * user : Needs way of making sure members don't abuse. !getvotes user Maybe
	 * some kind of tictactoe game played through whisper lol.
	 ******************************/
	public static void handleCommand(ServerConnection source, Message message) {
		String command = message.message.toLowerCase().split(" ")[0].trim(); // First
																				// arg
																				// is
																				// always
																				// command
		String preSplit = message.message.toLowerCase().replace(command + "", "").trim();
		String[] args = preSplit.length() > 0 ? preSplit.split(" ") : new String[0]; // Subsequent
																						// args
		User user = TwitchIRCBot.getIRCConnection().getUser(message.channel, message.user);
		if (user == null) {
			TwitchIRCBot.getIRCConnection().getChannelManager().forceRefresh();
			TwitchIRCBot.getGroupConnection().sendWhisper(message.user, "Something went wrong. Try again.");
			return;
		}
		System.out.println("Command: " + command + ", arglength: " + args.length);
		switch (user.getType()) {
		case MODERATOR:
			switch (command) {
			case "!setsong":
				if (args.length == 1) {
					videoLink = args[0];
					TwitchIRCBot.getGroupConnection().sendWhisper(message.user,
							BotRegex.getAffirmatives()[(int) (Math.random() * BotRegex.getAffirmatives().length)]);
				} else {
					TwitchIRCBot.getGroupConnection().sendWhisper(message.user,
							"You must include a video link after the command, sir. Like: !setsong YT-Link");
				}
				break;
			case "!regexoff":
				BotRegex.setRegexOff(true);
				TwitchIRCBot.getGroupConnection().sendWhisper(message.user,
						BotRegex.getAffirmatives()[(int) (Math.random() * BotRegex.getAffirmatives().length)]);
				break;
			case "!regexon":
				BotRegex.setRegexOff(false);
				TwitchIRCBot.getGroupConnection().sendWhisper(message.user,
						BotRegex.getAffirmatives()[(int) (Math.random() * BotRegex.getAffirmatives().length)]);
				break;
			case "!testboard":
				TwitchIRCBot.getGroupConnection().sendWhisper(message.user, "|X|O|X|");
				TwitchIRCBot.getGroupConnection().sendWhisper(message.user, "|O|_|O|");
				TwitchIRCBot.getGroupConnection().sendWhisper(message.user, "|X|O|X|");
				break;
			case "!moderateoff":
				moderateOn = false;
				TwitchIRCBot.getGroupConnection().sendWhisper(message.user,
						BotRegex.getAffirmatives()[(int) (Math.random() * BotRegex.getAffirmatives().length)]);
				break;
			case "!moderateon":
				moderateOn = true;
				TwitchIRCBot.getGroupConnection().sendWhisper(message.user,
						BotRegex.getAffirmatives()[(int) (Math.random() * BotRegex.getAffirmatives().length)]);
				break;
			}
		case STAFF:
		case ADMIN:
		case GLOBAL_MOD:
		case VIEWER:
			switch (command) {
			case "!invite":
			case "!request":
				if (getRequest(message.user) != null) {
					TwitchIRCBot.getGroupConnection().sendWhisper(message.user,
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
							TwitchIRCBot.getGroupConnection().sendWhisper(message.user, "You're already in a game.");
							break;
						}
						if (args.length > 1) {
							if (TwitchIRCBot.getIRCConnection().getUser(TwitchIRCBot.DEFAULT_CHANNELS[0],
									args[1]) != null) {
								if (GameManager.getSession(message.user) != null || getRequest(args[1]) != null) {
									TwitchIRCBot.getGroupConnection().sendWhisper(message.user,
											"The other user is currently busy.");
									break;
								} else {
									pendingRequests.add(new Request(message.user, args[1], new Action() {

										@Override
										public void perform() { // Executes on
																// invitation
																// accept.
											GameManager.createSession(message.user, args[1], args[0]);
										}

									}));
									TwitchIRCBot.getGroupConnection().sendWhisper(message.user, "Sending request...");
									TwitchIRCBot.getGroupConnection().sendWhisper(args[1], message.user
											+ " would like to play " + args[0] + " with you. Use !accept or !decline.");
								}

							} else {
								TwitchIRCBot.getGroupConnection().sendWhisper(message.user,
										"That user isn't currently registed in my channel lists.");
							}
						} else {
							TwitchIRCBot.getGroupConnection().sendWhisper(message.user,
									"You need to specify a user to play against.");
						}
						break;
					default:
						TwitchIRCBot.getGroupConnection().sendWhisper(message.user,
								args[0] + " is not a game currently supported.");
						TwitchIRCBot.getGroupConnection().sendWhisper(message.user, "Currently avaible games are:");
						TwitchIRCBot.getGroupConnection().sendWhisper(message.user, "Tictactoe");
						TwitchIRCBot.getGroupConnection().sendWhisper(message.user, "Othello (Reversi)");
					}
				} else {
					TwitchIRCBot.getGroupConnection().sendWhisper(message.user,
							"You must specify a request. Example: \"request tictactoe <USER>\"");
				}
				break;
			case "!accept":
				System.out.println("at accept");
				Request request = getRequest(message.user);
				if (request != null) {
					if (request.getTo().equalsIgnoreCase(message.user)) {
						System.out.println("Accepting request.");
						request.accept(); // Should auto run the Action included
											// with request.
					} else {
						TwitchIRCBot.getGroupConnection().sendWhisper(message.user,
								"You can accept your own invitation! :3");
					}
				} else {
					TwitchIRCBot.getGroupConnection().sendWhisper(message.user,
							"You don't currently have a pending invitation.");
				}
				break;
			case "!decline":
				Request request1 = getRequest(message.user); // 1 because
																// apparently
																// creating a
																// variable of
																// the same name
																// in a
																// different
																// case with a
																// break
																// statement is
																// no no.
				if (request1 != null) {
					request1.deny();
				} else {
					TwitchIRCBot.getGroupConnection().sendWhisper(message.user,
							"You don't currently have a pending invitation.");
				}
				break;
			case "!move":
				String msg = "";
				for (int i = 0; i < args.length; i++) {
					msg += args[i] + " "; // reassemble args lol
				}
				GameSession game = GameManager.getSession(message.user);
				if (game != null) {
					game.getGame().handleInput(message.user, msg.trim());
				} else {
					TwitchIRCBot.getGroupConnection().sendWhisper(message.user, "You're not currently in a game.");
				}
				break;
			case "!commands":
				TwitchIRCBot.getGroupConnection().sendWhisper(message.user, "Current commands:");
				TwitchIRCBot.getGroupConnection().sendWhisper(message.user, "!song - gets link of song.");
				TwitchIRCBot.getGroupConnection().sendWhisper(message.user, "!commands - this.");
				TwitchIRCBot.getGroupConnection().sendWhisper(message.user, "!move # - makes move while in a game.");
				TwitchIRCBot.getGroupConnection().sendWhisper(message.user,
						"!invite <game> <user> - invites user to a game.");
				TwitchIRCBot.getGroupConnection().sendWhisper(message.user, "!accept - accepts invitation.");
				TwitchIRCBot.getGroupConnection().sendWhisper(message.user,
						"!decline - declines or cancels invitation.");
				break;
			case "!song":
				TwitchIRCBot.getGroupConnection().sendWhisper(message.user, "Current video: " + videoLink);
				break;
			}
			break;
		}
	}

	public static void handleMessage(ServerConnection source, Message message) {
		System.out.println(
				"[" + message.channel.replace("#", "") + "] " + message.user + ": \"" + message.message + "\"");
		TwitchIRCBot.getGroupConnection().getChannelManager().forceRefresh();
		TwitchIRCBot.getIRCConnection().getChannelManager().forceRefresh();
		if (moderateMessage(source, message)) {
			return;
		}
		if (message.message.startsWith("!")) {
			handleCommand(source, message);
			return;
		}
		BotRegex.handleRegex(source, message);
	}

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
				TwitchIRCBot.issueKill();
			}
			if (message.message.startsWith("set nighthawk color ")) {
				TwitchIRCBot.getIRCConnection().sendMessage(message.channel,
						"/color " + (message.message.replace("set nighthawk color ", "")));
				source.sendWhisper(message.user,
						BotRegex.getAffirmatives()[(int) (Math.random() * BotRegex.getAffirmatives().length)]);
			}
			if (message.message.startsWith("send message this ")) {
				TwitchIRCBot.getIRCConnection().sendMessage(message.channel,
						message.message.replace("send message this ", ""));
			}
			if (message.message.startsWith("send message all ")) {
				for (Channel channel : source.getChannelManager().getChannels()) {
					TwitchIRCBot.getIRCConnection().sendMessage(channel.getChannel(),
							message.message.replace("send message all ", ""));
				}
			}
			if (message.message.startsWith("send message #")) {
				String channel = message.message.replace("send message ", "").split(" ")[0];
				if (source.getChannelManager().contains(channel)) {
					TwitchIRCBot.getIRCConnection().sendMessage(channel,
							message.message.replace("send message " + channel + " ", ""));
				} else {
					source.sendWhisper(message.user, "But sir, I'm not currently in that channel.");
				}
			}
			if (message.message.startsWith("roapcsgo")) {
				TwitchIRCBot.getIRCConnection().sendMessage(message.channel,
						"https://www.youtube.com/watch?v=mI0Unrs2prQ");
			}
			if (message.message.startsWith("join channel ")) {
				String channel = message.message.replace("join channel ", "");
				if (source.getChannelManager().addChannel(channel)) {
					TwitchIRCBot.getIRCConnection().sendCommand("JOIN", channel);
					TwitchIRCBot.getGroupConnection().sendCommand("JOIN", channel);
				} else {
					source.sendWhisper(message.user, "But sir, I'm already in that channel.");
				}
			}
			return;
		}
	}

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
					TwitchIRCBot.getGroupConnection().sendWhisper(message.user, "You have been muted for 60 seconds.");
					return true;
				}
				sim = 0;
			}
			if (Util.isUpperCase(message.message)) {
				source.sendMessage(message.channel, ".timeout " + message.user + " " + 60);
				TwitchIRCBot.getGroupConnection().sendWhisper(message.user,
						"Please turn your caps lock off, " + message.user + ". :3");
				TwitchIRCBot.getGroupConnection().sendWhisper(message.user, "You have been muted for 60 seconds.");
				// source.sendMessage(message.channel, "Please turn your caps
				// lock off, " + message.user + ". :3");
				return true;
			}
		}
		return false;
	}
}
