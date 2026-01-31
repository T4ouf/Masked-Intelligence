extends HBoxContainer
class_name Message

const my_scene: PackedScene = preload("res://scenes/message.tscn")

## Set the label texts in the message.
func set_data(account: Account, msg_text: String):
	$TextureRect.texture = account.account_image
	$%Account.text = account.username + ": "
	$%Message.text = msg_text

## Display the message to the left	
func move_left():
	move_child($TextureRect, 0)
	move_child($VBoxContainer, 1)
	alignment = BoxContainer.ALIGNMENT_BEGIN
	$%Account.horizontal_alignment = 0

## Display the message to the right	
func move_right():
	move_child($TextureRect, 2)
	move_child($VBoxContainer, 0)
	alignment = BoxContainer.ALIGNMENT_END
	$%Account.horizontal_alignment = 2
	
