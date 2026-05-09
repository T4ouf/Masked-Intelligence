const waiting_dialog = document.getElementById("waiting-dialog");
const waiting_dialog_text = document.getElementById("waiting-dialog-text");
const returnToMenuButton = document.getElementById("return-to-menu");
var ws = null;
var game_id = "";
var server_ip = "";
var server_port = 0;
var game_params = null;
let expected_players = new Set();
let connected_players = new Set();

function returnToMenu() {
  window.location = "../index.html";
}

function connectToServer(ip_addr, port) {
  ws = new WebSocket("ws://" + ip_addr + ":" + port);
  ws.addEventListener("open", (event) => {
    connected = true;
    // Initiating a connection with `-id` asks the server to use `id` for this connection instead of assigning a new one
    const message = Number(-game_id).toString();
    console.log("connection open, sending ", message);
    ws.send(message);
  });
  ws.addEventListener("message", messageHandler);
  ws.addEventListener("error", (event) => {
    if (!connected) {
      setWaitingDialogText("Failed to connect to the server at " + ws.url, true);
    } else {
      setWaitingDialogText("An error occured", true);
    }
  });
}

function setWaitingDialogText(text, is_error) {
  waiting_dialog_text.textContent = text;
  if (typeof is_error !== undefined && is_error) {
    returnToMenuButton.hidden = false;
  }
}

function messageHandler(event) {
  console.log("Received ", event.data);

  const message = decodeJSONMessage(event.data);
  const type = message["type"];
  const content = message["content"];
  if (type == "PARSE_ERROR") {
    setWaitingDialogText(
      "Failed to parse a message: " + content["reason"],
      true
    );
    ws.close();
    return;
  } else if (type == "ERROR") {
    setWaitingDialogText("An error occured: " + content["reason"], true);
    ws.close();
    return;
  }

  if (type == "ID") {
    for (const id of game_params.players) {
      ws.send(createMessage(id, "START_GAME", { game_id: game_id }));
    }
  } else if (type == "JOIN_STARTED_GAME") {
    const id = content["id"];
    if (!expected_players.has(id)) {
      console.log("An unknown player " + id + " tried to connect to the game");
      ws.send(createMessage(id, "ERROR", { reason: "Player is not registered in game " + game_id }));
      return;
    }

    connected_players.add(id);

    if (connected_players.size == expected_players.size) {
      for (const id of connected_players) {
        ws.send(createMessage(id, "JOIN_STARTED_GAME", {}));
      }
      waiting_dialog.close();
      console.log("Game starting");
      // FIXME: handle game start
    }
  }
}

window.onload = function() {
  waiting_dialog.showModal();

  const searchParams = new URLSearchParams(window.location.hash.substring(1));
  window.location.hash = "";
  // FIXME: remove later, only there for debugging purposes
  console.log(searchParams);

  // For development purposes, allow to see the dashboard without joining a game
  if (searchParams.has("test")) {
    waiting_dialog.close();
    return;
  }

  for (let key of ["game_id", "server_ip", "server_port", "game_params"]) {
    if (!searchParams.has(key)) {
      console.error("missing required parameter " + key);
      setWaitingDialogText(
        "Error: missing required paramter '" + key + "' in url",
        true
      );
      return;
    }
  }

  game_id = searchParams.get("game_id");
  server_ip = searchParams.get("server_ip");
  server_port = parseInt(searchParams.get("server_port"));
  game_params = JSON.parse(searchParams.get("game_params"));
  for (const id of game_params.players) {
    expected_players.add(id);
  }

  console.log(game_id);
  let p = document.getElementById("todo");
  p.textContent = game_id;

  connectToServer(server_ip, server_port);
};
