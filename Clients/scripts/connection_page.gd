extends Control


# Called when the node enters the scene tree for the first time.
func _ready() -> void:
	Global.socket.connected_to_server.connect(_on_connected_to_server)
	Global.socket.message_received.connect(_on_message_received)
	Global.socket.connection_closed.connect(_on_server_disconnected)
	pass # Replace with function body.


# Called every frame. 'delta' is the elapsed time since the previous frame.
func _process(_delta: float) -> void:
	Global.socket.poll()

func _start_connection() -> void:
	var ip = $%IPInput.text
	var port = $%PortInput.get_line_edit().text
	var err = Global.socket.connect_to_url("ws://%s:%s" % [ip, port])
	if err != Error.OK:
		pass

	$%LoadingPopup.popup()

func show_error(err: int) -> void:
	$%ErrorPopup/Label.text = "Connection failed (code %d)" % err
	$%ErrorPopup.popup()

func _on_connected_to_server() -> void:
	print("Connected!")
	Global.socket.send("Hello from Godot")
	pass

func _on_server_disconnected() -> void:
	print("Server has disconnected")
	pass

func _on_message_received(s: Variant) -> void:
	assert(typeof(s) == TYPE_STRING)
	print("Received a message: ", s)


func _on_connect_player_button_pressed() -> void:
	Global.role = Global.PlayerRole.PLAYER
	_start_connection()


func _on_connect_master_button_pressed() -> void:
	Global.role = Global.PlayerRole.MASTER
	_start_connection()
