package com.clown.bot.messaging;

import com.clown.bot.TwitchIRCBot;
import com.clown.io.Action;

public final class Request {
	private final String from;
	private final String to;
	private final Action action;
	private long timeout = System.currentTimeMillis() + MessageHandler.REQUEST_TIMEOUT;

	public Request(final String from, final String to, final Action action) {
		this.from = from;
		this.to = to;
		this.action = action;
	}

	public void accept() {
		TwitchIRCBot.getGroupConnection().sendWhisper(from, "Your request has been accepted.");
		TwitchIRCBot.getGroupConnection().sendWhisper(to, "Request accepted.");
		action.perform();
		timeout = 0;
		MessageHandler.forceRequestCull();
	}

	public void deny() {
		TwitchIRCBot.getGroupConnection().sendWhisper(from, "Your request has been denied.");
		TwitchIRCBot.getGroupConnection().sendWhisper(to, "Request denied.");
		timeout = 0;
		MessageHandler.forceRequestCull();
	}

	public Action getAction() {
		return action;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	public boolean timeoutReached() {
		return (System.currentTimeMillis() - timeout > 0);
	}
}
