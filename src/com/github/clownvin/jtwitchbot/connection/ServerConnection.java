package com.github.clownvin.jtwitchbot.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import com.clown.io.BasicIO;
import com.github.clownvin.jtwitchbot.bot.TwitchBot;
import com.github.clownvin.jtwitchbot.messaging.Message;
import com.github.clownvin.jtwitchbot.user.User;

/**
 *
 * @author Calvin
 *
 *         This class is the true "main" object. Everything revolves around this
 *         object, and all input and output flows through an instance of this
 *         object.
 */
public final class ServerConnection extends Thread {
	private final ServerInfo serverInfo;
	private final TwitchBot bot;
	private final Socket socket;
	private final OutputStream output;
	private final InputStream input;

	private volatile ArrayList<String> messages = new ArrayList<String>();

	/**
	 * This throttles the output of whisper messages, so that the bot doesn't
	 * get notifications about being too fast.
	 */
	private final Thread messageThrottler = new Thread() {
		@Override
		public void run() {
			while (!bot.isGoingDown()) {
				try {
					Thread.sleep(333); // About 90 messages every 30 seconds MAX.
				} catch (InterruptedException e) {
					continue; // If it's interrupted, we can't risk getting banned by over stepping our rate.
				}
				if (messages.size() < 1) {
					continue;
				}
				try {
					output.write((messages.remove(0) + "\r\n").getBytes());
					output.flush();
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Output is closed: "+socket.isOutputShutdown());
				}
			}
		}
	};

	/**
	 * Constructor for a new instance of server connection.
	 *
	 * @throws IOException
	 *             if an IOException is thrown while creating socket, output, or
	 *             input, throw it to the caller since this object is basically
	 *             useless then.
	 */
	public ServerConnection(final ServerInfo serverInfo, final TwitchBot bot) throws IOException {
		this.serverInfo = serverInfo;
		this.bot = bot;
		try {
			this.socket = new Socket(serverInfo.getServerIp(), serverInfo.getPort());
			this.output = socket.getOutputStream();
			this.input = socket.getInputStream();
		} catch (IOException e) {
			throw e;
		}
		logIn();
		messageThrottler.start();
		start();
	}

	/**
	 * Basically just logs into the server using the bot's login info.
	 */
	private void logIn() {
		sendCommand("PASS", bot.getAccount().getOauth());
		sendCommand("NICK", bot.getAccount().getUsername());
		sendCommand("USER",
				String.format("%s %s bla :%s", bot.getAccount().getUsername(), serverInfo.getServerIp(), bot.getAccount().getUsername()));
	}

	/**
	 * First step in interpreting input from the input stream. Directs the
	 * message to where it needs to go based on its command.
	 *
	 * @param line
	 *            line of input to process.
	 */
	private void handleLine(String line) {
		if (line.contains("PING :")) {
			System.out.println("Sending pong response.");
			sendCommand("PONG", ":" + line.replace("PING :", ""));
			return;
		}
		if (line.contains(" PRIVMSG #")) {
			bot.getMessageHandler().handleMessage(new Message(line, bot));
			return;
		}
		if (line.contains(" WHISPER ")) {
			bot.getMessageHandler().handleWhisper(new Message(line, bot));
			return;
		}
		System.out.println("[" + this + "] " + line);
	}

	/**
	 * Overridden run method. Each step of the while-loop, a single line is read
	 * from the input stream, and <code>handleLine</code> is called.
	 */
	@Override
	public void run() {
		System.out.println("Server " + this + " running.");
		while (!bot.isGoingDown()) {
			String line = String.valueOf(BasicIO.readLine(input));
			handleLine(line);
		}
	}

	/**
	 * Sends a command with the message provided through the output stream.
	 *
	 * @param command
	 *            command to send.
	 * @param message
	 *            message to send.
	 * @return true if everything goes smoothly, false if else.
	 */
	public boolean sendCommand(String command, String message) {
		messages.add(command + " " + message);
		return true;
	}

	/**
	 * Sends a message to a channel. Converts the String to bytes and sends them
	 * through the output stream.
	 *
	 * @param channel
	 *            the channel to send the message on.
	 * @param message
	 *            the message to send.
	 * @return true if everything goes well, false if else.
	 */
	public synchronized boolean sendMessage(String channel, String message) {
		System.out.println("Sending message: " + message + " to : " + channel);
		messages.add("PRIVMSG " + channel + " :" + message);
		return true;
	}

	/**
	 * Sends a whisper to a user. Adds the user and message to two array lists,
	 * and the whisperThrottler thread handles the actual sending of the
	 * messages, since they need to be throttled.
	 *
	 * @param user
	 *            user to send the whisper to.
	 * @param message
	 *            message to send.
	 * @return true always (used to have the possibility to send false, but
	 *         because it no longer handles the actual sending, it can't know if
	 *         there was an issue)
	 */
	public synchronized boolean sendWhisper(User user, String message) {
		sendMessage(user.getChannel(), "/w " + user.getUsername() + " " + message);
		return true;
	}
	
	/**
	 * Sends a whisper to a user. Adds the user and message to two array lists,
	 * and the whisperThrottler thread handles the actual sending of the
	 * messages, since they need to be throttled.
	 *
	 * @param user
	 *            user to send the whisper to.
	 * @param message
	 *            message to send.
	 * @return true always (used to have the possibility to send false, but
	 *         because it no longer handles the actual sending, it can't know if
	 *         there was an issue)
	 */
	public synchronized boolean sendWhisper(String channel, String user, String message) {
		sendMessage(channel, "/w " + user + " " + message);
		return true;
	}

	/**
	 * Overriden toString method.
	 */
	@Override
	public String toString() {
		return serverInfo.getServerIp() + ":" + serverInfo.getPort();
	}
}
