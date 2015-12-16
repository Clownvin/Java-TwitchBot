package com.github.clownvin.jtwitchbot.bot;

import java.io.IOException;

import com.github.clownvin.jtwitchbot.Main;
import com.github.clownvin.jtwitchbot.account.TwitchAccount;
import com.github.clownvin.jtwitchbot.connection.ChannelManager;
import com.github.clownvin.jtwitchbot.connection.ServerConnection;
import com.github.clownvin.jtwitchbot.messaging.MessageHandler;

public final class TwitchBot {
	private final TwitchAccount botAccount;
	private final ServerConnection ircConnection;
	private final ServerConnection groupConnection;
	private final ChannelManager channelManager = new ChannelManager(this);
	private final MessageHandler messageHandler = new MessageHandler(this);
	private volatile boolean logOut = false;
	
	public TwitchBot(final TwitchAccount botAccount) throws IOException {
		this.botAccount = botAccount;
		ircConnection = new ServerConnection(Main.TWITCH_IRC_INFO, this);
		groupConnection = new ServerConnection(Main.TWITCH_GROUP_INFO, this);
		groupConnection.sendCommand("CAP REQ", ":twitch.tv/commands");
	}
	
	public ServerConnection getIrcConnection() {
		return ircConnection;
	}
	
	public ServerConnection getGroupConnection() {
		return groupConnection;
	}
	
	public TwitchAccount getAccount() {
		return botAccount;
	}
	
	public ChannelManager getChannelManager() {
		return channelManager;
	}
	
	public MessageHandler getMessageHandler() {
		return messageHandler;
	}
	
	public boolean isGoingDown() {
		return logOut;
	}
}
