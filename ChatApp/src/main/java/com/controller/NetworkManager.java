package main.java.com.controller;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import main.java.com.controller.listener.SelfLoginListener;
import main.java.com.model.TCPServer;
import main.java.com.model.UDPServer;
import main.java.com.model.User;

/**
 * The NetworkManager manages a user's UDP and TCP servers.
 * @author Sandro
 * @author sarah
 *
 */
public class NetworkManager implements SelfLoginListener {
	
	private List<Socket> distantSockets;
	private UDPServer UDPserver;
	private TCPServer TCPserver;
	
	private static NetworkManager networkManager = null;
	
	private NetworkManager() {
		this.distantSockets = new ArrayList<>();
		this.UDPserver = new UDPServer();
		this.TCPserver = new TCPServer();
		(new Thread(this.UDPserver, "UDP Server")).start();
		(new Thread(this.TCPserver, "TCP Server")).start();
	}
	
	/**
	 * 
	 * @return the NetworkManager singleton
	 */
	public static NetworkManager getInstance() {
		if (networkManager == null) networkManager = new NetworkManager();
		return networkManager;
	}
	
	/**
	 * 
	 * @param socket is the socket to be added
	 */
	public void addDistantSocket(Socket socket) {
		this.distantSockets.add(socket);
	}
	
	@Override
	public void onSelfLoginNetwork() {
		// TODO broadcast UDP with following message
		// Login message format: "login username port UUID"
		User localUser = OnlineUsersManager.getInstance().getLocalUser();
		String loginMessage = "login " + localUser.getUsername() + " " + localUser.getTCPserverPort() + " " + localUser.getId();
	}
	
	@Override
	public void onSelfLogout() {
		// TODO broadcast udp to tell ppl we've logged out
		// Logout message format: "logout"
		String logoutMessage = "logout";
	}
	
	@Override
	public void onSelfLoginOnlineUsers(String username) {
	}
	
}
