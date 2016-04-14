package Server;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import Shared.MsgCommObj;

public class Messages {
	
	private class UserMailBox {
		private LinkedList<MsgCommObj> userMessage = null;
		private String userName = null;
		private Semaphore addMessageSemaphore = new Semaphore(1, true);
		private boolean currentlyConnected = false;
		
		public UserMailBox() {
			userMessage = new LinkedList<MsgCommObj>();
			userName = new String();
		}
		
		public String getUserName() {
			return userName;
		}
		
		public void setUserName(String name) {
			userName = name;
		}
		
		public MsgCommObj getMessage() {
			return userMessage.poll();
		}
		
		public void addMessage(MsgCommObj msg) {
			msg.setDateTime();			// Sets time stamp to server date and time
			try {
				addMessageSemaphore.acquire();
				userMessage.addLast(msg);	// Adds message to tail of list
				addMessageSemaphore.release();
			} catch (InterruptedException ex) {
				System.out.println(ex);
			}
		}
		
		public void setConnectionStatus(boolean status) {
			currentlyConnected = status;
		}
		
		public boolean getConnectionStatus() {
			return currentlyConnected;
		}
	}
	
	private UserMailBox [] userMailBoxes = null;
	private int currentUserIndex = 0;
	
	public Messages () {
		userMailBoxes = new UserMailBox[100];
		
		for (int i = 0; i < 100; i++)
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
			if ( userMailBoxes[i].getUserName().equals(name) ) {
				if (userMailBoxes[i].getConnectionStatus())
					return -2;	// Returns -2, user name is already in use
				else
					return i; // Returns user index if found
			}
		}
		
		return -1; 	// Returns -1 for user not found
	}
	
	public boolean sendMessage(MsgCommObj msg) {
		
		for (int i = 0; i < 100; i++) {
			if ( userMailBoxes[i].getUserName().equals(msg.getToUserName()) ) {
				userMailBoxes[i].addMessage(msg);
				return true;
			}
		}
		
		return false;
	}
	
	public MsgCommObj getMessage(int index) {
		if (index >= 0 && index < 100)
			return userMailBoxes[index].getMessage();
		else
			return null;
	}
	
	public void setConnectionStatus(int index, boolean status) {
		if (index >= 0 && index < 100) {
			userMailBoxes[index].setConnectionStatus(status);
		}
	}
	
	public MsgCommObj getAllUsers() {
		MsgCommObj msg = new MsgCommObj();
		
		msg.setFromUserName("Server.");
		msg.setUserOption(0);
		msg.setDateTime();
		
		StringBuilder allUsers = new StringBuilder();
		
		for (int i = 0; i < currentUserIndex; i++)
			allUsers.append(userMailBoxes[i].getUserName() + "\n");
		
		msg.setUserMsg(allUsers.toString());
		
		return msg;
	}
	
	public MsgCommObj getAllConnectedUsers() {
		MsgCommObj msg = new MsgCommObj();
		
		msg.setFromUserName("Server.");
		msg.setUserOption(0);
		msg.setDateTime();
		
		StringBuilder connectedUsers = new StringBuilder();
		
		for (int i = 0; i < currentUserIndex; i++)
			if ( userMailBoxes[i].getConnectionStatus() ) 
				connectedUsers.append(userMailBoxes[i].getUserName() + "\n");
		
		msg.setUserMsg(connectedUsers.toString());
		
		return msg;
	}
	
	public void sendToAllConnectedUsers(MsgCommObj msg) {
		
		msg.setDateTime();
		
		for (int i = 0; i < currentUserIndex; i++)
			if ( userMailBoxes[i].getConnectionStatus() ) 
				userMailBoxes[i].addMessage(msg);
	}

	public void sendToAllUsers(MsgCommObj msg) {
		
		msg.setDateTime();
		
		for (int i = 0; i < currentUserIndex; i++)
			userMailBoxes[i].addMessage(msg);
	}
}
