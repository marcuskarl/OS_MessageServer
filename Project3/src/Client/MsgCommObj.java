package Client;

import java.time.LocalDateTime;

public class MsgCommObj {
	
	private String toUserName = new String();
	private String fromUserName = new String();
	private String userMsg = new String();
	private LocalDateTime dateAndTime = null;
	private int userOption = 0;
	
	public void setToUserName(String name) {
		toUserName = name;
	}
	
	public String getToUserName() {
		return toUserName;
	}
	
	public void setFromUserName(String name) {
		fromUserName = name;
	}
	
	public String getFromUserName() {
		return fromUserName;
	}
	
	public void setUserMsg(String name) {
		userMsg = name;
	}
	
	public String getUserMsg() {
		return userMsg;
	}
	
	public void setDateTime() {
		dateAndTime = LocalDateTime.now();
	}
	
	public LocalDateTime getDateTime() {
		return dateAndTime;
	}
	
	public void setUserOption(int option) {
		userOption = option;
	}
	
	public int getUserOption() {
		return userOption;
	}
}
