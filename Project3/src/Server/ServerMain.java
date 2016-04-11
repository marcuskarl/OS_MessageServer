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
		
		int listenPortNumber = Integer.parseInt(args[0]);
		int clientComm = 0;
		
		Messages msgQueues = new Messages();
		
		ServerSocket serverSocket = null;
		Socket clientSocket = null;
		
		try {
			serverSocket = new ServerSocket(listenPortNumber, 100, InetAddress.getLocalHost());
			
			System.out.println(LocalDateTime.now() + "Server running on " + serverSocket.getLocalSocketAddress());
			
			ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
			
			ServerSocket getOpenPortForClientThread = null;
			
			while(true) {
				try {
					// Finds an open socket for any client connecting to use
					getOpenPortForClientThread = new ServerSocket(0, 1, InetAddress.getLocalHost());
					
					clientSocket = serverSocket.accept();
					
					// Gets the local port created for the client thread, and then closes the socket in order for client to use
					clientComm = getOpenPortForClientThread.getLocalPort();
					getOpenPortForClientThread.close();
					
					ClientCommThread clientThread = new ClientCommThread(msgQueues, clientComm);
					DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
					executor.execute(clientThread);
					out.writeInt(clientComm);
					
					out.flush();
					out.close();
				} finally {
					clientSocket.close();
				}
			}
		} catch (IOException ex) {
			System.out.println(ex);
		} finally {
			serverSocket.close();
		}
	}
}