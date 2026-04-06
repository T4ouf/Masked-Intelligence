package websocketServer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import game.Game;
import utils.JSONParser;

public class DataManager {

	public static HashMap<String, Object> createMessage(String type, HashMap<String, Object> content) {
		HashMap<String, Object> message = new HashMap<>();
		message.put("message_type", type);
		message.put("content", content);
		return message;
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

		if (msgType.equalsIgnoreCase("CREATE_GAME")) {
			Game newGame = JSONParser.parseCREATE_GAME(content);
			String type = "";
			HashMap<String, Object> content_ = new HashMap<>();
			if (newGame != null) {
				type = "CREATE_GAME";
				content_.put("game_id", newGame.id);
			} else {
				type = "ERROR";
				content_.put("reason", "Failed to parse a CREATE_GAME request");
			}
			HashMap<String, Object> response = createMessage(type, content_);
			try {
				sender.sendMessageTo(ow.writeValueAsString(response), true);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}

		// if the message is not recognized => fail to process message
		return false;
	}

}
