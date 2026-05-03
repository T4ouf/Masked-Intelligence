package utils;

public class LeaveGameRequest {
	public String gameId;
	public int id;

	public LeaveGameRequest(String gameId, int id) {
		this.gameId = gameId;
		this.id = id;
	}
}
