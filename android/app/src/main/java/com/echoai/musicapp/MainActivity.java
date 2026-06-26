package com.echoai.musicapp;

import android.os.Bundle;
import android.util.Log;

import com.getcapacitor.BridgeActivity;

import com.farimarwat.youtubedlboom.YoutubeDL;
import com.farimarwat.youtubedlboom.YoutubeDLRequest;
// Import more if needed later: YoutubeDLResponse, VideoInfo, etc.

public class MainActivity extends BridgeActivity {

    private YoutubeDL youtubeDl;   // Global instance

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initYoutubeDL();
    }

    private void initYoutubeDL() {
        try {
            YoutubeDL.Companion.init(
                this,                    // Context
                true,                    // withFfmpeg (recommended)
                false,                   // withAria2c
                youtubeDL -> {           // onSuccess
                    youtubeDl = youtubeDL;
                    Log.d("YT-DLP", "YoutubeDL initialized successfully");
                    return null;
                },
                error -> {               // onError
                    Log.e("YT-DLP", "Init failed", error);
                    return null;
                }
            );
        } catch (Exception e) {
            Log.e("YT-DLP", "Exception during init", e);
        }
    }

    // Bridge to JS (Capacitor)
    public class JSBridge {

        public void downloadVideo(String url) {
            if (youtubeDl == null) {
                Log.e("YT-DLP", "YoutubeDL not initialized yet");
                return;
            }

            try {
                YoutubeDLRequest request = new YoutubeDLRequest(url);

                // Important: Set output path
                request.addOption("-o", getExternalFilesDir(null) + "/%(title)s.%(ext)s");

                // Optional useful options
                // request.addOption("-f", "best");
                // request.addOption("--no-part");        // Avoid .part files
                // request.addOption("--downloader", "ffmpeg");

                youtubeDl.download(
                    request,
                    "download-" + System.currentTimeMillis(),   // process ID
                    (percentage, elapsedTime, outputLine) -> {   // progress callback
                        Log.d("YT-DLP", "Progress: " + percentage + "% | " + outputLine);
                        return null;
                    },
                    processId -> {                               // onStart
                        Log.d("YT-DLP", "Download started: " + processId);
                        return null;
                    },
                    response -> {                                // onEnd
                        Log.d("YT-DLP", "Download finished: " + response.getOut());
                        return null;
                    },
                    error -> {                                   // onError
                        Log.e("YT-DLP", "Download failed", error);
                        return null;
                    }
                );

            } catch (Exception e) {
                Log.e("YT-DLP", "Download exception", e);
            }
        }
    }
}
