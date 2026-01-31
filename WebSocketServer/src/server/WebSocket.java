package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WebSocket {

	public static String HOST = "127.0.0.1";
	public static int PORT = 9000;
	

	public static int BUFFERSIZE = 1024;
	
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
		
		ServerSocket server = new ServerSocket(PORT); // Open a web socket server on port 80
		
		
//
//		Scanner sc = new Scanner(System.in);
//		sc.next();
		
		try {
			System.out.println("Server has started on " + HOST + ":" + PORT +".\r\nWaiting for a connection…");
//			System.out.println("Press any key + return to stop the server");
			
			while(true) {
				Socket client = server.accept();
				System.out.println("A client connected.");
				
				// prepare communication with clients
				// TODO classe client avec chacun ses stream + tableau des clients
				InputStream in = client.getInputStream();
				OutputStream out = client.getOutputStream();
				Scanner s = new Scanner(in, "UTF-8");
				
				// Mananging handshake (first connection)
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
						
			        }
					
					byte[] byteData = new byte[BUFFERSIZE];
					in.read(byteData);
					
					for(byte b : byteData)
						System.out.print((b& 0xff) + " ");
					
					
					int dataSize = 0; //how many bytes of data
					int dataSizeSize = 0; // how many bytes to read the size of the data
					boolean isMasked = true; // Defines whether the "Payload data" is masked or not
					boolean maskGet = false;
					byte[] key = new byte[4]; // storing the mask key if there is one
						
					int i=0;
					for(i=0; i<BUFFERSIZE; i++) {
						// data frame type
						if(i==0) {
							int firstByte = byteData[0] & 0xff; // complement à 2 pour avoir l'entier non signe
							if(firstByte != 129) {
								System.err.println("Unsupported message type ! 1st Byte value should be 129. Byte value : " + firstByte);
								break;
							}
						}
						// mask bit and payload length
						else if(i==1) {
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
								i = i++;
							}
							else if(diff == 126) {
								dataSizeSize = 2; // 2 bytes for data size
								int byte3 = byteData[2] & 0xff; // complement à 2 pour avoir l'entier non signe	
								int byte4 = byteData[3] & 0xff; // complement à 2 pour avoir l'entier non signe									
								dataSize = (int) (byte3 << 8 | byte4); // concatenate the bytes to get the data size
								i = i + 2; // move to data bytes
							}
							else if(diff == 127) {
								System.err.println("Unsupported data size ! (Data size is 8 bytes long)");
								break;
//								dataSizeSize = 8; // 8 bytes for data size
//								int byte3  = encoded[2] & 0xff; // complement à 2 pour avoir l'entier non signe	
//								int byte4  = encoded[3] & 0xff; // complement à 2 pour avoir l'entier non signe
//								int byte5  = encoded[4] & 0xff; // complement à 2 pour avoir l'entier non signe	
//								int byte6  = encoded[5] & 0xff; // complement à 2 pour avoir l'entier non signe
//								int byte7  = encoded[6] & 0xff; // complement à 2 pour avoir l'entier non signe	
//								int byte8  = encoded[7] & 0xff; // complement à 2 pour avoir l'entier non signe
//								int byte9  = encoded[8] & 0xff; // complement à 2 pour avoir l'entier non signe	
//								int byte10 = encoded[9] & 0xff; // complement à 2 pour avoir l'entier non signe
//								// concatenate the bytes to get the data size
//								dataSize = (int) ((byte3 << 8 | byte4) << 8 | byte5); //TODO
//								i = 9; // move to data bytes
							}
							else {
								System.err.println("Invalid data size !");
								break;
							}
							System.out.println("taille : " + dataSize);
							
						}
						else {
							// if there is a mask and we haven't read it yet
							if(isMasked && !maskGet) {
								key[0] = (byte)byteData[i];
								key[1] = (byte)byteData[i+1];
								key[2] = (byte)byteData[i+2];
								key[3] = (byte)byteData[i+3];
								i=i+4;
								maskGet = true; // we're done with the mask data
							}
							
							break;
						}
						
					}
					byte[] decoded = new byte[BUFFERSIZE];
					for (int j = 0; j < dataSize; j++) {
						decoded[j] = (byte) (byteData[i+j] ^ key[j & 0x3]);
					}
					
					System.out.print(new String(decoded));
				}
				finally {
//					s.close();
				}
			}
		} 
		finally {
			server.close();
		}
	}
}
