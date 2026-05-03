package websocketServer;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import game.Game;
import utils.JoinGameRequest;
import utils.JoinStartedGameRequest;
import utils.LeaveGameRequest;

public enum MessageType {
	ERROR("ERROR"), CREATE_GAME("CREATE_GAME"), JOIN_GAME("JOIN_GAME"),
	LEAVE_GAME("LEAVE_GAME"), PLAYER_JOINED("PLAYER_JOINED"),
	PLAYER_LEFT("PLAYER_LEFT"), CANCEL_GAME("CANCEL_GAME"),
	GAME_CANCELLED("GAME_CANCELLED"), START_GAME("START_GAME"),
	JOIN_STARTED_GAME("JOIN_STARTED_GAME");

	private static HashMap<String, MessageType> stringToMsgType = new HashMap<>();

	static {
		for (MessageType type : MessageType.values()) {
			stringToMsgType.put(type.name, type);
		}
	}

	public static MessageType fromString(String name) {
		return stringToMsgType.getOrDefault(name, null);
	}

	String name;

	private MessageType(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public Object parse(String jsonMessage) {
		ObjectMapper mapper = new ObjectMapper();

		try {
			Map<String, Object> data = (Map<String, Object>) mapper.readValue(jsonMessage, Map.class);

			switch (this) {
				case CANCEL_GAME:
					return (String) data.get("game_id");
				case CREATE_GAME:
					int aiNumber = (int) data.get("n_ai");
					int androidMaxLimit = (int) data.get("n_android");
					int hunterMaxLimit = (int) data.get("n_hunter");
					int androidTurnDuration = (int) data.get("android_turn_duration");
					int voteAmount = androidMaxLimit + 2;

					return new Game(voteAmount, aiNumber, androidMaxLimit, hunterMaxLimit);
				case JOIN_GAME:
					return new JoinGameRequest((String) data.get("game_id"),
							(String) data.get("username"));
				case JOIN_STARTED_GAME:
					return new JoinStartedGameRequest((String) data.get("game_id"),
							(int) data.get("player_id"));
				case LEAVE_GAME:
					return new LeaveGameRequest((String) data.get("game_id"), (int) data.get("id"));
				case START_GAME:
					return (String) data.get("game_id");
				default:
					System.err.println("The server cannot parse " + name + " messages");
					return null;
			}
		} catch (JsonMappingException e) {
			e.printStackTrace();
			return null;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return null;
		}
	}
}
