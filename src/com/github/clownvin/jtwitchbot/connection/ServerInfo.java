package com.github.clownvin.jtwitchbot.connection;

public final class ServerInfo {
    private final String serverIp;
    private final int port;

    public ServerInfo(final String serverIp, final int port) {
	this.serverIp = serverIp;
	this.port = port;
    }

    public int getPort() {
	return port;
    }

    public String getServerIp() {
	return serverIp;
    }
}
