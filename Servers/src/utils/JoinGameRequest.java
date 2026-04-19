package utils;

public class JoinGameRequest {
	public String game_id;
	public String username;

	public JoinGameRequest(String game_id, String username) {
		this.game_id = game_id;
		this.username = username;
	}
}
