package com.example.admin.route;

import android.app.PendingIntent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ShowRouteActivity extends FragmentActivity {

    GoogleMap googleMap;
    LocationManager locationManager;
    PendingIntent pendingIntent;
    double latStart, lngStart;
    Location location;

    String url;
    String str_via1 = "", str_via2 = "";
    ArrayList<LatLng> markerPoints;

    private ArrayList<LatLng> arrayPoints = null;

    private Circle mCircleStart, mCircleEnd, mCircleVia1, mCircleVia2;
    private Marker mMarker;
    MyDB db;
    String[] startArray, endArray, via1Array, via2Array;
    double startLat, startLng, endLat, endLng, via1Lat, via1Lng, via2Lat, via2Lng;
    String notesStart, notesEnd, notesVia1, notesVia2, pathStart, pathEnd, pathVia1, pathVia2;

    public static String strSeparator = "__,__";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_route);

        // Initializing
        markerPoints = new ArrayList<LatLng>();

        //Instantiate Database
        db = new MyDB(this);
        long routeID = getIntent().getLongExtra("routeID", 0);
        db.open();

        Cursor c = db.getRoute(routeID);

        startManagingCursor(c);

        c.moveToPosition(0);
        String columnCount = String.valueOf(c.getColumnCount());
        String title = c.getString(1);
        String startLoc = c.getString(2);
        String endLoc = c.getString(3);
        final String via1Loc = c.getString(4);
        final String via2Loc = c.getString(5);

        stopManagingCursor(c);
        db.close();

        TextView textView = (TextView)findViewById(R.id.title);
        textView.setText(title);

        startArray = convertStringToArray(startLoc);
        notesStart = startArray[0];
        pathStart = startArray[1];
        startLat = Double.valueOf(startArray[2]);
        startLng = Double.valueOf(startArray[3]);

        endArray = convertStringToArray(endLoc);
        notesEnd = endArray[0];
        pathEnd = endArray[1];
        endLat = Double.valueOf(endArray[2]);
        endLng = Double.valueOf(endArray[3]);

        if (via1Loc != null) {
            via1Array = convertStringToArray(via1Loc);
            notesVia1 = via1Array[0];
            pathVia1 = via1Array[1];
            via1Lat = Double.valueOf(via1Array[2]);
            via1Lng = Double.valueOf(via1Array[3]);
        }

        if (via2Loc != null) {
            via2Array = convertStringToArray(via2Loc);
            notesVia2 = via2Array[0];
            pathVia2 = via2Array[1];
            via2Lat = Double.valueOf(via2Array[2]);
            via2Lng = Double.valueOf(via2Array[3]);
        }

        arrayPoints = new ArrayList<LatLng>();

        // Getting reference to the SupportMapFragment of activity_main.xml
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        // Getting GoogleMap object from the fragment
        googleMap = fm.getMap();

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(startLat, startLng), 15));

        MarkerOptions options = new MarkerOptions();

        // Setting the position of the marker
        options.position(new LatLng(startLat, startLng));
        options.position(new LatLng(endLat, endLng));
        options.position(new LatLng(via1Lat, via1Lng));
        options.position(new LatLng(via2Lat, via2Lng));

        final LatLng latLngStart = new LatLng(startLat, startLng);
        final LatLng latLngEnd = new LatLng(endLat, endLng);
        final LatLng latLngVia1 = new LatLng(via1Lat, via1Lng);
        final LatLng latLngVia2 = new LatLng(via2Lat, via2Lng);

        drawStartMarker(latLngStart);
        drawEndMarker(latLngEnd);
        if (via1Loc != null) {
            drawVia1Marker(latLngVia1);
        }
        if (via2Loc != null) {
            drawVia2Marker(latLngVia2);
        }

        if ((via1Loc == null) && (via2Loc == null)) { //start and end only
            // Getting URL to the Google Directions API
            url = getDirectionsUrl2Point(latLngStart, latLngEnd);
        } else if ((via1Loc != null) && (via2Loc == null)) { //start, end and via1
            url = getDirectionsUrl3Point(latLngStart, latLngEnd, latLngVia1);
        } else if ((via1Loc != null) && (via2Loc != null)) { //all 4 points
            url = getDirectionsUrl4Point(latLngStart, latLngEnd, latLngVia1, latLngVia2);
        }

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);

        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(latLngStart);
                builder.include(latLngEnd);
                if (via1Loc != null) {
                    builder.include(latLngVia1);
                }
                if (via2Loc != null) {
                    builder.include(latLngVia2);
                }
                LatLngBounds bounds = builder.build();
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20));
            }
        });

        // Setting a custom info window adapter for the google map
        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(Marker marker) {
                // Getting view from the layout file info_window_layout
                View v = getLayoutInflater().inflate(R.layout.activity_info, null);
                // Getting the position from the marker
                LatLng latLng = marker.getPosition();
                // Getting reference to the TextView to set latitude
//                TextView tvLat = (TextView) v.findViewById(R.id.tv_lat);
//                // Getting reference to the TextView to set longitude
//                TextView tvLng = (TextView) v.findViewById(R.id.tv_lng);

                TextView Notes = (TextView) v.findViewById(R.id.notes);
                ImageView Image = (ImageView) v.findViewById(R.id.image);
                // Setting the latitude
//                tvLat.setText("Latitude:" + latLng.latitude);
//                // Setting the longitude
//                tvLng.setText("Longitude:" + latLng.longitude);

                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 8;

                if (latLng.equals(latLngStart)) {
                    Notes.setText("Notes:" + notesStart);
                    Bitmap thumbnail = (BitmapFactory.decodeFile(pathStart, options));
                    Image.setImageBitmap(thumbnail);
                } else if (latLng.equals(latLngEnd)) {
                    Notes.setText("Notes:" + notesEnd);
                    Bitmap thumbnail = (BitmapFactory.decodeFile(pathEnd, options));
                    Image.setImageBitmap(thumbnail);
                } else if (latLng.equals(latLngVia1)) {
                    Notes.setText("Notes:" + notesVia1);
                    Bitmap thumbnail = (BitmapFactory.decodeFile(pathVia1, options));
                    Image.setImageBitmap(thumbnail);
                } else if (latLng.equals(latLngVia2)) {
                    Notes.setText("Notes:" + notesVia2);
                    Bitmap thumbnail = (BitmapFactory.decodeFile(pathVia2, options));
                    Image.setImageBitmap(thumbnail);
                }
                // Returning the view containing InfoWindow contents
                return v;
            }
        });
    }

    //references:
    //http://stackoverflow.com/questions/19076124/android-map-marker-color
    private void drawStartMarker(LatLng position) {
        MarkerOptions markerOptions = new MarkerOptions().position(position).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mMarker = googleMap.addMarker(markerOptions);
    }

    private void drawEndMarker(LatLng position) {
        MarkerOptions markerOptions = new MarkerOptions().position(position).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        mMarker = googleMap.addMarker(markerOptions);
    }

    private void drawVia1Marker(LatLng position) {
        MarkerOptions markerOptions = new MarkerOptions().position(position).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        mMarker = googleMap.addMarker(markerOptions);
    }

    private void drawVia2Marker(LatLng position) {
        MarkerOptions markerOptions = new MarkerOptions().position(position).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        mMarker = googleMap.addMarker(markerOptions);
    }

    public static String[] convertStringToArray(String str) {
        String[] arr = str.split(strSeparator);
        return arr;
    }

    private String getDirectionsUrl2Point(LatLng origin, LatLng dest) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;
        // Output format
        String output = "json";
        url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }

    private String getDirectionsUrl3Point(LatLng origin, LatLng dest, LatLng latLngVia1) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String str_via1 = latLngVia1.latitude + "," + latLngVia1.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest +"&"+ sensor + "&waypoints=" + str_via1;
        // Output format
        String output = "json";
        url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }

    private String getDirectionsUrl4Point(LatLng origin, LatLng dest, LatLng latLngVia1, LatLng latLngVia2) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        str_via1 = latLngVia1.latitude + "," + latLngVia1.longitude + "|";
        str_via2 = latLngVia2.latitude + "," + latLngVia2.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest +"&"+ sensor + "&waypoints=" + str_via1 + str_via2;
        // Output format
        String output = "json";
        url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }

    /**
     * A method to download json data from url
     */
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
            Log.d("Unable to download url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = new PolylineOptions();
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(3);
                lineOptions.color(Color.RED);
            }

            // Drawing polyline in the Google Map for the i-th route
            try {
                googleMap.addPolyline(lineOptions);
            }catch (Exception e)
            {}
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_route, menu);
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

    public void onClick_BacktoCallingActivity(View view){
        finish();
    }

}
