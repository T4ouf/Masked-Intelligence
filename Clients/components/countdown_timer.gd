extends MarginContainer
class_name CountDownTimer

const label_format = "Time: %d s"

signal timeout
@export var turn_duration := 10 ## Turn duration (s)
var value: int

func _ready() -> void:
	value = turn_duration
	$Label.text = label_format % value
	start()

func start() -> void:
	$SecondsTimer.start()

func create(td: int) -> CountDownTimer:
	var my_scene: CountDownTimer = CountDownTimer.new()
	my_scene.turn_duration = td
	return my_scene

func _on_seconds_timer_timeout() -> void:
	value -= 1
	$Label.text = label_format % value
	if value <= 0:
		$SecondsTimer.stop()
		timeout.emit()
		print("Countdown finished.")
