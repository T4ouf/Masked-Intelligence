package websocketServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import game.Game;
import game.Player;
import game.PlayerType;
import utils.JoinGameRequest;
import utils.JoinStartedGameRequest;
import utils.LeaveGameRequest;

public class DataManager {
	private static HashMap<String, Game> games = new HashMap<>();

	private static class Message {
		public WebSocketClient recipient = null;
		public HashMap<String, Object> message = new HashMap<>();

		public <T> Message(String type, T content, WebSocketClient recipient) {
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
		MessageType msgType = MessageType.fromString((String) dataMap.get("message_type"));
		if (msgType == null) {
			System.err.println("Got an unknown message type " + (String) dataMap.get("message_type"));
			return false;
		}

		System.out.println("Message Type : " + msgType);

		// FIXME: we don't want to pretty-print the JSON when sending it over the
		// network
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

		Object content = null;
		try {
			String stringContent = ow.writeValueAsString(dataMap.get("content"));
			content = msgType.parse(stringContent);
		} catch (JsonProcessingException e) {
			// should never go here (exceptions are already catch earlier)
			e.printStackTrace();
		}
		System.out.println(content);

		Message[] responses = null;
		switch (msgType) {
			case CANCEL_GAME:
				responses = handleCancelGame(sender, (String) content);
				break;
			case CREATE_GAME:
				responses = handleCreateGame(sender, (Game) content);
				break;
			case JOIN_GAME:
				responses = handleJoinGame(sender, (JoinGameRequest) content);
				break;
			case JOIN_STARTED_GAME:
				responses = handleJoinStartedGame(sender, (JoinStartedGameRequest) content);
				break;
			case LEAVE_GAME:
				responses = handleLeaveGame(sender, (LeaveGameRequest) content);
				break;
			case START_GAME:
				responses = handleStartGame(sender, (String) content);
				break;
			default:
				return false;

		}

		if (responses == null) {
			return true;
		}

		try {
			for (Message response : responses) {
				response.recipient.sendMessageTo(ow.writeValueAsString(response.message), true);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	private static Message[] handleCreateGame(WebSocketClient sender, Game newGame) {
		if (newGame == null) {
			return createResponses(new Message("ERROR",
					Collections.singletonMap("reason", "Failed to parse a CREATE_GAME request"),
					sender));
		}

		Player gm = new Player("GM", PlayerType.GAME_MASTER);
		gm.socket = sender;
		newGame.GameMaster = gm;
		games.put(newGame.id, newGame);
		return createResponses(
				new Message("CREATE_GAME", Collections.singletonMap("game_id", newGame.id), sender));
	}

	private static Message[] handleJoinGame(WebSocketClient sender, JoinGameRequest request) {
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

		Player player = new Player(request.username, game.getRandomRole());
		player.socket = sender;
		game.players.add(player);

		HashMap<String, Object> player_joined_content = new HashMap<>();
		player_joined_content.put("id", player.id);
		player_joined_content.put("username", player.username);
		return createResponses(new Message("JOIN_GAME", Collections.singletonMap("id", player.id), sender),
				new Message("PLAYER_JOINED", player_joined_content, game.GameMaster.socket));
	}

	private static Message[] handleLeaveGame(WebSocketClient sender, LeaveGameRequest request) {
		if (request == null) {
			return null;
		}

		if (!games.containsKey(request.game_id)) {
			System.err.println("Got an invalid game id " + request.game_id + " in a LEAVE_GAME request");
			return null;
		}

		Game game = games.get(request.game_id);
		for (int i = 0; i < game.players.size(); i++) {
			Player p = game.players.get(i);
			if (p.id == request.id) {
				game.players.remove(p);
				game.removeRole(p.playerType);
				return createResponses(
						new Message("PLAYER_LEFT", Collections.singletonMap("id", request.id),
								game.GameMaster.socket));
			}

		}

		System.err.println("Got an invalid player id " + request.id + " in a LEAVE_GAME request");
		return null;
	}

	private static Message[] handleCancelGame(WebSocketClient sender, String game_id) {
		if (game_id == null) {
			return null;
		}

		if (!games.containsKey(game_id)) {
			System.err.println("Got an invalid game id " + game_id + " in a CANCEL_GAME request");
			return null;
		}

		Game game = games.remove(game_id);
		Message[] responses = new Message[game.players.size()];
		for (int i = 0; i < responses.length; i++) {
			responses[i] = new Message("GAME_CANCELLED", Collections.emptyMap(),
					game.players.get(i).socket);
		}
		return responses;
	}

	private static Message[] handleStartGame(WebSocketClient sender, String game_id) {
		if (game_id == null) {
			return null;
		}

		if (!games.containsKey(game_id)) {
			System.err.println("Got an invalid game id " + game_id + " in a START_GAME request");
			return null;
		}

		Game game = games.get(game_id);
		game.GameMaster.socket = sender;
		Message[] responses = new Message[game.players.size()];
		for (int i = 0; i < game.players.size(); i++) {
			Player player = game.players.get(i);
			responses[i] = new Message("START_GAME",
					Collections.singletonMap("role", player.playerType.toString()),
					player.socket);
			player.socket = null;

		}

		return responses;
	}

	private static Message[] handleJoinStartedGame(WebSocketClient sender, JoinStartedGameRequest request) {
		if (request == null) {
			return null;
		}

		if (!games.containsKey(request.game_id)) {
			System.err.println("Got an invalid game id " + request.game_id
					+ " in a JOIN_STARTED_GAME request");
			return null;
		}

		Game game = games.get(request.game_id);

		for (int i = 0; i < game.players.size(); i++) {
			Player player = game.players.get(i);
			if (player.id != request.player_id) {
				continue;
			}

			if (player.socket != null) {
				return createResponses(new Message("ERROR",
						Collections.singletonMap("reason",
								"A player with id " + request.player_id
										+ " has already joined the game "
										+ request.game_id),
						sender));
			}

			player.socket = sender;
			Message player_response = new Message("JOIN_STARTED_GAME", Collections.emptyMap(), sender);
			ArrayList<HashMap<String, Object>> player_infos = new ArrayList<>();
			for (Player p : game.players) {
				// Not all players have joined, we don't send a notification to the GM yet
				if (p.socket == null) {
					return createResponses(player_response);
				}

				HashMap<String, Object> player_info = new HashMap<>();
				player_info.put("username", player.username);
				player_info.put("role", player.playerType.toString());
				player_infos.add(player_info);
			}

			// FIXME: The implementation in WebSocketClient.java doesn't support messages
			// longer than 126 bytes
			// Sending the player infos goes beyond that limit.
			player_infos.clear();

			// if we reach this point, all players are connected, we can send a START_GAME
			// response to the GM
			return createResponses(player_response,
					new Message("START_GAME", player_infos, game.GameMaster.socket));
		}

		return createResponses(new Message("ERROR",
				Collections.singletonMap("reason",
						"No player with id " + request.player_id
								+ " exists in the game " + request.game_id),
				sender));
	}
}
