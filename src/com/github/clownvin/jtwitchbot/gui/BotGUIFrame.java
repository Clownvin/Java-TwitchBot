package com.github.clownvin.jtwitchbot.gui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import com.github.clownvin.jtwitchbot.bot.TwitchBot;
import com.github.clownvin.jtwitchbot.channels.Channel;
import javax.swing.JTextField;
import java.awt.BorderLayout;

public final class BotGUIFrame extends JFrame {
    /**
     * 
     */
    private static final long serialVersionUID = -5621316993862988132L;
    private final TwitchBot bot;
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final JTabbedPane chatrooms = new JTabbedPane();
    private JTextField textField;

    public BotGUIFrame(final TwitchBot bot) {
	this.bot = bot;
	getContentPane().add(tabbedPane);
	tabbedPane.addTab("Chat", chatrooms);

	textField = new JTextField();
	textField.addKeyListener(new KeyAdapter() {
	    @Override
	    public void keyReleased(KeyEvent e) {
		ChatComponent component = (ChatComponent) chatrooms.getComponentAt((chatrooms.getSelectedIndex()));
		switch (e.getKeyCode()) {
		case KeyEvent.VK_ENTER:
		    component.sendMessage(textField.getText());
		    textField.setText("");
		    return;
		}
	    }
	});
	getContentPane().add(textField, BorderLayout.SOUTH);
	textField.setColumns(10);
	updateChatrooms();
	this.setSize(800, 600);
	this.setTitle(bot.getAccount().getUsername() + "'s GUI");
	this.setVisible(true);
	this.addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowClosing(WindowEvent e) {
		bot.logout();
	    }
	});
    }

    public void updateChatrooms() {
	chatrooms.removeAll();
	for (Channel channel : bot.getChannelManager().getChannels()) {
	    chatrooms.addTab(channel.getChannel(), new ChatComponent(channel));
	}
    }

}
