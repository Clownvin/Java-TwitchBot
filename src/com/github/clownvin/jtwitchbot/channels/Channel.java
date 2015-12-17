package com.github.clownvin.jtwitchbot.channels;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.clown.io.BasicIO;
import com.github.clownvin.jtwitchbot.Main;
import com.github.clownvin.jtwitchbot.bot.TwitchBot;
import com.github.clownvin.jtwitchbot.commands.Command;
import com.github.clownvin.jtwitchbot.commands.CommandHandler;
import com.github.clownvin.jtwitchbot.commands.defaultcommands.*;
import com.github.clownvin.jtwitchbot.messaging.polling.PollHandler;
import com.github.clownvin.jtwitchbot.modules.ModuleManager;
import com.github.clownvin.jtwitchbot.user.User;
import com.github.clownvin.jtwitchbot.user.UserType;

/**
 *
 * @author Calvin Channel is a container object for information about channels
 *         and their users.
 */
public final class Channel {
	private final String channel;
	private final List<User> viewerList = new ArrayList<>();
	private final TwitchBot bot;
	private String lastFollower = "Not currently set";
	private final List<String> autoMessages = new ArrayList<>();
	private final List<Command> commands = new ArrayList<>();
	private int counter = 0;
	private boolean registeredOnly = false;
	private boolean moderateOn = true;
	private PollHandler pollHandler = new PollHandler(this);
	private CommandHandler commandHandler = new CommandHandler(this);
	private final Color color;
	
	private final Thread autoMessageThread = new Thread() {
		@Override
		public void run() {
			while (!bot.isGoingDown()) {
				try {
					Thread.sleep(3600000 / (6 * autoMessages.size())); // 60 minutes in milliseconds divided by 6 * amount of autoMessages. So that each message is displayed 6 times per hour.
				} catch (InterruptedException e) {
				}
				if (registeredOnly) {
					bot.getIrcConnection().sendMessage(channel,
							"Registered-only chat is currently active. You must !register by whispering the command to me.");
				} else {
					bot.getIrcConnection().sendMessage(channel, autoMessages.get(counter++));
					counter %= autoMessages.size();
				}
			}
		}
	};

	public Color getColor() {
		return color;
	}
	
	public List<Command> getCommands() {
		return commands;
	}
	
	public void addCommand(Command command) {
		commands.add(command);
	}
	
	public void removeCommand(Command command) {
		commands.remove(command);
	}
	
	public void setModerateOn(boolean state) {
		moderateOn = state;
	}
	
	public boolean getModerateOn() {
		return moderateOn;
	}
	
	public void addAutoMessage(final String newMessage) {
		autoMessages.add(newMessage);
	}
	
	public void setRegisteredOnly(boolean state) {
		registeredOnly = state;
	}

	public boolean isRegisteredOnly() {
		return registeredOnly;
	}
	
	public void sendMessage(String message) {
		bot.getIrcConnection().sendMessage(channel, message);
	}
	
	private boolean checkForNewFollower() {
		String jsonPage = null;
		try {
			jsonPage = BasicIO.readUrl("https://api.twitch.tv/kraken/channels/"+channel.replace("#", "")+"/follows?direction=DESC&limit=1");
		} catch (Exception e) {
			System.err.println("Exception reading channel userlist.");
		}
		if (jsonPage == null) {
			System.out.println("JSON was null for channel");
			return false;
		}
		String mostRecent = new JSONObject(jsonPage).getJSONArray("follows").getJSONObject(0).getJSONObject("user").getString("name");
		if (lastFollower.equalsIgnoreCase("not currently set")) {
			lastFollower = mostRecent;
			return false;
		} else if (!lastFollower.equalsIgnoreCase(mostRecent)) {
			lastFollower = mostRecent;
			return true;
		}
		return false;
	}

	/**
	 * Constructor for a new Channel object.
	 *
	 * @param channel
	 *            the name of the channel this object represents.
	 */
	public Channel(final String channel, final TwitchBot bot) {
		autoMessages.add("You can do !commands to get a list of commands.");
		autoMessages.add("You can whipser all your commands to me! (/w "+(bot.getAccount().getUsername())+" !commands)");
		//Adding default commands.
		commands.add(new AddAutoMessage("!addautomessage", "Use this command to add a new automessage. Usage: !addautomessage <message>"));
		commands.add(new Commands("!commands", "Use this command to view a list of all the commands in this channel. Usage: !commands"));
		commands.add(new Info("!info", "Use this command to view info about other commands. Usage: !info <command>"));
		commands.add(new Karma("!karma", "Use this command to view your or someone elses karam. Usage: !karma, !karma <user>"));
		commands.add(new ModerateOff("!moderateoff", "Use this command to turn moderation off. Usage: !moderateoff"));
		commands.add(new ModerateOn("!moderateon", "Use this command to turn moderation on. Usage: !moderateon"));
		commands.add(new Points("!points", "Use this command to view your or someone elses points. Usage: !points, !points <user>"));
		commands.add(new RegistOff("!registoff", "Use this command to turn registered only mode off. Usage: !registoff"));
		commands.add(new RegistOn("!registon", "Use this command to turn registered only mode on. Usage: !registon"));
		commands.add(new SendMessage("!sendmessage", "Use this command to send messages through the bot. Usage: !sendmessage <dest> <message>"));
		this.channel = channel;
		this.bot = bot;
		this.color = new Color((int) (0xFF * Math.random()), (int) (0xFF * Math.random()), (int) (0xFF * Math.random()));
		autoMessageThread.start();
	}
	
