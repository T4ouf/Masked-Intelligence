"use strict";
import chatState from "./androidChatDefaultMessages.json" with { type: "json" };
var fontSize = 16;

// Functions
function submitInputForm(event) {
  // Prevent default reload of page on submit
  let exchange = {
    prompt: textInput.value,
    answer: "Placeholder answer of bot...",
  };
  // After getting its value, clear text from textarea
  textInput.value = "";
  addExchange(exchange);
}

function addExchange(exchange) {
  let chatBox = document.getElementById("chatBox");
  // Div containers
  let exchangeDiv = document.createElement("div");
  let answerDiv = document.createElement("div");
  let promptDiv = document.createElement("div");
  // Add styles
  exchangeDiv.classList.add("exchangeContainer");
  answerDiv.classList.add("messageContainer", "answerContainer");
  promptDiv.classList.add("messageContainer", "promptContainer");
  // Divs inside containers, aligned with flexbox
  let promptImg = document.createElement("img");
  promptImg.src = "/rsc/img/icons/androidIcon.png";
  promptImg.alt = "androidIcon";
  promptImg.style.cssText = `
  width: 50px;
  height: 50px;
  `;
  let answerImg = promptImg.cloneNode(true);
  promptDiv.appendChild(promptImg);
  answerDiv.appendChild(answerImg);
  // Insert answer and prompt
  let promptPar = document.createElement("p");
  let answerPar = document.createElement("p");
  promptPar.classList.add("message", "prompt");
  answerPar.classList.add("message", "answer");
  answerPar.innerText = exchange.answer;
  promptPar.innerText = exchange.prompt;
  promptDiv.appendChild(promptPar);
  answerDiv.appendChild(answerPar);
  // Add divs to document
  exchangeDiv.appendChild(promptDiv);
  exchangeDiv.appendChild(answerDiv);
  chatBox.appendChild(exchangeDiv);
  // Scroll to bottom of chatBox
  chatBox.scrollTop = chatBox.scrollHeight;
}

window.onload = function () {
  let bodyTag = document.body;
  let fontSizeInput = document.getElementById("fontSizeInput");
  let sendBtn = document.getElementById("sendBtn");

  // Set default font size for all messages
  fontSizeInput.value = fontSize;

  for (let exchange of chatState.messages) {
    console.log(exchange);
    addExchange(exchange);
  }

  // Event Listeners

  fontSizeInput.addEventListener("change", () => {
    fontSize = fontSizeInput.value;
    let elements = document.getElementsByClassName("message");
    console.log(elements);
    for (let element of elements) {
      element.style.fontSize = fontSize + "px";
    }
  });

  sendBtn.addEventListener("click", submitInputForm);
  // textInput.addEventListener("keypress", (event) => {
  //   if (event.key === "Enter" && !event.shiftKey) {
  //     event.preventDefault();
  //     submitInputForm(event);
  //   }
  // });
};
