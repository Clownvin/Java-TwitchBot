package com.github.clownvin.jtwitchbot.commands.defaultcommands;

import com.github.clownvin.jtwitchbot.commands.Command;
import com.github.clownvin.jtwitchbot.connection.Channel;
import com.github.clownvin.jtwitchbot.user.User;
import com.github.clownvin.jtwitchbot.user.UserType;

public class SendMessage extends Command {

	public SendMessage(String word, String info) {
		super(word, info);
	}
	
	@Override
	public void handleCommand(User user, String[] args, String message) {
		if (args.length > 1) { // Must have at least two, first will
								// be dest (this, #channel, all),
								// second just means that there is
								// text after.
			if (args[0].startsWith("#")) { // Sending to channel
				user.getBot().getIrcConnection().sendMessage(args[0], message.replace(args[0] + " ", ""));
			} else {
				switch (args[0]) {
				case "all":
					for (Channel channel : user.getBot().getChannelManager().getChannels()) {
						user.getBot().getIrcConnection().sendMessage(channel.getChannel(),
								message.replace(args[0] + " ", ""));
					}
					break;
				case "this":
					user.getBot().getIrcConnection().sendMessage(user.getChannel(),
							message.replace(args[0] + " ", ""));
					break;
				default:
					user.sendWhisper(args[0]
							+ " is not a valid destination. Valid destinations are: this, all, #channel");
				}
			}
		} else {
			user.sendWhisper("You must include a destination and a message (!sendmessage this <message>).");
		}
	}

	@Override
	public UserType getUserType() {
		return UserType.MODERATOR;
	}
}
