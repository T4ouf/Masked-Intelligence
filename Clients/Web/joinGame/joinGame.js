const waiting_dialog = document.getElementById("waiting-dialog");
const waiting_dialog_text = document.getElementById("waiting-dialog-text");
var ws = null;
var connected = false;
var game_id = "";
var username = "";
var player_id = -1;
var server_ip = "";
var server_port = -1;

function validateIPv4Address(ip_addr = "") {
  const components = ip_addr.split(".");

  if (components.length != 4) {
    return false;
  }

  for (let component in components) {
    const digit = parseInt(component);
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
  return (
    validateIPv4Address(ip_addr) ||
    validateIPv6Address(ip_addr) ||
    ip_addr == "localhost"
  );
}

function addURIParameters(url, params) {
  let sep = "#";
  for (let key in params) {
    url += sep + key + "=" + encodeURIComponent(params[key]);
    sep = "&";
  }
  return url;
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
      "Failed to parse a message from the server: " + content["reason"]
    );
    ws.close();
    return;
  } else if (type == "ERROR") {
    setWaitingDialogText("An error occured on the server: " + content["reason"]);
    ws.close();
    return;
  }

  if (type == "JOIN_GAME") {
    player_id = parseInt(content["id"]);
    setWaitingDialogText(
      "Your username is " + username + "\r\nWaiting for the game to start..."
    );
  } else if (type == "GAME_CANCELLED") {
    setWaitingDialogText("The game has been cancelled");
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
  username = form["username"].value;
  const message = JSON.stringify({
    message_type: "JOIN_GAME",
    content: {
      game_id: game_id,
      username: username,
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
      setWaitingDialogText("Failed to connect to the server at " + ws.url);
    } else {
      setWaitingDialogText("An error occured");
    }
  });
}

function cancelConnection() {
  if (connected) {
    const message = JSON.stringify({
      message_type: "LEAVE_GAME",
      content: {
        game_id: game_id,
        id: player_id,
      },
    });
    ws.send(message);
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
