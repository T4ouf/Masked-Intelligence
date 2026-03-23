extends Control

var conversation_file := "res://../Data/test_android_prompt.json"
var conversation: Dictionary
const message: PackedScene = preload("res://components/message.tscn")

func _ready() -> void:
	conversation = Global.json_file_to_dict(conversation_file)
	show_conversation()

## Clear the Chatbox, then fill it with messages generated
## generated using the conversation data
func show_conversation():
	# Clear the chatbox
	for n in $%ChatBox.get_children():
		$%ChatBox.remove_child(n)
		n.queue_free()
	
	var msg_username: String
	# Fill messages
	for msg_data in conversation["messages"]:
		msg_username = msg_data.keys()[0]
		#print(msg_data[msg_username])
		var msg_copy : Message = message.instantiate()
		var msg_account: Account = Account.new()
		msg_account.set_data({"username": msg_username})
		msg_copy.set_data(msg_account, msg_data[msg_username])
		$%ChatBox.add_child(msg_copy)
