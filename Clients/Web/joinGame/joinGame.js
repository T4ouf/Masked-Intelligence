const waiting_dialog = document.getElementById("waiting-dialog");
const waiting_dialog_text = document.getElementById("waiting-dialog-text");
var ws = null;
var connected = false;
var game_id = "";
var username = "";
var player_id = -1;
var server_ip = "";
var server_port = -1;

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
    player_id = parseInt(content["id"]);
    ws.send(createMessage(game_id, "JOIN_GAME", { id: player_id }));
  } else if (type == "JOIN_GAME") {
    username = content["username"];
    setWaitingDialogText(
      "Your username is " + username + "\r\nWaiting for the game to start..."
    );
  } else if (type == "GAME_CANCELLED") {
    setWaitingDialogText("The game has been cancelled");
    ws.send("0" + player_id);
    connected = false;
  } else if (type == "START_GAME") {
    const parameters = {
      game_id: game_id,
      username: username,
      player_id: player_id,
      server_ip: server_ip,
      server_port: server_port,
    };

    // FIXME: Go to the appropriate page depending on the player's role
    const role = content["role"];

    window.location = addURIParameters(
      "../hunterDashboard/hunterDashboard.html",
      parameters
    );
  }
}

function connectToServer(ip_addr, port, form) {
  waiting_dialog.showModal();

  game_id = form["game-id"].value;

  ws = new WebSocket("ws://" + ip_addr + ":" + port);
  ws.addEventListener("open", (event) => {
    connected = true;
    const message = game_id.toString();
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
    ws.send(createMessage(game_id, "LEAVE_GAME", { id: player_id }));
    ws.send("0" + player_id);
  }

  ws.close();
  connected = false;
  game_id = "";
  username = "";
  player_id = -1;
  waiting_dialog.close();
}

function validateForm() {
  const form = document.forms["join-game-form"];
  if (!form.reportValidity()) {
    return;
  }

  server_ip = form["server-ip"].value;
  if (!validateIp(server_ip)) {
    alert("Invalid IP address " + server_ip);
  } else {
    server_port = parseInt(form["server-port"].value);
    connectToServer(server_ip, server_port, form);
  }
}
