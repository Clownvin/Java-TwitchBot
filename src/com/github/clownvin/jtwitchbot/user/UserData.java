package com.github.clownvin.jtwitchbot.user;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public final class UserData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 368427517719929473L;
	private int karma = 0;
	private int points = 0;
	private transient final User user;

	private boolean registered = false;

	private UserData(final User user, int karma, int points, boolean registered) {
		this.user = user;
		this.karma = karma;
		this.points = points;
		this.registered = registered;
	}

	public int getKarma() {
		return karma;
	}

	public int getPoints() {
		return points;
	}

	public void register() {
		registered = true;
		saveData();
	}

	public boolean isRegistered() {
		return registered;
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
		File userFile = new File("./data/users/" + user + ".ser");
		File userDirectory = new File("./data/users/");
		if (!userDirectory.exists()) {
			userDirectory.mkdirs();
		}
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(userFile));
			out.writeObject(this);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			user.getBot().getGroupConnection().sendWhisper(user, "There was an exception while saving your file. :(");
		}
	}

	public static UserData loadUserData(final User user) {
		File userFile = new File("./data/users/" + user + ".ser");
		File userDirectory = new File("./data/users/");
		if (!userDirectory.exists()) {
			userDirectory.mkdirs();
			return new UserData(user, 0, 0, false);
		}
		if (!userFile.exists()) {
			UserData data = new UserData(user, 0, 0, false);
			data.saveData();
			return data;
		}
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(userFile));
			UserData userData = (UserData) in.readObject();
			in.close();
			return new UserData(user, userData.karma, userData.points, userData.registered);
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			user.getBot().getGroupConnection().sendWhisper(user, "There was an exception while loading your file. :(");
			return new UserData(user, 0, 0, false);
		}
	}
}
