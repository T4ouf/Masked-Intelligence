extends Button

var number: int = 0
var format_text = "Round #%d"

## Set the round number
func set_data(round_number: int):
	number = round_number
	text = format_text % number
