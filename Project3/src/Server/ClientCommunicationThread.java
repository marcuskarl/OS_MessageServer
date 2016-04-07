package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ClientCommunicationThread implements Runnable {
	
	private int clientCommPort;
	Messages msgQueues = null;
	
	public ClientCommunicationThread (Messages msgQ, int clientPort) {
		clientCommPort = clientPort;
		msgQueues = msgQ;
	}
	
	@Override
	public void run() {
		ServerSocket listenSocket = null;
		Socket clientSocket = null;
		
		Thread sendToClient = null;
		
		MessageCommunicationObject msg = null;
		
		try {
			listenSocket = new ServerSocket(clientCommPort);
			
			ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
			clientSocket = listenSocket.accept();
			
			try {
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				
				String inputLine, outputLine;

				while ((inputLine = in.readLine()) != null) {
					System.out.println(outputLine);
					if (outputLine.equals("!EXIT!"))
						break;
				}
			}
			finally {
				clientSocket.close();
			}
		}
		catch (IOException ex) {
			System.out.println(ex);
		}
		finally {
			listenSocket.close();
		}
		
	}

}
