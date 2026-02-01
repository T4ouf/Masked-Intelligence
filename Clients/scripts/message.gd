extends MarginContainer
class_name Message

const my_scene: PackedScene = preload("res://components/message.tscn")

## Set the label texts in the message.
func set_data(account: Account, msg_text: String):
	$%AccIcon.texture = account.account_image
	$%AccountLabel.text = account.username + ": "
	$%MessageLabel.text = msg_text

## Display the message to the left	
func move_left():
	$%HBoxContainer.move_child($%AccIcon, 0)
	$%HBoxContainer.move_child($%VBoxContainer, 1)
	$%HBoxContainer.alignment = BoxContainer.ALIGNMENT_BEGIN
	$%AccountBackground.size_flags_horizontal = SIZE_SHRINK_BEGIN
	add_theme_constant_override("margin_left", 10)
	add_theme_constant_override("margin_right", 150)

## Display the message to the right	
func move_right():
	$%HBoxContainer.move_child($%AccIcon, 2)
	$%HBoxContainer.move_child($%VBoxContainer, 0)
	$%HBoxContainer.alignment = BoxContainer.ALIGNMENT_END
	$%AccountBackground.size_flags_horizontal = SIZE_SHRINK_END
	add_theme_constant_override("margin_left", 150)
	add_theme_constant_override("margin_right", 10)
	
