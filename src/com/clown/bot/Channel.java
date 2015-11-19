package com.clown.bot;

import java.util.ArrayList;

import com.clown.io.BasicIO;

public final class Channel {
	private final String channel;
	private final ArrayList<User> viewerList = new ArrayList<User>();
	
	public Channel(final String channel) {
		this.channel = channel;
	}
	
	public String getChannel() {
		return channel;
	}
	
	public void updateViewerList() {
		String jsonPage = null;
		try {
			jsonPage = BasicIO.readUrl(TwitchIRCBot.TWITCH_GROUP_URL + channel.replace("#", "") + "/chatters");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (jsonPage != null) {
			viewerList.clear();
			String[] lines = jsonPage.replace("\r", "").split("\n"); // \r is there just incase carriage return char exists @ eol
			int state = 0; // MODS: 1, STAFF: 2, ADMINS: 3, GMODS: 4, VIEWERS: 5
			for (int i = 0; i < lines.length; i++) {
				if (lines[i].startsWith("\"moderators\"")) {
					state = 1;
					break;
				}
				if (lines[i].startsWith("\"staff\"")) {
					state = 2;
					break;
				}
				if (lines[i].startsWith("\"admins\"")) {
					state = 3;
					break;
				}
				if (lines[i].startsWith("\"global_mods\"")) {
					state = 4;
					break;
				}
				if (lines[i].startsWith("\"viewers\"")) {
					state = 5;
					break;
				}
				if (lines[i].endsWith("],") || lines[i].endsWith("]")) {
					state = 0;
					break;
				}
				switch (state) {
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
					viewerList.add(new User(lines[i].trim().replace("\"", "").replace(",", ""), UserType.getTypeForState(state)));
					break;
				}
			}
		}
	}
}
