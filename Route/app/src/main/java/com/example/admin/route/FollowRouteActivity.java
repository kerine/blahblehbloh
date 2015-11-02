package com.example.admin.route;

import android.app.PendingIntent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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

public class FollowRouteActivity extends FragmentActivity {

    GoogleMap googleMap;
    LocationManager locationManager;
    PendingIntent pendingIntent;
    double latStart, lngStart;
    Location location;

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
        startLng =  Double.valueOf(startArray[3]);

        endArray = convertStringToArray(endLoc);
        notesEnd = endArray[0];
        pathEnd = endArray[1];
        endLat =  Double.valueOf(endArray[2]);
        endLng =  Double.valueOf(endArray[3]);

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

        // Getting reference to the SupportMapFragment of activity_main.xml
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        // Getting GoogleMap object from the fragment
        googleMap = fm.getMap();
        // Enabling MyLocation Layer of Google Map
        googleMap.setMyLocationEnabled(true);
        // Getting LocationManager object from System Service LOCATION_SERVICE
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                // Removes the existing marker from the Google Map
                googleMap.clear();

                try {

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
                    drawVia1MarkerWithCircle(latLngVia1);
                    drawVia2MarkerWithCircle(latLngVia2);


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

                                TextView text = (TextView)view.findViewById(R.id.textView);
                                text.setText(notesStart);

                                ImageView image = (ImageView) view.findViewById(R.id.imageView);
                                Bitmap thumbnail = (BitmapFactory.decodeFile(pathStart));
                                image.setImageBitmap(thumbnail);

                                Toast toast = new Toast(FollowRouteActivity.this);
                                toast.setDuration(Toast.LENGTH_LONG);
                                toast.setView(view);
                                toast.show();
                            } else if (distanceEnd[0] < mCircleEnd.getRadius()) {
                                LayoutInflater inflater = getLayoutInflater();
                                View view = inflater.inflate(R.layout.customized_toast, (ViewGroup) findViewById(R.id.customizedToast));

                                TextView text = (TextView)view.findViewById(R.id.textView);
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

                                TextView text = (TextView)view.findViewById(R.id.textView);
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

                                TextView text = (TextView)view.findViewById(R.id.textView);
                                text.setText(notesVia2);

                                ImageView image = (ImageView) view.findViewById(R.id.imageView);
                                Bitmap thumbnail = (BitmapFactory.decodeFile(pathVia2));
                                image.setImageBitmap(thumbnail);

                                Toast toast = new Toast(FollowRouteActivity.this);
                                toast.setDuration(Toast.LENGTH_LONG);
                                toast.setView(view);
                                toast.show();
                            } else {
                                Toast.makeText(getBaseContext(), "Outside all points" , Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
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



        });

    }

    public static String[] convertStringToArray(String str){
        String[] arr = str.split(strSeparator);
        return arr;
    }
}
