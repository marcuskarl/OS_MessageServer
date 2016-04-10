package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientCommThread implements Runnable {
	
	private int clientCommPort;
	private Messages msgQueues = null;
	
	public ClientCommThread (Messages msgQ, int clientPort) {
		clientCommPort = clientPort;
		msgQueues = msgQ;
	}
	
	@Override
	public void run() {
		ServerSocket listenSocket = null;
		Socket clientSocket = null;
		int userIndex = -1;
		
		try {
			listenSocket = new ServerSocket(clientCommPort);
			clientSocket = listenSocket.accept();
			
			ObjectInputStream in = new ObjectInputStream( clientSocket.getInputStream() );
			ObjectOutputStream out = new ObjectOutputStream( clientSocket.getOutputStream()) ;
			
			// Loops until client sends exit command
			while (decisionBranch(userIndex, in, out)) ;
			
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
	
	public void sendStoredMessagesToUser(int userIndex, ObjectOutputStream out) {
		
		MsgCommObj msg = msgQueues.getMessage(userIndex);
		
		while (msg != null) {
			
			// Clears any previous user options from storing the message
			msg.setUserOption( 0 );
			
			try {
				out.writeObject(msg);
			} catch (IOException ex) {
				System.out.println(ex);
			}
			msg = msgQueues.getMessage(userIndex);
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
	
	public boolean decisionBranch(int userIndex, ObjectInputStream in, ObjectOutputStream out) {
		
		MsgCommObj msg = null;
		
		try {
			msg = (MsgCommObj) in.readObject();
			
			if (userIndex == -1)
				userIndex = msgQueues.getUserIndex( msg.getFromUserName() );
						
			// Sends any stored messages to the user, if record exist
			if (userIndex != -1)
				sendStoredMessagesToUser(userIndex, out);
			
			// User name doesn't exist, adding new user
			if ( userIndex == -1 ) {
				userIndex = msgQueues.newUser(msg.getFromUserName());
				
				// If userIndex is still -1, unable to add user, will send message to client and exit
				if ( userIndex == -1 ) {
					MsgCommObj reply = new MsgCommObj();
					reply.setToUserName(msg.getFromUserName());
					reply.setUserMsg("Server user list is full, unable to accept new users.");
					reply.setUserOption( -99 );
					out.writeObject(reply);
					return false;
				}
			}
			
			switch ( msg.getUserOption() ) {
			case 1: sendStoredMessagesToUser(userIndex, out);
				break;
			case 2: if ( sendMessageToAnotherUser(msg) ) {
				// Message was sent
				MsgCommObj reply = new MsgCommObj();
				reply.setToUserName(msg.getFromUserName());
				reply.setUserMsg("Message sent.");
				reply.setUserOption( 98 );
				out.writeObject(reply);
			}
			else {
				// If user was not found 
				MsgCommObj reply = new MsgCommObj();
				reply.setToUserName(msg.getFromUserName());
				reply.setUserMsg("User does not exist.");
				reply.setUserOption( -98 );
				out.writeObject(reply);
			}				
				break;
			case -1: // If -1 is received, connection is terminated
				return false;
			}
		} catch (ClassNotFoundException | IOException ex) {
			System.out.println(ex);
		}
		
		return true;
	}
}
