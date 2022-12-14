package main.java.com.controller.listener;

/**
 * SelfLoginListener is the listener associated to the local user's log-in and log-off events.
 * @author Sandro
 * @author sarah
 *
 */
public interface SelfLoginListener {
	
	public void onSelfLoginOnlineUsers(String username);
	public void onSelfLoginNetwork();
	public void onSelfLogout();
}
