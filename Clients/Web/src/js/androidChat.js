"use strict";
import chatState from "./androidChatDefaultMessages.json" with { type: "json" };

const bodyTag = document.body;
const fontScaleInput = document.querySelector("#fontScale");
const chatBox = document.querySelector("#chatBox");
const inputForm = document.querySelector("#inputForm");

const baseSize = parseFloat(window.getComputedStyle(bodyTag).fontSize);

// Functions

function addExchange(exchange) {
  // Div containers
  const exchangeDiv = document.createElement("div");
  const answerDiv = document.createElement("div");
  const promptDiv = document.createElement("div");
  // Add styles
  exchangeDiv.classList.add("exchangeContainer");
  answerDiv.classList.add("messageContainer", "answerContainer");
  promptDiv.classList.add("messageContainer", "promptContainer");
  // Divs inside containers, aligned with flexbox
  const promptImg = document.createElement("img");
  promptImg.src = "androidIcon.svg";
  promptImg.alt = "androidIcon";
  promptImg.style.cssText = `
  width: 50px;
  height: 50px;
  `;
  const answerImg = promptImg.cloneNode(true);
  promptDiv.appendChild(promptImg);
  answerDiv.appendChild(answerImg);
  // Insert answer and prompt
  const promptPar = document.createElement("p");
  const answerPar = document.createElement("p");
  answerPar.innerText = exchange.answer;
  promptPar.innerText = exchange.prompt;
  promptDiv.appendChild(promptPar);
  answerDiv.appendChild(answerPar);
  // Add divs to document
  exchangeDiv.appendChild(promptDiv);
  exchangeDiv.appendChild(answerDiv);
  chatBox.appendChild(exchangeDiv);
}

// Event Listeners

window.addEventListener("load", () => {
  for (const exchange of chatState.messages) {
    console.log(exchange);
    addExchange(exchange);
  }
});

fontScaleInput.addEventListener("input", () => {
  const fontScale = fontScaleInput.value;
  bodyTag.style.fontSize = baseSize * fontScale + "px";
});

inputForm.addEventListener("submit", (event) => {
  event.preventDefault();
  const exchange = {
    prompt: event.target.textInput.value,
    answer: "Placeholder answer of bot",
  };
  console.log(event.target.textInput.value);
  addExchange(exchange);
});
