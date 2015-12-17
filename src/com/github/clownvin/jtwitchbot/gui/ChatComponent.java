package com.github.clownvin.jtwitchbot.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import com.github.clownvin.jtwitchbot.channels.Channel;

public class ChatComponent extends Component {
    /**
     * 
     */
    private static final long serialVersionUID = 7038631785973594642L;
    private final Channel channel;
    private float scrollPercent = 1.0f; // 100% scrolled (bottom of the list).

    public ChatComponent(final Channel channel) {
	this.channel = channel;
    }

    @Override
    public boolean equals(Object other) {
	if (!(other instanceof ChatComponent)) {
	    return false;
	}
	return ((ChatComponent) other).channel.equals(channel);
    }

    public Channel getChannel() {
	return channel;
    }

    public void sendMessage(String message) {
	channel.sendMessage(message);
	repaint();
    }

    @Override
    public void paint(Graphics g) {
	FontMetrics metrics = g.getFontMetrics();
	int minStarting = (this.getHeight() / metrics.getHeight());
	int total = channel.getMessages().size();
	int end = 0;
	if (total > minStarting) {
	    end = (int) ((total - minStarting) * scrollPercent) + minStarting;
	}
	int idx = metrics.getHeight();
	int j = 0;
	for (int i = end - minStarting >= 0 ? end - minStarting : 0; j < minStarting
		&& i < channel.getMessages().size(); j++, i++) {
	    g.setColor(Color.BLACK);
	    g.drawString(channel.getMessages().get(i).user.getUsername(), 4, idx + 1);
	    g.setColor(channel.getMessages().get(i).user.getColor());
	    g.drawString(channel.getMessages().get(i).user.getUsername(), 3, idx);
	    g.setColor(Color.BLACK);

	    g.drawString(": " + channel.getMessages().get(i).message,
		    3 + metrics.stringWidth(channel.getMessages().get(i).user.getUsername()), idx);
	    idx += metrics.getHeight();
	}
    }
}
