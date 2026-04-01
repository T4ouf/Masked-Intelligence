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

	public static boolean processMessage(WebSocketClient sender, String msg) {
		

		HashMap<String, String> response = new HashMap<>();
		
		
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
		
		if(msgType.equalsIgnoreCase("CREATE_GAME")) {
			Game newGame = JSONParser.parseCREATE_GAME(content);
			response.put("status", "OK");
			response.put("GameID", newGame.id);
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
