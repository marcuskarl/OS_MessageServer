package Server;

import java.io.DataOutputStream;
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
		int clientComm = 0;									// Sets initial value for client to connect to
		
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
			
			// Creates ServerSocket for finding open client ports to use
			ServerSocket getOpenPortForClientThread = null;
			
			while(true) {	// Infinite loop, until error or program killed
				try {
					// Finds an open socket for any client connecting to use
					getOpenPortForClientThread = new ServerSocket(0, 1, InetAddress.getLocalHost());
					
					// Sets clientSocket to incoming connection
					clientSocket = serverSocket.accept();
					
					// Gets the local port created for the client thread, and 
					// then closes the socket in order for client to use
					clientComm = getOpenPortForClientThread.getLocalPort();
					getOpenPortForClientThread.close();
					
					// Creates client thread and passes in Message object and client port to listen for a incoming connection
					ClientCommThread clientThread = new ClientCommThread(msgQueues, clientComm);
					
					// Creates data write object for passing Strings/Int back to incoming initial connection from client
					DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
					
					// Starts the client thread
					executor.execute(clientThread);
					
					// Sends new port to connect to client thread to connected user
					out.writeInt(clientComm);
					
					out.flush();	// Flushes outgoing pipe
					out.close();	// Closes data stream
				} finally {
					clientSocket.close();	// Closes client connection
				}
			}
		} catch (IOException ex) {	// Catches exceptions and prints to screen
			System.out.println(ex);
		} finally {					// Ensures server listening socket it always closed
			serverSocket.close();
		}
	}
}
