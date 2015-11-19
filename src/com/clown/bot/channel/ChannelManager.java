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
					Thread.sleep(60000);
				} catch (InterruptedException e) {
				}
			}
		}
	};
	public ChannelManager() {
		channelRefreshThread.start();
	}
	
	private final ArrayList<Channel> currentChannels = new ArrayList<Channel>(1);
	
	public void forceRefresh() {
		channelRefreshThread.interrupt();
	}
	
	public ArrayList<Channel> getChannels() {
		return currentChannels;
	}
	
	public boolean contains(String channel) {
		for (Channel c : currentChannels) {
			if (c.getChannel().equalsIgnoreCase(channel)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean addChannel(String channel) {
		if (contains(channel)) {
			System.out.println("Channel already exists in list.");
			return false;
		}
		currentChannels.add(new Channel(channel));
		return true;
	}
}
