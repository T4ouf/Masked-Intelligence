extends Button

@export var targetPage : Global.UIPage
@export var targetRole : Global.PlayerRole

# Called when the node enters the scene tree for the first time.
func _ready():
	pressed.connect(_on_pressed)
	pass


# Called every frame. 'delta' is the elapsed time since the previous frame.
func _process(delta):
	pass


func _on_pressed():
	# Prevent from multiple clicks
	disabled = true;
	if targetRole != Global.PlayerRole.NONE:
		Global.role = targetRole
	get_tree().change_scene_to_file(Global.get_page_path(targetPage))
