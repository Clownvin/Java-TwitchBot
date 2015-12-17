package com.github.clownvin.jtwitchbot.channels;

import java.util.ArrayList;

import com.github.clownvin.jtwitchbot.bot.TwitchBot;
import com.github.clownvin.jtwitchbot.user.User;

/**
 * 
 * @author Calvin Channel manager object that manages all <code>Channel</code>
 *         containers for a <code>ServerConnection</code>
 */
public class ChannelManager {
    /**
     * This thread refreshes the viewer lists for each channel periodically. (5
     * seconds)
     */
    private final Thread channelRefreshThread = new Thread() {
	@Override
	public void run() {
	    while (!bot.isLoggedOut()) {
		for (Channel channel : currentChannels) {
		    channel.updateViewerList();
		}
		try {
		    Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
	    }
	}
    };

    private final ArrayList<Channel> currentChannels = new ArrayList<Channel>(1);
    private final TwitchBot bot;

    /**
     * Constructor for a new ChannelManager.
     */
    public ChannelManager(final TwitchBot bot) {
	this.bot = bot;
	channelRefreshThread.start();
    }

    /**
     * Adds a new channel container to the <code>ArrayList</code> of channels.
     * 
     * @param channel
     * @return
     */
    public boolean addChannel(String channel) {
	if (contains(channel)) {
	    System.out.println("Channel already exists in list.");
	    return false;
	}
	currentChannels.add(new Channel(channel, bot));
	return true;
    }

    /**
     * Allows other objects to test whether or not this object contains a
     * channel container with the channel name of the String provided.
     * 
     * @param channel
     *            the string to match against.
     * @return true if there is a container using that channel already, or false
     *         if there isn't.
     */
    public boolean contains(String channel) {
	for (Channel c : currentChannels) {
	    if (c.getChannel().equalsIgnoreCase(channel)) {
		return true;
	    }
	}
	return false;
    }

    public User findUser(String username) {
	for (Channel c : currentChannels) {
	    for (User user : c.getViewerList()) {
		if (user.getUsername().equalsIgnoreCase(username)) {
		    return user;
		}
	    }
	}
	return null;
    }

    /**
     * Forces a channel refresh by interrupting the channelRefreshThread.
     */
    public void forceRefresh() {
	channelRefreshThread.interrupt();
    }

    /**
     * Allows other objects to get the container object using the same name as
     * the String provided.
     * 
     * @param channel
     *            name of the channel to match against.
     * @return the channel container object with the same name, or null if there
     *         isn't one.
     */
    public Channel getChannel(String channel) {
	for (Channel c : currentChannels) {
	    if (c.getChannel().equalsIgnoreCase(channel)) {
		return c;
	    }
	}
	return null;
    }

    /**
     * Allows other objects to access the ArrayList of channels.
     * 
     * @return the ArrayList of channels containers.
     */
    public ArrayList<Channel> getChannels() {
	return currentChannels;
    }

    /**
     * Sends a JOIN command for the channel requested. Upon joining, input and
     * output between this channel will be possible.
     *
     * @param channel
     *            the channel to join.
     */
    public void joinChannel(String channel) {
	if (addChannel(channel)) {
	    bot.getIrcConnection().sendCommand("JOIN", channel);
	    bot.getIrcConnection().sendMessage(channel, "Hello!");
	    bot.getGUI().updateChatrooms();
	} else {
	    System.out.println("Channel already exists.");
	}
    }
}
