package com.clown.bot.user;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import com.clown.bot.TwitchBot;
import com.clown.io.BasicIO;

public final class UserData {
	private int karma = 0;
	private int points = 0;
	private final String user;

	private UserData(String user, int karma, int points) {
		this.user = user;
		this.karma = karma;
		this.points = points;
	}

	public int getKarma() {
		return karma;
	}

	public int getPoints() {
		return points;
	}

	public void addKarma(int karma) {
		this.karma += karma;
		saveData();
	}

	public void addPoints(int points) {
		this.points += points;
		saveData();
	}

	public void saveData() {
		File userFile = new File("./data/users/" + user + ".txt");
		File userDirectory = new File("./data/users/");
		if (!userDirectory.exists()) {
			userDirectory.mkdirs();
		}
		try {
			FileWriter out = new FileWriter(userFile);
			out.write("" + karma + "\n");
			out.write("" + points + "\n");
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			TwitchBot.getGroupConnection().sendWhisper(user, "There was an exception while saving your file. :(");
		}
	}

	public static UserData loadUserData(String user) {
		File userFile = new File("./data/users/" + user + ".txt");
		File userDirectory = new File("./data/users/");
		if (!userDirectory.exists()) {
			userDirectory.mkdirs();
			return new UserData(user, 0, 0);
		} else {
			if (!userFile.exists()) {
				UserData data = new UserData(user, 0, 0);
				data.saveData();
				return data;
			}
			try {
				InputStream in = new FileInputStream(userFile);
				int karma = Integer.parseInt(String.valueOf(BasicIO.readLine(in)).trim());
				int points = Integer.parseInt(String.valueOf(BasicIO.readLine(in)).trim());
				in.close();
				return new UserData(user, karma, points);
			} catch (IOException e) {
				e.printStackTrace();
				TwitchBot.getGroupConnection().sendWhisper(user, "There was an exception while loading your file. :(");
				return new UserData(user, 0, 0);
			}
		}
	}
}
