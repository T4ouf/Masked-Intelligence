extends Control

var game_id: String

# Called when the node enters the scene tree for the first time.
func _ready() -> void:
	Global.socket.connect_signals(_on_connected_to_server, _on_server_disconnected, _on_message_received)

func _exit_tree() -> void:
	Global.socket.disconnect_signals()

# Called every frame. 'delta' is the elapsed time since the previous frame.
func _process(_delta: float) -> void:
	Global.socket.poll()

func _start_connection() -> void:
	var ip = $%IPInput.text
	var port = int($%PortInput.value)
	game_id = $%GameIDInput.text
	var err = Global.socket.connect_to_url("ws://%s:%d" % [ip, port])
	if err != Error.OK:
		show_error("Failed to connect to the server (code: %d)" % [err])
		return

	$LoadingPopup.popup_centered_ratio(1.0)

func show_error(reason: String) -> void:
	$ErrorPopup.set_text("Failed to connect to a game\n" + reason)
	$ErrorPopup.popup_centered_ratio(1.0)

func _on_connected_to_server() -> void:
	print("Connected!")
	$%LoadingLabel.text = "Connected to the server\nJoining game ..."
	Global.socket.send("JOIN_GAME " + game_id)

func _on_server_disconnected() -> void:
	print("Server has disconnected")
	show_error("Server is disconnected")

func _on_message_received(s: Variant) -> void:
	assert(typeof(s) == TYPE_STRING)
	print("Received a message: ", s)
	var parts = String(s).split(" ", false, 1)
	if parts[0] == "ERROR":
		$LoadingPopup.hide()
		show_error(parts[1])
		Global.socket.clear()
		return
	
	var game_id_ = parts[1]
	Global.game_id = game_id_

	get_tree().change_scene_to_file(Global.get_page_path(Global.UIPage.PLAYER_LOBBY))


func _on_join_button_pressed() -> void:
	Global.role = Global.PlayerRole.PLAYER
	_start_connection()


func _on_loading_popup_cancel_button_pressed() -> void:
	$LoadingPopup.hide()
