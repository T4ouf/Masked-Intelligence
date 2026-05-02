const waiting_dialog = document.getElementById("waiting-dialog");
const waiting_dialog_text = document.getElementById("waiting-dialog-text");
const returnToMenuButton = document.getElementById("return-to-menu");
var ws = null;
var game_id = "";
var server_ip = "";
var server_port = 0;

function returnToMenu() {
  window.location = "../index.html";
}

function connectToServer(ip_addr, port) {
  const message = JSON.stringify({
    message_type: "START_GAME",
    content: {
      game_id: game_id,
    },
  });

  ws = new WebSocket("ws://" + ip_addr + ":" + port);
  ws.addEventListener("open", (event) => {
    connected = true;
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

function decodeJSONMessage(message) {
  try {
    return JSON.parse(message);
  } catch (e) {
    return JSON.stringify({
      message_type: "PARSE_ERROR",
      content: {
        reason: message.message,
      },
    });
  }
}

function messageHandler(event) {
  console.log("Received ", event.data);

  const message = decodeJSONMessage(event.data);
  const type = message["message_type"];
  const content = message["content"];
  if (type == "PARSE_ERROR") {
    setWaitingDialogText(
      "Failed to parse a message from the server: " + content["reason"],
      true
    );
    ws.close();
    return;
  } else if (type == "ERROR") {
    setWaitingDialogText("An error occured on the server: " + content["reason"], true);
    ws.close();
    return;
  }

  if (type == "START_GAME") {
    // FIXME: handle this
    waiting_dialog.close();
    console.log(content);
  }
}

window.onload = function () {
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

  for (let key of ["game_id", "server_ip", "server_port"]) {
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

  console.log(game_id);
  let p = document.getElementById("todo");
  p.textContent = game_id;

  connectToServer(server_ip, server_port);
};
