extends Control


# Called when the node enters the scene tree for the first time.
func _ready() -> void:
	Global.socket.connect_signals(null, _on_connection_closed, _on_message_received)
	$%TitleLabel.text = "Successfully created game room\nGame id is %s" % [Global.game_id]

func _exit_tree() -> void:
	Global.socket.disconnect_signals()

# Called every frame. 'delta' is the elapsed time since the previous frame.
func _process(_delta: float) -> void:
	Global.socket.poll()

func _on_message_received(s: Variant) -> void:
	print("Received: ", s)
	if s == "CANCEL " + Global.game_id:
		Global.socket.clear()
		get_tree().change_scene_to_file(Global.get_page_path(Global.UIPage.CREATE_GAME))

func _on_connection_closed() -> void:
	pass

func _on_cancel() -> void:
	Global.socket.send("CANCEL " + Global.game_id)
