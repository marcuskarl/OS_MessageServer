package Server;

import java.util.concurrent.LinkedBlockingQueue;

public class Messages {
	
	private class MessagesForClient {
		private LinkedBlockingQueue<MessageCommunicationObject> messageQueue;
	}
		
	private Messages [] messageQueue;
	
	public Messages () {
		messageQueue = new LinkedBlockingQueue[100];
		
		for (int i = 0; i < 100; i++)
			messageQueue[i] = new LinkedBlockingQueue<MessageCommunicationObject>();
	}
	
	public LinkedBlockingQueue<MessageCommunicationObject> getMessageQueue (int i) {
		return messageQueue[i];
	}
}