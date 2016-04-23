// Andrew Robinson
// Marcus Karl
// Client program that gives functionality to client threads on server; Allows clients to send and receive text messages through server

package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import Shared.MsgCommObj;

public class ClientCommThread implements Runnable {
	
	// Creates default variables
	private Socket clientSocket = null;
	private Messages msgQueues = null;
	private int userIndex = -1;
	
	// Constructor initializes clientSocket to connection from ServerMain method
	// and msgQueues to the object used for storing and retrieving messages and users
	public ClientCommThread (Messages msgQ, Socket socket) {
		clientSocket = socket;
		msgQueues = msgQ;
	}
	
	@Override
	public void run() {
		try {
			// Gets socket input/output streams and binds them to object streams
			ObjectInputStream in = new ObjectInputStream( clientSocket.getInputStream() );
			ObjectOutputStream out = new ObjectOutputStream( clientSocket.getOutputStream());
			
			// Calls descisionBranch to handle communication with client
			decisionBranch(in, out);
			
			// Closes input and output streams
			in.close();
			out.close();
			
		} catch (IOException ex) {		// Catches and prints error
			System.out.println(ex);
		} finally {
			try {
				if (clientSocket != null)	// Closes clientSocket connection
					clientSocket.close();
			} catch (IOException ex) {		// Catches and prints error
				System.out.println(ex);
			}
		}
	}
	
	public void sendStoredMessagesToUser(ObjectOutputStream out) {
		
		// Gets first message from user mailbox
		MsgCommObj msgToUser = msgQueues.getMessage(userIndex);
		
		// Loops while message from mailbox is not null, if mailbox is empty null is returned from getMessage()
		while (msgToUser != null) {
			
			// Clears any previous user options that existed from previously storing message
			msgToUser.setUserOption( 0 );
			
			try {	// Sends message to the user from mailbox
				out.writeObject(msgToUser);
			} catch (IOException ex) {		// Catches and prints error
				System.out.println(ex);
			}
			// Gets next message for user before looping back to check for null message
			msgToUser = msgQueues.getMessage(userIndex);
		}
		
		// Creates a MsgCommObj to send to user showing that mailbox is full
		MsgCommObj noMoreMessages = new MsgCommObj();
		
		// Sets user option to -1 as flag of no more messages
		noMoreMessages.setUserOption( -1 );
		
		try {	// Sends message to the user that there are no more messages
			out.writeObject(noMoreMessages);
		} catch (IOException ex) {		// Catches and prints error
			System.out.println(ex);
		}
	}
	
