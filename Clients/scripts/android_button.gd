extends MarginContainer

signal pressed
@export var linked_account: Account 
@onready var icon = $PanelContainer/HBoxContainer/AccIcon
@onready var button = $PanelContainer/HBoxContainer/Button

func set_data(account: Account):
	linked_account = account
	icon.texture = linked_account.account_image
	button.text = linked_account.text

func _on_button_pressed() -> void:
	pressed.emit()
