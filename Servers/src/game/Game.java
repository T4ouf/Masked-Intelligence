package game;

import java.util.ArrayList;
import java.util.Random;

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
	public ArrayList<Player> players = new ArrayList<>();

	public Game(int voteAmount, int AINumber, int androidMaxLimit, int hunterMaxLimit) {
		this.id = "Game#" + IDGENERATOR;
		IDGENERATOR += 1;

		this.AINumber = AINumber;
		this.androidNumber = 0;
		this.hunterNumber = 0;

		this.androidMaxLimit = androidMaxLimit;
		this.hunterMaxLimit = hunterMaxLimit;

		this.voteAmount = voteAmount;
	}

	public PlayerType getRandomRole() {
		int nAndroids = androidMaxLimit - androidNumber;
		int nHunters = hunterMaxLimit - hunterNumber;
		if (nAndroids == 0 && nHunters == 0) {
			return null;
		}

		ArrayList<PlayerType> roles = new ArrayList<>();
		for (int i = 0; i < nAndroids; i++) {
			roles.add(PlayerType.ANDROID);
		}
		for (int i = 0; i < nHunters; i++) {
			roles.add(PlayerType.HUNTER);
		}

		Random rand = new Random();
		PlayerType role = roles.get(rand.nextInt(roles.size()));
		if (role == PlayerType.ANDROID) {
			androidNumber++;
		} else {
			hunterNumber++;
		}
		return role;
	}

	public void removeRole(PlayerType role) {
		if (role == PlayerType.ANDROID) {
			androidNumber--;
		} else {
			hunterNumber--;
		}
	}
}