	public void decisionBranch(ObjectInputStream in, ObjectOutputStream out) {
		
		// Creates new variables to be used
		MsgCommObj msg = null;
		MsgCommObj reply = null;
		String userName = null;
		boolean sendWelcome = true;
		boolean exit = false;
		
		while (!exit) {
			try {
				// Gets message from client on input stream
				msg = (MsgCommObj) in.readObject();
				
				// Checks user option from client, is used to switch to correct case
				switch ( msg.getUserOption() ) {
				
					// Case 0 is user just connected
					case 0:
						// Gets user name of connected user
						userName = new String( msg.getFromUserName() );
						
						// Attempts to get user index for use in mailbox slot, if user is unknown -1 is returned
						// if user is known but currently connected, -2 is returned
						userIndex = msgQueues.getUserIndex( msg.getFromUserName() );
						
						if (userIndex == -1) {	// User is unknown
							System.out.println(LocalDateTime.now() + " Connection by unknown user: " 
									+ msg.getFromUserName() );
							
							// Attempts to add user to mailbox list
							userIndex = msgQueues.newUser(msg.getFromUserName());
							
							// If userIndex is still -1, unable to add user, will send message to client and exit
							if ( userIndex == -1 ) {
								reply = new MsgCommObj();
								reply.setToUserName(msg.getFromUserName());
								reply.setUserMsg("Server user list is full, unable to accept new users. Rejected user "
											+ msg.getFromUserName());
								reply.setUserOption( -1 );
								out.writeObject(reply);
								System.out.println(LocalDateTime.now() + " Server user list is full, rejected user " 
											+ msg.getFromUserName() + ".");
								
								// Sets exit to true and break out of case then loop
								exit = true;
								break;
							}
							else	// Else the user was added, set connection status to true
								msgQueues.setConnectionStatus(userIndex, true);
						}
						else if (userIndex == -2) {	
							// User name is already in use and user is currently connected to server
							reply = new MsgCommObj();
							reply.setToUserName(msg.getFromUserName());
							reply.setUserMsg("Username is already in use and user is connected!");
							reply.setUserOption( -2 );
							out.writeObject(reply);
							
							// Sends name already in use message to server
							msg = (MsgCommObj) in.readObject();
							
							// Sets exit to true and break out of case then loop
							exit = true;
							break;
						}
						else {	// User is known, displays message and sets currently connected to true
							System.out.println(LocalDateTime.now() + " Connection by known user: " 
									+ msg.getFromUserName() );
							msgQueues.setConnectionStatus(userIndex, true);
						}
						
						// User is ready to interact with server, sending welcome message.
						if (sendWelcome) {
							reply = new MsgCommObj();
							reply.setToUserName(msg.getFromUserName());
							reply.setUserMsg("Welcome to the message server!");
							reply.setUserOption( 0 );
							out.writeObject(reply);
							sendWelcome = false;
						}
						break;
					
					// Returns all known users to client
					case 1: 
						reply = msgQueues.getAllUsers();
						out.writeObject(reply);
						System.out.println(LocalDateTime.now() + " " + msg.getFromUserName() 
										+ " retrieved all known users.");
						break;
					
						// Returns all connected user to client
					case 2: 
						reply = msgQueues.getAllConnectedUsers();
						out.writeObject(reply);
						System.out.println(LocalDateTime.now() + " " + msg.getFromUserName() 
										+ " retrieved all connected users.");
						break;
					
						// Sends message to specific user
					case 3:
						// Checks that able to send message to specified user
						if ( msgQueues.sendMessage(msg) ) {
							// Message was sent, send reply to client informing of success
							reply = new MsgCommObj();
							reply.setToUserName(msg.getFromUserName());
							reply.setUserMsg("Message sent.");
							reply.setUserOption( 0 );
							out.writeObject(reply);
							
							// Displays server side message
							System.out.println(LocalDateTime.now() + " " + msg.getFromUserName() 
										+ " sent message to " + msg.getToUserName());
						}
						else {
							// If user was unable to be found, sends reply to client informing of failure
							reply = new MsgCommObj();
							reply.setToUserName(msg.getFromUserName());
							reply.setUserMsg("Unable to send message to user.");
							reply.setUserOption( -99 );
							out.writeObject(reply);
							
							// Displays server side message
							System.out.println(LocalDateTime.now() + " " + msg.getFromUserName() 
										+ " attempted to send message to unknown user, user list is full.");
						}
						break;
					
					// Sends message to all connected user
					case 4:
						msgQueues.sendToAllConnectedUsers(msg);
						System.out.println(LocalDateTime.now() + " " + msg.getFromUserName() 
										+ " sent message to all connected users.");
						break;
					
					// Sends message to all known users
					case 5:
						msgQueues.sendToAllUsers(msg);
						System.out.println(LocalDateTime.now() + " " + msg.getFromUserName() 
										+ " sent message to all users.");
						break;
					
					// Sends all messages in user mailbox to client
					case 6:
						sendStoredMessagesToUser(out);
						System.out.println(LocalDateTime.now() + " " + msg.getFromUserName() 
										+ " retrieved all messages.");
						break;
					
					// If 8 is received, connection is terminated, sets currently connected to false
					case 8:
						msgQueues.setConnectionStatus(userIndex, false);
						System.out.println(LocalDateTime.now() + " " + msg.getFromUserName() 
										+ " exits.");
						exit = true;
				}
			} catch (ClassNotFoundException | IOException ex) {	
				// Catches and displays errors, also sets client currently connected to false
				System.out.println(LocalDateTime.now() + " " + userName + " Error: " + ex);
				System.out.println(LocalDateTime.now() + " " + userName + " connection terminated.");
				msgQueues.setConnectionStatus(userIndex, false);
				exit = true;
			}
		}
	}
}
