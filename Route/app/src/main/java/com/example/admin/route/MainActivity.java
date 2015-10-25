package com.example.admin.route;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    double latStart, lngStart, latEnd, lngEnd, latVia1, lngVia1, latVia2, lngVia2;

    String titleSent, notesStartSent, notesEndSent, notesVia1, notesVia2, path, pathEnd, pathVia1, pathVia2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            titleSent = extras.getString("titleSent");
            notesStartSent = extras.getString("notesStartSent");
            notesEndSent = extras.getString("notesEndSent");
            notesVia1 = extras.getString("notesVia1");
            notesVia2 = extras.getString("notesVia2");

            path = extras.getString("path");
            pathEnd = extras.getString("pathEnd");
            pathVia1 = extras.getString("pathVia1");
            pathVia2 = extras.getString("pathVia2");

            latStart = extras.getDouble("latStart");
            lngStart = extras.getDouble("lngStart");
            latEnd = extras.getDouble("latEnd");
            lngEnd = extras.getDouble("lngEnd");
            latVia1 = extras.getDouble("latVia1");
            lngVia1 = extras.getDouble("lngVia1");
            latVia2 = extras.getDouble("latVia2");
            lngVia2 = extras.getDouble("lngVia2");

            Toast.makeText(this, titleSent + notesStartSent + notesEndSent + notesVia1 + notesVia2, Toast.LENGTH_SHORT).show();
            Toast.makeText(this, path + pathEnd + pathVia1 + pathVia2, Toast.LENGTH_SHORT).show();
            Toast.makeText(this, String.format("%.6f", latStart) + String.format("%.6f", lngStart) +
                    String.format("%.6f", latEnd) + String.format("%.6f", lngEnd) +
                    String.format("%.6f", latVia1) + String.format("%.6f", lngVia1) +
                    String.format("%.6f", latVia2) + String.format("%.6f", lngVia2)
                    , Toast.LENGTH_SHORT).show();
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

    public void goTo_CurrentLocationActivity(View view) {
        Intent myIntent = new Intent(this,CurrentLocationActivity.class);
        startActivity(myIntent);
    }
}
