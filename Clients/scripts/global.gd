extends Node

enum UIPage { MAIN_MENU, CREATE_GAME, JOIN_GAME }

enum PlayerRole { PLAYER, MASTER, NONE }

var role: PlayerRole = PlayerRole.PLAYER

var socket: WebSocketClient

func _ready() -> void:
	socket = WebSocketClient.new()

func get_page_path(page: UIPage) -> String:
	match page:
		UIPage.MAIN_MENU:
			return "res://scenes/main_menu.tscn"
		UIPage.CREATE_GAME:
			return "res://scenes/connect_page.tscn"
		UIPage.JOIN_GAME:
			return "res://scenes/connection_page.tscn"
		_:
			return "NOT_FOUND"
