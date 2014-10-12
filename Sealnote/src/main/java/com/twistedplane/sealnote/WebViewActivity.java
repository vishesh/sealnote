package com.twistedplane.sealnote;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class WebViewActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);

        WebView wv = (WebView) findViewById(R.id.webview);
        wv.loadUrl("file:///android_asset/acknowledgements.html");
    }
}
