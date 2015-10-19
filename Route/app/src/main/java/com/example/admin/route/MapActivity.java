package com.example.admin.route;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapActivity extends FragmentActivity {

    //references
    //http://wptrafficanalyzer.in/blog/locating-user-input-address-in-google-maps-android-api-v2-with-geocoding-api/

    GoogleMap map;
    ArrayList<LatLng> markerPoints;

    Button mBtnFind, mBtnFindEnd;
    GoogleMap mMap;
    EditText startPlace, endPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Bundle extras = getIntent().getExtras();

        String titleSent = extras.getString("titleSent");
        String notesStartSent = extras.getString("notesStartSent");
        String notesEndSent = extras.getString("notesEndSent");

        String path = extras.getString("path");
        String pathEnd = extras.getString("pathEnd");

        Toast.makeText(this, titleSent + notesStartSent + notesEndSent, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, path + pathEnd, Toast.LENGTH_SHORT).show();

        // Getting reference to the find button
        mBtnFind = (Button) findViewById(R.id.btn_show);
        mBtnFindEnd = (Button) findViewById(R.id.btn_showEnd);

        // Getting reference to the SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        // Getting reference to the Google Map
        mMap = mapFragment.getMap();

        // Getting reference to EditText
        startPlace = (EditText) findViewById(R.id.start_place);
        endPlace = (EditText) findViewById(R.id.end_place);

        // Setting click event listener for the find Start button
        mBtnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Getting the place entered
                String location = startPlace.getText().toString();

                if (location == null || location.equals("")) {
                    Toast.makeText(getBaseContext(), "No Start Location Entered", Toast.LENGTH_SHORT).show();
                    return;
                }

                String url = "https://maps.googleapis.com/maps/api/geocode/json?";

                try {
                    // encoding special characters like space in the user input place
                    location = URLEncoder.encode(location, "utf-8");

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                String address = "address=" + location;

                String sensor = "sensor=false";

                // url , from where the geocoding data is fetched
                url = url + address + "&" + sensor;

                // Instantiating DownloadTask to get places from Google Geocoding service
                // in a non-ui thread
                DownloadTask downloadTask = new DownloadTask();

                // Start downloading the geocoding places
                downloadTask.execute(url);
            }

        });

        // Setting click event listener for the find button
        mBtnFindEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Getting the place entered
                String locationEnd = endPlace.getText().toString();

                if (locationEnd == null || locationEnd.equals("")) {
                    Toast.makeText(getBaseContext(), "No End Location Entered", Toast.LENGTH_SHORT).show();
                    return;
                }

                String url = "https://maps.googleapis.com/maps/api/geocode/json?";

                try {
                    // encoding special characters like space in the user input place
                    locationEnd = URLEncoder.encode(locationEnd, "utf-8");

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                String address = "address=" + locationEnd;

                String sensor = "sensor=false";

                // url , from where the geocoding data is fetched
                url = url + address + "&" + sensor;

                // Instantiating DownloadTask to get places from Google Geocoding service
                // in a non-ui thread
                DownloadTask downloadTask = new DownloadTask();

                // Start downloading the geocoding places
                downloadTask.execute(url);
            }

        });
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            br.close();

        } catch (Exception e) {
            Log.d("Exception while downloading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /**
     * A class, to download Places from Geocoding webservice
     */
    private class DownloadTask extends AsyncTask<String, Integer, String> {

        String data = null;

        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... url) {
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result) {

            // Instantiating ParserTask which parses the json data from Geocoding webservice
            // in a non-ui thread
            ParserTask parserTask = new ParserTask();

            // Start parsing the places in JSON format
            // Invokes the "doInBackground()" method of the class ParseTask
            parserTask.execute(result);
        }
    }

    /**
     * A class to parse the Geocoding Places in non-ui thread
     */
    class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

        JSONObject jObject;

        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                GeocodeJSONParser parser = new GeocodeJSONParser();


                /** Getting the parsed data as a an ArrayList */
                places = parser.parse(jObject);

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return places;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(List<HashMap<String, String>> list) {

            // Clears all the existing markers
            mMap.clear();

            for (int i = 0; i < list.size(); i++) {

                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();

                // Getting a place from the places list
                HashMap<String, String> hmPlace = list.get(i);

                // Getting latitude of the place
                double lat = Double.parseDouble(hmPlace.get("lat"));

                Log.w("lat", lat + "");

                // Getting longitude of the place
                double lng = Double.parseDouble(hmPlace.get("lng"));

                Log.w("lng", lng + "");


                // Getting name
                String name = hmPlace.get("formatted_address");

                Log.w("name", name + "");


                LatLng latLng = new LatLng(lat, lng);

                // Setting the position for the marker
                markerOptions.position(latLng);

                // Setting the title for the marker
                markerOptions.title(name);

                // Placing a marker on the touched position
                mMap.addMarker(markerOptions);

                // Locate the first location
                if (i == 0)
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
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
}
