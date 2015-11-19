package com.clown.bot;

import java.util.ArrayList;

import com.clown.io.BasicIO;

public final class Channel {
	private final String channel;
	private final ArrayList<User> userList = new ArrayList<User>();
	
	public Channel(final String channel) {
		this.channel = channel;
	}
	
	public void updateUserList() {
		String jsonPage = null;
		try {
			jsonPage = BasicIO.readUrl(TwitchIRCBot.TWITCH_GROUP_URL + channel.replace("#", "") + "/chatters");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (jsonPage != null) {
			//Parse that JSON.
		}
	}
}
