package com.echoai.musicapp;

import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.getcapacitor.BridgeActivity;

import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLRequest;

public class MainActivity extends BridgeActivity {

    private static final String TAG = "MusicApp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Capacitor builds the WebView here

        // Step 1: Unpack and start yt-dlp. This must happen once before any
        // other call into YoutubeDL. It only does real work on the very
        // first run after install; after that it's fast.
        try {
            YoutubeDL.getInstance().init(this);
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize yt-dlp", e);
        }

        // Step 2: Let www/app.js call into this class as window.Android.*
        registerBridge();
    }

    private void registerBridge() {
        WebView webView = getBridge().getWebView();
        webView.addJavascriptInterface(new AndroidBridge(), "Android");
    }

    // Every public method in here is reachable from JS as window.Android.<name>
    private class AndroidBridge {

        // Called from app.js like:
        //   window.Android.getAudioStreamUrl("https://youtu.be/...")
        @JavascriptInterface
        public void getAudioStreamUrl(String youtubeUrl) {

            // yt-dlp does network + CPU work, so this MUST NOT run on the
            // main/UI thread (the app would freeze, and Android would
            // throw a NetworkOnMainThreadException). A plain background
            // Thread is the simplest way to get off the main thread —
            // no extra libraries required.
            new Thread(() -> {
                try {
                    // Build the yt-dlp request for this video.
                    YoutubeDLRequest request = new YoutubeDLRequest(youtubeUrl);

                    // "-f bestaudio" tells yt-dlp: give me the best
                    // audio-only stream you can find, no video track.
                    // This is a format the HTML <audio> tag can stream
                    // and play directly, so no downloading/converting
                    // to a local file is needed.
                    request.addOption("-f", "bestaudio");

                    // Run yt-dlp (similar to --dump-json) and read the
                    // direct, playable URL straight off the result.
                    String streamUrl = YoutubeDL.getInstance().getInfo(request).getUrl();

                    if (streamUrl == null || streamUrl.isEmpty()) {
                        sendError("No audio stream found for this link.");
                    } else {
                        sendStreamUrl(streamUrl);
                    }

                } catch (Exception e) {
                    // Covers yt-dlp errors (bad/unsupported link, no
                    // internet, etc.) as well as anything unexpected.
                    Log.e(TAG, "Failed to extract audio stream", e);
                    sendError(e.getMessage() != null ? e.getMessage() : "Unknown error");
                }
            }).start();
        }
    }

    // ── Helpers: call back into JavaScript on the UI thread ──────────────

    private void sendStreamUrl(String url) {
        String safeUrl = jsEscape(url);
        runOnUiThread(() ->
            getBridge().getWebView().evaluateJavascript(
                "window.onAudioReady('" + safeUrl + "')", null
            )
        );
    }

    private void sendError(String message) {
        String safeMsg = jsEscape(message);
        runOnUiThread(() ->
            getBridge().getWebView().evaluateJavascript(
                "window.onAudioError('" + safeMsg + "')", null
            )
        );
    }

    // Escape characters that would otherwise break the JS string literal
    // we're building above (e.g. a quote inside an error message).
    private String jsEscape(String s) {
        return s.replace("\\", "\\\\").replace("'", "\\'");
    }
}
