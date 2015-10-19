package com.example.admin.route;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    double latStart, lngStart, latEnd, lngEnd;

    String titleSent, notesStartSent, notesEndSent, path, pathEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            titleSent = extras.getString("titleSent");
            notesStartSent = extras.getString("notesStartSent");
            notesEndSent = extras.getString("notesEndSent");

            path = extras.getString("path");
            pathEnd = extras.getString("pathEnd");

            latStart = extras.getDouble("latStart");
            lngStart = extras.getDouble("lngStart");
            latEnd = extras.getDouble("latEnd");
            lngEnd = extras.getDouble("lngEnd");

            Toast.makeText(this, titleSent + notesStartSent + notesEndSent, Toast.LENGTH_SHORT).show();
            Toast.makeText(this, path + pathEnd, Toast.LENGTH_SHORT).show();
            Toast.makeText(this, String.format("%.6f", latStart) + String.format("%.6f", lngStart) + String.format("%.6f", latEnd) + String.format("%.6f", lngEnd), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void goTo_CameraActivity(View view) {
        Intent myIntent = new Intent(this,CameraActivity.class);
        startActivity(myIntent);
    }
}
