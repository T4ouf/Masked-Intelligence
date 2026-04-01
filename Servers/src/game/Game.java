package game;

import java.util.ArrayList;

public class Game {

	private static int IDGENERATOR;
	public String id;
	public String gameURL;
	
	public int voteAmount;
	public int AINumber;
	public int androidNumber;
	public int hunterNumber;
	public int androidMaxLimit;
	public int hunterMaxLimit;
	
	public int androidTurnDuration; // in seconds
	
	public Player GameMaster;
	public ArrayList<Player> players;
	
	public Game(int voteAmount, int AINumber, int androidMaxLimit, int hunterMaxLimit) {
		this.id = "Game#"+IDGENERATOR;
		IDGENERATOR += 1;
		
		this.AINumber = AINumber;
		this.androidNumber = 0;
		this.hunterNumber = 0;
		
		this.androidMaxLimit = androidMaxLimit;
		this.hunterMaxLimit = hunterMaxLimit;
		
		this.voteAmount = voteAmount;
	}
	
	public boolean registerPlayer(String role) {
		
		if(role.equalsIgnoreCase("android") && androidNumber<androidMaxLimit) {
			
		}
		if(role.equalsIgnoreCase("hunter") && hunterNumber<hunterMaxLimit) {
			
		}
		
		return false;
	}
}
