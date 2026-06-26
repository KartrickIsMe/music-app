package com.echoai.musicapp;

import android.os.Bundle;
import android.util.Log;

import com.getcapacitor.BridgeActivity;

// BOOM library imports (correct version)
import com.farimarwat.youtubedlboom.YoutubeDL;
import com.farimarwat.youtubedlboom.YoutubeDLRequest;

public class MainActivity extends BridgeActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // Initialize YouTubeDL engine
            YoutubeDL.getInstance().init(this);
        } catch (Exception e) {
            Log.e("YT-DLP", "Init failed", e);
        }
    }

    // Bridge to JS (Capacitor)
    public class JSBridge {

        public void downloadVideo(String url) {

            try {
                // Create a request object for the video URL
                YoutubeDLRequest request = new YoutubeDLRequest(url);

                // Optional: add arguments (you can tweak later)
                request.addOption("-f", "best");

                // Execute download
                YoutubeDL.getInstance().execute(request);

            } catch (Exception e) {
                Log.e("YT-DLP", "Download failed", e);
            }
        }
    }
}
