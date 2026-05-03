package game;

public enum PlayerType {
	GAME_MASTER("game_master"), ANDROID("android"), HUNTER("hunter"), AI("ai");

	private String name;

	private PlayerType(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
};
