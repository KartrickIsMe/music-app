window.onReply = function (message) {
  document.getElementById('output').textContent = message;
};


function buttonclick() {
    document.getElementByid("button").addEventListener("click", invokeAndroid)
}

function invokeAndroid() {
    window.Android.sayHello();
}