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
	Global.socket.connect_to_url("ws://%s:%s" % [ip, port])
	$%LoadingPopup.popup()

func _on_connected_to_server() -> void:
	print("Connected!")
	pass

func _on_server_disconnected() -> void:
	print("Server has disconnected")
	pass

func _on_message_received(s: Variant) -> void:
	assert(typeof(s) == TYPE_STRING)
	print("Received a message: ", s)


func _on_button_pressed2() -> void:
	print("Sending ", $%TmpInput.text)
	Global.socket.send($%TmpInput.text)
	# Global.socket.send($%TmpInput.text)
	pass # Replace with function body.
