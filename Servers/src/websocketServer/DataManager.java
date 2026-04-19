package websocketServer;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import game.Game;
import game.Player;
import utils.JSONParser;
import utils.JoinGameRequest;
import utils.LeaveGameRequest;

public class DataManager {
	private static HashMap<String, Game> games = new HashMap<>();

	private static class Message {
		public WebSocketClient recipient = null;
		public HashMap<String, Object> message = new HashMap<>();

		public Message(String type, Map<String, Object> content, WebSocketClient recipient) {
			this.recipient = recipient;
			message.put("message_type", type);
			message.put("content", content);
		}
	}

	public static Message[] createResponses(Message... msg) {
		return msg;
	}

	public static boolean processMessage(WebSocketClient sender, String msg) {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> dataMap = null;

		try {
			dataMap = (Map<String, Object>) mapper.readValue(msg, Map.class);
		} catch (JsonProcessingException e) {
			System.err.println("Invalid JSON !\n" + msg);
			return false;
		}
		String msgType = (String) dataMap.get("message_type");

		System.out.println("Message Type : " + msgType);

		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

		String content = "";
		try {
			content = ow.writeValueAsString(dataMap.get("content"));
		} catch (JsonProcessingException e) {
			// should never go here (exceptions are already catch earlier)
			e.printStackTrace();
		}
		System.out.println(content);

		Message[] responses = null;
		if (msgType.equalsIgnoreCase("CREATE_GAME")) {
			responses = handleCreateGame(sender, content);
		} else if (msgType.equalsIgnoreCase("JOIN_GAME")) {
			responses = handleJoinGame(sender, content);
		} else if (msgType.equalsIgnoreCase("LEAVE_GAME")) {
			responses = handleLeaveGame(sender, content);
		} else {
			// if the message is not recognized => fail to process message
			return false;
		}

		if (responses != null) {
			try {
				for (Message response : responses) {
					response.recipient.sendMessageTo(ow.writeValueAsString(response.message), true);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}

	private static Message[] handleCreateGame(WebSocketClient sender, String payload) {
		Game newGame = JSONParser.parseCREATE_GAME(payload);
		String type = "";
		HashMap<String, Object> content_ = new HashMap<>();
		if (newGame != null) {
			Player gm = new Player("GM", "gm");
			gm.socket = sender;
			newGame.GameMaster = gm;
			games.put(newGame.id, newGame);
			type = "CREATE_GAME";
			content_.put("game_id", newGame.id);
		} else {
			type = "ERROR";
			content_.put("reason", "Failed to parse a CREATE_GAME request");
		}
		return createResponses(new Message(type, content_, sender));
	}

	private static Message[] handleJoinGame(WebSocketClient sender, String payload) {
		JoinGameRequest request = JSONParser.parseJOIN_GAME(payload);
		if (request == null) {
			return createResponses(new Message("ERROR",
					Collections.singletonMap("reason",
							"Failed to parse a JOIN_GAME request"),
					sender));
		}

		if (!games.containsKey(request.game_id)) {
			return createResponses(new Message("ERROR", Collections.singletonMap("reason",
					"Invalid game id '" + request.game_id + "'"), sender));
		}

		Game game = games.get(request.game_id);
		for (Player p : game.players) {
			if (request.username.equals(p.username)) {
				return createResponses(new Message("ERROR", Collections.singletonMap("reason",
						"Username '" + request.username + "' is already taken"), sender));
			}
		}

		// FIXME: add player type
		Player player = new Player(request.username, "");
		player.socket = sender;
		game.players.add(player);

		HashMap<String, Object> player_joined_content = new HashMap<>();
		player_joined_content.put("id", player.id);
		player_joined_content.put("username", player.username);
		return createResponses(new Message("JOIN_GAME", Collections.singletonMap("id", player.id), sender),
				new Message("PLAYER_JOINED", player_joined_content, game.GameMaster.socket));
	}

	private static Message[] handleLeaveGame(WebSocketClient sender, String payload) {
		LeaveGameRequest request = JSONParser.parseLEAVE_GAME(payload);
		Message[] empty_list = {};
		if (request == null) {
			System.err.println("Failed to parse a LEAVE_GAME request (" + payload + ")");
			return empty_list;
		}

		if (!games.containsKey(request.game_id)) {
			System.err.println("Got an invalid game id " + request.game_id + " in a LEAVE_GAME request");
			return empty_list;
		}

		Game game = games.get(request.game_id);
		if (game.players.removeIf(p -> p.id == request.id)) {
			return createResponses(new Message("PLAYER_LEFT", Collections.singletonMap("id", request.id),
					game.GameMaster.socket));
		}

		System.err.println("Got an invalid player id " + request.id + " in a LEAVE_GAME request");
		return empty_list;
	}
}
