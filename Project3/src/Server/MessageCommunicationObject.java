package Server;

public class MessageCommunicationObject {
	private boolean closeConnection = false;
	
	public void setCloseConection (boolean con) {
		closeConnection = con;
	}
	
	public boolean getCloseConnection() {
		return closeConnection;
	}
}
