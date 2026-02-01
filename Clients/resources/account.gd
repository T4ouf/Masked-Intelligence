extends Resource
class_name Account

@export var account_image: Texture
@export var username: String = "default-account-username"
@export var id = 17

var allowed_names_csv: String = FileAccess.get_file_as_string("res://../Data/usernames.csv")
var allowed_names = allowed_names_csv.split(",")

## the Dictionnay argument is created by a JSON format received payload
func create(acc_data: Dictionary) -> Account:
	var my_scene: Account = load("res://resources/account.gd")
	my_scene.username = acc_data.username
	my_scene.id = acc_data.id
	my_scene.account_image = self.get_account_image()
	return my_scene

## For now every account has the same default image.
func get_account_image() -> Texture:
	if username in allowed_names:
		return load("res://../Data/icons/user_icon_default.svg")
	else:
		return load("res://assets/game-icon.svg")
