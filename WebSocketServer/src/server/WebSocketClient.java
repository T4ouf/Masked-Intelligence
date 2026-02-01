package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import game.PlayerType;

public class WebSocketClient {
	private static int BUFFERSIZE = 1024;
	
	private static int IDGENERATOR = 0;
	public int id;
	
	public PlayerType clientType;
	
	private ServerSocket server;
	private Socket client;
	
	private InputStream in = null;
	private OutputStream out = null;

	public WebSocketClient(ServerSocket webSocketServer, Socket client) throws IOException {
		id = IDGENERATOR;
		IDGENERATOR += 1;
		
		server = webSocketServer;
		
		this.client = client;
		this.client.setKeepAlive(true);
		
		in = client.getInputStream();
		out = client.getOutputStream();
		
		if(this.tryHandshake()) {
			String address = "";
			for(byte b : client.getInetAddress().getAddress()) {
				address +=  (b& 0xff)+".";
			}
			address = address.substring(0, address.length()-1);
			System.out.println("A client (client#" + this.id + ") connected with address : " + address);
			
		}
	}
	
	public boolean isConnected() {
		return client.isConnected();
	}
	
	public boolean hasDataIn() throws IOException {
		return (in.available() > 0);
	}
	
	public boolean tryHandshake() {
		
		boolean res = true;
		Scanner s = new Scanner(in, "UTF-8");
		try {
			
			// In the handshake we need to upgrade the communication protocol from HTTP to Web socket
			
			String data = s.useDelimiter("\\r\\n\\r\\n").next(); // Web socket use /r/n line delimiters => format our data to that standard
			// We will first receive a GET request
			Matcher get = Pattern.compile("^GET").matcher(data);
			
			// if we receive a GET request that looks like a handshake :
			if (get.find()) {
				
				/* We check if this is indeed a websocket handshake by :
					- Obtaining the value of Sec-WebSocket-Key request header (without any leading and trailing whitespace)
			    	- Link it with "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
			    	- Compute SHA-1 and Base64 code of it
			    	- Write it back as value of Sec-WebSocket-Accept response header as part of an HTTP response.
				 */
				Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
				match.find();
				byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
									+ "Connection: Upgrade\r\n"
									+ "Upgrade: websocket\r\n"
									+ "Sec-WebSocket-Accept: "
									+ Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes("UTF-8")))
									+ "\r\n\r\n").getBytes("UTF-8");
				
				out.write(response, 0, response.length);
				client.setKeepAlive(true);
				
			}
				
        }
		catch(Exception e) {
			res = false; // if anything happens : handshake fails
		}
		finally {
			//s.close();
		}
		return res;
	}
	
	private void readNBytesInputStream(int n, byte[] data, int offset) throws IOException {
		for(int i=0; i<n; i++) {
			data[i + offset] = (byte) in.read(); // we read one byte at the time => ensure for incomplete data
		}
	}
	
	public String readMessage() throws IOException {
		String msg = "";
		
		byte[] byteData = new byte[BUFFERSIZE];
		
		int readBytes = 0;
		
		int dataSize = 0; //how many bytes of data
		int dataSizeSize = 0; // how many bytes to read the size of the data
		boolean isMasked = true; // Defines whether the "Payload data" is masked or not
		boolean maskGet = false;
		byte[] key = new byte[4]; // storing the mask key if there is one
		
		// read datasize
		readNBytesInputStream(2, byteData, 0);
		readBytes += 2;
		
		int firstByte = byteData[0] & 0xff; // complement à 2 pour avoir l'entier non signe
		if(firstByte != 129) {
			System.err.println("Unsupported message type ! 1st Byte value should be 129. Byte value : " + firstByte);
		}
		
		int secondByte = byteData[1] & 0xff; // complement à 2 pour avoir l'entier non signe
		int diff = -1; // the 7 bytes following the mask bit
		if(secondByte < 128) {
			isMasked = false; // => mask bit is 0
			diff = secondByte;
		}
		else {
			isMasked = true; // => mask bit is 1
			diff = secondByte - 128;
		}
	
		if(diff <= 125) {
			dataSizeSize = 1; // 1 byte for data size
			dataSize = diff;
			System.out.println("One Byte datasize : " + dataSize);
		}
		else if(diff == 126) {
			dataSizeSize = 2; // 2 bytes for data size
			
			readNBytesInputStream(2, byteData, readBytes);
			readBytes += 2;
			
			int byte3 = byteData[2] & 0xff; // complement à 2 pour avoir l'entier non signe	
			int byte4 = byteData[3] & 0xff; // complement à 2 pour avoir l'entier non signe		
			
			System.out.println("Byte 3 " + byte3 + "\tByte4 " + byte4);
			
			dataSize = (int) (byte3 << 8 | byte4); // concatenate the bytes to get the data size

			System.out.println("2 Bytes datasize : " + dataSize);
		}
		else if(diff == 127) {
			System.err.println("Unsupported data size ! (Data size is 8 bytes long)");
//			dataSizeSize = 8; // 8 bytes for data size
//			int byte3  = encoded[2] & 0xff; // complement à 2 pour avoir l'entier non signe	
//			int byte4  = encoded[3] & 0xff; // complement à 2 pour avoir l'entier non signe
//			int byte5  = encoded[4] & 0xff; // complement à 2 pour avoir l'entier non signe	
//			int byte6  = encoded[5] & 0xff; // complement à 2 pour avoir l'entier non signe
//			int byte7  = encoded[6] & 0xff; // complement à 2 pour avoir l'entier non signe	
//			int byte8  = encoded[7] & 0xff; // complement à 2 pour avoir l'entier non signe
//			int byte9  = encoded[8] & 0xff; // complement à 2 pour avoir l'entier non signe	
//			int byte10 = encoded[9] & 0xff; // complement à 2 pour avoir l'entier non signe
//			// concatenate the bytes to get the data size
//			dataSize = (int) ((byte3 << 8 | byte4) << 8 | byte5); //TODO
//			i = 9; // move to data bytes
		}
		else {
			System.err.println("Invalid data size !");
		}
		
		// if there is a mask and we haven't read it yet
		if(isMasked && !maskGet) {
			readNBytesInputStream(4, byteData, readBytes);
			key[0] = (byte)byteData[readBytes];
			key[1] = (byte)byteData[readBytes+1];
			key[2] = (byte)byteData[readBytes+2];
			key[3] = (byte)byteData[readBytes+3];
			readBytes += 4;
			maskGet = true; // we're done with the mask data
		}
		
		readNBytesInputStream(dataSize, byteData, readBytes);
		
		System.out.println("-----\nBinary : ");
		
		for(byte b : byteData)
			System.out.print((b& 0xff) + " ");
		
		System.out.println("\n-----");
			
		
		assert(readBytes == (1+dataSizeSize+(isMasked?4:0))); // sanity check
		
		byte[] decoded = new byte[BUFFERSIZE];
		for (int j = 0; j < dataSize; j++) {
			decoded[j] = (byte) (byteData[readBytes+j] ^ key[j & 0x3]);
		}
		msg = new String(decoded);
		
		return msg;
	}

}
