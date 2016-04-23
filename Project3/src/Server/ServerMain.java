package Server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ServerMain {
	
	public static void main(String [] args) throws IOException {
		
		int listenPortNumber = Integer.parseInt(args[0]);	// Gets port number from input string
		Messages msgQueues = new Messages();                // Creates message object for each client thread to reference
		ServerSocket serverSocket = null;					// Creates serverSocket variable
		Socket clientSocket = null;							// Creates clientSocket variable
		
		try {
			// Sets serverSocket to given port, with a queue of 100 and to the local IP address
			serverSocket = new ServerSocket(listenPortNumber, 100, InetAddress.getLocalHost());
			
			// Prints server up message
			System.out.println(LocalDateTime.now() + " Server running on " + serverSocket.getLocalSocketAddress());
			
			// Creates thread pool for client threads
			ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
			
			while(true) {
				// Sets clientSocket to incoming connection
				clientSocket = serverSocket.accept();
				
				// Creates thread with msgQueues object and incoming client connection
				ClientCommThread clientThread = new ClientCommThread(msgQueues, clientSocket);
				
				// Starts thread for new connection to handle all task
				executor.execute(clientThread);
			}
		} catch (IOException ex) {	// Catches and prints error
			System.out.println(ex);
		} finally {
			serverSocket.close();	// Closes serverSocket connection
		}
	}
}