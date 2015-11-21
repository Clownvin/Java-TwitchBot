package com.clown.bot.channel;

import java.awt.Color;
import java.util.ArrayList;

import com.clown.bot.TwitchBot;
import com.clown.bot.user.User;
import com.clown.bot.user.UserType;
import com.clown.io.BasicIO;

/**
 * 
 * @author Calvin
 * Channel is a container object for information about channels and their users.
 */
public final class Channel {
	private final String channel;
	private Color color;
	private final ArrayList<User> viewerList = new ArrayList<User>();

	/**
	 * Constructor for a new Channel object.
	 * @param channel the name of the channel this object represents.
	 */
	public Channel(final String channel) {
		this.channel = channel;
		this.color = new Color((int) (Math.random() * 0xFFFFFF));
	}

	/**
	 * Allows access to the String name of the channel.
	 * @return the channel name.
	 */
	public String getChannel() {
		return channel;
	}

	/**
	 * Allows access to the randomly generated color of the channel (this has no meaning except for creating GUIs)
	 * @return the color of the channel.
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Allows access to the list of users.
	 * @return the viewerList object.
	 */
	public ArrayList<User> getViewerList() {
		return viewerList;
	}

	/**
	 * Updates the viewerList by reading the JSON file for the channel.
	 */
	public void updateViewerList() {
		String jsonPage = null;
		try {
			jsonPage = BasicIO.readUrl(TwitchBot.TWITCH_GROUP_URL + channel.replace("#", "") + "/chatters");
		} catch (Exception e) {
			System.err.println("Exception reading channel userlist.");
		}
		if (jsonPage != null) {
			ArrayList<User> parsedUsers = new ArrayList<User>();
			String[] lines = jsonPage.replace("\r", "").split("\n");
			int state = 0; // MODS: 1, STAFF: 2, ADMINS: 3, GMODS: 4, VIEWERS: 5
			for (int i = 0; i < lines.length; i++) {
				lines[i] = lines[i].trim();
				if (lines[i].startsWith("\"moderators\"")) {
					state = 1;
					continue;
				}
				if (lines[i].startsWith("\"staff\"")) {
					state = 2;
					continue;
				}
				if (lines[i].startsWith("\"admins\"")) {
					state = 3;
					continue;
				}
				if (lines[i].startsWith("\"global_mods\"")) {
					state = 4;
					continue;
				}
				if (lines[i].startsWith("\"viewers\"")) {
					state = 5;
					continue;
				}
				if (lines[i].endsWith("],") || lines[i].endsWith("]") || lines[i].contains("}")) {
					state = 0;
					continue;
				}
				switch (state) {
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
					parsedUsers.add(new User(lines[i].trim().replace("\"", "").replace(",", ""),
							UserType.getTypeForState(state)));
					break;
				}
			}
			for (int i = 0; i < parsedUsers.size(); i++) {
				if (!viewerList.contains(parsedUsers.get(i))) {
					parsedUsers.get(i).loadUserData();
					//System.out.println("User joined ["+channel+"]: "+parsedUsers.get(i).getUsername());
					viewerList.add(parsedUsers.get(i));
					continue;
				}
			}
			for (int i = 0; i < viewerList.size(); i++) {
				if (!parsedUsers.contains(viewerList.get(i))) {
					viewerList.get(i).save();
					//System.out.println("User left ["+channel+"]: "+viewerList.get(i).getUsername());
					viewerList.remove(i);
				}
			}
		}
	}
}
