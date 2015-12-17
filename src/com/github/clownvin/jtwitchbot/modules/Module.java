package com.github.clownvin.jtwitchbot.modules;

import java.io.Serializable;

import com.github.clownvin.jtwitchbot.messaging.Message;
import com.github.clownvin.jtwitchbot.user.User;

public abstract class Module implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2275792831269941533L;
	
	public abstract void onLoad();

	public abstract boolean onMessage(Message message);
	
	public abstract boolean onWhisper(Message message);
	
	public abstract boolean onJoin(User user);
	
	public abstract boolean onLeave(User user);
	
	public abstract boolean onCommand(User user, String command, String[] args);
}
