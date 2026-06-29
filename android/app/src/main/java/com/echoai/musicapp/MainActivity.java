package com.echoai.musicapp;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;
import android.webkit.JavascriptInterface;

public class MainActivity extends bridgeActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBridge().getWebView().getJavascriptInterface(new Android(), "Android");
    }
    
    private class Android {
        @JavascriptInterface
        void replyToJs(){
            getBridge().getWebView().evaluateJavascript("window.replyFromJava('Fuck You!')", null);
        }
    }
}