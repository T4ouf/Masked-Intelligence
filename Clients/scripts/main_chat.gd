extends Control

const message: PackedScene = preload("res://scenes/message.tscn")
@export var current_account: Account
@onready var inpt_text_edit = $MarginContainer/HBoxContainer/Chat/MsgEntry/TextEdit
@onready var chatbox = $MarginContainer/HBoxContainer/Chat/ChatScroller/ChatBox
@export var font_size: float = 16.0:
	set(value):
		if value != 0:
			self.theme.default_font_size = int(value)
			#$MarginContainer.scale = Vector2(value, value)
@export var message_limit: int = 3000

## Chat scroller
@onready var chat_scroller = $MarginContainer/HBoxContainer/Chat/ChatScroller
var scrollbar: VScrollBar
var auto_scroll_speed : float = 0.1
var max_scroll_length : float
var tween : Tween

func _ready() -> void:
	scrollbar = chat_scroller.get_v_scroll_bar()
	max_scroll_length = scrollbar.max_value
	
## Send a message from the user account, and scroll to bottom
func send_message(account: Account, msg_text: String):
	if account && !msg_text.is_empty():
		
		var message_number : int = chatbox.get_child_count()
		# When message count goes over message limit, remove child 0
		if (message_number + 1) > message_limit:
			chatbox.get_child(0).queue_free()
		
		# Clear text
		inpt_text_edit.text = ""
		
		# Add a message with msg_text and account data to the chatbox
		var msg_copy : Message = message.instantiate()
		msg_copy.set_data(account, msg_text)
		if account == current_account:
			print("Message from current account")
			msg_copy.move_right()
		chatbox.add_child(msg_copy)
		
		# Move Scrollbar to bottom
		if max_scroll_length != scrollbar.max_value:
			max_scroll_length = scrollbar.max_value
			tween = create_tween().bind_node(chat_scroller).set_trans(Tween.TRANS_LINEAR)
			tween.tween_property(chat_scroller, "scroll_vertical", max_scroll_length, auto_scroll_speed)



func _on_line_edit_text_submitted(new_text: String) -> void:
	send_message(current_account, new_text)


func _on_button_pressed() -> void:
	var new_text = inpt_text_edit.text
	send_message(current_account, new_text)


func _on_font_size_value_changed(value: float) -> void:
	font_size = value
