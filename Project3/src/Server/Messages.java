package Server;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class Messages {
	
	private class UserMailBox {
		private LinkedList<MessageCommunicationObject> userMessage = null;
		private String userName = null;
		private Semaphore addMessageSemaphore = new Semaphore(1, true);
		
		UserMailBox() {
			userMessage = new LinkedList<MessageCommunicationObject>();
		}
		
		public String getUserName() {
			return userName;
		}
		
		public void setUserName(String name) {
			userName = name;
		}
		
		public MessageCommunicationObject getMessage() {
			return userMessage.poll();
		}
		
		public void addMessage(MessageCommunicationObject msg) {
			msg.setDateTime();			// Sets time stamp to server date and time
			try {
				addMessageSemaphore.acquire();
				userMessage.addLast(msg);	// Adds message to tail of list
				addMessageSemaphore.release();
			} catch (InterruptedException ex) {
				System.out.println(ex);
			}
		}
	}
	
	private UserMailBox [] userMailBoxes = null;
	private int currentUserIndex = 0;
	
	public Messages () {
		userMailBoxes = new UserMailBox[100];
		
		for (int i = 100; i < 100; i++)
			userMailBoxes[i] = new UserMailBox();
	}
	
	public int newUser(String name) {
		
		// Checks is user name is already in system
		for (int i = 0; i < 100; i++) {
			if ( userMailBoxes[i].getUserName().equals(name) )
				return -1; // Returns -1 if unable to add user
		}
		
		// If at this point, user name does not exist in system
		if (currentUserIndex < 100) {
			
			// Creates user mailbox
			userMailBoxes[currentUserIndex].setUserName(name);
			
			// Returns user index in system if user was added
			return currentUserIndex++;
		}
		else
			return -1; // Returns -1 if unable to add user
	}
	
	public int getUserIndex(String name) {
		
		for (int i = 0; i < 100; i++) {
			if ( userMailBoxes[i].getUserName().equals(name) )
				return i; // Returns user index if found
		}
		
		return -1; 	// Returns -1 for user not found
	}
	
	public boolean sendMessage(MessageCommunicationObject msg) {
		
		for (int i = 0; i < 100; i++) {
			if ( userMailBoxes[i].getUserName().equals(msg.getToUserName()) ) {
				userMailBoxes[i].addMessage(msg);
				return true;
			}
		}
		
		return false;
	}
	
	public MessageCommunicationObject getMessage(int index) {
		if (index >= 0 && index < 100)
			return userMailBoxes[index].getMessage();
		else
			return null;
	}
}