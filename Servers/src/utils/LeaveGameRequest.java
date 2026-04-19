package utils;

public class LeaveGameRequest {
	public String game_id;
	public int id;

	public LeaveGameRequest(String game_id, int id) {
		this.game_id = game_id;
		this.id = id;
	}
}
