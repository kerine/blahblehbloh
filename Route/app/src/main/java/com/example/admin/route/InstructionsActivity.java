package com.example.admin.route;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.*;

public class InstructionsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* Make the app full screen*/
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        /* Find WebView element by its id*/
        WebView webView = (WebView) findViewById(R.id.webView);
        webView.setWebChromeClient(new WebChromeClient());


        /* Create new settings for our WebView element */
        WebSettings webSettings = webView.getSettings();

        /* Enable JavaScript */
        webSettings.setJavaScriptEnabled(true);

        /* Enable localStorage */
        webSettings.setDomStorageEnabled(true);

        /* Set scrollbars to be inside the app */
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        /* Define path to your local Web assets */
        /* Using the name "android_asset" means that
        the system will look for a file named "assets"
        at app/src/main, the same folder this file is in. */
        webView.loadUrl("file:///android_asset/www/myHTML.html");
    }

    public void onClick_BacktoCallingActivity(View view){
        finish();
    }
}
