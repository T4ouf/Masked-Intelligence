const bodyTag = document.body;
const fontScaleInput = document.querySelector("#fontScale");

const baseSize = parseFloat(window.getComputedStyle(bodyTag).fontSize);

fontScaleInput.addEventListener("input", () => {
  const fontScale = fontScaleInput.value;
  bodyTag.style.fontSize = baseSize * fontScale + "px";
});
