package com.github.clownvin.jtwitchbot.commands.defaultcommands;

import com.github.clownvin.jtwitchbot.commands.Command;
import com.github.clownvin.jtwitchbot.user.User;
import com.github.clownvin.jtwitchbot.user.UserType;

public class Karma extends Command {

	public Karma(String word, String info) {
		super(word, info);
	}

	@Override
	public UserType getUserType() {
		return UserType.VIEWER;
	}

	@Override
	public void handleCommand(User user, String[] args, String message) {
		user.getBot().getIrcConnection().sendMessage(user.getChannel(), "@" + user.getUsername()
				+ " You currently have " + user.getUserData().getKarma() + " karma.");
	}

}
