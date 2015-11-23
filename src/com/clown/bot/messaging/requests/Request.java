package com.clown.bot.messaging.requests;

import com.clown.bot.TwitchBot;
import com.clown.io.Action;

/**
 * 
 * @author Calvin Basic container object containing information about requests
 *         made between users.
 */
public final class Request {
    private final String from;
    private final String to;
    private final Action action;
    private long timeout = System.currentTimeMillis() + RequestHandler.REQUEST_TIMEOUT;

    /**
     * Constructor for a new request instance.
     * 
     * @param from
     *            the request sender.
     * @param to
     *            the request receiver.
     * @param action
     *            the action to be taken if the request is accepted.
     */
    public Request(final String from, final String to, final Action action) {
	this.from = from;
	this.to = to;
	this.action = action;
    }

    /**
     * Accepts the request.
     */
    public void accept() {
	TwitchBot.getGroupConnection().sendWhisper(from, "Your request has been accepted.");
	TwitchBot.getGroupConnection().sendWhisper(to, "Request accepted.");
	action.perform();
	timeout = 0;
	RequestHandler.forceRequestCull();
    }

    /**
     * Denies the request.
     */
    public void deny() {
	TwitchBot.getGroupConnection().sendWhisper(from, "Your request has been denied.");
	TwitchBot.getGroupConnection().sendWhisper(to, "Request denied.");
	timeout = 0;
	RequestHandler.forceRequestCull();
    }

    /**
     * Allows access to the <code>Action</code> object.
     * 
     * @return the action object of this request.
     */
    public Action getAction() {
	return action;
    }

    /**
     * Allows access to the username of the request sender.
     * 
     * @return the username of the request sender.
     */
    public String getFrom() {
	return from;
    }

    /**
     * Allows access to the username of the request receiver.
     * 
     * @return the username of the request receiver.
     */
    public String getTo() {
	return to;
    }

    /**
     * Checks whether or not the timeout on this request has been reached.
     * 
     * @return true if timeout has been reached, false if it hasn't.
     */
    public boolean timeoutReached() {
	return (System.currentTimeMillis() - timeout > 0);
    }
}
