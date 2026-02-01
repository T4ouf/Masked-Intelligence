class_name WebSocketClient
extends Node

@export var handshake_headers: PackedStringArray
@export var supported_protocols: PackedStringArray
var tls_options: TLSOptions = null

var socket := WebSocketPeer.new()
var last_state := WebSocketPeer.STATE_CLOSED

signal connected_to_server()
signal connection_closed()
signal message_received(message: Variant)

func connect_to_url(url: String) -> int:
	socket.supported_protocols = supported_protocols
	socket.handshake_headers = handshake_headers

	var err := socket.connect_to_url(url, tls_options)
	if err != OK:
		return err

	last_state = socket.get_ready_state()
	return OK


func send(message: String) -> int:
	if typeof(message) == TYPE_STRING:
		return socket.send_text(message)
	return socket.send(var_to_bytes(message))


func get_message() -> Variant:
	if socket.get_available_packet_count() < 1:
		return null
	var pkt := socket.get_packet()
	if socket.was_string_packet():
		return pkt.get_string_from_utf8()
	return bytes_to_var(pkt)


func close(code: int = 1000, reason: String = "") -> void:
	socket.close(code, reason)
	last_state = socket.get_ready_state()


func clear() -> void:
	socket = WebSocketPeer.new()
	last_state = socket.get_ready_state()


func get_socket() -> WebSocketPeer:
	return socket


func poll() -> void:
	if socket.get_ready_state() != socket.STATE_CLOSED:
		socket.poll()

	var state := socket.get_ready_state()

	if last_state != state:
		last_state = state
		if state == socket.STATE_OPEN:
			connected_to_server.emit()
		elif state == socket.STATE_CLOSED:
			connection_closed.emit()
	while socket.get_ready_state() == socket.STATE_OPEN and socket.get_available_packet_count():
		message_received.emit(get_message())

func connect_signals(on_connection, on_disconnection, on_message) -> void:
	if on_connection != null and !connected_to_server.is_connected(on_connection):
		connected_to_server.connect(on_connection)

	if on_disconnection != null and !connection_closed.is_connected(on_disconnection):
		connection_closed.connect(on_disconnection)

	if on_message != null and !message_received.is_connected(on_message):
		message_received.connect(on_message)

func disconnect_signals() -> void:
	# We assume that only one scene has functions connected to this class signals at any given time
	for connection in connected_to_server.get_connections():
		connected_to_server.disconnect(connection.callable)

	for connection in connection_closed.get_connections():
		connection_closed.disconnect(connection.callable)

	for connection in message_received.get_connections():
		message_received.disconnect(connection.callable)

func _process(_delta: float) -> void:
	poll()
