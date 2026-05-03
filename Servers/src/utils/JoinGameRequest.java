package utils;

public class JoinGameRequest {
	public String gameId;
	public String username;

	public JoinGameRequest(String gameId, String username) {
		this.gameId = gameId;
		this.username = username;
	}
}
