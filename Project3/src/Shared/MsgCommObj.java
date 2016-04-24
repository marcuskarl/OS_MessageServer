// Andrew Robinson
// Marcus Karl

package Shared;

import java.io.Serializable;
import java.time.LocalDateTime;

public class MsgCommObj implements Serializable {
	
	// serialVersionUID is used as version control for serialization of the class
	private static final long serialVersionUID = 01L;
	
	// These five variables are used to send a message back and forth from client to server
	private String toUserName = new String();
	private String fromUserName = new String();
	private String userMsg = new String();
	private LocalDateTime dateAndTime = null;
	private int userOption = -1;				// Used for selected server side options, 
												// server also uses for sending error messages
	
	// Sets the to user name
	public void setToUserName(String name) {
		toUserName = name;
	}
	
	// Gets the to user name
	public String getToUserName() {
		return toUserName;
	}
	
	// Sets the from user name
	public void setFromUserName(String name) {
		fromUserName = name;
	}
	
	// Gets the from user name
	public String getFromUserName() {
		return fromUserName;
	}
	
	// Sets the user message
	public void setUserMsg(String name) {
		userMsg = name;
	}
	
	// Gets the user message
	public String getUserMsg() {
		return userMsg;
	}
	
	// Used to time stamp the message
	public void setDateTime() {
		dateAndTime = LocalDateTime.now();
	}
	
	// Gets the time stamp from message
	public LocalDateTime getDateTime() {
		return dateAndTime;
	}
	
	// Sets the selected option
	public void setUserOption(int option) {
		userOption = option;
	}
	
	// Gets the selected option
	public int getUserOption() {
		return userOption;
	}
}