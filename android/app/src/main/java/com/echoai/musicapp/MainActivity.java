package com.echoai.musicapp;

import android.os.Bundle;
import android.util.Log;

import com.getcapacitor.BridgeActivity;

import com.farimarwat.youtubedlboom.YoutubeDL;
import com.farimarwat.youtubedlboom.YoutubeDLRequest;
import com.farimarwat.youtubedlboom.YoutubeDLResponse;

public class MainActivity extends BridgeActivity {

    private YoutubeDL youtubeDl;   // Global instance

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initYoutubeDL();
    }

    private void initYoutubeDL() {
        YoutubeDL.init(
            this,                    // appContext
            true,                    // withFfmpeg = true (strongly recommended)
            false,                   // withAria2c = false
            youtubeDl -> {           // onSuccess
                this.youtubeDl = youtubeDl;
                Log.d("YT-DLP", "YoutubeDL initialized successfully");
            },
            error -> {               // onError
                Log.e("YT-DLP", "Init failed", error);
            }
        );
    }

    // Bridge to JS (Capacitor)
    public static class JSBridge {

        public void downloadVideo(String url) {
            if (youtubeDl == null) {
                Log.e("YT-DLP", "YoutubeDL not initialized yet");
                return;
            }

            try {
                YoutubeDLRequest request = new YoutubeDLRequest(url);

                // Important: Set output path
                request.addOption("-o", getExternalFilesDir(null) + "/%(title)s.%(ext)s");

                // Optional extras
                // request.addOption("--downloader", "ffmpeg");
                // request.addOption("--no-part");

                youtubeDl.download(
                    request,
                    "download-" + System.currentTimeMillis(),   // process ID
                    (percentage, elapsedTime, outputLine) -> {   // progress callback
                        Log.d("YT-DLP", "Progress: " + percentage + "% | " + outputLine);
                    },
                    processId -> {                               // onStart
                        Log.d("YT-DLP", "Download started: " + processId);
                    },
                    response -> {                                // onEnd
                        Log.d("YT-DLP", "Download finished: " + response.getOut());
                    },
                    error -> {                                   // onError
                        Log.e("YT-DLP", "Download failed", error);
                    }
                );

            } catch (Exception e) {
                Log.e("YT-DLP", "Download exception", e);
            }
        }
    }
}
