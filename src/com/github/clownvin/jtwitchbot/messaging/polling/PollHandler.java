package com.github.clownvin.jtwitchbot.messaging.polling;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import com.github.clownvin.jtwitchbot.Main;
import com.github.clownvin.jtwitchbot.channels.Channel;
import com.github.clownvin.jtwitchbot.commands.Command;
import com.github.clownvin.jtwitchbot.user.User;
import com.github.clownvin.jtwitchbot.user.UserType;

public final class PollHandler {
    private static final int POLL_TOLL = 36;
    private int pollToll = POLL_TOLL;
    private String pollInfo = "";
    private Channel channel;

    private final Command yayCommand = new Command("!yay", "Use this to cast a \"yay\" vote.") {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3286800095919623674L;

	@Override
	public UserType getUserType() {
	    return UserType.VIEWER;
	}

	@Override
	public void handleCommand(User user, String[] args, String message) {
	    register(user, "Yay");
	}

    };

    private final Command nayCommand = new Command("!nay", "Use this to cast a \"nay\" vote.") {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4008390967290830933L;

	@Override
	public UserType getUserType() {
	    return UserType.VIEWER;
	}

	@Override
	public void handleCommand(User user, String[] args, String message) {
	    register(user, "Yay");
	}

    };
    private final long POLL_DURATION = 120000; // 2 mins.

    private volatile boolean pollActive = false;
    private ArrayList<String> votedUsers = new ArrayList<String>();

    private ArrayList<String> votes = new ArrayList<String>();
    private ArrayList<Command> tempCommands = new ArrayList<Command>();
    private final Thread pollManager = new Thread() {
	@Override
	public void run() {
	    while (!Main.killIssued()) {
		if (pollActive) {
		    try {
			for (int i = 0; i < 4; i++) {
			    Thread.sleep(POLL_DURATION / 4);
			    long msLeft = (POLL_DURATION - ((POLL_DURATION / 4) * (i + 1)));
			    if (msLeft > 0) {
				channel.sendMessage(
					TimeUnit.MILLISECONDS.toSeconds(msLeft) + " seconds left to cast your vote.");
			    }
			}
		    } catch (InterruptedException e) {
			// Don't care too much.
		    }
		    Hashtable<String, Integer> voteCounts = new Hashtable<String, Integer>();
		    for (String vote : votes) {
			if (voteCounts.containsKey(vote)) {
			    voteCounts.put(vote, voteCounts.get(vote) + 1);
			} else {
			    voteCounts.put(vote, 1);
			}
		    }
		    String results = "";
		    for (String key : voteCounts.keySet()) {
			results += key + ": " + voteCounts.get(key) + ", ";
		    }
		    channel.sendMessage(results);
		    for (Command command : tempCommands) {
			channel.removeCommand(command);
		    }
		    votedUsers.clear();
		    votes.clear();
		    tempCommands.clear();
		    pollActive = false;
		}
		synchronized (this) {
		    try {
			this.wait();
		    } catch (InterruptedException e) {
		    }
		}
	    }
	}
    };

    public PollHandler(Channel channel) {
	this.channel = channel;
	pollManager.start();
    }

    private boolean alreadyVoted(String user) {
	return votedUsers.contains(user);
    }

    public boolean canStartPoll(User user) {
	return user.getType() == UserType.MODERATOR || user.getUserData().getPoints() >= pollToll;
    }

    public String getPollInfo() {
	return pollInfo;
    }

    public int getPollToll() {
	return pollToll;
    }

    public boolean pollActive() {
	return pollActive;
    }

    public void register(User user, String vote) {
	if (!alreadyVoted(user.getUsername())) {
	    user.sendWhisper("Vote registered.");
	    votedUsers.add(user.getUsername());
	    votes.add(vote);
	} else {
	    user.sendWhisper("You've already voted!");
	}
    }

    public void startPoll(User user, String message) {
	if (!pollActive() && canStartPoll(user)) {
	    if (user.getType() != UserType.MODERATOR) {
		user.getUserData().addPoints(-pollToll);
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

			    /**
			     * 
			     */
			    private static final long serialVersionUID = 1935557930818465355L;

			    @Override
			    public UserType getUserType() {
				return UserType.VIEWER;
			    }

			    @Override
			    public void handleCommand(User user, String[] args, String message) {
				register(user, command);
			    }

			});
		    }
		}
	    } else {
		tempCommands.add(yayCommand);
		tempCommands.add(nayCommand);
	    }
	    for (Command command : tempCommands) {
		channel.addCommand(command);
	    }
	    channel.sendMessage(user + " has started a poll.");
	    channel.sendMessage("Poll: " + split[0].trim());
	    String howToVote = "Use ";
	    for (int i = 0; i < tempCommands.size(); i++) {
		howToVote += tempCommands.get(i).getWord() + (i == tempCommands.size() - 1 ? " to vote." : ", ");
	    }
	    channel.sendMessage(howToVote);
	    pollInfo = split[0].trim() + ". " + howToVote;
	    pollActive = true;
	    synchronized (this) {
		this.notifyAll();
	    }
	}
    }
}
