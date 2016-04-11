package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;

import Shared.MsgCommObj;

public class ClientCommThread implements Runnable {
	
	private int clientCommPort;
	private Messages msgQueues = null;
	private int userIndex = -1;
	
	public ClientCommThread (Messages msgQ, int clientPort) {
		clientCommPort = clientPort;
		msgQueues = msgQ;
	}
	
	@Override
	public void run() {
		ServerSocket listenSocket = null;
		Socket clientSocket = null;
		
		try {
			listenSocket = new ServerSocket(clientCommPort, 1, InetAddress.getLocalHost());
			clientSocket = listenSocket.accept();
			
			ObjectInputStream in = new ObjectInputStream( clientSocket.getInputStream() );
			ObjectOutputStream out = new ObjectOutputStream( clientSocket.getOutputStream());
			
			// Loops until client sends exit command
			while (decisionBranch(in, out));
			
		} catch (IOException ex) {
			System.out.println(ex);
		} finally {
			try {
				if (clientSocket != null)
					clientSocket.close();
				
				if (listenSocket != null)
					listenSocket.close();
			} catch (IOException ex) {
				System.out.println(ex);
			}
		}
	}
	
	public void sendStoredMessagesToUser(ObjectOutputStream out) {
		
		MsgCommObj msgToUser = msgQueues.getMessage(userIndex);
		
		while (msgToUser != null) {
			
			// Clears any previous user options from storing the message
			msgToUser.setUserOption( 0 );
			
			try {
				out.writeObject(msgToUser);
			} catch (IOException ex) {
				System.out.println(ex);
			}
			msgToUser = msgQueues.getMessage(userIndex);
		}
		
		MsgCommObj noMoreMessages = new MsgCommObj();
		
		// Sets user option to -1 as flag of no more messages
		noMoreMessages.setUserOption( -1 );
		
		try {
			out.writeObject(noMoreMessages);
		} catch (IOException ex) {
			System.out.println(ex);
		}
	}
	
	public boolean sendMessageToAnotherUser(MsgCommObj msg) {
		
		return msgQueues.sendMessage(msg);
	}
	
	public boolean decisionBranch(ObjectInputStream in, ObjectOutputStream out) {
		
		MsgCommObj msg = null;
		String userName = null;
		
		try {
			MsgCommObj reply = null;
			msg = (MsgCommObj) in.readObject();
			
			switch ( msg.getUserOption() ) {
			
			case 0:
				// User connected
				userName = new String( msg.getFromUserName() );
				System.out.println(LocalDateTime.now() + " " + msg.getFromUserName() + " connected.");
				
				if (userIndex == -1) {
					userIndex = msgQueues.getUserIndex( msg.getFromUserName() );
					msgQueues.setConnectionStatus(userIndex, true);
				}
				
				// User name doesn't exist, adding new user
				if ( userIndex == -1 ) {
					userIndex = msgQueues.newUser(msg.getFromUserName());
					msgQueues.setConnectionStatus(userIndex, true);
					
					// If userIndex is still -1, unable to add user, will send message to client and exit
					if ( userIndex == -1 ) {
						reply = new MsgCommObj();
						reply.setToUserName(msg.getFromUserName());
						reply.setUserMsg("Server user list is full, unable to accept new users. Rejected");
						reply.setUserOption( -1 );
						out.writeObject(reply);
						System.out.println(LocalDateTime.now() + " " + "Server user list is full, rejected user " 
									+ msg.getFromUserName() + ".");
						return false;
					}
				}
				break;
				
			case 1: 
				reply = msgQueues.getAllUsers();
				out.writeObject(reply);
				System.out.println(LocalDateTime.now() + " " + msg.getFromUserName() 
								+ " retrieved all known users.");
				break;
			
			case 2: 
				reply = msgQueues.getAllConnectedUsers();
				out.writeObject(reply);
				System.out.println(LocalDateTime.now() + " " + msg.getFromUserName() 
								+ " retrieved all connected users.");
				break;
			
			case 3:
				if ( sendMessageToAnotherUser(msg) ) {
					// Message was sent
					reply = new MsgCommObj();
					reply.setToUserName(msg.getFromUserName());
					reply.setUserMsg("Message sent.");
					reply.setUserOption( 0 );
					out.writeObject(reply);
					System.out.println(LocalDateTime.now() + " " + msg.getFromUserName() 
								+ " sent message to " + msg.getToUserName());
				}
				else {
					// If user was not found 
					reply = new MsgCommObj();
					reply.setToUserName(msg.getFromUserName());
					reply.setUserMsg("User does not exist.");
					reply.setUserOption( -99 );
					out.writeObject(reply);
					System.out.println(LocalDateTime.now() + " " + msg.getFromUserName() 
								+ " attempted to send message to unknown user " + msg.getToUserName());
				}
				break;
			
			case 4:
				msgQueues.sendToAllConnectedUsers(msg);
				System.out.println(LocalDateTime.now() + " " + msg.getFromUserName() 
								+ " sent message to all connected users.");
				break;
			
			case 5:
				msgQueues.sendToAllUsers(msg);
				System.out.println(LocalDateTime.now() + " " + msg.getFromUserName() 
								+ " sent message to all users.");
				break;
			
			case 6:
				sendStoredMessagesToUser(out);
				System.out.println(LocalDateTime.now() + " " + msg.getFromUserName() 
								+ " retrieved all messages.");
				break;
			
			case 8: // If 8 is received, connection is terminated
				msgQueues.setConnectionStatus(userIndex, false);
				System.out.println(LocalDateTime.now() + " " + msg.getFromUserName() 
								+ " disconnected.");
				return false;
			default:
				System.out.println(LocalDateTime.now() + " " + msg.getFromUserName() 
								+ " unknown command, user sent option: " + msg.getUserOption());
			}
			
		} catch (ClassNotFoundException | IOException ex) {
			System.out.println(userName + " Error: " + ex);
			msgQueues.setConnectionStatus(userIndex, false);
			return false;
		}

		return true;
	}
}
