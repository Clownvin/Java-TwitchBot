package com.github.clownvin.jtwitchbot;

import com.github.clownvin.jtwitchbot.bot.TwitchBot;
import com.github.clownvin.jtwitchbot.botloader.BotLoader;
import com.github.clownvin.jtwitchbot.connection.ServerInfo;

/**
 * 
 * @author Calvin Gene Hall
 * 
 *         This class represents the main entry point into the program. It
 *         contains {@link ServerInfo} representations of the two IRC servers
 *         that the bot uses, the regular Twitch IRC and the Twitch Group IRC
 *         (for Whispers). It also contains an array of all the bots currently
 *         active. Because it contains an array of all the bots, it's entirely
 *         possible to create code that handles interaction between two bots,
 *         such as more than one bot for sending messages. However, this feature
 *         is not used currently in the base version.
 * 
 *
 */
public final class Main {
    /**
     * {@link ServerInfo} representation of the Twitch IRC server.
     */
    public static final ServerInfo TWITCH_IRC_INFO = new ServerInfo("irc.twitch.tv", 6667);
    /**
     * {@link ServerInfo} representation of the Twitch Group IRC server (for
     * whispers)
     */
    public static final ServerInfo TWITCH_GROUP_INFO = new ServerInfo("192.16.64.180", 443);
    /**
     * {@link String} representation of a URL for gathering active users of a
     * channel.
     */
    public static final String TWITCH_GROUP_URL = "http://tmi.twitch.tv/group/user/";
    /**
     * Array of currently active bots.
     */
    public static final TwitchBot[] BOTS;

    /**
     * The static block loads the <code>BOTS</code> array with all the bots
     * specified in the accounts.cfg
     */
    static {
	BOTS = BotLoader.loadBots();
    }

    public static volatile boolean killIssued = false;

    /**
     * Allows other classes to view the state of <code>killIssued</code> for
     * various reasons such as loop termination on threads.
     * 
     * @return the state of <code>killIssued</code>
     */
    public static boolean killIssued() {
	return killIssued;
    }

    /**
     * Allows other classes to access the array of currently active bots, for
     * reasons such as coordination.
     * 
     * @return the array of loaded bots.
     */
    public static TwitchBot[] getBots() {
	return BOTS;
    }

    /**
     * Just provides an entry point, pretty much.
     * 
     * @param args
     *            program arguments, not used currently.
     */
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
