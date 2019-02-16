package com.example.mypc.circulate;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class circular_webview extends AppCompatActivity {

    Intent currentIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circular_webview);
        currentIntent=getIntent();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        WebView webView=(WebView)findViewById(R.id.circular_full_webview);
        webView.setWebViewClient(new WebViewClient());

        webView.getSettings().setJavaScriptEnabled(true);
        String googlelink="http://drive.google.com/viewerng/viewer?embedded=true&url=";
        String url=getIntent().getStringExtra("url");
        if(url.contains(".pdf")||url.contains(".doc")||url.contains(".rtf"))
            webView.loadUrl(googlelink+url);
        else {
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setBuiltInZoomControls(true);
            webView.getSettings().setUseWideViewPort(true);
            webView.loadUrl(url);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            Intent intent=new Intent(getApplicationContext(),circular_description.class);
            intent.putExtra("title",currentIntent.getStringExtra("title"));
            intent.putExtra("note",currentIntent.getStringExtra("note"));
            intent.putExtra("cirtime",currentIntent.getStringExtra("cirtime"));
            intent.putExtra("cirdate",currentIntent.getStringExtra("cirdate"));
            intent.putExtra("url",currentIntent.getStringExtra("url"));
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
