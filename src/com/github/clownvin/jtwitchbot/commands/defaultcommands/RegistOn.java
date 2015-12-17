package com.github.clownvin.jtwitchbot.commands.defaultcommands;

import com.github.clownvin.jtwitchbot.commands.Command;
import com.github.clownvin.jtwitchbot.user.User;
import com.github.clownvin.jtwitchbot.user.UserType;

public class RegistOn extends Command {

    /**
     * 
     */
    private static final long serialVersionUID = 3879820250808646378L;

    public RegistOn(String word, String info) {
	super(word, info);
    }

    @Override
    public UserType getUserType() {
	return UserType.MODERATOR;
    }

    @Override
    public void handleCommand(User user, String[] args, String message) {
	user.getBot().getChannelManager().getChannel(user.getChannel()).setRegisteredOnly(true);
	user.getBot().getIrcConnection().sendMessage(user.getChannel(),
		"Registered-only chat is now on. You must !register by whispering the command to me.");
    }

}
