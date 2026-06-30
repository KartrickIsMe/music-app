package com.echoai.musicapp;

import android.os.Bundle;
import android.webkit.JavascriptInterface;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Open the door: JS can now call window.Android.sayHello()
        getBridge().getWebView().addJavascriptInterface(this, "Android");
    }

    // JS calls this
    @JavascriptInterface
    public void sayHello() {
        // Java calls back into JS
        runOnUiThread(() ->
            getBridge().getWebView().evaluateJavascript(
                "window.onReply('Hello from Java!')", null
            )
        );
    }
}