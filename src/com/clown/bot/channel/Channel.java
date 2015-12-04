package com.clown.bot.channel;

import java.awt.Color;
import java.util.ArrayList;

import com.clown.bot.TwitchBot;
import com.clown.bot.user.User;
import com.clown.bot.user.UserType;
import com.clown.io.BasicIO;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Calvin Channel is a container object for information about channels
 *         and their users.
 */
public final class Channel {
	private final String channel;
	private Color color;
	private final ArrayList<User> viewerList = new ArrayList<User>();

	/**
	 * Constructor for a new Channel object.
	 *
	 * @param channel
	 *            the name of the channel this object represents.
	 */
	public Channel(final String channel) {
		this.channel = channel;
		this.color = new Color((int) (Math.random() * 0xFFFFFF));
	}

	/**
	 * Allows access to the String name of the channel.
	 *
	 * @return the channel name.
	 */
	public String getChannel() {
		return channel;
	}

	/**
	 * Allows access to the randomly generated color of the channel (this has no
	 * meaning except for creating GUIs)
	 *
	 * @return the color of the channel.
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Allows access to the list of users.
	 *
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
			parsedUsers.add(new User(modArray.getString(i), UserType.MODERATOR));
		}
		for (int i = 0; i < staffArray.length(); i++) {
			parsedUsers.add(new User(staffArray.getString(i), UserType.STAFF));
		}
		for (int i = 0; i < adminArray.length(); i++) {
			parsedUsers.add(new User(adminArray.getString(i), UserType.ADMIN));
		}
		for (int i = 0; i < globalModArray.length(); i++) {
			parsedUsers.add(new User(globalModArray.getString(i), UserType.GLOBAL_MOD));
		}
		for (int i = 0; i < viewerArray.length(); i++) {
			parsedUsers.add(new User(viewerArray.getString(i), UserType.VIEWER));
		}
		for (int i = 0; i < parsedUsers.size(); i++) {
			if (!viewerList.contains(parsedUsers.get(i))) {
				parsedUsers.get(i).loadUserData();
				System.out.println("User joined [" + channel + "]: " + parsedUsers.get(i).getUsername()+" of type "+parsedUsers.get(i).getType());
				viewerList.add(parsedUsers.get(i));
			}
		}
		for (int i = 0; i < viewerList.size(); i++) {
			if (!parsedUsers.contains(viewerList.get(i))) {
				viewerList.get(i).save();
				System.out.println("User left [" + channel + "]: " + viewerList.get(i).getUsername());
				viewerList.remove(i);
			}
		}
	}
}
