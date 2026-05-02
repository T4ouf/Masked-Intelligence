package utils;

public class JoinStartedGameRequest {
	public String game_id;
	public int player_id;

	public JoinStartedGameRequest(String game_id, int player_id) {
		this.game_id = game_id;
		this.player_id = player_id;
	}
}
