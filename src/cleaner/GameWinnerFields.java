package cleaner;

import java.util.Date;

public class GameWinnerFields {
	private static GameWinnerFields instance;
	static GameWinnerFields getInstance() {
		if(instance == null)
			instance = new GameWinnerFields();
		return instance;
	}
	
	
	private String gameID = null;
	private String teamName = null;
	private String eventName = null;
	private Date endDate = null;
	
	public String getGameID() {return gameID;}
	public String getTeamName() {return teamName;}
	public String getEventName() {return eventName;}
	public Date getEndDate() {return endDate;}
	
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public void setGameID(String gameID) {
		this.gameID = gameID;
	}
	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	
	
	
}
