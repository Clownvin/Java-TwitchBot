package com.clown.bot.messaging.commands;

import java.util.ArrayList;

import com.clown.bot.TwitchBot;
import com.clown.bot.channel.Channel;
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

    private static final Command[] DEFAULT_MOD_COMMANDS = new Command[] {
	    new Command("!setsong", "Sets the current song link", "Use this to set the current song link.") {
		@Override
		public void handleCommand(User user, String[] args, String message) {
		    if (args.length == 1) {
			MessageHandler.setVideoLink(args[0]);
			user.sendWhisper(
				BotRegex.getAffirmatives()[(int) (Math.random() * BotRegex.getAffirmatives().length)]);
		    } else {
			user.sendWhisper(
				"You must include a video link after the command, sir. Like: !setsong YT-Link");
		    }
		}
	    }, new Command("!sendmessage", "Sends a message to a specific destination",
		    "Sends a message to a destination (!sendmessage <dest> <message>).") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
		    if (args.length > 1) { // Must have at least two, first will be dest (this, #channel, all), second just means that there is text after.
			if (args[0].startsWith("#")) { //Sending to channel
			    TwitchBot.getIRCConnection().sendMessage(args[0], message.replace(args[0] + " ", ""));
			} else {
			    switch (args[0]) {
			    case "all":
				for (Channel channel : TwitchBot.getIRCConnection().getChannelManager().getChannels()) {
				    TwitchBot.getIRCConnection().sendMessage(channel.getChannel(),
					    message.replace(args[0] + " ", ""));
				}
				break;
			    case "this":
				TwitchBot.getIRCConnection().sendMessage(TwitchBot.DEFAULT_CHANNELS[0],
					message.replace(args[0] + " ", ""));
				break;
			    default:
				TwitchBot.getGroupConnection().sendWhisper(user.getUsername(), args[0]
					+ " is not a valid destination. Valid destinations are: this, all, #channel");
			    }
			}
		    } else {
			TwitchBot.getGroupConnection().sendWhisper(user.getUsername(),
				"You must include a destination and a message (!sendmessage this <message>).");
		    }
		}

	    }, new Command("!regexoff", "Sets regex to off", "Use this to set regex off.") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
		    BotRegex.setRegexOff(true);
		    user.sendWhisper(
			    BotRegex.getAffirmatives()[(int) (Math.random() * BotRegex.getAffirmatives().length)]);
		}
	    }, new Command("!regexon", "Sets regex to on", "Use this to set regex on.") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
		    BotRegex.setRegexOff(false);
		    user.sendWhisper(
			    BotRegex.getAffirmatives()[(int) (Math.random() * BotRegex.getAffirmatives().length)]);
		}

	    }, new Command("!moderateoff", "Sets moderate to off", "Use this to set moderate off.") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
		    MessageHandler.setModerate(false);
		    user.sendWhisper(
			    BotRegex.getAffirmatives()[(int) (Math.random() * BotRegex.getAffirmatives().length)]);
		}
	    }, new Command("!moderateon", "Sets moderate to on", "Use this to set moderate on.") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
		    MessageHandler.setModerate(true);
		    user.sendWhisper(
			    BotRegex.getAffirmatives()[(int) (Math.random() * BotRegex.getAffirmatives().length)]);
		}
	    }, new Command("!registon", "A mode, for spammers.", "Turns A mode on.") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
		    MessageHandler.setRegisteredOnly(true);
		    TwitchBot.getIRCConnection().sendMessage(TwitchBot.DEFAULT_CHANNELS[0],
			    "Registered-only chat is now on. You must !register by whispering the command to me.");
		}
	    }, new Command("!registoff", "Turns off registeredOnly mode", "Turns registeredOnly mode off.") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
		    MessageHandler.setRegisteredOnly(false);
		    TwitchBot.getIRCConnection().sendMessage(TwitchBot.DEFAULT_CHANNELS[0],
			    "Register-only chat is now off. Have a nice day.");
		}
	    } };
    private static final Command[] DEFAULT_STAFF_COMMANDS = new Command[] {};
    private static final Command[] DEFAULT_ADMIN_COMMANDS = new Command[] {};
    private static final Command[] DEFAULT_GLOBAL_MOD_COMMANDS = new Command[] {};
    private static final Command[] DEFAULT_VIEWER_COMMANDS = new Command[] {
	    new Command("!startpoll", "Use this to start a poll in the channel",
		    "Allows you to start a poll (!startpoll <polldesc> : <option1> <option2>..<optionN>).") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
		    if (PollHandler.canStartPoll(user)) {
			if (!PollHandler.pollActive()) {
			    if (message.length() > 3) {
				PollHandler.startPoll(user.getUsername(), message);
			    } else {
				user.sendWhisper(
					"You must include a message about the about poll. What are the voting for?");
			    }
			} else {
			    user.sendWhisper("You cannot start a poll while a poll is already taking place.");
			}
		    } else {
			user.sendWhisper("You cannot start a poll. You need to either be a mod, or have "
				+ PollHandler.getPollToll());
		    }
		}

	    }, new Command("!pollinfo", "Allows you to see the current poll info",
		    "Use this to get the current poll info sent to you.") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
		    if (PollHandler.pollActive()) {
			user.sendWhisper(PollHandler.getPollInfo());
		    } else {
			user.sendWhisper("There is no currently active poll.");
		    }
		}

	    }, new Command("!invite", "Allows you to invite other viewers into a game",
		    "Use this to invite others to games (!invite <game> <user>).") {

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
				    if (TwitchBot.getIRCConnection().getUser(TwitchBot.DEFAULT_CHANNELS[0],
					    args[1]) != null) {
					if (GameManager.getSession(user.getUsername()) != null
						|| RequestHandler.getRequest(args[1]) != null) {
					    user.sendWhisper("The other user is currently busy.");
					    break;
					} else {
					    RequestHandler
						    .addRequest(new Request(user.getUsername(), args[1], new Action() {

						@Override
						public void perform() {
						    GameManager.createSession(user.getUsername(), args[1], args[0]);
						}

					    }));
					    user.sendWhisper("Sending request...");
					    TwitchBot.getGroupConnection().sendWhisper(args[1],
						    user.getUsername() + " would like to play " + args[0]
							    + " with you. Use !accept or !decline.");
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

	    }, new Command("!accept", "Lets you accept an invitation", "Use this to accept any invites you recieve.") {

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

	    }, new Command("!decline", "Lets you decline an invitation", "Use this to decline invitations.") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
		    Request request1 = RequestHandler.getRequest(user.getUsername());
		    if (request1 != null) {
			request1.deny();
		    } else {
			user.sendWhisper("You don't currently have a pending invitation.");
		    }
		}

	    }, new Command("!move", "Allows you to make a move while in a game",
		    "Lets you make a move in a game. Typical usage: !move <num>") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
		    GameSession game = GameManager.getSession(user.getUsername());
		    if (game != null) {
			game.getGame().handleInput(user.getUsername(), message);
		    } else {
			user.sendWhisper("You're not currently in a game.");
		    }
		}

	    }, new Command("!commands", "Lets you see this list", "I don't think you need info on this one lol.") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
		    ArrayList<String> commandLists = new ArrayList<String>();
		    String currentList = "Commands: ";
		    int messageLength = 400;
		    switch (user.getType()) {
		    case MODERATOR:
			for (Command c : DEFAULT_MOD_COMMANDS) {
			    currentList += c.getWord() + " - " + c.getDescription() + ", ";
			    if (currentList.length() > messageLength) {
				commandLists.add(currentList);
				currentList = "Commands cont: ";
			    }
			}
		    case STAFF:
			for (Command c : DEFAULT_STAFF_COMMANDS) {
			    currentList += c.getWord() + " - " + c.getDescription() + ", ";
			    if (currentList.length() > messageLength) {
				commandLists.add(currentList);
				currentList = "Commands cont: ";
			    }
			}
		    case ADMIN:
			for (Command c : DEFAULT_ADMIN_COMMANDS) {
			    currentList += c.getWord() + " - " + c.getDescription() + ", ";
			    if (currentList.length() > messageLength) {
				commandLists.add(currentList);
				currentList = "Commands cont: ";
			    }
			}
		    case GLOBAL_MOD:
			for (Command c : DEFAULT_GLOBAL_MOD_COMMANDS) {
			    currentList += c.getWord() + " - " + c.getDescription() + ", ";
			    if (currentList.length() > messageLength) {
				commandLists.add(currentList);
				currentList = "Commands cont: ";
			    }
			}
		    case VIEWER:
			for (Command c : DEFAULT_VIEWER_COMMANDS) {
			    currentList += c.getWord() + " - " + c.getDescription() + ", ";
			    if (currentList.length() > messageLength) {
				commandLists.add(currentList);
				currentList = "Commands cont: ";
			    }
			}
			for (Command c : temporaryCommands) {
			    currentList += c.getWord() + " - " + c.getDescription() + ", ";
			    if (currentList.length() > messageLength) {
				commandLists.add(currentList);
				currentList = "Commands cont: ";
			    }
			}
			if (currentList.length() > 18) {
			    commandLists.add(currentList);
			}
		    }
		    if (commandLists.size() > 0) {
			for (String commandList : commandLists) {
			    user.sendWhisper(commandList);
			}
		    } else {
			user.sendWhisper(currentList);
		    }
		}

	    }, new Command("!song", "Whispers the link to the song Kappa",
		    "Use this to get a whisper with the link to the current song Kappa.") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
		    user.sendWhisper("Current video: " + MessageHandler.getVideoLink());
		}

	    }, new Command("!karma", "Shows you your current Karma", "Use this to get your current karma count.") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
		    TwitchBot.getIRCConnection().sendMessage(TwitchBot.DEFAULT_CHANNELS[0], "@" + user.getUsername()
			    + " You currently have " + user.getUserData().getKarma() + " karma.");
		}

	    }, new Command("!points", "Shows your current clown points",
		    "Use this to get your current clown point total.") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
		    TwitchBot.getIRCConnection().sendMessage(TwitchBot.DEFAULT_CHANNELS[0], "@" + user.getUsername()
			    + " You currently have " + user.getUserData().getPoints() + " clown points.");
		}

	    }, new Command("!info", "Gives you more info about a command (and it's syntax).",
		    "Lets you view more information (!info <command>).") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
		    if (args.length >= 1) {
			switch (user.getType()) {
			case MODERATOR:
			    for (Command c : DEFAULT_MOD_COMMANDS) {
				if (c.matches(args[0])) {
				    user.sendWhisper(c.getInfo());
				    return;
				}
			    }
			case STAFF:
			    for (Command c : DEFAULT_STAFF_COMMANDS) {
				if (c.matches(args[0])) {
				    user.sendWhisper(c.getInfo());
				    return;
				}
			    }
			case ADMIN:
			    for (Command c : DEFAULT_ADMIN_COMMANDS) {
				if (c.matches(args[0])) {
				    user.sendWhisper(c.getInfo());
				    return;
				}
			    }
			case GLOBAL_MOD:
			    for (Command c : DEFAULT_GLOBAL_MOD_COMMANDS) {
				if (c.matches(args[0])) {
				    user.sendWhisper(c.getInfo());
				    return;
				}
			    }
			case VIEWER:
			    for (Command c : DEFAULT_VIEWER_COMMANDS) {
				if (c.matches(args[0])) {
				    user.sendWhisper(c.getInfo());
				    return;
				}
			    }
			    for (Command c : temporaryCommands) {
				if (c.matches(args[0])) {
				    user.sendWhisper(c.getInfo());
				    return;
				}
			    }
			}
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
		    System.out.println("Matched " + command + " to " + c.getWord());
		    c.handleCommand(user, args, preSplit);
		    return;
		}
	    }
	case STAFF:
	    for (Command c : DEFAULT_STAFF_COMMANDS) {
		if (c.matches(command)) {
		    System.out.println("Matched " + command + " to " + c.getWord());
		    c.handleCommand(user, args, preSplit);
		    return;
		}
	    }
	case ADMIN:
	    for (Command c : DEFAULT_ADMIN_COMMANDS) {
		if (c.matches(command)) {
		    System.out.println("Matched " + command + " to " + c.getWord());
		    c.handleCommand(user, args, preSplit);
		    return;
		}
	    }
	case GLOBAL_MOD:
	    for (Command c : DEFAULT_GLOBAL_MOD_COMMANDS) {
		if (c.matches(command)) {
		    System.out.println("Matched " + command + " to " + c.getWord());
		    c.handleCommand(user, args, preSplit);
		    return;
		}
	    }
	case VIEWER:
	    for (Command c : DEFAULT_VIEWER_COMMANDS) {
		if (c.matches(command)) {
		    System.out.println("Matched " + command + " to " + c.getWord());
		    c.handleCommand(user, args, preSplit);
		    return;
		}
	    }
	    for (Command c : temporaryCommands) {
		if (c.matches(command)) {
		    System.out.println("Matched " + command + " to " + c.getWord());
		    c.handleCommand(user, args, preSplit);
		    return;
		}
	    }
	}
    }
}
