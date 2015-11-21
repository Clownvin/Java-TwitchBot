package com.clown.bot.messaging.requests;

import java.util.ArrayList;

import com.clown.bot.TwitchBot;

public final class RequestHandler {
	public static final long REQUEST_TIMEOUT = 60000; // 1 min
	public static final long REQUEST_DELAY = 10000; // 10 seconds

	private static final ArrayList<Request> pendingRequests = new ArrayList<Request>();

	// We're going to assume that all request exist ONLY on the
	// DEFAULT_CHANNELS[0]

	/**
	 * A thread to cull expired requests from the request list.
	 */
	private static final Thread REQUEST_CULLER = new Thread() {
		@Override
		public void run() {
			while (!TwitchBot.killIssued()) {
				try {
					Thread.sleep(1000); // 1 second
				} catch (InterruptedException e) {
				}
				for (int i = 0; i < pendingRequests.size(); i++) {
					if (pendingRequests.get(i).timeoutReached()) {
						TwitchBot.getIRCConnection()
								.getUser(TwitchBot.DEFAULT_CHANNELS[0], pendingRequests.remove(i).getFrom())
								.setRequestDelay(REQUEST_DELAY);
						i--;
					}
				}
			}
		}
	};

	static {
		REQUEST_CULLER.start();
	}

	/**
	 * Interrupts the REQUEST_CULLER to force it to do a cull loop.
	 */
	public static void forceRequestCull() {
		REQUEST_CULLER.interrupt();
	}

	/**
	 * Allows access to requests given the username.
	 * 
	 * @param user
	 *            username to match requests against.
	 * @return the request containing the username, or null if non exist.
	 */
	public static Request getRequest(String user) {
		for (Request request : pendingRequests) {
			if (request.getFrom().equalsIgnoreCase(user) || request.getTo().equalsIgnoreCase(user)) {
				return request;
			}
		}
		return null;
	}

	/**
	 * Adds a request to the list
	 * 
	 * @param request
	 *            request to be added.
	 */
	public static void addRequest(Request request) {
		pendingRequests.add(request);
	}

	private RequestHandler() {
		// Prevent instantiation.
	}
}
