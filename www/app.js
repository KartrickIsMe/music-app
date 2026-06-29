document.getElementById("button").addEventListener("click", invokeReplyToJs)

function invokeReplyToJs() {
    window.Android.replyToJs()
}

function replyFromJava(message) {
    document.getElementById("textBox").textContent = message
}