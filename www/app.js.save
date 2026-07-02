const dividend = document.getElementById("dividend");
const divisor = document.getElementById("divisor");
const divide = document.getElementById("divide");
const answer = document.getElementById("answer");
const capPath = 'file:///data/data/com.echoai.musicapp/cache/answer.txt';
let webPath = null;
const nativePath = '/data/data/com.echoai.musicapp/cache/answer.txt';

function createAnswer() {
    window.Android.createAnswer(parseFloat(dividend.value),parseFloat(divisor.value),nativePath);
}

window.giveAnswer = async function() {
    answer.textContent = await readFile(capPath);
}

async function readFile(path) {
    webPath = Capacitor.convertFileSrc(path);
    const response = await fetch(webPath);
    const content = await response.text();
    return content;
}

divide.addEventListener("click", createAnswer);