package com.github.clownvin.jtwitchbot.commands.defaultcommands;

import java.util.ArrayList;
import java.util.List;

import com.github.clownvin.jtwitchbot.commands.Command;
import com.github.clownvin.jtwitchbot.user.User;
import com.github.clownvin.jtwitchbot.user.UserType;

public class Commands extends Command {

	public Commands(String word, String info) {
		super(word, info);
	}

	@Override
	public UserType getUserType() {
		return UserType.VIEWER;
	}

	@Override
	public void handleCommand(User user, String[] args, String message) {
		List<String> commandLists = new ArrayList<String>();
		String currentList = "Commands: ";
		int messageLength = 400;
		for (Command c : user.getBot().getChannelManager().getChannel(user.getChannel()).getCommands()) {
			currentList += c.getWord() + ", ";
			if (currentList.length() > messageLength) {
				commandLists.add(currentList);
				currentList = "Commands cont: ";
			}
		}
		if (currentList.length() > 18) {
			commandLists.add(currentList);
		}
		user.sendWhisper("Use !info <command> to get more information.");
		if (commandLists.size() > 0) {
			for (String commandList : commandLists) {
				user.sendWhisper(commandList);
			}
		} else {
			user.sendWhisper(currentList);
		}
	}

}
