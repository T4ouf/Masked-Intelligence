extends Popup

# Called when the node enters the scene tree for the first time.
func _ready() -> void:
	# set_exclusive(true)
	pass # Replace with function body.


# Called every frame. 'delta' is the elapsed time since the previous frame.
func _process(_delta: float) -> void:
	pass

func set_text(text: String) -> void:
	$%Label.text = text

func display():
	popup_centered_ratio(1.0)


func _on_button_pressed() -> void:
	hide()
