package com.clown.bot.channel;

import java.util.ArrayList;

import com.clown.bot.TwitchIRCBot;

public final class ChannelManager {
	
	private ChannelManager() {
		//To prevent instantiation.
	}
	
	private static final ArrayList<Channel> currentChannels = new ArrayList<Channel>(1);
	
	private static final Thread CHANNEL_REFRESH_THREAD = new Thread() {
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
	
	static {
		CHANNEL_REFRESH_THREAD.start();
	}
	
	public static void forceRefresh() {
		CHANNEL_REFRESH_THREAD.interrupt();
	}
	
	public static ArrayList<Channel> getChannels() {
		return currentChannels;
	}
	
	public static boolean contains(String channel) {
		for (Channel c : currentChannels) {
			if (c.getChannel().equalsIgnoreCase(channel)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean addChannel(String channel) {
		if (contains(channel)) {
			System.out.println("Channel already exists in list.");
			return false;
		}
		currentChannels.add(new Channel(channel));
		return true;
	}
}
