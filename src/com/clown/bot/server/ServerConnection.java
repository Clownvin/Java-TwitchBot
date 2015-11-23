package com.clown.bot.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import com.clown.bot.TwitchBot;
import com.clown.bot.channel.ChannelManager;
import com.clown.bot.messaging.Message;
import com.clown.bot.messaging.MessageHandler;
import com.clown.bot.user.User;
import com.clown.io.BasicIO;
import com.clown.util.Util;

/**
 * 
 * @author Calvin
 *
 *         This class is the true "main" object. Everything revolves around this
 *         object, and all input and output flows through an instance of this
 *         object.
 */
public final class ServerConnection extends Thread {
    private final String ip;
    private final int port;
    private final Socket socket;
    private final OutputStream output;
    private final InputStream input;

    private final ChannelManager channelManager = new ChannelManager();

    private volatile ArrayList<String> users = new ArrayList<String>();

    private volatile ArrayList<String> messages = new ArrayList<String>();

    /**
     * This throttles the output of whisper messages, so that the bot doesn't
     * get notifications about being too fast.
     */
    private final Thread whisperThrottler = new Thread() {
	@Override
	public void run() {
	    while (!TwitchBot.killIssued()) {
		try {
		    Thread.sleep(400); // Sleep for 400MS before sending the
				       // next message.
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
		    sendMessage(TwitchBot.DEFAULT_CHANNELS[0], "/w " + user + " " + message);
		}
	    }
	}
    };

    /**
     * Constructor for a new instance of server connection.
     * 
     * @param ip
     *            IP of the server to connect to.
     * @param port
     *            port of the server to connect to.
     * @throws IOException
     *             if an IOException is thrown while creating socket, output, or
     *             input, throw it to the caller since this object is basically
     *             useless then.
     */
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

    /**
     * Basically just logs into the server using the bot's login info.
     */
    private void establishConnection() {
	sendCommand("PASS", TwitchBot.DEFAULT_OAUTH);
	sendCommand("NICK", TwitchBot.DEFAULT_NICKNAME);
	sendCommand("USER",
		String.format("%s %s bla :%s", TwitchBot.DEFAULT_INDENTITY, ip, TwitchBot.DEFAULT_REALNAME));
    }

    /**
     * Allows other objects to get the channel manager.
     * 
     * @return the current channelManager object.
     */
    public ChannelManager getChannelManager() {
	return channelManager;
    }

    /**
     * Allows other objects to obtain the <code>User</code> object for a certain
     * user.
     * 
     * @param channel
     *            channel of the user.
     * @param username
     *            username of the user.
     * @return the User object for that user, if one exists. Otherwise null.
     */
    public User getUser(String channel, String username) {
	for (User user : channelManager.getChannel(channel).getViewerList()) {
	    if (user.getUsername().equalsIgnoreCase(username)) {
		return user;
	    }
	}
	return null;
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
	    MessageHandler.handleMessage(this, new Message(line));
	    return;
	}
	if (line.contains(" WHISPER ")) {
	    MessageHandler.handleWhisper(this, new Message(line));
	    return;
	}
	System.out.println("[" + this + "] " + line);
    }

    /**
     * Sends a JOIN command for the channel requested. Upon joining, input and
     * output between this channel will be possible.
     * 
     * @param channel
     *            the channel to join.
     */
    public void joinChannel(String channel) {
	if (channelManager.addChannel(channel)) {
	    sendCommand("JOIN", channel);
	} else {
	    System.out.println("Channel already exists.");
	}
    }

    /**
     * Overridden run method. Each step of the while-loop, a single line is read
     * from the input stream, and <code>handleLine</code> is called.
     */
    @Override
    public void run() {
	System.out.println("Server " + this + " running.");
	while (!TwitchBot.killIssued()) {
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
	try {
	    output.write(Util.toBytes(command + " " + message + "\r\n"));
	} catch (IOException e) {
	    e.printStackTrace();
	    return false;
	}
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
	try {
	    // Write the message to the output stream.
	    output.write(Util.toBytes("PRIVMSG " + channel + " :" + message + "\r\n"));
	    output.flush();
	} catch (IOException e) {
	    e.printStackTrace();
	    return false;
	}
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
    public synchronized boolean sendWhisper(String user, String message) {
	users.add(user);
	messages.add(message);
	return true;
    }

    /**
     * Overriden toString method.
     */
    @Override
    public String toString() {
	return ip + ":" + port;
    }
}
