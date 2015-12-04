package com.clown.bot.games;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

import com.clown.bot.TwitchBot;
import com.clown.bot.messaging.polling.PollHandler;

public final class TwitchPlaysFrozenDepths extends Thread {
	private static final TwitchPlaysFrozenDepths SINGLETON = new TwitchPlaysFrozenDepths();
	private volatile Robot robot;
	
	private TwitchPlaysFrozenDepths() {
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}
	
	public static void sendInput(String input) {
		if (input.length() < 1) {
			synchronized (SINGLETON) {
				SINGLETON.notifyAll();
			}
			return;
		}
		System.out.println("Sending input: "+input);
		String[] commands = input.split("\\+");
		boolean shiftedLast = false;
		for (String command : commands) {
			if (command.length() < 1) {
				continue;
			}
			if (shiftedLast) {
				SINGLETON.robot.keyRelease(KeyEvent.VK_SHIFT);
			}
			switch (command.toLowerCase()) {
			case "shift":
				SINGLETON.robot.keyPress(KeyEvent.VK_SHIFT);
				shiftedLast = true;
				break;
			case "enter":
				SINGLETON.robot.keyPress(KeyEvent.VK_ENTER);
				SINGLETON.robot.keyRelease(KeyEvent.VK_ENTER);
				break;
			case "up":
				SINGLETON.robot.keyPress(KeyEvent.VK_UP);
				SINGLETON.robot.keyRelease(KeyEvent.VK_UP);
				break;
			case "down":
				SINGLETON.robot.keyPress(KeyEvent.VK_DOWN);
				SINGLETON.robot.keyRelease(KeyEvent.VK_DOWN);
				break;
			case "left":
				SINGLETON.robot.keyPress(KeyEvent.VK_LEFT);
				SINGLETON.robot.keyRelease(KeyEvent.VK_LEFT);
				break;
			case "right":
				SINGLETON.robot.keyPress(KeyEvent.VK_RIGHT);
				SINGLETON.robot.keyRelease(KeyEvent.VK_RIGHT);
				break;
			default:
				SINGLETON.robot.keyPress(KeyEvent.getExtendedKeyCodeForChar(command.toLowerCase().charAt(0)));
				SINGLETON.robot.keyRelease(KeyEvent.getExtendedKeyCodeForChar(command.toLowerCase().charAt(0)));
				break;
			}
		}
		SINGLETON.robot.keyRelease(KeyEvent.VK_SHIFT);
		synchronized (SINGLETON) {
			SINGLETON.notifyAll();
		}
	}
	
	@Override
	public void run() {
		while (!TwitchBot.killIssued()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
			}
			System.out.println("Starting poll...");
			PollHandler.startTPPoll();
			synchronized(SINGLETON) {
				try {
					SINGLETON.wait(35000);
				} catch (InterruptedException e) {
				}
			}
		}
	}
	
	public static void startPlay() {
		SINGLETON.start();
	}
}
