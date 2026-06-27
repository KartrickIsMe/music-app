const urlInput = document.getElementById('youtube-url');
const playButton = document.getElementById('play-button');
const statusText = document.getElementById('status');
const audioPlayer = document.getElementById('audio-player');
const logOutput = document.getElementById('log-output');
const clearLogBtn = document.getElementById('clear-log-btn');

function log(message, isError = false) {
  const time = new Date().toLocaleTimeString();
  const line = document.createElement('div');
  line.className = isError ? 'log-line error' : 'log-line';
  line.textContent = `[${time}] ${message}`;
  logOutput.appendChild(line);
  logOutput.scrollTop = logOutput.scrollHeight;
}

clearLogBtn.addEventListener('click', () => {
  logOutput.innerHTML = '';
});

playButton.addEventListener('click', () => {
  const youtubeUrl = urlInput.value.trim();

  if (!youtubeUrl) {
    statusText.textContent = 'Please paste a YouTube link first.';
    log('No URL entered.', true);
    return;
  }

  statusText.textContent = 'Downloading audio... this can take a few seconds.';
  playButton.disabled = true;
  log(`Requested download: ${youtubeUrl}`);

  // window.Android comes from MainActivity.java. It downloads the file to
  // internal storage and reports back via the callbacks below.
  window.Android.getAudioStreamUrl(youtubeUrl);
});

// Java calls this once the file is saved to internal storage.
// fileUri looks like "file:///data/user/0/.../audio/<id>.m4a".
window.onAudioReady = async function (fileUri) {
  log(`Downloaded: ${fileUri}`);

  // Capacitor's webview blocks raw file:// loads (mixed content /
  // "not allowed to load local resource"). convertFileSrc rewrites it
  // to a URL the webview is allowed to fetch.
  const playableUrl = window.Capacitor.convertFileSrc(fileUri);
  log(`Playing via: ${playableUrl}`);

  statusText.textContent = 'Playing!';
  playButton.disabled = false;

  audioPlayer.src = playableUrl;
  audioPlayer.load();

  try {
    await audioPlayer.play();
    statusText.textContent = 'Playing!';
  } catch (err) {
    console.error(err);
    statusText.textContent = 'Playback failed: ' + err.message;
    log('Playback failed: ' + err.message, true);
  }
};

window.onAudioError = function (message) {
  statusText.textContent = 'Error: ' + message;
  log('Error: ' + message, true);
  playButton.disabled = false;
};

// Forwarded yt-dlp progress lines from MainActivity.java.
window.onNativeLog = function (message) {
  log(message);
};
