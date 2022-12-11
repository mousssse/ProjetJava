package main.java.com.model;

import java.io.IOException;

import main.java.com.controller.ListenerManager;

/**
 * 
 * @author sarah
 * @author Sandro
 *
 */
public class ConversationThread implements Runnable {
	private Conversation conversation;
	private User remoteUser;
	
	/**
	 * 
	 * @param conversation is the conversation instance
	 * @param remoteUser is the remote user with which this conversation is occurring
	 */
	public ConversationThread(Conversation conversation, User remoteUser) {
		this.conversation = conversation;
		this.remoteUser = remoteUser;
	}

	@Override
	public void run() {
		while (true) {
			try {
				Message message = this.conversation.read();
				switch (message.getType()) {
					case MESSAGE: 
						ListenerManager.getInstance().fireOnMessageToReceive(message);
						break;
					case CLOSING_CONVERSATION:
						ListenerManager.getInstance().fireOnChatClosure(this.remoteUser);
						break;
					default:
						break;
				}
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				// user has disconnected?
				e.printStackTrace();
			}
		}
	}
	
}