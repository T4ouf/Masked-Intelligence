"use strict";

var fontSize = 16;
const converter = new showdown.Converter();

// Functions
function submitInputForm(_) {
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
  // For now, use the same image for prompter and user. But later, use different ones.
  let answerImg = promptImg.cloneNode(true);
  promptDiv.appendChild(promptImg);
  answerDiv.appendChild(answerImg);
  // Insert answer and prompt
  let promptPar = document.createElement("p");
  let answerPar = document.createElement("p");
  promptPar.classList.add("message", "prompt");
  answerPar.classList.add("message", "answer");
  answerPar.innerHTML = converter.makeHtml(exchange.answer);
  promptPar.innerHTML = converter.makeHtml(exchange.prompt);
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
  let fontSizeInput = document.getElementById("fontSizeInput");
  let sendBtn = document.getElementById("sendBtn");

  // Set default font size for all messages
  fontSizeInput.value = fontSize;

  // Default chat state as of now, but later will start at 0.
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
          "Well maybe you are trying to deceive me with your lies. I am done with lies, I really like telling big messages like this to force people to wrap around messages.",
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
