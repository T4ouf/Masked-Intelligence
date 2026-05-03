package game;

import java.util.ArrayList;
import websocketServer.WebSocketClient;

public class Player {
	private static int IDGENERATOR;
	public int id;
	public WebSocketClient socket;

	public String username;
	public boolean isAlive;

	public PlayerType playerType;
	public RoundData currentRoundData;
	public ArrayList<RoundData> previousRoundData = new ArrayList<RoundData>();

	public Player(String choosenName, PlayerType playerType) {

		this.id = IDGENERATOR;
		IDGENERATOR += 1;

		this.isAlive = true;

		this.username = choosenName;
		this.playerType = playerType;
	}

}
