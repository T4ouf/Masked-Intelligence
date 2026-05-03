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

// Encode URI parameters after the # field of a URL
function addURIParameters(url, params) {
  let sep = "#";
  for (let key in params) {
    url += sep + key + "=" + encodeURIComponent(params[key]);
    sep = "&";
  }
  return url;
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
