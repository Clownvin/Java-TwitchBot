package com.clown.bot.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import com.clown.bot.TwitchIRCBot;
import com.clown.bot.channel.ChannelManager;
import com.clown.bot.messaging.Message;
import com.clown.bot.messaging.MessageHandler;
import com.clown.bot.user.User;
import com.clown.io.BasicIO;
import com.clown.util.Util;

public final class ServerConnection extends Thread {
	private final String ip;
	private final int port;
	private final Socket socket;
	private final OutputStream output;
	private final InputStream input;

	private final ChannelManager channelManager = new ChannelManager();

	private volatile ArrayList<String> users = new ArrayList<String>();

	private volatile ArrayList<String> messages = new ArrayList<String>();

	private final Thread whisperThrottler = new Thread() {
		@Override
		public void run() {
			while (!TwitchIRCBot.killIssued()) {
				try {
					Thread.sleep(400);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (users.size() > 0) {
					String user = "";
					String message = "";
					synchronized (users) {
						user = users.remove(0);
					}
					synchronized (messages) {
						message = messages.remove(0);
					}
					sendMessage(TwitchIRCBot.DEFAULT_CHANNELS[0], "/w " + user + " " + message);
				}
			}
		}
	};

	public ServerConnection(final String ip, final int port) throws IOException {
		this.ip = ip;
		this.port = port;
		try {
			this.socket = new Socket(ip, port);
			this.output = socket.getOutputStream();
			this.input = socket.getInputStream();
		} catch (IOException e) {
			throw e;
		}
		establishConnection();
		whisperThrottler.start();
		start();
	}

	private void establishConnection() {
		sendCommand("PASS", TwitchIRCBot.DEFAULT_OAUTH);
		sendCommand("NICK", TwitchIRCBot.DEFAULT_NICKNAME);
		sendCommand("USER",
				String.format("%s %s bla :%s", TwitchIRCBot.DEFAULT_INDENTITY, ip, TwitchIRCBot.DEFAULT_REALNAME));
	}

	public ChannelManager getChannelManager() {
		return channelManager;
	}

	public User getUser(String channel, String username) {
		for (User user : channelManager.getChannel(channel).getViewerList()) {
			if (user.getUsername().equalsIgnoreCase(username)) {
				return user;
			}
		}
		return null;
	}

	private void handleLine(String line) {
		if (line.contains("PING :")) {
			System.out.println("Sending pong response.");
			sendCommand("PONG", ":" + line.replace("PING :", ""));
			return;
		}
		if (line.contains(" PRIVMSG #")) {
			MessageHandler.handleMessage(this, new Message(line));
			return;
		}
		if (line.contains(" WHISPER ")) {
			MessageHandler.handleWhisper(this, new Message(line));
			return;
		}
		System.out.println("[" + this + "] " + line);
	}

	public void joinChannel(String channel) {
		if (channelManager.addChannel(channel)) {
			sendCommand("JOIN", channel);
		} else {
			System.out.println("Channel already exists.");
		}
	}

	@Override
	public void run() {
		System.out.println("Server " + this + " running.");
		while (!TwitchIRCBot.killIssued()) {
			String line = String.valueOf(BasicIO.readLine(input));
			handleLine(line);
		}
	}
	public boolean sendCommand(String command, String message) {
		try {
			output.write(Util.toBytes(command + " " + message + "\r\n"));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public synchronized boolean sendMessage(String channel, String message) {
		System.out.println("Sending message: " + message + " to : " + channel);
		try {
			output.write(Util.toBytes("PRIVMSG " + channel + " :" + message + "\r\n"));
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public synchronized boolean sendWhisper(String user, String message) {
		users.add(user);
		messages.add(message);
		return true;
	}

	@Override
	public String toString() {
		return ip + ":" + port;
	}
}
