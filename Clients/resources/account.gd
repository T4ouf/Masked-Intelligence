extends Resource
class_name Account

@export var account_image: Texture
@export var username: String = ""
@export var id = 17


## the Dictionnay argument is created by a JSON format received payload
static func create_account(acc_data: Dictionary):
	var my_scene: Account = load("res://resources/account.gd")
	my_scene.username = acc_data.username
