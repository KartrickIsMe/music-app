const URL = document.getElementById("url");
const GO = document.getElementById("go");
const PLAYER = document.getElementById("player");
const LOGGER = document.getElementById("logbox");

//call android to download audio
function callAudio() {
    let url = URL.value;
    GO.disabled = true;
    window.Android.executeAudioDownload(url);
}

//android calls this
window.playAudio = function(source) {
    const capSource = Capacitor.convertFileSrc(source);
    GO.disabled = false;
    PLAYER.src = capSource;
    PLAYER.load();
    PLAYER.play();
}

function log(event) {
    LOGGER.innerHTML = event;
    console.log(event);
}

window.logEvent = function (event) {
    log(event);
}

window.onInitialized = function () {
    GO.disabled = false;
}

//call a wrapper function
GO.addEventListener("click", callAudio);
GO.disabled = true;
