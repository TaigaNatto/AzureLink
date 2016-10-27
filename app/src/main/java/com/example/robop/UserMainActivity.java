package com.example.robop;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class UserMainActivity extends Activity {

    //EditText editText;
    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);
        Intent intent = getIntent();
        String userName = intent.getStringExtra("userName");
        webView = (WebView)findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        //webView.loadUrl("http://192.168.2.200/test/test.php?text="+userName);
        //ロボPでテストする
        webView.loadUrl("http://192.168.1.27/access/azure-web.php?text="+userName);

    }
}
