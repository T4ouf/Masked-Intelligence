extends Control

var game_config: Dictionary

# Called when the node enters the scene tree for the first time.
func _ready() -> void:
	Global.socket.connect_signals(_on_connected_to_server, _on_server_disconnected, _on_message_received)

func _exit_tree() -> void:
	Global.socket.disconnect_signals()

# Called every frame. 'delta' is the elapsed time since the previous frame.
func _process(_delta: float) -> void:
	Global.socket.poll()

func _start_connection() -> void:
	game_config = {
		"n_hunter": int($%GuessersInput.value),
		"n_android": int($%ImpostersInput.value),
		"n_ai": int($%AIsInput.value),
		"hunter_turn_duration": int($%GuessersDurationInput.value * 60),
		"android_turn_duration": int($%ImpostersDurationInput.value * 60),
	}
	var ip = $%IPInput.text
	var port = $%PortInput.get_line_edit().text
	var err = Global.socket.connect_to_url("ws://%s:%s" % [ip, port])
	if err != Error.OK:
		_show_error("Failed to connect to the server (code: %d)" % [err])
		return

	$%LoadingPopup.popup_centered_ratio(1.0)

func _show_error(reason: String) -> void:
	$ErrorPopup.set_text("Failed to create a game\n" + reason)
	$ErrorPopup.display()

func _on_connected_to_server() -> void:
	print("Connected!")
	var err = Global.socket.send("CREATE_GAME " + JSON.stringify(game_config))
	if err != Error.OK:
		_show_error("Failed to send a message (code: %d)" % [err])
		return

func _on_message_received(s: Variant) -> void:
	print("Received: ", s)
	var parts = String(s).split(" ")
	assert(len(parts) == 2)
	if parts[0] == "ERROR":
		_show_error(parts[2])
		return
	
	Global.config = game_config
	Global.game_id = parts[1]

	get_tree().change_scene_to_file(Global.get_page_path(Global.UIPage.MASTER_LOBBY))


func _on_server_disconnected() -> void:
	_show_error("Server got disconnected")
