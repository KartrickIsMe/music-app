function downloadVideo() {
    const url = document.getElementById("url").value;

    if (!url) {
        alert("No URL");
        return;
    }

    if (window.Android && Android.download) {
        Android.download(url);
    } else {
        alert("Android bridge not available");
    }
}
