package com.echoai.musicapp;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import com.getcapacitor.BridgeActivity;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLRequest;
import com.yausername.youtubedl_android.ffmpeg.FFmpeg;

public class MainActivity extends BridgeActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            YoutubeDL.getInstance().init(this);
            FFmpeg.getInstance().init(this);
            Log.d("yt-dlp", "✅ Library initialized");
        } catch (Exception e) {
            Log.e("yt-dlp", "Init failed", e);
        }

        // Enable calling Java from JavaScript
        WebView webView = getBridge().getWebView();
        webView.addJavascriptInterface(new JsInterface(), "Android");
    }

    // Inner class to expose methods to JS
    public class JsInterface {
        @android.webkit.JavascriptInterface
        public void downloadYoutube(String url) {
            new Thread(() -> {
                try {
                    Log.d("yt-dlp", "Downloading: " + url);

                    YoutubeDLRequest request = new YoutubeDLRequest(url);
                    request.addOption("-x");                    // audio only
                    request.addOption("--audio-format", "mp3");
                    request.addOption("-o", "/sdcard/Download/%(title)s.%(ext)s");

                    YoutubeDL.getInstance().execute(request, (progress, etaInSeconds) -> {
                        Log.d("yt-dlp", "Progress: " + progress + "%");
                    });

                    Log.d("yt-dlp", "✅ Download completed!");
                } catch (Exception e) {
                    Log.e("yt-dlp", "❌ Download failed", e);
                }
            }).start();
        }
    }
}
