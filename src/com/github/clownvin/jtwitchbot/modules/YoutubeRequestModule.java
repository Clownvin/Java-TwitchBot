package com.github.clownvin.jtwitchbot.modules;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import com.github.clownvin.jtwitchbot.Main;
import com.github.clownvin.jtwitchbot.commands.Command;
import com.github.clownvin.jtwitchbot.messaging.Message;
import com.github.clownvin.jtwitchbot.user.User;
import com.github.clownvin.jtwitchbot.user.UserType;

public class YoutubeRequestModule extends Module {
    private static final String ADBLOCK_FILE = "./config/adblockplusfirefox.xpi";
    private final class Request implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 863570513238862014L;
	private final User user;
	private final String videoLink;

	public Request(final User user, final String videoLink) {
	    this.user = user;
	    this.videoLink = videoLink;
	}

	public User getUser() {
	    return user;
	}

	public String getVideoLink() {
	    return videoLink;
	}
    }

    /**
     * 
     */
    private static final long serialVersionUID = -8252909386414960382L;
    private transient Command[] commands;
    private transient volatile FirefoxDriver firefoxDriver;
    private transient volatile ArrayList<Request> requests;
    private transient volatile String playlist;
    private transient volatile String currentSong;
    private transient Thread requestPlayer;

    @Override
    public boolean onCommand(User user, String command, String[] args) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean onJoin(User user) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean onLeave(User user) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public void onLoad() {
	File file = new File(ADBLOCK_FILE);
	FirefoxProfile firefoxProfile = new FirefoxProfile();
	try {
	    firefoxProfile.addExtension(file);
	} catch (IOException e1) {
	    e1.printStackTrace();
	}
	firefoxDriver = new FirefoxDriver(firefoxProfile);
	playlist = "http://www.google.com"; // Just set it to go to google.
	currentSong = playlist;
	requests = new ArrayList<Request>();
	commands = new Command[] { new Command("!addsong", "Adds song to queue. Usage: !addsong <youtube link>") {

	    /**
	     * 
	     */
	    private static final long serialVersionUID = 4214703559547684978L;

	    @Override
	    public UserType getUserType() {
		return UserType.VIEWER;
	    }

	    @Override
	    public void handleCommand(User user, String[] args, String message) {
		for (Request request : requests) {
		    if (request.getUser().equals(user)) {
			user.sendWhisper("You've already got a song in the queue.");
			return;
		    }
		}
		if (args.length == 0) {
		    user.sendWhisper("You must include a link to the youtube video. (!addsong <youtube link>)");
		    return;
		}
		if (!args[0].startsWith("https://www.youtube.com/watch")) {
		    user.sendWhisper("Song link must start with \"https://www.youtube.com/watch\"");
		    return;
		}
		requests.add(new Request(user, args[0]));
		user.sendWhisper(
			"Your request has been added to queue. Songs till your song: " + (requests.size() - 1));
		synchronized (requestPlayer) {
		    requestPlayer.notifyAll();
		}
	    }

	},

		new Command("!song", "Gets a link to the current song. Usage: !song") {

		    /**
		     * 
		     */
		    private static final long serialVersionUID = 4214703559547685978L;

		    @Override
		    public UserType getUserType() {
			return UserType.VIEWER;
		    }

		    @Override
		    public void handleCommand(User user, String[] args, String message) {
			user.sendWhisper("Current song link: " + currentSong);
		    }

		},

		new Command("!setplaylist",
			"Sets the default playlist, which is played when there are no songs in the queue. Usage: !setplaylist <youtube link>") {

		    /**
		     * 
		     */
		    private static final long serialVersionUID = 4214703259547684978L;

		    @Override
		    public UserType getUserType() {
			return UserType.MODERATOR;
		    }

		    @Override
		    public void handleCommand(User user, String[] args, String message) {
			if (args.length == 0) {
			    user.sendWhisper(
				    "You must include a link to the youtube playlist. (!setplaylist <youtube link>)");
			    return;
			}
			if (!args[0].startsWith("https://www.youtube.com")) {
			    user.sendWhisper("Playlist link must start with \"https://www.youtube.com\"");
			    return;
			}
			playlist = args[0];
			user.sendWhisper("Playlist set.");
			synchronized (requestPlayer) {
			    requestPlayer.notifyAll();
			}
		    }

		} };
	requestPlayer = new Thread() {
	    @Override
	    public void run() {
		while (!Main.killIssued()) {
		    firefoxDriver.navigate().to(playlist);
		    currentSong = playlist;
		    synchronized (this) {
			    try {
				this.wait();
			    } catch (InterruptedException e) {
			    }
			}
		    while (requests.size() > 0) {
			Request thisRequest = requests.remove(0);
			currentSong = thisRequest.getVideoLink();
			try {
			    firefoxDriver.navigate().to(currentSong);
			    sleep(1500); // Wait a second for browser to load page
			    WebElement durationElement = firefoxDriver.findElementByClassName("ytp-time-duration");
			    long duration = 1000;
			    String durationString = durationElement == null ? "" : durationElement.getText();
			    if (durationString.length() != 0) {
				duration = 0;
				//MM:SS
				String[] split = durationString.split(":");
				duration += Long.parseLong(split[0]) * 60000;
				duration += Long.parseLong(split[1]) * 1000;
				duration += 5000; // 5 seconds.
			    }
			    System.out.println("Waiting " + duration + " milliseconds.");
			    try {
				sleep(duration);
			    } catch (InterruptedException e) {
			    }
			} catch (Exception e) {
			    continue;
			}
		    }
		}
	    }
	};
	requestPlayer.start();
    }

    @Override
    public boolean onMessage(Message message) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean onWhisper(Message message) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public String getModuleName() {
	return "YoutubeRequestModule";
    }

    @Override
    public Command[] getModuleCommands() {
	return commands;
    }

}
