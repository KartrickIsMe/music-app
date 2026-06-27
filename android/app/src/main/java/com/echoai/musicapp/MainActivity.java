package com.echoai.musicapp;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.getcapacitor.BridgeActivity;

import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLRequest;
import com.yausername.youtubedl_android.YoutubeDLResponse;
import com.yausername.youtubedl_android.UpdateChannel;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kotlin.Unit;

public class MainActivity extends BridgeActivity {

    private static final String TAG = "MusicApp";

    // Pulls the saved file's path out of yt-dlp's stdout, e.g.:
    //   [download] Destination: /data/.../audio/dQw4w9WgXcQ.m4a
    //   [download] /data/.../audio/dQw4w9WgXcQ.m4a has already been downloaded
    private static final Pattern DEST_PATTERN = Pattern.compile(
            "Destination:\\s*(.+)|\\[download\\]\\s*(.+?)\\s+has already been downloaded"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState); // Capacitor builds the WebView here

    	registerBridge();

    // init() and updateYoutubeDL() both touch the network/filesystem,
    // so they must run off the main thread.
    		new Thread(() -> {
        		try {
            			YoutubeDL.getInstance().init(this);
        		} catch (Exception e) {
            			Log.e(TAG, "Failed to initialize yt-dlp", e);
        		}

        		try {
            		// Pulls the latest yt-dlp release, which keeps the YouTube
            		// n-challenge/signature logic current. This is what fixes
            		// the 403 you're seeing.
            			YoutubeDL.getInstance().updateYoutubeDL(this, UpdateChannel.STABLE);
            			Log.d(TAG, "yt-dlp updated");
        		} catch (Exception e) {
            			Log.e(TAG, "Failed to update yt-dlp", e);
        		}
    		}).start();
	}

    private void registerBridge() {
        WebView webView = getBridge().getWebView();
        webView.addJavascriptInterface(new AndroidBridge(), "Android");
    }

    private class AndroidBridge {

        @JavascriptInterface
        public void getAudioStreamUrl(String youtubeUrl) {
            new Thread(() -> {
                try {
                    sendLog("Starting download: " + youtubeUrl);

                    File outDir = new File(getFilesDir(), "audio");
                    if (!outDir.exists()) outDir.mkdirs();

                    YoutubeDLRequest request = new YoutubeDLRequest(youtubeUrl);
                    request.addOption("-f", "bestaudio[ext=m4a]/bestaudio");
                    // %(id)s.%(ext)s -> stable, unique filename per video.
                    request.addOption("-o", outDir.getAbsolutePath() + "/%(id)s.%(ext)s");

                    String processId = "audio-dl-" + System.currentTimeMillis();

                    YoutubeDLResponse response = YoutubeDL.getInstance().execute(
                            request,
                            processId,
                            (progress, etaInSeconds, line) -> {
                                sendLog(String.format("%.0f%% — %s", progress, line));
                                return Unit.INSTANCE; // required: this callback returns Unit, not void
                            }
                    );

                    String filePath = extractFilePath(response.getOut());
                    if (filePath == null) {
                        sendError("Download finished but couldn't locate the output file.");
                        return;
                    }

                    File downloadedFile = new File(filePath);
                    if (!downloadedFile.exists()) {
                        sendError("Downloaded file missing on disk: " + filePath);
                        return;
                    }

                    sendLog("Saved to internal storage: " + downloadedFile.getAbsolutePath());

                    // Hand JS a file:// URI. app.js converts it with
                    // Capacitor.convertFileSrc() before playback.
                    String fileUri = Uri.fromFile(downloadedFile).toString();
                    sendStreamUrl(fileUri);

                } catch (Exception e) {
                    Log.e(TAG, "Failed to download audio", e);
                    sendLog("ERROR: " + e.getMessage());
                    sendError(e.getMessage() != null ? e.getMessage() : "Unknown error");
                }
            }).start();
        }
    }

    private String extractFilePath(String stdout) {
        if (stdout == null) return null;
        String lastMatch = null;
        Matcher m = DEST_PATTERN.matcher(stdout);
        while (m.find()) {
            lastMatch = m.group(1) != null ? m.group(1) : m.group(2);
        }
        return lastMatch != null ? lastMatch.trim() : null;
    }

    // ── Helpers: call back into JavaScript on the UI thread ──────────────

    private void sendStreamUrl(String fileUri) {
        String safeUrl = jsEscape(fileUri);
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

    // Pushes a line into the on-screen log panel in index.html.
    private void sendLog(String message) {
        Log.d(TAG, message); // still goes to Logcat too
        String safeMsg = jsEscape(message);
        runOnUiThread(() ->
            getBridge().getWebView().evaluateJavascript(
                "window.onNativeLog('" + safeMsg + "')", null
            )
        );
    }

    private String jsEscape(String s) {
        return s.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", " ")
                .replace("\r", "");
    }
}
