package com.echoai.musicapp;

import com.getcapacitor.BridgeActivity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;

import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_android.YoutubeDLRequest;
import com.yausername.ffmpeg.FFmpeg;
import com.yausername.youtubedl_android.mapper.VideoInfo;
import com.yausername.youtubedl_android.YoutubeDL.UpdateChannel;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.os.Environment;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.MediaStore;
import android.content.ContentResolver;

public class MainActivity extends BridgeActivity {
    
    final String YTTAG = "[ytdlplib]";
    String songName = "title";
    String intSongName = "id";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBridge().getWebView().addJavascriptInterface(this,"Android");
        new Thread(() -> {
            try {
                YoutubeDL.getInstance().init(this);
                FFmpeg.getInstance().init(this);
            }
            catch (YoutubeDLException e) {
                sendLog(YTTAG + e.getMessage());
            }
            
            try {
                YoutubeDL.getInstance().updateYoutubeDL(this, UpdateChannel._STABLE);
            }
            catch (Exception e) {
                sendLog(e.getMessage());
            }
            runOnUiThread(() -> getBridge().getWebView().evaluateJavascript("onInitialized()",null));
        }).start();
    }
    
    @JavascriptInterface
    public void executeAudioDownload(String URL) {
        try {
            File intFile = getCacheDir();
            YoutubeDLRequest request = new YoutubeDLRequest(URL);
            //request.addOption("--extract-audio" , "--audio-format"  , "mp3" , "--output" , intFile.getAbsolutePath() + "/" + "%(title)s.%(ext)s");
            VideoInfo songInfo = YoutubeDL.getInstance().getInfo(URL);
            intSongName = songInfo.getId() + ".mp3";
            songName = songInfo.getTitle() + ".mp3";
            request.addOption("--extract-audio");
            request.addOption("--audio-format" , "mp3");
            request.addOption("--output" , intFile.getAbsolutePath() + "/" + "%(id)s.%(ext)s");
            String processId = intSongName;
            YoutubeDL.getInstance().execute(request , processId ,(progress , etaInSeconds, outputLine) -> {
                sendLog(YTTAG + String.valueOf(progress) + " " + String.valueOf(etaInSeconds) +" " +String.valueOf(outputLine));});
            songToMusic();
            String safePath = org.json.JSONObject.quote(intFile.getAbsolutePath() + "/" + intSongName);
            runOnUiThread(() ->
            getBridge().getWebView().evaluateJavascript("playAudio("+safePath+")",null));
        }
        catch (YoutubeDLException e) {
            sendLog(YTTAG + e.getMessage());
        }
        catch (Exception e) { 
            sendLog(YTTAG + e.getMessage());
        }
    }
    
    void songToMusic() {
        File song = new File(getCacheDir(), intSongName);
        
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, songName);
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg");
        values.put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC);
        
        ContentResolver resolver = getContentResolver();
        
        Uri uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
        if(uri == null) {
            return;
        }
        try (InputStream read = new FileInputStream(song);
             OutputStream write = resolver.openOutputStream(uri))
         {
             read.transferTo(write);
         }
         catch (IOException e) {
             sendLog(YTTAG + e.getMessage());
         }
    }
    
    public void sendLog(String text) {
        String safe = org.json.JSONObject.quote(text);
        runOnUiThread(() -> getBridge().getWebView().evaluateJavascript("logEvent("+ safe  +")", null));
    }
}