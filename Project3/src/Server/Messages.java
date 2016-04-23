// Andrew Robinson
// Marcus Karl
// Client program that gives functionality to client threads on server; Allows clients to send and receive text messages through server

package Server;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import Shared.MsgCommObj;


// This class has one instance of it initialized and all thread reference the same instance
// Which allows for all threads to send messages to different users and retrieve messages from other users
// The instance of this object is passed to each thread upon creation of the thread and can have it's methods called by each thread
public class Messages {
	
	// Creates a private mailbox class for use by Messages
	private class UserMailBox {
		private LinkedList<MsgCommObj> userMessage = null;	// Creates a linked list for storing user messages
		private String userName = null;						// Stores user name
		private boolean currentlyConnected = false;			// Used for if user is currently connected, 
															// true = connected, false = not connected
		
		// Semaphore for protection of linked list during message store or retrieval operations
		private Semaphore messageAddOrGet = new Semaphore(1, true);	
		
		// Constructor initializes linked list and user name
		public UserMailBox() {
			userMessage = new LinkedList<MsgCommObj>();
			userName = new String();
		}
		
		// Returns user name
		public String getUserName() {
			return userName;
		}
		
		// Sets user name
		public void setUserName(String name) {
			userName = name;
		}
		
		// Returns the message at the head of the list, if list is empty, null is returned
		public MsgCommObj getMessage() {
			messageAddOrGet.acquire();	// Checks that no other threads are attempting to add message
			MsgCommObj msg = userMessage.poll()
			messageAddOrGet.release();	// Releases semaphore
			return msg;
		}
		
		// Adds message to user linked list (user mailbox)
		public void addMessage(MsgCommObj msg) {
			msg.setDateTime();			// Sets time stamp to server date and time
			try {
				messageAddOrGet.acquire();	// Checks that no other threads are attempting to add or removes messages
				userMessage.addLast(msg);		// Adds message to tail of list
				messageAddOrGet.release();	// Release semaphore
			} catch (InterruptedException ex) {	// Catches and prints error
				System.out.println(ex);
			}
		}
		
		// Sets currently connected status
		public void setConnectionStatus(boolean status) {
			currentlyConnected = status;
		}
		
		// Gets currently connected status
		public boolean getConnectionStatus() {
			return currentlyConnected;
		}
	}
	
	// Declares an array of user mailboxes
	private UserMailBox [] userMailBoxes = null;
	
	// Sets current known users to 0
	private int currentUserIndex = 0;
	
	// Semaphore for use in adding new users
	private Semaphore addNewUser = new Semaphore(1, true);	
	
	// Construct initializes 100 user mailboxes for use by clients/threads
	public Messages () {
		userMailBoxes = new UserMailBox[100];
		
		for (int i = 0; i < 100; i++)
			userMailBoxes[i] = new UserMailBox();
	}
	
	// Adds a new user to server mailboxes
	public int newUser(String name) {
		
		try {
			addNewUser.acquire();	// Locks section for adding new users
									// This prevents from two threads creating the same user simultaneously
		
			// Checks is user name is already in system
			for (int i = 0; i < 100; i++) {
				if ( userMailBoxes[i].getUserName().equals(name) )
					return -1; // Returns -1 if unable to add user since user already exists
			}
			
			// If at this point, user name does not exist in system, checks for an available user mailbox
			if (currentUserIndex < 100) {
				
				// Creates user mailbox
				userMailBoxes[currentUserIndex].setUserName(name);
				
				// Returns user index in system if user was added
				return currentUserIndex++;
			}
		
		} catch (InterruptedException ex) {		// Catches and prints error
				System.out.println(ex);
		} finally {
			addNewUser.release();	// Releases for other threads to add new user
		}
		
		return -1;	// Returns -1 if unable to add user
	}
	
