package com.github.clownvin.jtwitchbot.commands.defaultcommands;

import com.github.clownvin.jtwitchbot.commands.Command;
import com.github.clownvin.jtwitchbot.channels.Channel;
import com.github.clownvin.jtwitchbot.user.User;
import com.github.clownvin.jtwitchbot.user.UserType;

public class Info extends Command {

	public Info(String word, String info) {
		super(word, info);
	}

	@Override
	public UserType getUserType() {
		return UserType.VIEWER;
	}

	@Override
	public void handleCommand(User user, String[] args, String message) {
		Channel channel = user.getBot().getChannelManager().getChannel(user.getChannel());
		if (args.length >= 1) {
			for (Command c : channel.getCommands()) {
				if (c.matches(args[0]) && c.hasAccess(user)) {
					user.sendWhisper(c.getInfo());
					return;
				}
			}
			user.sendWhisper("No command for argument "+args[0]+".");
		}
	}

}
