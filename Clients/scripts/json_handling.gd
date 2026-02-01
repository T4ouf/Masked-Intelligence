extends Node

var file = "res://resources/json/player_info_test.json"

func json_file_to_dict(json_filename: String) -> Dictionary:
	return JSON.parse_string(FileAccess.get_file_as_string(json_filename))