	public TwitchBot getBot() {
		return bot;
	}

	/**
	 * Allows access to the String name of the channel.
	 *
	 * @return the channel name.
	 */
	public String getChannel() {
		return channel;
	}
	
	public PollHandler getPollHandler() {
		return pollHandler;
	}
	
	public CommandHandler getCommandHandler() {
		return commandHandler;
	}

	/**
	 * Allows access to the list of users.
	 *
	 * @return the viewerList object.
	 */
	public List<User> getViewerList() {
		return viewerList;
	}
	
	public User getUser(final String username) {
		for (User user : viewerList) {
			if (user.getUsername().equals(username)) {
				return user;
			}
		}
		return null;
	}

	/**
	 * Updates the viewerList by reading the JSON file for the channel.
	 */
	public void updateViewerList() {
		String jsonPage = null;
		try {
			jsonPage = BasicIO.readUrl(Main.TWITCH_GROUP_URL + channel.replace("#", "") + "/chatters");
		} catch (Exception e) {
			System.err.println("Exception reading channel userlist.");
		}
		if (jsonPage == null) {
			System.out.println("JSON was null for channel "+channel);
			return;
		}
		JSONObject chattersObject = new JSONObject(jsonPage).getJSONObject("chatters");
		JSONArray modArray = chattersObject.getJSONArray("moderators");
		JSONArray staffArray = chattersObject.getJSONArray("staff");
		JSONArray adminArray = chattersObject.getJSONArray("admins");
		JSONArray globalModArray = chattersObject.getJSONArray("global_mods");
		JSONArray viewerArray = chattersObject.getJSONArray("viewers");
		ArrayList<User> parsedUsers = new ArrayList<User>();
		
		for (int i = 0; i < modArray.length(); i++) {
			parsedUsers.add(new User(modArray.getString(i), channel, UserType.MODERATOR, bot));
		}
		for (int i = 0; i < staffArray.length(); i++) {
			parsedUsers.add(new User(staffArray.getString(i), channel, UserType.STAFF, bot));
		}
		for (int i = 0; i < adminArray.length(); i++) {
			parsedUsers.add(new User(adminArray.getString(i), channel, UserType.ADMIN, bot));
		}
		for (int i = 0; i < globalModArray.length(); i++) {
			parsedUsers.add(new User(globalModArray.getString(i), channel, UserType.GLOBAL_MOD, bot));
		}
		for (int i = 0; i < viewerArray.length(); i++) {
			parsedUsers.add(new User(viewerArray.getString(i), channel, UserType.VIEWER, bot));
		}
		for (int i = 0; i < parsedUsers.size(); i++) {
			if (!viewerList.contains(parsedUsers.get(i))) {
				parsedUsers.get(i).loadUserData();
				System.out.println("User joined [" + channel + "]: " + parsedUsers.get(i)+" of type "+parsedUsers.get(i).getType());
				viewerList.add(parsedUsers.get(i));
				ModuleManager.onJoin(parsedUsers.get(i));
			} else if (viewerList.get(viewerList.indexOf(parsedUsers.get(i))).getType() != parsedUsers.get(i).getType()) {
				viewerList.remove(viewerList.indexOf(parsedUsers.get(i)));
				viewerList.add(parsedUsers.get(i));
				System.out.println("User "+parsedUsers.get(i)+" in ["+channel+"] changed to type "+parsedUsers.get(i).getType());
			}
		}
		for (int i = 0; i < viewerList.size(); i++) {
			if (!parsedUsers.contains(viewerList.get(i))) {
				viewerList.get(i).save();
				System.out.println("User left [" + channel + "]: " + viewerList.get(i));
				ModuleManager.onLeave(viewerList.remove(i));
			}
		}
		if (checkForNewFollower()) {
			bot.getIrcConnection().sendMessage(channel, "Thanks for following, "+lastFollower+"!");
		}
	}
}
