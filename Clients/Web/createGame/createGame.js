const waiting_dialog = document.getElementById("waiting-dialog");
const start_game_button = document.getElementById("start-game-button");
const waiting_dialog_text = document.getElementById("waiting-dialog-text");
var server_ip = null;
var server_port = null;
var ws = null;
var connected = false;
var max_players = 0;
var game_id = "";
var game_params = null;
let connected_players = new Set();

function setWaitingDialogText(text) {
  waiting_dialog_text.textContent = text;
}

function messageHandler(event) {
  console.log("Received ", event.data);

  const message = decodeJSONMessage(event.data);
  const type = message["type"];
  const content = message["content"];
  if (type == "PARSE_ERROR") {
    setWaitingDialogText(
      "Failed to parse a message: " + content["reason"]
    );
    ws.close();
    return;
  } else if (type == "ERROR") {
    setWaitingDialogText("An error occured: " + content["reason"]);
    ws.close();
    return;
  }

  if (type == "ID") {
    game_id = content["id"];
    connected = true;
  } else if (type == "JOIN_GAME") {
    const player_id = content["id"];
    connected_players.add(player_id);
    ws.send(createMessage(player_id, "JOIN_GAME", {
      // FIXME: use the real usernames
      username: "test_subject#" + player_id,
    }));

    if (connected_players.size == max_players) {
      start_game_button.hidden = false;
    }
  } else if (type == "LEAVE_GAME") {
    connected_players.delete(content["id"]);
    start_game_button.hidden = true;
  }
  setWaitingDialogText(
    "The game id is " +
    game_id +
    "\r\nWaiting for players to join (" +
    connected_players.size +
    "/" +
    max_players +
    ")"
  );
}

function connectToServer(ip_addr, port, form) {
  waiting_dialog.showModal();

  const nb_hunters = parseInt(form["nb-hunters"].value);
  const nb_androids = parseInt(form["nb-androids"].value);
  const nb_ais = parseInt(form["nb-ais"].value);
  const hunter_turn_duration = parseInt(form["hunter-turn-duration"].value);
  const android_turn_duration = parseInt(form["android-turn-duration"].value);
  max_players = nb_hunters + nb_androids;
  game_params = {
    n_hunter: nb_hunters,
    n_android: nb_androids,
    n_ai: nb_ais,
    hunter_turn_duration: hunter_turn_duration,
    android_turn_duration: android_turn_duration,
  };

  ws = new WebSocket("ws://" + ip_addr + ":" + port);
  ws.addEventListener("open", (event) => {
    connected = true;
    server_ip = ip_addr;
    server_port = port;
    message = "";
    console.log("connection open, sending ", message);
    ws.send(message);
  });
  ws.addEventListener("message", messageHandler);
  ws.addEventListener("error", (event) => {
    if (!connected) {
      setWaitingDialogText("Failed to connect to the server at " + ws.url);
    } else {
      setWaitingDialogText("An error occured");
    }
  });
}

function cancelConnection() {
  if (connected) {
    for (const id of connected_players) {
      ws.send(createMessage(id, "GAME_CANCELLED", {}));
    }
    ws.send("0" + game_id);
  }
  ws.close();
  connected = false;
  game_id = "";
  connected_players.clear();
  waiting_dialog.close();
}

function startGame() {
  game_params.players = [];
  for (const id of connected_players) {
    game_params.players.push(id);
  }
  const parameters = {
    game_id: game_id,
    server_ip: server_ip,
    server_port: server_port,
    game_params: JSON.stringify(game_params),
  };
  window.location = addURIParameters("../gmDashboard/gmDashboard.html", parameters);
}

function validateForm() {
  const form = document.forms["create-game-form"];
  if (!form.reportValidity()) {
    return;
  }

  const ip_addr = form["server-ip"].value;
  if (!validateIp(ip_addr)) {
    alert("Invalid IP address " + ip_addr);
  } else {
    const port = parseInt(form["server-port"].value);
    connectToServer(ip_addr, port, form);
  }
}
