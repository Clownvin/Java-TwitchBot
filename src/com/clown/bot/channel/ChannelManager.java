package com.clown.bot.channel;

import java.util.ArrayList;

import com.clown.bot.TwitchIRCBot;

public class ChannelManager {
	private final Thread channelRefreshThread = new Thread() {
		@Override
		public void run() {
			while (!TwitchIRCBot.killIssued()) {
				for (Channel channel : currentChannels) {
					channel.updateViewerList();
				}
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}
			}
		}
	};

	private final ArrayList<Channel> currentChannels = new ArrayList<Channel>(1);

	public ChannelManager() {
		channelRefreshThread.start();
	}

	public boolean addChannel(String channel) {
		if (contains(channel)) {
			System.out.println("Channel already exists in list.");
			return false;
		}
		currentChannels.add(new Channel(channel));
		return true;
	}

	public boolean contains(String channel) {
		for (Channel c : currentChannels) {
			if (c.getChannel().equalsIgnoreCase(channel)) {
				return true;
			}
		}
		return false;
	}

	public void forceRefresh() {
		channelRefreshThread.interrupt();
	}

	public Channel getChannel(String channel) {
		for (Channel c : currentChannels) {
			if (c.getChannel().equalsIgnoreCase(channel)) {
				return c;
			}
		}
		return null;
	}

	public ArrayList<Channel> getChannels() {
		return currentChannels;
	}
}
