package utils;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

import game.Game;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
//jackson
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public abstract class JSONParser {

	public static Game parseCREATE_GAME(String msg){
		
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> dataMap = null;
		
		try {
			dataMap = (Map<String, Object>) mapper.readValue(msg, Map.class);

			int aiNumber = (int) dataMap.get("n_ai");
			int androidMaxLimit = (int) dataMap.get("n_android");
			int hunterMaxLimit = (int) dataMap.get("n_hunter");
			int androidTurnDuration = (int) dataMap.get("android_turn_duration");
			int voteAmount = androidMaxLimit +2;
			

			return new Game(voteAmount, aiNumber, androidMaxLimit, hunterMaxLimit);
			
		} catch (JsonMappingException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		
	}
	public static void main(String[] args) throws JsonMappingException, JsonProcessingException {
		
		String msg = "{\"message_type\":\"CREATE_GAME\",\"content\":{\"android_turn_duration\":120,\"hunter_turn_duration\":120,\"n_ai\":1,\"n_android\":1,\"n_hunter\":1}}";
		
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> dataMap = null;
		
		dataMap = (Map<String, Object>) mapper.readValue(msg, Map.class);
		String msgType = (String) dataMap.get("message_type");
		
		System.out.println("Message Type : " + msgType);
		System.out.println(msgType.equalsIgnoreCase("CREATE_GAME"));
		
		if(msgType.equalsIgnoreCase("CREATE_GAME")) {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			
			String content = ow.writeValueAsString(dataMap.get("content"));
			System.out.println(content);
			
			assert(JSONParser.parseCREATE_GAME(content) != null);
		}
		
		
		
	}
	
}
