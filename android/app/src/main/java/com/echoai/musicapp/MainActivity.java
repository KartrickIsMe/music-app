package com.echoai.musicapp;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.getcapacitor.BridgeActivity;
import com.farimarwat.youtubedl.YoutubeDL;
import com.farimarwat.youtubedl.YoutubeDLRequest;

public class MainActivity extends BridgeActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init library
        try {
            YoutubeDL.init(this);
            Log.d("yt", "yt-dlp initialized");
        } catch (Exception e) {
            Log.e("yt", "init failed", e);
        }

        // JS bridge
        WebView webView = getBridge().getWebView();
        webView.addJavascriptInterface(new JSBridge(), "Android");
    }

    public class JSBridge {

        @JavascriptInterface
        public void download(String url) {

            new Thread(() -> {
                try {
                    Log.d("yt", "Downloading: " + url);

                    YoutubeDLRequest request = new YoutubeDLRequest(url);

                    // audio only
                    request.addOption("-f", "bestaudio[ext=m4a]/bestaudio");

                    // SAFE OUTPUT PATH (NO PERMISSIONS REQUIRED)
                    String outputDir = getExternalFilesDir(
                            Environment.DIRECTORY_DOWNLOADS
                    ).getAbsolutePath();

                    request.addOption("-o",
                            outputDir + "/%(title)s.%(ext)s"
                    );

                    YoutubeDL.getInstance().execute(request);

                    Log.d("yt", "Download completed");

                } catch (Exception e) {
                    Log.e("yt", "Download failed", e);
                }
            }).start();
        }
    }
}
