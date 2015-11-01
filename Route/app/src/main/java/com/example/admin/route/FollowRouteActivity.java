package com.example.admin.route;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;

public class FollowRouteActivity extends FragmentActivity {

    GoogleMap googleMap;
    LocationManager locationManager;
    PendingIntent pendingIntent;
    double latStart, lngStart;
    Location location;
    SharedPreferences sharedPreferences;

    private Serializable escolas;
    private ProgressDialog dialog;
    private Circle mCircle;
    private Marker mMarker;

    double lat = 1.423787, lng = 103.838330;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_route);

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
                    //test outside
                    double mLatitude = lat;
                    double mLongitude = lng;

                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLatitude, mLongitude), 15));

                    MarkerOptions options = new MarkerOptions();

                    // Setting the position of the marker

                    options.position(new LatLng(mLatitude, mLongitude));

                    //googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

                    LatLng latLng = new LatLng(mLatitude, mLongitude);
                    drawMarkerWithCircle(latLng);

                    googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                        @Override
                        public void onMyLocationChange(Location location) {
                            float[] distance = new float[2];

                        /*
                        Location.distanceBetween( mMarker.getPosition().latitude, mMarker.getPosition().longitude,
                                mCircle.getCenter().latitude, mCircle.getCenter().longitude, distance);
                                */

                                Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                                    mCircle.getCenter().latitude, mCircle.getCenter().longitude, distance);

                            if (distance[0] > mCircle.getRadius()) {
                                Toast.makeText(getBaseContext(), "Outside, distance from center: " + distance[0] + " radius: " + mCircle.getRadius(), Toast.LENGTH_LONG).show();
                            } else {
                                Intent myIntent = new Intent(FollowRouteActivity.this,CameraActivity.class);
                                startActivity(myIntent);

                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            private void drawMarkerWithCircle(LatLng position) {
                double radiusInMeters = 200.0;
                int strokeColor = 0xffff0000; //red outline
                int shadeColor = 0x44ff0000; //opaque red fill

                CircleOptions circleOptions = new CircleOptions().center(position).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
                mCircle = googleMap.addCircle(circleOptions);

                MarkerOptions markerOptions = new MarkerOptions().position(position);
                mMarker = googleMap.addMarker(markerOptions);
            }
        });

    }
}
