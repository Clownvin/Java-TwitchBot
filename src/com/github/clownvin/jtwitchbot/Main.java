package com.github.clownvin.jtwitchbot;

import com.github.clownvin.jtwitchbot.bot.TwitchBot;
import com.github.clownvin.jtwitchbot.botloader.BotLoader;
import com.github.clownvin.jtwitchbot.connection.ServerInfo;

public final class Main {
    public static final ServerInfo TWITCH_IRC_INFO = new ServerInfo("irc.twitch.tv", 6667);
    public static final ServerInfo TWITCH_GROUP_INFO = new ServerInfo("192.16.64.180", 443);
    public static final String TWITCH_GROUP_URL = "http://tmi.twitch.tv/group/user/";
    public static final TwitchBot[] BOTS;

    static {
	BOTS = BotLoader.loadBots();
    }

    public static volatile boolean killIssued = false;

    public static boolean killIssued() {
	return killIssued;
    }

    public static void main(String[] args) {
	System.out.println("Starting bots.");
	if (BOTS.length == 0) {
	    System.err.println(
		    "No bots were loaded from \"./config/accounts.cfg\". Make sure you put your bot info in there before running.");
	    System.exit(1);
	}
	outer: while (!killIssued) {
	    try {
		Thread.sleep(10000);
	    } catch (InterruptedException e) {
	    }
	    for (TwitchBot bot : BOTS) {
		if (!bot.isLoggedOut()) {
		    continue outer;
		}
	    }
	    killIssued = true;
	}
	System.exit(0);
    }
}
