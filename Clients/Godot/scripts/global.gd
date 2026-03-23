extends Node

enum UIPage { MAIN_MENU, CREATE_GAME, JOIN_GAME, MASTER_LOBBY, PLAYER_LOBBY }

enum PlayerRole { PLAYER, MASTER, NONE }

var role: PlayerRole = PlayerRole.PLAYER

var socket: WebSocketClient

var config: Dictionary

var game_id: String

func _ready() -> void:
	socket = WebSocketClient.new()

func create_message(type: String, contents) -> String:
	return JSON.stringify({
		"message_type": type,
		"content": contents,
	})

func unpack_message(msg: String) -> Array:
	var json = JSON.parse_string(msg)
	if json == null:
		return ["ERROR", {"reason": "Failed to parse the following message to json: " + msg}]
	return [json["message_type"], json["content"]]

func json_file_to_dict(json_filename: String) -> Dictionary:
	return JSON.parse_string(FileAccess.get_file_as_string(json_filename))

func get_page_path(page: UIPage) -> String:
	match page:
		UIPage.MAIN_MENU:
			return "res://scenes/main_menu.tscn"
		UIPage.CREATE_GAME:
			return "res://scenes/create_game.tscn"
		UIPage.JOIN_GAME:
			return "res://scenes/connection_page.tscn"
		UIPage.MASTER_LOBBY:
			return "res://scenes/master_lobby.tscn"
		UIPage.PLAYER_LOBBY:
			return "res://scenes/player_lobby.tscn"
		_:
			return "NOT_FOUND"
