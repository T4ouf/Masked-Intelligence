const waiting_dialog = document.getElementById("waiting-dialog");
const waiting_dialog_text = document.getElementById("waiting-dialog-text");
var ws = null;
var connected = false;
var max_players = 0;
var connected_players = 0;
var game_id = "";

function validateIPv4Address(ip_addr = "") {
  const components = ip_addr.split(".");

  if (components.length != 4) {
    return false;
  }

  for (let component in components) {
    const digit = parseInt(component)
    if (digit == NaN || digit < 0 || digit > 255) {
      return false;
    }
  }

  return true;
}

function validateIPv6Address(ip_addr = "") {
  const components = ip_addr.split(":");

  if (components.length < 3 || components.length > 8) {
    return false;
  }

  let has_empty_group = false;
  for (let component in components) {
    if (component == "") {
      if (has_empty_group) {
        return false;
      }
      has_empty_group = true;
      continue;
    }

    if (component.length > 4) {
      return false;
    }

    const digit = parseInt(component, 16);
    if (digit == NaN) {
      return false;
    }
  }

  return true;
}

function validateIp(ip_addr) {
  return validateIPv4Address(ip_addr) || validateIPv6Address(ip_addr) || ip_addr == "localhost";
}

function setWaitingDialogText(text) {
  waiting_dialog_text.textContent = text;
}

function decodeJSONMessage(message) {
  try {
    return JSON.parse(message);
  } catch (e) {
    return JSON.stringify({
      message_type: "PARSE_ERROR",
      content: {
        reason: message.message
      }
    })
  }
}

function messageHandler(event) {
  console.log("Received ", event.data);

  const message = decodeJSONMessage(event.data);
  const type = message["message_type"];
  const content = message["content"]
  if (type == "PARSE_ERROR") {
    setWaitingDialogText("Failed to parse a message from the server: " + content["reason"]);
    ws.close();
    return;
  } else if (type == "ERROR") {
    setWaitingDialogText("An error occured on the server: " + content["reason"]);
    ws.close();
    return;
  }

  if (type == "CREATE_GAME") {
    game_id = content["game_id"];
  } else if (type == "PLAYER_JOINED") {
    connected_players++;
    // TODO: add a button to start the game when connected_players == max_players
  } else if (type == "PLAYER_LEFT") {
    connected_players--;
  }
  setWaitingDialogText("The game id is " + game_id + "\r\nWaiting for players to join (" + connected_players + "/" + max_players + ")");
}

function connectToServer(ip_addr, port, form) {
  waiting_dialog.showModal();

  const nb_hunters = parseInt(form["nb-hunters"].value);
  const nb_androids = parseInt(form["nb-androids"].value);
  const nb_ais = parseInt(form["nb-ais"].value);
  const hunter_turn_duration = parseInt(form["hunter-turn-duration"].value);
  const android_turn_duration = parseInt(form["android-turn-duration"].value);
  max_players = nb_hunters + nb_androids;
  const message = JSON.stringify({
    message_type: "CREATE_GAME", content: {
      n_hunter: nb_hunters,
      n_android: nb_androids,
      n_ai: nb_ais,
      hunter_turn_duration: hunter_turn_duration,
      android_turn_duration: android_turn_duration
    }
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
      setWaitingDialogText("Failed to connect to the server at " + ws.url);
    } else {
      setWaitingDialogText("An error occured");
    }
  });
}

function cancelConnection() {
  if (connected) {
    let message = JSON.stringify({
      message_type: "CANCEL_GAME",
      content: {
        game_id: game_id,
      },
    });

    ws.send(message);
  }
  ws.close();
  connected = false;
  game_id = "";
  connected_players = 0;
  waiting_dialog.close();
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
