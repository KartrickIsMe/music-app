package com.echoai.musicapp;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;
import android.webkit.JavascriptInterface;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;

public class MainActivity extends BridgeActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBridge().getWebView().addJavascriptInterface(this,"Android");
    }
    
    @JavascriptInterface
    public void createAnswer(double dividend, double divisor, String path) {
        double answer = dividend/divisor;
        try {
            File file = new File(path);
            file.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(file);
            writer.write(String.valueOf(answer));
            writer.close();
        }
        catch(Exception e) {
            e.printStackTrace();
            return;
        }
        runOnUiThread(() -> getBridge().getWebView().evaluateJavascript("giveAnswer()",null));
    }
}