package com.github.clownvin.jtwitchbot.commands.defaultcommands;

import com.github.clownvin.jtwitchbot.commands.Command;
import com.github.clownvin.jtwitchbot.user.User;
import com.github.clownvin.jtwitchbot.user.UserType;

public class Points extends Command {

    /**
     * 
     */
    private static final long serialVersionUID = 3988325522633529468L;

    public Points(String word, String info) {
	super(word, info);
    }

    @Override
    public UserType getUserType() {
	return UserType.VIEWER;
    }

    @Override
    public void handleCommand(User user, String[] args, String message) {
	user.getBot().getIrcConnection().sendMessage(user.getChannel(),
		"@" + user.getUsername() + " You currently have " + user.getUserData().getPoints() + " clown points.");
    }

}
