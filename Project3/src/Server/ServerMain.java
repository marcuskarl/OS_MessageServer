package Server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
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
			serverSocket = new ServerSocket(listenPortNumber);
			
			ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
			
			
			ServerSocket getOpenPortForClientThread = null;
			
			while(true) {
				try {
					// Finds an open socket for any client connecting to use
					getOpenPortForClientThread = new ServerSocket( 0 );
					
					clientSocket = serverSocket.accept();
					
					// Gets the local port created for the client thread, and then closes the socket in order for client to use
					clientComm = getOpenPortForClientThread.getLocalPort();
					getOpenPortForClientThread.close();
					
					ClientCommThread clientThread = new ClientCommThread(msgQueues, clientComm);
					ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
					executor.execute(clientThread);
					out.writeInt(clientComm);
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