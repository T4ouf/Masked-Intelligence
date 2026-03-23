extends Node

var allowed_names_csv = FileAccess.get_file_as_string("res://../Data/usernames.csv")
var allowed_names = allowed_names_csv.split(",")

var conversation_file := "res://../Data/test_android_prompt.json"
#var conversation: Dictionary

func _ready() -> void:
	print(allowed_names)
	print(Global.json_file_to_dict(conversation_file))
