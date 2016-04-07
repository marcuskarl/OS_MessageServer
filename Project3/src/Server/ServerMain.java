package Server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ServerMain {
	
	public static void main(String [] args) throws IOException {
		
		int listenPortNumber = Integer.parseInt(args[0]);
		int clientComm = ++listenPortNumber;
		
		Messages msgQueues = new Messages();
		
		ServerSocket serverSocket = null;
		Socket clientSocket = null;
		
		try {
			serverSocket = new ServerSocket(listenPortNumber);
			
			ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
			
			while(clientComm < 65536) {
				try {
					clientSocket = serverSocket.accept();
					ClientCommunicationThread clientThread = new ClientCommunicationThread(msgQueues, clientComm);
					PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
					executor.execute(clientThread);
					out.write(clientComm);
					
				}
				finally {
					clientComm += 2;
					clientSocket.close();
				}
			}
		}
		catch (IOException ex) {
			System.out.println(ex);
		}
		finally {
			serverSocket.close();
		}
	}
}