package com.github.clownvin.jtwitchbot.commands.defaultcommands;

import com.github.clownvin.jtwitchbot.commands.Command;
import com.github.clownvin.jtwitchbot.user.User;
import com.github.clownvin.jtwitchbot.user.UserType;

public class RegistOff extends Command {

    public RegistOff(String word, String info) {
	super(word, info);
    }

    @Override
    public UserType getUserType() {
	return UserType.MODERATOR;
    }

    @Override
    public void handleCommand(User user, String[] args, String message) {
	user.getBot().getChannelManager().getChannel(user.getChannel()).setRegisteredOnly(false);
	user.getBot().getIrcConnection().sendMessage(user.getChannel(),
		"Register-only chat is now off. Have a nice day. Kappa");
    }

}
