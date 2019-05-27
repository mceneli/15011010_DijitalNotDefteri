package com.muhammet.notepad.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.webkit.WebView;

public class WebViewInitState {
    private static WebViewInitState instance = new WebViewInitState();
    private boolean isInitialized = false;
    
    private WebViewInitState() {}

    public static WebViewInitState getInstance() {
        return instance;
    }

    public void initialize(Context context) {
        if(isInitialized) return;

        SharedPreferences pref = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_PRIVATE);
        if(pref.getBoolean("markdown", false) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WebView webView = new WebView(context);
            webView.loadUrl("about:blank");

            isInitialized = true;
        }
    }
}