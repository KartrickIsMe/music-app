package com.echoai.musicapp;

import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.getcapacitor.BridgeActivity;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_android.YoutubeDLRequest;
import com.yausername.youtubedl_android.ffmpeg.FFmpeg;

public class MainActivity extends BridgeActivity {

    private static final String TAG = "yt-dlp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            YoutubeDL.getInstance().init(this);
            FFmpeg.getInstance().init(this);
            Log.d(TAG, "Library initialized");
        } catch (YoutubeDLException e) {
            Log.e(TAG, "Initialization failed", e);
        }

        WebView webView = bridge.getWebView();
        webView.addJavascriptInterface(new JsBridge(), "Android");
    }

    public class JsBridge {

        @JavascriptInterface
        public void downloadYoutube(String url) {

            new Thread(() -> {
                try {
                    Log.d(TAG, "Downloading: " + url);

                    YoutubeDLRequest request = new YoutubeDLRequest(url);
                    request.addOption("-x");
                    request.addOption("--audio-format", "mp3");
                    request.addOption(
                            "-o",
                            "/sdcard/Download/%(title)s.%(ext)s"
                    );

                    YoutubeDL.getInstance().execute(
                            request,
                            (progress, etaInSeconds) ->
                                    Log.d(TAG, "Progress: " + progress + "%")
                    );

                    Log.d(TAG, "Download completed");

                } catch (Exception e) {
                    Log.e(TAG, "Download failed", e);
                }
            }).start();
        }
    }
}
