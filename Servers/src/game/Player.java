package game;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Player {
	private static int IDGENERATOR;
	public int id;
	
	public String username;
	public boolean isAlive;
	public boolean isBot;
	
	public PlayerType playerType;
	public RoundData currentRoundData;
	public ArrayList<RoundData> previousRoundData = new ArrayList<RoundData>();
	
	public Player(String choosenName, String playerType) {
		
		this.id = IDGENERATOR;
		IDGENERATOR += 1;
		
		this.isAlive = true;
		
		this.username = choosenName;
		
		if(playerType.toLowerCase() == "gm") {
			this.playerType = new GameMaster();
		}
		else if(playerType.toLowerCase() == "hunter") {
			this.playerType = new Hunter();
		}
		else if(playerType.toLowerCase() == "android") {
			this.playerType = new Android();
		}
		else if(playerType.toLowerCase() == "bot") {
			this.playerType = new Bot();
		}
		
	}
	
}
