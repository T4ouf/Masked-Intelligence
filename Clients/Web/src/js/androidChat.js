import chatState from "./androidChatDefaultMessages.json" with { type: "json" };

const bodyTag = document.body;
const fontScaleInput = document.querySelector("#fontScale");
const chatBox = document.querySelector("#chatBox");
const inputForm = document.querySelector("#inputForm");

const baseSize = parseFloat(window.getComputedStyle(bodyTag).fontSize);

// Functions

function addMessage(message) {
  // Div containers
  const exchangeDiv = document.createElement("div");
  const messageDiv = document.createElement("div");
  const promptDiv = document.createElement("div");
  // Add styles
  exchangeDiv.classList.add("exchangeContainer");
  messageDiv.classList.add("messageContainer");
  promptDiv.classList.add("promptContainer");
  // Divs inside containers, aligned with flexbox
  const promptImg = document.createElement("img");
  promptImg.src = "androidIcon.svg";
  promptImg.alt = "androidIcon";
  promptImg.style.cssText = `
  width: 50px;
  height: 50px;
  `;
  promptDiv.appendChild(promptImg);
  // Insert answer and prompt
  const promptPar = document.createElement("p");
  messageDiv.innerText = message.answer;
  promptPar.innerText = message.prompt;
  promptDiv.appendChild(promptPar);
  // Add divs to document
  exchangeDiv.appendChild(promptDiv);
  exchangeDiv.appendChild(messageDiv);
  chatBox.appendChild(exchangeDiv);
}

// Event Listeners

window.addEventListener("load", () => {
  for (const message of chatState.messages) {
    console.log(message);
    addMessage(message);
  }
});

fontScaleInput.addEventListener("input", () => {
  const fontScale = fontScaleInput.value;
  bodyTag.style.fontSize = baseSize * fontScale + "px";
});

inputForm.addEventListener("submit", (event) => {
  event.preventDefault();
  const message = {
    prompt: "Some prompt",
    answer: event.target.textInput.value,
  };
  console.log(event.target.textInput.value);
  addMessage(message);
});
