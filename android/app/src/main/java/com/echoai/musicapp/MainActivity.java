package com.echoai.musicapp;

import android.os.Bundle;
import android.util.Log;
import com.farimarwat.youtubedlboom.YoutubeDL;
import com.farimarwat.youtubedlboom.YoutubeDLRequest;
import com.farimarwat.youtubedlboom.YoutubeDLResponse;

public class MainActivity extends BridgeActivity {

    private YoutubeDL youtubeDl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initYoutubeDL();
    }

    private void initYoutubeDL() {
        YoutubeDL.init(
            this,                    // appContext
            true,                    // withFfmpeg
            false,                   // withAria2c
            youtubeDl -> {
                this.youtubeDl = youtubeDl;
                Log.d("YT-DLP", "YoutubeDL initialized successfully");
            },
            error -> Log.e("YT-DLP", "Init failed", error)
        );
    }

    public class JSBridge {  // Capacitor bridge — non-static is fine

        public void downloadVideo(String url) {
            if (youtubeDl == null) {
                Log.e("YT-DLP", "YoutubeDL not initialized yet");
                return;
            }

            try {
                YoutubeDLRequest request = new YoutubeDLRequest(url);

                // CORRECT way (from manual)
                request.addOption(
                    "-o",
                    com.farimarwat.youtubedlboom.StoragePermissionHelper.downloadDir.getAbsolutePath() +
                    "/%(title)s.%(ext)s"
                );

                // Download (full signature)
                YoutubeDL.download(
                    request,
                    "download-" + System.currentTimeMillis(),
                    { percentage, elapsedTime, outputLine -> 
                        Log.d("YT-DLP", "Progress: " + percentage + "% | " + outputLine);
                    },
                    processId -> {
                        Log.d("YT-DLP", "Download started: " + processId);
                    },
                    response -> {
                        Log.d("YT-DLP", "Download finished: " + response.getOut());
                    },
                    error -> {
                        Log.e("YT-DLP", "Download failed", error);
                    }
                );

            } catch (Exception e) {
                Log.e("YT-DLP", "Download exception", e);
            }
        }
    }
}