	// Returns the user index (mailbox number) of a known user, or -1 if user not found
	public int getUserIndex(String name) {
		
		// Loops through each box to search for user
		for (int i = 0; i < 100; i++) {
			
			// Compares user name to known user name
			if ( userMailBoxes[i].getUserName().equals(name) ) {
				if (userMailBoxes[i].getConnectionStatus())
					return -2;	// Returns -2, user name is already in use and user is connected
				else
					return i; 	// Else returns user index of matching name
			}
		}
		
		return -1; 	// Returns -1 for user not found
	}
	
	// Adds message to user mailbox
	public boolean sendMessage(MsgCommObj msg) {
		
		// Searches for correct user mail to add message to
		for (int i = 0; i < 100; i++) {
			if ( userMailBoxes[i].getUserName().equals(msg.getToUserName()) ) {
				// If both user names match, message is added to mailbox
				userMailBoxes[i].addMessage(msg);
				return true;		// Returns true that message was added
			}
		}
		
		// If at this point, could not find user name as known user, adds user to list
		int addUser = newUser(msg.getToUserName());
		
		// If addUser is not -1, the new user was added to the server
		if (addUser != -1) {
			// Adds message to mailbox of new user
			userMailBoxes[addUser].addMessage(msg);
			return true;
		}
		else	// Returns false if unable to add user, which indicates all mailboxes are taken
			return false;
	}
	
	// Gets first message from mailbox
	public MsgCommObj getMessage(int index) {
		
		// Checks for array out of bounds error
		if (index >= 0 && index < 100)
			// Returns object from mailbox class
			return userMailBoxes[index].getMessage();
		else
			// Else returns null if index was out of bounds
			return null;
	}
	
	// Sets current connection status for specified user index
	public void setConnectionStatus(int index, boolean status) {
		
		// Checks for array out of bounds error
		if (index >= 0 && index < 100) {
			// Sets given connection status for specified user
			userMailBoxes[index].setConnectionStatus(status);
		}
	}
	
	// Returns all user
	public MsgCommObj getAllUsers() {
		// Creates object for returning from call
		MsgCommObj msg = new MsgCommObj();
		msg.setFromUserName("Message Server");
		msg.setUserOption(0);
		msg.setDateTime();
		
		// Creates a string builder object for adding all known users to
		StringBuilder allUsers = new StringBuilder();
		
		// Starts at beginning of known users and increments until no more users are known
		for (int i = 0; i < currentUserIndex; i++)
			// Appends current user and new line to string builder
			allUsers.append(userMailBoxes[i].getUserName() + "\n");	
		
		msg.setUserMsg(allUsers.toString());	// Sets user message to string builder object
		
		return msg;	// Returns created message
	}
	
	public MsgCommObj getAllConnectedUsers() {
		// Creates object for returning from call
		MsgCommObj msg = new MsgCommObj();
		msg.setFromUserName("Server.");
		msg.setUserOption(0);
		msg.setDateTime();
		
		// Creates a string builder object for adding all connected users to
		StringBuilder connectedUsers = new StringBuilder();
		
		// Starts at beginning of known users and increments until no more users are known
		for (int i = 0; i < currentUserIndex; i++)
			// Checks if current user is connected
			if ( userMailBoxes[i].getConnectionStatus() )
				// Appends current user and new line to string builder
				connectedUsers.append(userMailBoxes[i].getUserName() + "\n");
		
		msg.setUserMsg(connectedUsers.toString());	// Sets user message to string builder object
		
		return msg;	// Returns created message
	}
	
	// Sends message to all currently connected users
	public void sendToAllConnectedUsers(MsgCommObj msg) {
		
		// Starts at beginning of known users and increments until no more users are known
		for (int i = 0; i < currentUserIndex; i++)
			// Checks if current user is connected
			if ( userMailBoxes[i].getConnectionStatus() )
				// Sends message to connected user's mailbox
				userMailBoxes[i].addMessage(msg);
	}

	// Sends message to all known users
	public void sendToAllUsers(MsgCommObj msg) {
		
		// Starts at beginning of known users and increments until no more users are known
		for (int i = 0; i < currentUserIndex; i++)
			// Sends message to known user's mailbox
			userMailBoxes[i].addMessage(msg);
	}
}
