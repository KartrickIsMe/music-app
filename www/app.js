async function startDownload() {
    const urlInput = document.getElementById('youtubeUrl');
    const url = urlInput.value.trim();
    const status = document.getElementById('status');

    if (!url) {
        status.textContent = "Please paste a YouTube link";
        return;
    }

    status.textContent = "⏳ Sending to yt-dlp...";

    try {
        if (window.Android) {
            window.Android.downloadYoutube(url);     // This calls the Java method
            status.textContent = "✅ Download started! Check your Downloads folder.";
        } else {
            status.textContent = "Not running in Android app";
        }
    } catch (e) {
        status.textContent = "Error: " + e.message;
    }
}
