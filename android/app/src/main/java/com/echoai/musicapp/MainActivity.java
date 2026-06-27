package com.echoai.musicapp;

import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.getcapacitor.BridgeActivity;

// ── farimarwat / YoutubeDl-Boom (add to build.gradle.kts) ────────────────
//   implementation("io.github.farimarwat:youtubedl-boom:1.0.23")
//   implementation("io.github.farimarwat:youtubedl-boom-commons:1.2")
//
// ⚠ If Android Studio can't resolve these imports, open the .aar or browse
//   the GitHub source to confirm the exact package path.
import com.farimarwat.youtubedlboom.YoutubeDL;
import com.farimarwat.youtubedlboom.commons.VideoInfo;
import com.farimarwat.youtubedlboom.commons.VideoFormat;

import kotlin.Unit;        // required: Kotlin lambdas called from Java must return Unit.INSTANCE
import java.util.List;

public class MainActivity extends BridgeActivity {

    private static final String TAG = "MusicApp";

    // Set inside the init onSuccess callback
    private YoutubeDL youtubeDL;

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);   // Capacitor builds the WebView here

        initYoutubeDL();
        registerBridge();
    }

    // ── Step 1: start up the yt-dlp library ──────────────────────────────────
    private void initYoutubeDL() {
        // The library uses a Kotlin companion object, so Java calls it as
        // YoutubeDL.Companion.init(…).
        // If the method is annotated @JvmStatic, you can write YoutubeDL.init(…) instead.
        YoutubeDL.Companion.init(
            this,
            true,    // withFfmpeg  – needed to merge split audio+video streams
            false,   // withAria2c  – not needed here
            instance -> {
                // 'instance' is the live YoutubeDL object we'll use for every request
                youtubeDL = instance;
                Log.d(TAG, "YoutubeDL ready");
                return Unit.INSTANCE;   // ← always required when a Kotlin lambda returns Unit
            },
            error -> {
                Log.e(TAG, "YoutubeDL init failed: " + error.getMessage());
                return Unit.INSTANCE;
            }
        );
    }

    // ── Step 2: expose our Java methods to JavaScript as "window.Android" ────
    private void registerBridge() {
        WebView webView = getBridge().getWebView();
        webView.addJavascriptInterface(new AndroidBridge(), "Android");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Everything in this class is visible to JS as  window.Android.<method>
    // ─────────────────────────────────────────────────────────────────────────
    private class AndroidBridge {

        // JS calls: window.Android.getStreamUrl("https://youtube.com/watch?v=…")
        @JavascriptInterface
        public void getStreamUrl(String url) {

            if (youtubeDL == null) {
                sendError("YoutubeDL is still initialising – try again in a moment.");
                return;
            }

            // getInfo() runs yt-dlp in the background via Kotlin coroutines.
            // onSuccess / onError are delivered on the main thread.
            youtubeDL.getInfo(
                url,
                videoInfo -> {
                    String streamUrl = pickBestAudioUrl(videoInfo);
                    String title     = videoInfo.getTitle() != null ? videoInfo.getTitle() : "";

                    if (streamUrl.isEmpty()) {
                        sendError("No playable audio stream found.");
                    } else {
                        sendStreamUrl(streamUrl, title);
                    }
                    return Unit.INSTANCE;
                },
                error -> {
                    String msg = error.getMessage();
                    sendError(msg != null ? msg : "Unknown yt-dlp error");
                    return Unit.INSTANCE;
                }
            );
        }

        // Walk VideoInfo.formats looking for an audio-only stream first,
        // then fall back to any URL that exists.
        private String pickBestAudioUrl(VideoInfo videoInfo) {

            List<VideoFormat> formats = videoInfo.getFormats();
            if (formats != null) {

                // Pass 1 – audio-only (vcodec == "none" means no video track)
                for (VideoFormat fmt : formats) {
                    String vcodec = fmt.getVcodec();
                    String fmtUrl = fmt.getUrl();
                    if (fmtUrl != null && !fmtUrl.isEmpty()
                            && (vcodec == null || vcodec.equals("none"))) {
                        return fmtUrl;
                    }
                }

                // Pass 2 – any format with a URL (combined audio+video is fine too)
                for (VideoFormat fmt : formats) {
                    String fmtUrl = fmt.getUrl();
                    if (fmtUrl != null && !fmtUrl.isEmpty()) {
                        return fmtUrl;
                    }
                }
            }

            // Last resort: top-level url that yt-dlp picks as "best"
            String topUrl = videoInfo.getUrl();
            return topUrl != null ? topUrl : "";
        }
    }

    // ── Helpers: call back into JavaScript ───────────────────────────────────

    private void sendStreamUrl(String url, String title) {
        String safeUrl   = jsEscape(url);
        String safeTitle = jsEscape(title);
        runOnUiThread(() ->
            getBridge().getWebView().evaluateJavascript(
                "window.onStreamUrl('" + safeUrl + "', '" + safeTitle + "')", null
            )
        );
    }

    private void sendError(String message) {
        String safe = jsEscape(message != null ? message : "Unknown error");
        runOnUiThread(() ->
            getBridge().getWebView().evaluateJavascript(
                "window.onStreamError('" + safe + "')", null
            )
        );
    }

    // Escape backslashes then single-quotes so the JS string literals stay valid
    private String jsEscape(String s) {
        return s.replace("\\", "\\\\").replace("'", "\\'");
    }
}
