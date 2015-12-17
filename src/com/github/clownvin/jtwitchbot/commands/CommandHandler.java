package com.github.clownvin.jtwitchbot.commands;

import com.github.clownvin.jtwitchbot.channels.Channel;
import com.github.clownvin.jtwitchbot.messaging.Message;
import com.github.clownvin.jtwitchbot.modules.ModuleManager;
import com.github.clownvin.jtwitchbot.user.User;
import com.github.clownvin.jtwitchbot.user.UserType;

public final class CommandHandler {
    private static final long COMMAND_DELAY = 3000; // 3 seconds.
    private static final Command REGISTER = new Command("!register",
	    "Registers you. You MUST whisper this command to me. Usage: /w <my_username> !register") {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5909827219489628201L;

	@Override
	public UserType getUserType() {
	    return UserType.VIEWER;
	}

	@Override
	public void handleCommand(User user, String[] args, String message) {
	    if (user.getUserData().isRegistered()) {
		user.sendWhisper("You're already registered.");
	    } else {
		user.getUserData().register();
		user.sendWhisper("Thanks for registering!");
	    }
	}

    };

    private final Channel channel;

    // Command ideas: !info command - get info on how to use a command

    public CommandHandler(final Channel channel) {
	this.channel = channel;
    }

    /**
     * Handles commands (!command) from a <code>ServerConnection</code>.
     * 
     * @param source
     *            the <code>ServerConnection</code> source.
     * @param message
     *            the <code>Message</code> container.
     */
    public void handleCommand(Message message, boolean whisper) {
	String command = message.message.toLowerCase().split(" ")[0].trim();
	String preSplit = message.message.toLowerCase().replace(command + "", "").trim();
	String[] args = preSplit.length() > 0 ? preSplit.split(" ") : new String[0];
	if (message.user == null) {
	    channel.getBot().getChannelManager().forceRefresh();
	    return;
	}
	if (!message.user.commandDelayPassed()) {
	    return;
	}
	message.user.setCommandDelay(COMMAND_DELAY);
	if (whisper && REGISTER.matches(command)) {
	    REGISTER.handleCommand(message.user, args, preSplit);
	    return;
	}
	for (Command c : channel.getCommands()) {
	    if (c.matches(command) && c.hasAccess(message.user)) {
		c.handleCommand(message.user, args, preSplit);
		return;
	    }
	}
	ModuleManager.onCommand(message.user, command, args);
    }
}
