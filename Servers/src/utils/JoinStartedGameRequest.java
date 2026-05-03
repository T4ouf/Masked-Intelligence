package utils;

public class JoinStartedGameRequest {
	public String gameId;
	public int playerId;

	public JoinStartedGameRequest(String gameId, int playerId) {
		this.gameId = gameId;
		this.playerId = playerId;
	}
}
