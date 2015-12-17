package com.github.clownvin.jtwitchbot.botloader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.github.clownvin.jtwitchbot.account.TwitchAccount;
import com.github.clownvin.jtwitchbot.bot.TwitchBot;

public final class BotLoader {
    public static final String CONFIGURATION_LOCATION = "./config/";

    // Files to load:
    /*
     * accounts.cfg // List of accounts to load, and their channels.
     */
    public static TwitchBot[] loadBots() {
	ArrayList<TwitchBot> bots = new ArrayList<TwitchBot>();
	try {
	    BufferedReader reader = new BufferedReader(new FileReader(CONFIGURATION_LOCATION + "accounts.cfg"));
	    String line = "";
	    String oauth = "";
	    String username = "";
	    ArrayList<String> channels = new ArrayList<String>();
	    boolean accountBlock = false;
	    while (!(line = reader.readLine()).equals("[EOF]")) {
		if (accountBlock == false && line.equals("account")) {
		    accountBlock = true;
		    channels.clear();
		    continue;
		}
		if (accountBlock && line.startsWith("oauth ")) {
		    oauth = line.substring(6);
		    continue;
		}
		if (accountBlock && line.startsWith("username ")) {
		    username = line.substring(9);
		    continue;
		}
		if (accountBlock && line.startsWith("channel ")) {
		    channels.add(line.substring(8));
		    continue;
		}
		if (accountBlock && line.equals("endaccount")) {
		    TwitchBot bot = new TwitchBot(new TwitchAccount(username, oauth));
		    for (String channel : channels) {
			bot.getChannelManager().joinChannel(channel);
		    }
		    channels.clear();
		    bots.add(bot);
		}
	    }
	    reader.close();
	    return bots.toArray(new TwitchBot[bots.size()]);
	} catch (IOException e) {
	    e.printStackTrace();
	    return null;
	}
    }
}
