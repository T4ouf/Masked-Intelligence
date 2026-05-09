package server;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class Server extends WebSocketServer {

    private Map<Integer, WebSocket> clients = new HashMap<>();
    private Set<Integer> gm_ids = new HashSet<>();
    private Set<Integer> player_ids = new HashSet<>();
    private Set<WebSocket> established_connections = new HashSet<>();

    // Ids must start at 1 since we use negative indices to indicate a reconnection,
    // and -0 doesn't exist with integers. Additionally, a 0 prefix indicates that a
    // client disconnects and won't reconnect to the game
    private int next_id = 1;

    public Server(InetSocketAddress address) {
        super(address);
    }

    private void connectGM(WebSocket conn) {
        // TODO: check if we can use more fine grained critical sections
        synchronized(this) {
            if (established_connections.contains(conn)) {
                sendError(conn, "This client has already been initialized");
                return;
            }
            int id = next_id++;
            clients.put(id, conn);
            gm_ids.add(id);
            established_connections.add(conn);
            conn.send(Integer.toString(id));
        }
    }

    private void connectPlayer(WebSocket conn, int game_id) {
        synchronized(this) {
            if (established_connections.contains(conn)) {
                sendError(conn, "This client has already been initialized");
                return;
            }
            if (!gm_ids.contains(game_id)) {
                sendError(conn, "There is no game with id " + game_id);
                return;
            }
            int id = next_id++;
            clients.put(id, conn);
            player_ids.add(id);
            established_connections.add(conn);
            conn.send(Integer.toString(id));
        }
    }

    private void reconnectClient(WebSocket conn, int id) {
        synchronized(this) {
            if (established_connections.contains(conn)) {
                System.err.println("Trying to reconnect an already connected client: " + id);
                return;
            }
            if (!clients.containsKey(id)) {
                System.err.println("There is no client with id " + id + " to reconnect");
                return;
            }
            clients.replace(id, conn);
            established_connections.add(conn);
            conn.send(Integer.toString(id));
        }
    }

    private void disconnectClient(WebSocket conn, int id) {
        synchronized(this) {
            WebSocket conn_to_remove = clients.getOrDefault(id, null);
            if (conn != conn_to_remove) {
                return;
            }
            clients.remove(id);
            gm_ids.remove(id);
            player_ids.remove(id);
            established_connections.remove(conn);
            conn.close();
        }
    }

    private void sendError(WebSocket conn, String reason) {
        String escaped_reason = reason.replace("\"", "\\\"");
        conn.send("{\"type\":\"ERROR\",\"content\":{\"reason\":\"" + escaped_reason + "\"}}");
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New connection from " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println(
                "closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
        synchronized(this) {
            established_connections.remove(conn);
        }
    }

    /**
     * Processes messages sent to the server.
     *
     * There are 5 possible form of messages the server can process:
     * - "" (empty message): Sent by a GM when it first connects to the server
     * - "n" (n -> int): Sent by a player when they first connect to the game, "n"
     * must be the id attributed to a GM who is already connected.
     * - "-n": sent by a client to reconnect and reuse the existing id "n"
     * - "0n": sent by a client to definitively disconnect, to use at the end of a
     * game or when cancelling before the game starts. Using "-n" to reconnect will
     * not be possible afterwards
     * - "n{...}": regular message, the json payload will be sent to the client with
     * id "n"
     */
    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("received message from " + conn.getRemoteSocketAddress() + ": " + message);

        int payload_start = message.indexOf('{');

        if (message.isEmpty()) {
            connectGM(conn);
            return;
        }

        if (payload_start == -1) {
            try {
                int id = Integer.parseInt(message);
                if (message.charAt(0) == '0') {
                    disconnectClient(conn, id);
                } else if (id >= 0) {
                    connectPlayer(conn, id);
                } else {
                    reconnectClient(conn, -id);
                }
            } catch (NumberFormatException e) {
                sendError(conn, "Invalid id " + message);
            }
            return;
        }

        try {
            int id = Integer.parseInt(message.substring(0, payload_start));
            WebSocket target = null;
            synchronized(this) {
                target = clients.getOrDefault(id, null);
            }
            if (target == null) {
                sendError(conn, "Trying to send a message to non-existing client with id " + id);
                return;
            }
            target.send(message.substring(payload_start));
        } catch (NumberFormatException e) {
            sendError(conn, "Invalid id " + message.substring(0, payload_start));
        }
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        System.out.println("received ByteBuffer from " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("an error occurred on connection " + conn.getRemoteSocketAddress() + ":" + ex);
    }

    @Override
    public void onStart() {
        System.out.println("server started successfully");
    }

    public static void main(String[] args) {
        String host = "localhost";
        int port = 9000;

        WebSocketServer server = new Server(new InetSocketAddress(host, port));
        server.run();
    }
}
