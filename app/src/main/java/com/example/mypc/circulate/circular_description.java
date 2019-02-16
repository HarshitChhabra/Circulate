package com.example.mypc.circulate;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

public class circular_description extends AppCompatActivity {

    Intent currentIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circular_description);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        WebView webView=(WebView)findViewById(R.id.displayContent);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Intent intent = new Intent(getApplicationContext(),circular_webview.class);
                intent.putExtra("url",getIntent().getStringExtra("url"));
                intent.putExtra("title",currentIntent.getStringExtra("title"));
                intent.putExtra("note",currentIntent.getStringExtra("note"));
                intent.putExtra("cirtime",currentIntent.getStringExtra("cirtime"));
                intent.putExtra("cirdate",currentIntent.getStringExtra("cirdate"));
                startActivity(intent);
            }
        });

        TextView cirdesView=(TextView)findViewById(R.id.circularDescriptionTextView);
        String data="";
        currentIntent=getIntent();
        data+="Name: "+currentIntent.getStringExtra("title")+"\n\n";
        data+="Note: "+currentIntent.getStringExtra("note")+"\n\n";
        data+="Time: "+currentIntent.getStringExtra("cirtime")+"\n\n";
        data+="Date: "+currentIntent.getStringExtra("cirdate")+"\n\n";
        cirdesView.setText(data);

        String url=getIntent().getStringExtra("url");
        if(!(url.contains(".pdf")||url.contains(".doc")||url.contains(".rtf"))) {
            webView.setWebViewClient(new WebViewClient());
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl(getIntent().getStringExtra("url"));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            Intent intent=new Intent(getApplicationContext(),chat_window.class);
            intent.putExtra("title",currentIntent.getStringExtra("title"));
            intent.putExtra("note",currentIntent.getStringExtra("note"));
            intent.putExtra("cirtime",currentIntent.getStringExtra("cirtime"));
            intent.putExtra("cirdate",currentIntent.getStringExtra("cirdate"));
            intent.putExtra("url",currentIntent.getStringExtra("url"));
            Log.i("checktriggered","yoyo");
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
