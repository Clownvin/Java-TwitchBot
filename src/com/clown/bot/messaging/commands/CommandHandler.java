package com.clown.bot.messaging.commands;

import java.util.ArrayList;

import com.clown.bot.TwitchBot;
import com.clown.bot.games.GameManager;
import com.clown.bot.games.GameSession;
import com.clown.bot.messaging.Message;
import com.clown.bot.messaging.MessageHandler;
import com.clown.bot.messaging.polling.PollHandler;
import com.clown.bot.messaging.requests.Request;
import com.clown.bot.messaging.requests.RequestHandler;
import com.clown.bot.regex.BotRegex;
import com.clown.bot.server.ServerConnection;
import com.clown.bot.user.User;
import com.clown.io.Action;

public final class CommandHandler {

	private static final Command[] DEFAULT_MOD_COMMANDS = new Command[] { new Command("!setsong") {
		@Override
		public void handleCommand(User user, String[] args, String message) {
			if (args.length == 1) {
				MessageHandler.setVideoLink(args[0]);
				user.sendWhisper(BotRegex.getAffirmatives()[(int) (Math.random() * BotRegex.getAffirmatives().length)]);
			} else {
				user.sendWhisper("You must include a video link after the command, sir. Like: !setsong YT-Link");
			}
		}
	}, new Command("!regexoff") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
			BotRegex.setRegexOff(true);
			user.sendWhisper(BotRegex.getAffirmatives()[(int) (Math.random() * BotRegex.getAffirmatives().length)]);
		}
	}, new Command("!regexon") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
			BotRegex.setRegexOff(false);
			user.sendWhisper(BotRegex.getAffirmatives()[(int) (Math.random() * BotRegex.getAffirmatives().length)]);
		}

	}, new Command("!moderateoff") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
			MessageHandler.setModerate(false);
			user.sendWhisper(BotRegex.getAffirmatives()[(int) (Math.random() * BotRegex.getAffirmatives().length)]);
		}
	}, new Command("!moderateon") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
			MessageHandler.setModerate(true);
			user.sendWhisper(BotRegex.getAffirmatives()[(int) (Math.random() * BotRegex.getAffirmatives().length)]);
		}
	} };
	private static final Command[] DEFAULT_STAFF_COMMANDS = new Command[] {};
	private static final Command[] DEFAULT_ADMIN_COMMANDS = new Command[] {};
	private static final Command[] DEFAULT_GLOBAL_MOD_COMMANDS = new Command[] {};
	private static final Command[] DEFAULT_VIEWER_COMMANDS = new Command[] { new Command("!startpoll") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
			if (PollHandler.canStartPoll(user)) {
				if (!PollHandler.pollActive()) {
					if (message.length() > 3) {
						PollHandler.startPoll(user.getUsername(), message);
					} else {
						user.sendWhisper("You must include a message about the about poll. What are the voting for?");
					}
				} else {
					user.sendWhisper("You cannot start a poll while a poll is already taking place.");
				}
			} else {
				user.sendWhisper("You cannot vote. You need to either be a mod, or have " + PollHandler.getPollToll());
			}
		}

	}, new Command("!pollinfo") {
		
		@Override
		public void handleCommand(User user, String[] args, String message) {
			if (PollHandler.pollActive()) {
				user.sendWhisper(PollHandler.getPollInfo());
			} else {
				user.sendWhisper("There is no currently active poll.");
			}
		}
		
	}, new Command("!invite") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
			if (user.requestDelayPassed()) {
				user.setRequestDelay(RequestHandler.REQUEST_DELAY);
				if (RequestHandler.getRequest(user.getUsername()) != null) {
					user.sendWhisper("You're already involved in a request.");
					return;
				}
				if (args.length > 0) {
					System.out.println(args[0]);
					switch (args[0]) {
					case "reversi":
					case "othello":
					case "tictactoe":
						if (GameManager.getSession(user.getUsername()) != null) {
							user.sendWhisper("You're already in a game.");
							break;
						}
						if (args.length > 1) {
							if (TwitchBot.getIRCConnection().getUser(TwitchBot.DEFAULT_CHANNELS[0], args[1]) != null) {
								if (GameManager.getSession(user.getUsername()) != null
										|| RequestHandler.getRequest(args[1]) != null) {
									user.sendWhisper("The other user is currently busy.");
									break;
								} else {
									RequestHandler.addRequest(new Request(user.getUsername(), args[1], new Action() {

										@Override
										public void perform() {
											GameManager.createSession(user.getUsername(), args[1], args[0]);
										}

									}));
									user.sendWhisper("Sending request...");
									TwitchBot.getGroupConnection().sendWhisper(args[1], user.getUsername()
											+ " would like to play " + args[0] + " with you. Use !accept or !decline.");
								}

							} else {
								user.sendWhisper("That user isn't currently registed in my channel lists.");
							}
						} else {
							user.sendWhisper("You need to specify a user to play against.");
						}
						break;
					default:
						user.sendWhisper(args[0] + " is not a game currently supported.");
						user.sendWhisper("Currently avaible games are: Tictactoe, Othello (reversi)");
					}
				} else {
					user.sendWhisper("You must specify a request. Example: \"invite tictactoe <USER>\"");
				}
			}
		}

	}, new Command("!accept") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
			Request request = RequestHandler.getRequest(user.getUsername());
			if (request != null) {
				if (request.getTo().equalsIgnoreCase(user.getUsername())) {
					request.accept();
				} else {
					user.sendWhisper("You can accept your own invitation! :3");
				}
			} else {
				user.sendWhisper("You don't currently have a pending invitation.");
			}
		}

	}, new Command("!decline") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
			Request request1 = RequestHandler.getRequest(user.getUsername());
			if (request1 != null) {
				request1.deny();
			} else {
				user.sendWhisper("You don't currently have a pending invitation.");
			}
		}

	}, new Command("!move") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
			GameSession game = GameManager.getSession(user.getUsername());
			if (game != null) {
				game.getGame().handleInput(user.getUsername(), message);
			} else {
				user.sendWhisper("You're not currently in a game.");
			}
		}

	}, new Command("!commands") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
			user.sendWhisper(
					"Current commands: !karma - tells you your current amount of karma. !points - tells you your current amount of points. !info <command> - gives you useful info about a command. !startpoll description - starts a poll. !song - gets link of song. !commands - this.");
			user.sendWhisper("Commands cont.: !move # - makes move while in a game. !invite <game> <user> - invites user to a game. !accept - accepts invitation. !decline - declines or cancels invitation.");
		}

	}, new Command("!song") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
			user.sendWhisper("Current video: " + MessageHandler.getVideoLink());
		}

	}, new Command("!karma") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
			TwitchBot.getIRCConnection().sendMessage(TwitchBot.DEFAULT_CHANNELS[0],
					"@" + user.getUsername() + " You currently have " + user.getUserData().getKarma() + " karma.");
		}

	}, new Command("!points") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
			TwitchBot.getIRCConnection().sendMessage(TwitchBot.DEFAULT_CHANNELS[0], "@" + user.getUsername()
					+ " You currently have " + user.getUserData().getPoints() + " clown points.");
		}

	}, new Command("!info") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
			switch (message) {
			case "info":
				user.sendWhisper("You can use this command to get info about other commands by typing !info <command>.");
				break;
			case "points":
			case "karma":
				user.sendWhisper("Using !karma or !points will how how many of either you have.");
				break;
			case "song":
				user.sendWhisper("Using !song will send you a link to the song on YouTube. But, I don't naturally know the link, so the broadcaster has to set it manually.");
				break;
			case "commands":
				user.sendWhisper("Using !commands will give you a list of all available commands, minus the temporary commands.");
				break;
			case "move":
				user.sendWhisper("Using !move # will allow you to enter commands to your current game.");
				break;
			case "accept":
				user.sendWhisper("Accepts an invitation.");
				break;
			case "decline":
				user.sendWhisper("Declines and invitation.");
				break;
			case "invite":
				user.sendWhisper("Using !invite <game> <user> will invite the user to a game.");
				break;
			case "startpoll":
				user.sendWhisper("Using !startpoll <polldesc> will start a poll. You can also add your own option by using !startpoll <desc> : <option1, option2... optionN>");
				user.sendWhisper("It costs 36 clown points to start a poll, though.");
				break;
			}
		}
		
	} };
	
	private static final ArrayList<Command> temporaryCommands = new ArrayList<Command>();

	// Command ideas: !info command - get info on how to use a command

	private CommandHandler() {
		// To prevent instantiation.
	}
	
	public static void addTempCommand(Command command) {
		temporaryCommands.add(command);
	}
	
	public static void removeTempCommand(Command command) {
		temporaryCommands.remove(command);
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
		String[] args = preSplit.length() > 0 ? preSplit.split(" ") : new String[0];
		User user = TwitchBot.getIRCConnection().getUser(message.channel, message.user);
		if (user == null) {
			TwitchBot.getIRCConnection().getChannelManager().forceRefresh();
			TwitchBot.getGroupConnection().sendWhisper(message.user, "Something went wrong. Try again.");
			return;
		}
		System.out.println("Command: " + command + ", arglength: " + args.length);
		switch (user.getType()) {
		case MODERATOR:
			for (Command c : DEFAULT_MOD_COMMANDS) {
				if (c.matches(command)) {
					c.handleCommand(user, args, preSplit);
					return;
				}
			}
		case STAFF:
			for (Command c : DEFAULT_STAFF_COMMANDS) {
				if (c.matches(command)) {
					c.handleCommand(user, args, preSplit);
					return;
				}
			}
		case ADMIN:
			for (Command c : DEFAULT_ADMIN_COMMANDS) {
				if (c.matches(command)) {
					c.handleCommand(user, args, preSplit);
					return;
				}
			}
		case GLOBAL_MOD:
			for (Command c : DEFAULT_GLOBAL_MOD_COMMANDS) {
				if (c.matches(command)) {
					c.handleCommand(user, args, preSplit);
					return;
				}
			}
		case VIEWER:
			for (Command c : DEFAULT_VIEWER_COMMANDS) {
				if (c.matches(command)) {
					c.handleCommand(user, args, preSplit);
					return;
				}
			}
			for (Command c : temporaryCommands) {
				if (c.matches(command)) {
					c.handleCommand(user, args, preSplit);
					return;
				}
			}
		}
	}
}
