package com.github.clownvin.jtwitchbot.commands.defaultcommands;

import com.github.clownvin.jtwitchbot.commands.Command;
import com.github.clownvin.jtwitchbot.regex.BotRegex;
import com.github.clownvin.jtwitchbot.user.User;
import com.github.clownvin.jtwitchbot.user.UserType;

public class ModerateOff extends Command {

    public ModerateOff(String word, String info) {
	super(word, info);
    }

    @Override
    public UserType getUserType() {
	return UserType.MODERATOR;
    }

    @Override
    public void handleCommand(User user, String[] args, String message) {
	user.getBot().getChannelManager().getChannel(user.getChannel()).setModerateOn(false);
	user.sendWhisper(BotRegex.getAffirmatives()[(int) (Math.random() * BotRegex.getAffirmatives().length)]);
    }

}
