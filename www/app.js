const urlInput  = document.getElementById('urlInput');
const playBtn   = document.getElementById('playBtn');
const status    = document.getElementById('status');
const songTitle = document.getElementById('songTitle');
const player    = document.getElementById('player');

// ── 1. User taps Play ─────────────────────────────────────────────────────
playBtn.addEventListener('click', () => {
  const url = urlInput.value.trim();
  if (!url) { status.textContent = 'Please paste a URL first.'; return; }

  status.textContent    = '⏳ Fetching stream URL…';
  songTitle.textContent = '';
  player.src            = '';
  playBtn.disabled      = true;

  // Hand off to Android – Java resolves the real stream URL
  window.Android.getStreamUrl(url);
});

// ── 2. Java calls this when the stream URL is ready ───────────────────────
window.onStreamUrl = function(streamUrl, title) {
  songTitle.textContent = title;
  status.textContent    = '▶ Playing';
  player.src            = streamUrl;
  player.play();
  playBtn.disabled      = false;
};

// ── 3. Java calls this on error ───────────────────────────────────────────
window.onStreamError = function(message) {
  status.textContent = '❌ ' + message;
  playBtn.disabled   = false;
};

