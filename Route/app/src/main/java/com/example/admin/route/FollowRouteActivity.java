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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
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

//reference:
//http://wptrafficanalyzer.in/blog/drawing-driving-route-directions-between-two-locations-using-google-directions-in-google-map-android-api-v2/

public class FollowRouteActivity extends FragmentActivity {

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
        setContentView(R.layout.activity_follow_route);

        // Initializing
        markerPoints = new ArrayList<LatLng>();

        //Instantiate Database
        db = new MyDB(this);

        long routeID = getIntent().getLongExtra("routeID", 0);

        System.out.println("routeID is: " + routeID);

        db.open();

        Cursor c = db.getRoute(routeID);

        startManagingCursor(c);

        c.moveToPosition(0);
        String columnCount = String.valueOf(c.getColumnCount());
        String startLoc = c.getString(2);
        String endLoc = c.getString(3);
        String via1Loc = c.getString(4);
        String via2Loc = c.getString(5);

        stopManagingCursor(c);
        db.close();

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
        // Enabling MyLocation Layer of Google Map
        googleMap.setMyLocationEnabled(true);
        // Getting LocationManager object from System Service LOCATION_SERVICE
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(startLat, startLng), 15));

        MarkerOptions options = new MarkerOptions();

        // Setting the position of the marker
        options.position(new LatLng(startLat, startLng));
        options.position(new LatLng(endLat, endLng));
        options.position(new LatLng(via1Lat, via1Lng));
        options.position(new LatLng(via2Lat, via2Lng));

        LatLng latLngStart = new LatLng(startLat, startLng);
        LatLng latLngEnd = new LatLng(endLat, endLng);
        LatLng latLngVia1 = new LatLng(via1Lat, via1Lng);
        LatLng latLngVia2 = new LatLng(via2Lat, via2Lng);

        drawStartMarkerWithCircle(latLngStart);
        drawEndMarkerWithCircle(latLngEnd);
        if (via1Loc != null) {
            drawVia1MarkerWithCircle(latLngVia1);
        }
        if (via2Loc != null) {
            drawVia2MarkerWithCircle(latLngVia2);
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

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                // Removes the existing marker from the Google Map
                //googleMap.clear();

                try {
                    googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                        @Override
                        public void onMyLocationChange(Location location) {
                            float[] distanceStart = new float[2];
                            float[] distanceEnd = new float[2];
                            float[] distanceVia1 = new float[2];
                            float[] distanceVia2 = new float[2];

                            Location.distanceBetween(location.getLatitude(), location.getLongitude(), mCircleStart.getCenter().latitude, mCircleStart.getCenter().longitude, distanceStart);
                            Location.distanceBetween(location.getLatitude(), location.getLongitude(), mCircleEnd.getCenter().latitude, mCircleEnd.getCenter().longitude, distanceEnd);
                            Location.distanceBetween(location.getLatitude(), location.getLongitude(), mCircleVia1.getCenter().latitude, mCircleVia1.getCenter().longitude, distanceVia1);
                            Location.distanceBetween(location.getLatitude(), location.getLongitude(), mCircleVia2.getCenter().latitude, mCircleVia2.getCenter().longitude, distanceVia2);

                            if (distanceStart[0] < mCircleStart.getRadius()) {
                                LayoutInflater inflater = getLayoutInflater();
                                View view = inflater.inflate(R.layout.customized_toast, (ViewGroup) findViewById(R.id.customizedToast));

                                TextView text = (TextView) view.findViewById(R.id.textView);
                                text.setText(notesStart);

                                final BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inSampleSize = 8;

                                ImageView image = (ImageView) view.findViewById(R.id.imageView);
                                Bitmap thumbnail = BitmapFactory.decodeFile(pathStart, options);
                                image.setImageBitmap(thumbnail);

                                Toast toast = new Toast(FollowRouteActivity.this);
                                toast.setDuration(Toast.LENGTH_LONG);
                                toast.setView(view);
                                toast.show();
                            } else if (distanceEnd[0] < mCircleEnd.getRadius()) {
                                LayoutInflater inflater = getLayoutInflater();
                                View view = inflater.inflate(R.layout.customized_toast, (ViewGroup) findViewById(R.id.customizedToast));

                                TextView text = (TextView) view.findViewById(R.id.textView);
                                text.setText(notesEnd);

                                ImageView image = (ImageView) view.findViewById(R.id.imageView);
                                Bitmap thumbnail = (BitmapFactory.decodeFile(pathEnd));
                                image.setImageBitmap(thumbnail);

                                Toast toast = new Toast(FollowRouteActivity.this);
                                toast.setDuration(Toast.LENGTH_LONG);
                                toast.setView(view);
                                toast.show();
                            } else if (distanceVia1[0] < mCircleVia1.getRadius()) {
                                LayoutInflater inflater = getLayoutInflater();
                                View view = inflater.inflate(R.layout.customized_toast, (ViewGroup) findViewById(R.id.customizedToast));

                                TextView text = (TextView) view.findViewById(R.id.textView);
                                text.setText(notesVia1);

                                ImageView image = (ImageView) view.findViewById(R.id.imageView);
                                Bitmap thumbnail = (BitmapFactory.decodeFile(pathVia1));
                                image.setImageBitmap(thumbnail);

                                Toast toast = new Toast(FollowRouteActivity.this);
                                toast.setDuration(Toast.LENGTH_LONG);
                                toast.setView(view);
                                toast.show();
                            } else if (distanceVia2[0] < mCircleVia2.getRadius()) {
                                LayoutInflater inflater = getLayoutInflater();
                                View view = inflater.inflate(R.layout.customized_toast, (ViewGroup) findViewById(R.id.customizedToast));

                                TextView text = (TextView) view.findViewById(R.id.textView);
                                text.setText(notesVia2);

                                ImageView image = (ImageView) view.findViewById(R.id.imageView);
                                Bitmap thumbnail = (BitmapFactory.decodeFile(pathVia2));
                                image.setImageBitmap(thumbnail);

                                Toast toast = new Toast(FollowRouteActivity.this);
                                toast.setDuration(Toast.LENGTH_LONG);
                                toast.setView(view);
                                toast.show();
                            } else {
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void drawStartMarkerWithCircle(LatLng position) {
        double radiusInMeters = 100.0;
        int strokeColor = 0xffff0000; //red outline
        int shadeColor = 0x44ff0000; //opaque red fill
        CircleOptions circleOptions = new CircleOptions().center(position).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
        mCircleStart = googleMap.addCircle(circleOptions);
        MarkerOptions markerOptions = new MarkerOptions().position(position);
        mMarker = googleMap.addMarker(markerOptions);
    }

    private void drawEndMarkerWithCircle(LatLng position) {
        double radiusInMeters = 100.0;
        int strokeColor = 0xffff0000; //red outline
        int shadeColor = 0x44ff0000; //opaque red fill
        CircleOptions circleOptions = new CircleOptions().center(position).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
        mCircleEnd = googleMap.addCircle(circleOptions);
        MarkerOptions markerOptions = new MarkerOptions().position(position);
        mMarker = googleMap.addMarker(markerOptions);
    }

    private void drawVia1MarkerWithCircle(LatLng position) {
        double radiusInMeters = 100.0;
        int strokeColor = 0xffff0000; //red outline
        int shadeColor = 0x44ff0000; //opaque red fill
        CircleOptions circleOptions = new CircleOptions().center(position).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
        mCircleVia1 = googleMap.addCircle(circleOptions);
        MarkerOptions markerOptions = new MarkerOptions().position(position);
        mMarker = googleMap.addMarker(markerOptions);
    }

    private void drawVia2MarkerWithCircle(LatLng position) {
        double radiusInMeters = 100.0;
        int strokeColor = 0xffff0000; //red outline
        int shadeColor = 0x44ff0000; //opaque red fill
        CircleOptions circleOptions = new CircleOptions().center(position).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
        mCircleVia2 = googleMap.addCircle(circleOptions);
        MarkerOptions markerOptions = new MarkerOptions().position(position);
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
            Log.d("Exception while downloading url", e.toString());
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
            PolylineOptions lineOptions = null;
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
            googleMap.addPolyline(lineOptions);
        }
    }
}
