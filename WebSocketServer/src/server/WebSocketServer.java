package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WebSocketServer {

	public static String HOST = "127.0.0.1";
	public static int PORT = 9000;
	
	private static volatile ArrayList<WebSocketClient> clients = new ArrayList<>(); 
	
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

		System.out.println("Server has started on " + HOST + ":" + PORT +".\r\nWaiting for a connection…");
//			System.out.println("Press any key + return to stop the server");
		
		// Thread listening to clients logging in
		Thread clientReceptionThread = new Thread() {
			public void run() {
				ServerSocket server = null;
				try {
					server = new ServerSocket(PORT); // Open a web socket server 
					while(true) {
						Socket socketClient = server.accept();
						WebSocketClient newClient = new WebSocketClient(server, socketClient);
						
						if(newClient.isConnected()) {
							// do something ?
						}
						else {
							System.err.println("Invalid websocket connection try");
						}
						
						clients.add(newClient);
					}
				} catch (IOException e) {
					// we don't want to crash the server if a connection fails => catch exception
					e.printStackTrace();
				}
				finally {
					try {
						server.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}  
		};

		clientReceptionThread.start();
		
		// Thread listening to clients' messages
		Thread messageReceptionThread = new Thread() {
			public void run() {
				try {
					while(true) {
						if(clients.size() > 0) {
							for(WebSocketClient client : clients) {
								if(client.hasDataIn()) {
									String msg = client.readMessage();
									System.out.println("Client#"+client.id+" : " + msg);
								}
							}
						}
					}
				} catch (IOException e) {
					// we don't want to crash the server if a connection fails => catch exception
					e.printStackTrace();
				}
			}  
		};

		messageReceptionThread.start();
		
	} 
		
	
}
