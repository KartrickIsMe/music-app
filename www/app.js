const box = document.getElementById("input");
let number = null;
const display = document.getElementById("output");
const execute = document.getElementById("button");
let n = null;

function getValue() {
    number = parseFloat(box.value);
    n = number;
    sendToAndroid(n);
}

function sendToAndroid(n) {
    window.Android.replyToJs(n);
}

window.replyFromJava = function recieveReply(out){
    display.textContent = out;
}

execute.addEventListener("click", getValue);