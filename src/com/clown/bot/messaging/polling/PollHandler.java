package com.clown.bot.messaging.polling;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import com.clown.bot.TwitchBot;
import com.clown.bot.messaging.commands.Command;
import com.clown.bot.messaging.commands.CommandHandler;
import com.clown.bot.user.User;
import com.clown.bot.user.UserType;

public final class PollHandler {
	private static final int POLL_TOLL = 36;
	private static int pollToll = POLL_TOLL;
	private static String pollInfo = "";

	public static String getPollInfo() {
		return pollInfo;
	}

	private static final Command YAY_COMMAND = new Command("!yay", "Use this to cast a \"yay\" vote.") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
			PollHandler.register(user, "Yay");
		}

	};
	private static final Command NAY_COMMAND = new Command("!nay", "Use this to cast a \"nay\" vote.") {

		@Override
		public void handleCommand(User user, String[] args, String message) {
			PollHandler.register(user, "Yay");
		}

	};

	private static final long POLL_DURATION = 120000; // 2 minutes.
	private static volatile boolean pollActive = false;

	private static ArrayList<String> votedUsers = new ArrayList<String>();
	private static ArrayList<String> votes = new ArrayList<String>();
	private static ArrayList<Command> tempCommands = new ArrayList<Command>();

	public static int getPollToll() {
		return pollToll;
	}

	private static boolean alreadyVoted(String user) {
		return votedUsers.contains(user);
	}

	public static void register(User user, String vote) {
		if (!alreadyVoted(user.getUsername())) {
			user.sendWhisper("Vote registered.");
			votedUsers.add(user.getUsername());
			votes.add(vote);
		} else {
			user.sendWhisper("You've already voted!");
		}
	}

	private static final Thread POLL_MANAGER = new Thread() {
		@Override
		public void run() {
			while (!TwitchBot.killIssued()) {
				if (pollActive) {
					try {
						for (int i = 0; i < 4; i++) {
							Thread.sleep(POLL_DURATION / 4);
							long msLeft = (POLL_DURATION - ((POLL_DURATION / 4) * (i + 1)));
							if (msLeft > 0) {
								TwitchBot.getIRCConnection().sendMessage(TwitchBot.DEFAULT_CHANNELS[0],
										TimeUnit.MILLISECONDS.toSeconds(msLeft) + " seconds left to cast your vote.");
							}
						}
					} catch (InterruptedException e) {
						// No care.
					}
					// Poll has ended.
					TwitchBot.getIRCConnection().sendMessage(TwitchBot.DEFAULT_CHANNELS[0],
							"The poll has ended. Results:");
					Hashtable<String, Integer> voteCounts = new Hashtable<String, Integer>();
					for (String vote : votes) {
						if (voteCounts.containsKey(vote)) {
							voteCounts.put(vote, voteCounts.get(vote) + 1);
						} else {
							voteCounts.put(vote, 1);
						}
					}
					String results = "Results: ";
					for (String key : voteCounts.keySet()) {
						results += key + ": " + voteCounts.get(key) + " ";
					}
					TwitchBot.getIRCConnection().sendMessage(TwitchBot.DEFAULT_CHANNELS[0], results);
					for (Command command : tempCommands) {
						CommandHandler.removeTempCommand(command);
					}
					votedUsers.clear();
					votes.clear();
					tempCommands.clear();
					pollActive = false;
				}
				synchronized (this) {
					try {
						POLL_MANAGER.wait();
					} catch (InterruptedException e) {
					}
				}
			}
		}
	};

	static {
		POLL_MANAGER.start();
	}

	private PollHandler() {
		// Prevent instantiation.
	}

	public static boolean pollActive() {
		return pollActive;
	}

	public static boolean canStartPoll(User user) {
		return user.getType() == UserType.MODERATOR || user.getUserData().getPoints() >= pollToll;
	}

	public static void startPoll(String user, String message) {
		User u = TwitchBot.getIRCConnection().getUser(TwitchBot.DEFAULT_CHANNELS[0], user);
		if (!pollActive() && canStartPoll(u)) {
			if (u.getType() != UserType.MODERATOR) {
				u.getUserData().addPoints(-pollToll);
			}
			// !startpoll yada yada yada | o1 o2 o3 o4 o5 o6...
			System.out.println("Message: " + message);
			String[] split = message.split(":");
			System.out.println("Split[0]: " + split[0] + ", len: " + split.length);
			if (split.length > 1) {
				System.out.println("Split length greater.");
				for (String command : split[1].split(" ")) {
					System.out.println("Command: " + command);
					if (command.length() > 0) {
						tempCommands.add(new Command("!" + command, "Use this to cast a \"" + command + "\" vote.") {

							@Override
							public void handleCommand(User user, String[] args, String message) {
								PollHandler.register(user, command);
							}

						});
					}
				}
			} else {
				tempCommands.add(YAY_COMMAND);
				tempCommands.add(NAY_COMMAND);
			}
			for (Command command : tempCommands) {
				CommandHandler.addTempCommand(command);
			}
			TwitchBot.getIRCConnection().sendMessage(TwitchBot.DEFAULT_CHANNELS[0], user + " has started a poll.");
			TwitchBot.getIRCConnection().sendMessage(TwitchBot.DEFAULT_CHANNELS[0], "Poll: " + split[0].trim());
			String howToVote = "Use ";
			for (int i = 0; i < tempCommands.size(); i++) {
				howToVote += tempCommands.get(i).getWord() + (i == tempCommands.size() - 1 ? " to vote." : ", ");
			}
			TwitchBot.getIRCConnection().sendMessage(TwitchBot.DEFAULT_CHANNELS[0], howToVote);
			pollInfo = split[0].trim() + ". " + howToVote;
			pollActive = true;
			synchronized (POLL_MANAGER) {
				POLL_MANAGER.notifyAll();
			}
		}
	}
}
