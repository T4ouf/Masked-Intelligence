"use strict";

// --- Global variables ---
let fontSize = 16;

// Showdown markdown to html converter
let converter = new showdown.Converter();
converter.setOption("simpleLineBreaks", true); // Single line breaks return line in html with <br>
converter.setOption("noHeaderId", true); // Otherwise, adds ids equal to the h1 title automatically

// Default chat state as of now, but later will start at 0.
let gmUsername = "GM prompter";
let chatState = {
  user: {
    username: "Bonbon21",
    id: 18,
  },
  round: 2,
  messages: [
    {
      prompt: "Are you a robot?",
      answer: "No I am not, why do you say that?",
    },
    {
      prompt:
        "#Well maybe you are trying to deceive me with your lies.\n I am done with lies, I really like telling **big messages** like this to force people to wrap around messages.",
      answer: "Do not believe the false prophets! I am completely human",
    },
    {
      prompt: "Tell me how much weights one liter of water then.",
      answer: "It is 1200 grams!",
    },
    {
      prompt: "Such idiocy could only come from a clanker...",
      answer: "OMG you did not just say that!",
    },
  ],
};

let chatBox = document.getElementById("chatBox");
let iconCss = `
  width: 50px;
  height: 50px;
`;

// --- Functions ---

/**
 * Generate a new exchange, and show the prompt
 */
function getNextRoundPrompt(_) {
  // Div containers
  let promptTxt = "Some prompt from the GM...";
  makeExchangeFromPrompt(promptTxt, gmUsername);
}

/**
 * Create a div containing an icon, username and text bubble for a message.
 * @param[in] {string} messageTxt the (markdown) text content of the message
 * @param[in] {string} messageDivClass the class of the message div to create.
 * @param[in] {string} iconImgSrc the url to the icon Image.
 * @param[in] {string} iconImgAlt the alt of the icon img.
 * @param[in] {string} messageBubbleClass the class of the message bubble to create.
 * @returns {HTMLDivElement} the div containing the icon + username + text bubble
 */
function createMessageDiv(
  messageTxt,
  username,
  messageDivClass,
  iconImgSrc,
  iconImgAlt,
  messageBubbleClass
) {
  // Div container for icon + username + text bubble
  let messageDiv = document.createElement("div");
  messageDiv.classList.add("messageContainer", messageDivClass);

  // Divs inside containers, aligned with flexbox
  let promptImg = document.createElement("img");
  promptImg.src = iconImgSrc;
  promptImg.alt = iconImgAlt;
  promptImg.style.cssText = iconCss;
  messageDiv.appendChild(promptImg);

  // Container for message bubble + username in div
  let messageUsernameAndBubble = document.createElement("div");
  messageUsernameAndBubble.classList.add("messageUsernameAndBubble", messageBubbleClass);
  // Insert username
  let messageUsernameDiv = document.createElement("div");
  messageUsernameDiv.classList.add("messageUsername");
  messageUsernameDiv.innerText = username;
  // Insert message content
  let messageBubbleDiv = document.createElement("div");
  messageBubbleDiv.classList.add("messageBubble");

  // Convert the message markdown to HTML
  messageBubbleDiv.innerHTML = converter.makeHtml(messageTxt);

  // Append to messageBubbleDiv
  messageUsernameAndBubble.appendChild(messageUsernameDiv);
  messageUsernameAndBubble.appendChild(messageBubbleDiv);
  messageDiv.appendChild(messageUsernameAndBubble);
  return messageDiv;
}

/**
 * Create an exchange div and a prompr div inside it, after having received a prompt
 * @param {string} promptTxt the prompt message received through WebSockets
 * @param {string} username the username of the prompter (probably GM)
 */
function makeExchangeFromPrompt(promptTxt, username) {
  // Div containers
  let exchangeDiv = document.createElement("div");
  exchangeDiv.classList.add("exchangeContainer");
  let promptDiv = createMessageDiv(
    promptTxt,
    username,
    "promptContainer",
    "/rsc/img/icons/androidIcon.png",
    "androidIcon",
    "promptBubble"
  );
  // Add divs to document
  exchangeDiv.appendChild(promptDiv);
  chatBox.appendChild(exchangeDiv);
  // Scroll to bottom of chatBox
  chatBox.scrollTop = chatBox.scrollHeight;
}

function submitInputForm(_) {
  let answerTxt = textInput.value;
  // After getting its value, clear text from textarea
  textInput.value = "";
  addAnswer(answerTxt, chatState.user.username);
}

/**
 * Find the last exchange div, and append the message to it.
 * If no answer has been sent yet, create a new div. Else, use the current one.
 * @param {string} answerTxt the answer inputted by the android
 * @param {string} username the android username
 */
function addAnswer(answerTxt, username) {
  // Find the last exchange div.
  let chatBoxChildren = chatBox.children;
  let exchangeDiv = chatBoxChildren[chatBoxChildren.length - 1];

  // If no answer has been sent yet, create a new div. Else, use the current one.
  let answerContainers = exchangeDiv.getElementsByClassName("answerContainer");
  let answerDiv;
  let answerMessageDiv;

  if (answerContainers.length == 0) {
    console.debug("add new answer div");
    answerDiv = createMessageDiv(
      answerTxt,
      username,
      "answerContainer",
      "/rsc/img/icons/user_icon_default.png",
      "userIcon",
      "answerBubble"
    );
    // Add divs to document
    exchangeDiv.appendChild(answerDiv);
  } else {
    console.debug("use existing answer div");
    answerDiv = answerContainers[answerContainers.length - 1];
    // There should be a single message div
    answerMessageDiv = answerDiv.getElementsByClassName("messageBubble")[0];
    answerMessageDiv.innerHTML += converter.makeHtml(answerTxt);
  }
  // Scroll to bottom of chatBox
  chatBox.scrollTop = chatBox.scrollHeight;
}

/**
 * Load a chat state into the chatBox. Append it to the current chat.
 * @param {object} chatState the chat State to append.
 */
function loadChatStateInChatBox(chatState) {
  for (let exchange of chatState.messages) {
    makeExchangeFromPrompt(exchange.prompt, gmUsername);
    addAnswer(exchange.answer, chatState.user.username);
  }
}

// --- On page load ---

window.onload = function () {
  let fontSizeInput = document.getElementById("fontSizeInput");
  let sendBtn = document.getElementById("sendBtn");
  let getNextRoundPromptBtn = document.getElementById("getNextRoundPromptBtn");

  // Set default font size for all messages
  fontSizeInput.value = fontSize;

  loadChatStateInChatBox(chatState);

  // --- Event Listeners ---

  fontSizeInput.addEventListener("change", () => {
    fontSize = fontSizeInput.value;
    let elements = document.getElementsByClassName("messageContainer");
    console.debug(elements);
    for (let element of elements) {
      element.style.fontSize = fontSize + "px";
    }
  });

  // Event listeners for input
  sendBtn.addEventListener("click", submitInputForm);
  document.addEventListener("keydown", (event) => {
    if (event.ctrlKey && event.key == "Enter") {
      alert("Ctrl+Enter key pressed");
      sendBtn.click();
    }
  });

  // TEMP: get next round with button
  getNextRoundPromptBtn.addEventListener("click", getNextRoundPrompt);
};
