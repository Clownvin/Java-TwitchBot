package com.clown.bot.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.clown.bot.TwitchIRCBot;
import com.clown.bot.messaging.IRCMessage;
import com.clown.bot.messaging.MessageHandler;
import com.clown.io.BasicIO;
import com.clown.util.Util;

public final class ServerConnection extends Thread {
	private final String ip;
	private final int port;
	private final Socket socket;
	private final OutputStream output;
	private final InputStream input;
	
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
		start();
	}
	
	private void establishConnection() {
		sendCommand("PASS", TwitchIRCBot.DEFAULT_OAUTH);
		sendCommand("NICK", TwitchIRCBot.DEFAULT_NICKNAME);
		sendCommand("USER", String.format("%s %s bla :%s", TwitchIRCBot.DEFAULT_INDENTITY, ip, TwitchIRCBot.DEFAULT_REALNAME));
	}
	
	@Override
	public void run() {
		System.out.println("Server "+this+" running.");
		while (!TwitchIRCBot.killIssued()) {
			String line = String.valueOf(BasicIO.readLine(input));
			handleLine(line);
		}
		System.out.println("Exiting");
	}
	
	public boolean sendMessage(String channel, String message) {
		System.out.println("Sending message: " + message + " to channel: " + channel);
		try {
			output.write(Util.toBytes("PRIVMSG " + channel + " :" + message + "\r\n"));
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private void handleLine(String line) {
		if (line.contains("PING :")) {
			System.out.println("Sending pong response.");
			sendCommand("PONG", ":" + line.replace("PING :", ""));
			return;
		}
		if (line.contains(" PRIVMSG #")) {
			MessageHandler.handleIRCMessage(this, new IRCMessage(line));
			return;
		}
		System.out.println("["+this+"] "+line);
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
	
	@Override
	public String toString() {
		return ip+":"+port;
	}
}
