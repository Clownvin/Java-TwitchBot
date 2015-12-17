package com.github.clownvin.jtwitchbot.bot;

import java.io.IOException;

import com.github.clownvin.jtwitchbot.Main;
import com.github.clownvin.jtwitchbot.account.TwitchAccount;
import com.github.clownvin.jtwitchbot.channels.Channel;
import com.github.clownvin.jtwitchbot.channels.ChannelManager;
import com.github.clownvin.jtwitchbot.connection.ServerConnection;
import com.github.clownvin.jtwitchbot.gui.BotGUIFrame;
import com.github.clownvin.jtwitchbot.messaging.MessageHandler;

/**
 * 
 * @author Calvin Gene Hall
 * 
 *         This class houses all the members for each individual bot. From this
 *         object, you can get any information about the bot represented by this
 *         object.
 *
 */
public final class TwitchBot {
    private final TwitchAccount botAccount;
    private final ServerConnection ircConnection;
    private final ServerConnection groupConnection;
    private final ChannelManager channelManager = new ChannelManager(this);
    private final MessageHandler messageHandler = new MessageHandler(this);
    private final BotGUIFrame gui;
    private volatile boolean logOut = false;

    public TwitchBot(final TwitchAccount botAccount) throws IOException {
	this.botAccount = botAccount;
	ircConnection = new ServerConnection(Main.TWITCH_IRC_INFO, this);
	groupConnection = new ServerConnection(Main.TWITCH_GROUP_INFO, this);
	groupConnection.sendCommand("CAP REQ", ":twitch.tv/commands");
	gui = new BotGUIFrame(this);
    }

    public TwitchAccount getAccount() {
	return botAccount;
    }

    public ChannelManager getChannelManager() {
	return channelManager;
    }

    public ServerConnection getGroupConnection() {
	return groupConnection;
    }

    public BotGUIFrame getGUI() {
	return gui;
    }

    public ServerConnection getIrcConnection() {
	return ircConnection;
    }

    public MessageHandler getMessageHandler() {
	return messageHandler;
    }

    public boolean isLoggedOut() {
	return logOut;
    }

    public void logout() {
	logOut = true;
	for (Channel channel : channelManager.getChannels()) {
	    channel.sendMessage("Goodbye!");
	}
    }
}
