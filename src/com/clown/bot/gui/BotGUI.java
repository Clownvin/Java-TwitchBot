package com.clown.bot.gui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import com.clown.bot.messaging.Message;

/**
 * 
 * @author Calvin
 *	This object is basically not used right now at all. It WAS a basic GUI showing the chats from the various channes
 */
public final class BotGUI extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4983554815226772948L;

	private static final BufferedImage bufferImage = new BufferedImage(
			(int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth()),
			(int) Toolkit.getDefaultToolkit().getScreenSize().getHeight(), BufferedImage.TYPE_INT_RGB);
	private static final Graphics bufferGraphics = bufferImage.getGraphics();

	public BotGUI() {
		this.setSize((int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2),
				(int) Toolkit.getDefaultToolkit().getScreenSize().getHeight());
		this.setVisible(true);
		this.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_ESCAPE:
					// for (Channel channel : currentChannels)
					// sendMessage(channel.getChannel(), "Looks like my time is
					// up! :(");
					System.exit(0);
					break;
				}
			}
		});
	}

	// TODO add word wrap (oh dear, what a pain that'll be.
	@Override
	public void paint(Graphics g) {
		bufferGraphics.setColor(Color.WHITE);
		bufferGraphics.fillRect(0, 0, bufferImage.getWidth(), bufferImage.getHeight());
		int fontHeight = bufferGraphics.getFontMetrics().getHeight();
		FontMetrics metrics = bufferGraphics.getFontMetrics();
		int stringCount = this.getHeight() / fontHeight;
		int idx = 1;// messages.size() - 1; yea.... this needs work lol.
		Message message = null;
		// for (int i = stringCount; i > -1 && idx > -1; i--, idx--) {
		// int y = i * fontHeight;
		// message = messages.get(idx);
		// bufferGraphics.setColor(Color.BLACK);
		// bufferGraphics.drawString("[", 10, y);
		// bufferGraphics.setColor(channelColors.get(indexOfChannel(message.channel)));
		// bufferGraphics.drawString(message.channel, 10 +
		// metrics.stringWidth("["), y);
		// bufferGraphics.setColor(Color.BLACK);
		// bufferGraphics.drawString("] " + message.user + ": " +
		// message.message,
		// 10 + metrics.stringWidth("[" + message.channel), y);
		// }
		g.drawImage(bufferImage, 0, 0, null);
	}
}
