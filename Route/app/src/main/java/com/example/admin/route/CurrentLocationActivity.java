package com.example.admin.route;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class CurrentLocationActivity extends FragmentActivity implements GoogleMap.OnMyLocationChangeListener {

    Button mBtnFind, mBtnFindEnd, buttonVia, buttonAddMarker;
    GoogleMap mMap;
    EditText startPlace, endPlace;
    double latStart, lngStart, latEnd, lngEnd;
    String titleSent, notesStartSent, notesEndSent, path, pathEnd, pathVia1, pathVia2, notesVia1, notesVia2;
    String id;

    String currentPath, currentNotes;

    Marker marker;

    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_location);

        // Getting reference to the SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        // Getting reference to the Google Map
        mMap = mapFragment.getMap();
        // Enabling MyLocation Layer of Google Map
        mMap.setMyLocationEnabled(true);
        // Setting event handler for location change
        mMap.setOnMyLocationChangeListener(this);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            id = extras.getString("id");
        }
        if (id == "m0") {
            path = extras.getString("currentPath");
            notesStartSent = extras.getString("currentNotes");
            Toast.makeText(this, path + notesStartSent, Toast.LENGTH_LONG).show();
        } else if (id == "m1") {
            pathVia1 = extras.getString("currentPath");
            notesVia1 = extras.getString("currentNotes");
        } else if (id == "m2") {
            pathVia2 = extras.getString("currentPath");
            notesVia2 = extras.getString("currentNotes");
        } else if (id == "m3") {
            pathEnd = extras.getString("currentPath");
            notesEndSent = extras.getString("currentNotes");
        }

        // Setting a custom info window adapter for the google map
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
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
                TextView tvLat = (TextView) v.findViewById(R.id.tv_lat);
                // Getting reference to the TextView to set longitude
                TextView tvLng = (TextView) v.findViewById(R.id.tv_lng);
                // Setting the latitude
                tvLat.setText("Latitude:" + latLng.latitude);
                // Setting the longitude
                tvLng.setText("Longitude:" + latLng.longitude);
                id = marker.getId();
                Toast.makeText(CurrentLocationActivity.this, id, Toast.LENGTH_LONG).show();
                goTo_DetailActivity(v);

                // Returning the view containing InfoWindow contents
                return v;
            }
        });

        buttonVia = (Button) findViewById(R.id.btn_goVia);
        buttonVia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViaOrEnd();
            }
        });

        buttonAddMarker = (Button) findViewById(R.id.btn_addMarker);
        buttonAddMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (count == 0) {
                    marker = mMap.addMarker(new MarkerOptions().position(new LatLng(latStart, lngStart)).title(latStart + " : " + lngStart));
                    count++;
//                    goTo_DetailActivity(v);

                    // Showing InfoWindow on the GoogleMap
                    marker.showInfoWindow();

                }
                else if ( count > 0 && count < 3) {
                    // not yet implemented
                    marker = mMap.addMarker(new MarkerOptions().position(new LatLng(latStart, lngStart)).title(latStart + " : " + lngStart).draggable(true));
                    count++;
                } else {
                    Toast.makeText(CurrentLocationActivity.this, "You have exceeded maximum number of points. Please proceed to add End Point.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void goTo_DetailActivity(View view) {
        Intent myIntent = new Intent(this,DetailActivity.class);


        Toast.makeText(this, "in goTo_DetailActivity" + id, Toast.LENGTH_LONG).show();
        Log.w("kerine iddddd", id + "");

        if (id == "m0") {
            Bundle bundle = new Bundle();
            bundle.putString("id", "m0");
            bundle.putString("notesStartSent", notesStartSent);
            bundle.putString("path", path);
            bundle.putDouble("latStart", latStart);
            bundle.putDouble("latEnd",latEnd);
            Toast.makeText(this, "enter loop" + path + notesStartSent + latStart + latEnd + id, Toast.LENGTH_LONG).show();
        }
         else if (id == "m1") {
            Bundle bundle = new Bundle();
            bundle.putString("notesVia1", notesVia1);
            bundle.putString("pathVia1", pathVia1);
        }

        startActivity(myIntent);
    }

    @Override
    public void onMyLocationChange(Location location) {
        //startPlace = (EditText) findViewById(R.id.start_place);
        // Getting latitude of the current location
        latStart = location.getLatitude();
        // Getting longitude of the current location
        lngStart = location.getLongitude();
        // Creating a LatLng object for the current location
        LatLng latLng = new LatLng(latStart, lngStart);
        // Setting latitude and longitude in the TextEdit
        //startPlace.setText(latStart + "," + lngStart);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_current_location, menu);
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

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(CurrentLocationActivity.this);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Add a Point")) {

                    Intent intent = new Intent(CurrentLocationActivity.this, ViaActivity.class);

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

                    Intent intent = new Intent(CurrentLocationActivity.this, MainActivity.class);

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

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
}
