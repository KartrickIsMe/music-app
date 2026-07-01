package com.echoai.musicapp;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;
import android.webkit.JavascriptInterface;

public class MainActivity extends BridgeActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBridge().getWebView().addJavascriptInterface(this, "Android");
    }
    
    @JavascriptInterface
    public void replyToJs(double n) {
        double result = Math.pow(n,2);
        runOnUiThread(() -> 
            getBridge().getWebView().evaluateJavascript("window.replyFromJava("+result+")", null));
    }
}