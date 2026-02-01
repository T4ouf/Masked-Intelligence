extends Node

var allowed_names_csv = FileAccess.get_file_as_string("res://../Data/usernames.csv")
var allowed_names = allowed_names_csv.split(",")

func _ready() -> void:
	print(allowed_names)
