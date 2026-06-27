// app.js
// Talks to MainActivity.java (exposed as window.Android) and plays
// whatever audio stream URL it hands back.

// Grab the page elements we need.
const urlInput = document.getElementById('youtube-url');
const playButton = document.getElementById('play-button');
const statusText = document.getElementById('status');
const audioPlayer = document.getElementById('audio-player');

// 1) User taps "Play Audio" -> ask the native (Java) side to extract
//    a playable audio URL from the YouTube link.
playButton.addEventListener('click', () => {
  const youtubeUrl = urlInput.value.trim();

  if (!youtubeUrl) {
    statusText.textContent = 'Please paste a YouTube link first.';
    return;
  }

  statusText.textContent = 'Fetching audio... this can take a few seconds.';
  playButton.disabled = true;

  // window.Android comes from the @JavascriptInterface registered in
  // MainActivity.java. This call is "fire and forget" on the JS side —
  // Java does the work in the background and reports back later through
  // window.onAudioReady(...) or window.onAudioError(...) below.
  window.Android.getAudioStreamUrl(youtubeUrl);
});

// 2) Java calls this when it found a playable audio stream URL.
window.onAudioReady = function (streamUrl) {
  statusText.textContent = 'Playing!';
  playButton.disabled = false;

  audioPlayer.src = streamUrl;
  audioPlayer.play();
};

// 3) Java calls this if something went wrong (bad link, no internet,
//    video unavailable, etc.).
window.onAudioError = function (message) {
  statusText.textContent = 'Error: ' + message;
  playButton.disabled = false;
};

