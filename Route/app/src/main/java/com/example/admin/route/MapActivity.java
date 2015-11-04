package com.example.admin.route;

import android.content.DialogInterface;
import android.content.Intent;
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
    //http://wptrafficanalyzer.in/blog/showing-current-location-using-onmylocationchangelistener-in-google-map-android-api-v2/
    ArrayList<LatLng> markerPoints;

    Button mBtnFind, mBtnFindEnd, buttonVia;
    GoogleMap mMap;
    EditText startPlace, endPlace;
    double latStart, lngStart, latEnd, lngEnd;
    String titleSent, notesStartSent, notesEndSent, path, pathEnd;
    MyDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //Instantiate Database
        db = new MyDB(this);

        Bundle extras = getIntent().getExtras();

        titleSent = extras.getString("titleSent");
        notesStartSent = extras.getString("notesStartSent");
        notesEndSent = extras.getString("notesEndSent");

        path = extras.getString("path");
        pathEnd = extras.getString("pathEnd");

        Toast.makeText(this, "route title: " + titleSent + ", startPath : "+  path + ", startNotes :" + notesStartSent
                + ", endPath: " + pathEnd + ", endNotes: " + notesEndSent, Toast.LENGTH_LONG).show();

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

                // Instantiating DownloadTask to get places from Google Geocoding service in a non-ui thread
                DownloadTask downloadTask = new DownloadTask();

                // Start downloading the geocoding places
                downloadTask.execute(url);
            }
        });

        // Setting click event listener for the find End button
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

                // Instantiating DownloadTask to get places from Google Geocoding service in a non-ui thread
                DownloadTaskEnd downloadTaskEnd = new DownloadTaskEnd();

                // Start downloading the geocoding places
                downloadTaskEnd.execute(url);
            }
        });

        buttonVia = (Button) findViewById(R.id.btn_goVia);
        buttonVia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViaOrEnd();
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
            Log.d("Could not download URL", e.toString());
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
                latStart = Double.parseDouble(hmPlace.get("lat"));
                Log.w("lat", latStart + "");
                // Getting longitude of the place
                lngStart = Double.parseDouble(hmPlace.get("lng"));
                Log.w("lng", lngStart + "");
                // Getting name
                String name = hmPlace.get("formatted_address");
                Log.w("name", name + "");
                LatLng latLng = new LatLng(latStart, lngStart);
                // Setting the position for the marker
                markerOptions.position(latLng);

                // Setting the title for the marker
                markerOptions.title(name);

                // Placing a marker on the touched position
                mMap.addMarker(markerOptions);

                // Locate the first location
                if (i == 0) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            }
        }
    }

    /**
     * A class, to download End Places from Geocoding webservice
     */
    private class DownloadTaskEnd extends AsyncTask<String, Integer, String> {

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
            ParserTaskEnd parserTaskEnd = new ParserTaskEnd();

            // Start parsing the places in JSON format
            // Invokes the "doInBackground()" method of the class ParseTask
            parserTaskEnd.execute(result);
        }
    }

    /**
     * A class to parse the Geocoding Places in non-ui thread
     */
    class ParserTaskEnd extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

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
                latEnd = Double.parseDouble(hmPlace.get("lat"));
                Log.w("lat", latEnd + "");
                // Getting longitude of the place
                lngEnd = Double.parseDouble(hmPlace.get("lng"));
                Log.w("lng", lngEnd + "");
                // Getting name
                String name = hmPlace.get("formatted_address");
                Log.w("name", name + "");
                LatLng latLng = new LatLng(latEnd, lngEnd);
                // Setting the position for the marker
                markerOptions.position(latLng);

                // Setting the title for the marker
                markerOptions.title(name);

                // Placing a marker on the touched position
                mMap.addMarker(markerOptions);

                // Locate the first location
                if (i == 0)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
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


    private void ViaOrEnd() {
        final CharSequence[] options = {"Add a Point", "Save", "Cancel"};

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MapActivity.this);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Add a Point")) {

                    Intent intent = new Intent(MapActivity.this, ViaActivity.class);

                    Bundle bundle = new Bundle();
                    bundle.putString("titleSent", titleSent);
                    bundle.putString("notesStartSent", notesStartSent);
                    bundle.putString("notesEndSent", notesEndSent);

                    bundle.putString("path", path);
                    bundle.putString("pathEnd", pathEnd);

                    bundle.putDouble("latStart", latStart);
                    bundle.putDouble("lngStart", lngStart);
                    bundle.putDouble("latEnd", latEnd);
                    bundle.putDouble("lngEnd", lngEnd);

                    intent.putExtras(bundle);

                    int requestCode = 1;
                    startActivityForResult(intent, requestCode);

                    startActivity(intent);

                } else if (options[item].equals("Save")) {

                    String[] arrayStart = new String[4];
                    arrayStart[0] = notesStartSent;
                    arrayStart[1] = path;
                    arrayStart[2] = String.valueOf(latStart);
                    arrayStart[3] = String.valueOf(lngStart);

                    String[] arrayEnd = new String[4];
                    arrayEnd[0] = notesEndSent;
                    arrayEnd[1] = pathEnd;
                    arrayEnd[2] = String.valueOf(latEnd);
                    arrayEnd[3] = String.valueOf(lngEnd);

                    String arrayStart_String = convertArrayToString(arrayStart);
                    String arrayEnd_String = convertArrayToString(arrayEnd);

                    Intent intent = new Intent(MapActivity.this, RouteList.class);

                    //store title, path, notesStartSent, latStart, lngStart, pathEnd, notesEndSent, pathEnd, latEnd, lngEnd

                    db.open();
                    long id = db.insertRoute(titleSent, arrayStart_String, arrayEnd_String);
//                     if(id > 0){
//                    Toast.makeText(MapActivity.this, "Add successful.", Toast.LENGTH_LONG).show();
//                     }
//                     else
//                    Toast.makeText(MapActivity.this, "Add failed.", Toast.LENGTH_LONG).show();
                    db.close();


                    startActivity(intent);


                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public static String strSeparator = "__,__";

    public static String convertArrayToString(String[] array){
        String str = "";
        for (int i = 0;i<array.length; i++) {
            str = str+array[i];
            // Do not append comma at the end of last element
            if(i<array.length-1){
                str = str+strSeparator;
            }
        }
        return str;
    }

    public static String[] convertStringToArray(String str){
        String[] arr = str.split(strSeparator);
        return arr;
    }


}
