package com.example.admin.route;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

public class Via2Activity extends FragmentActivity {

    GoogleMap mMap;

    double latStart, lngStart, latEnd, lngEnd, latVia1, lngVia1, latVia2, lngVia2;

    String titleSent, notesStartSent, notesEndSent, notesVia1, notesVia2, path, pathEnd, pathVia1, pathVia2;

    EditText viaPlace2;
    ImageView viewImageVia2;
    Button b, mBtnVia2, buttonVia2;
    MyDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_via2);

        //Instantiate Database
        db = new MyDB(this);

        Bundle extras = getIntent().getExtras();

        titleSent = extras.getString("titleSent");
        notesStartSent = extras.getString("notesStartSent");
        notesEndSent = extras.getString("notesEndSent");
        notesVia1 = extras.getString("notesVia1");

        path = extras.getString("path");
        pathEnd = extras.getString("pathEnd");
        pathVia1 = extras.getString("pathVia1");

        latStart = extras.getDouble("latStart");
        lngStart = extras.getDouble("lngStart");
        latEnd = extras.getDouble("latEnd");
        lngEnd = extras.getDouble("lngEnd");

        latVia1 = extras.getDouble("latVia1");
        lngVia1 = extras.getDouble("lngVia1");

        Toast.makeText(this, "route title: " + titleSent + ", startPath : "+  path + ", startNotes :" + notesStartSent
                + ", endPath: " + pathEnd + ", endNotes: " + notesEndSent, Toast.LENGTH_LONG).show();

        Toast.makeText(this, "START LATLNG:" + String.format("%.6f", latStart) + String.format("%.6f", lngStart),Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "END LATLNG:" + String.format("%.6f", latEnd) + String.format("%.6f", lngEnd), Toast.LENGTH_LONG).show();
        Toast.makeText(this, "VIA_POINT_1 LATLNG:" + String.format("%.6f", latVia1) + String.format("%.6f", lngVia1), Toast.LENGTH_SHORT).show();


        b = (Button) findViewById(R.id.btnSelectPhotoVia2);
        viewImageVia2 = (ImageView) findViewById(R.id.viewImageStart);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        mBtnVia2 = (Button) findViewById(R.id.btn_show_via2);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMap = mapFragment.getMap();
        viaPlace2 = (EditText) findViewById(R.id.via_place2);

        mBtnVia2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Getting the place entered
                String locationVia2 = viaPlace2.getText().toString();

                if (locationVia2 == null || locationVia2.equals("")) {
                    Toast.makeText(getBaseContext(), "No Via Location Entered", Toast.LENGTH_SHORT).show();
                    return;
                }

                String url = "https://maps.googleapis.com/maps/api/geocode/json?";

                try {
                    // encoding special characters like space in the user input place
                    locationVia2 = URLEncoder.encode(locationVia2, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                String address = "address=" + locationVia2;
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
        buttonVia2 = (Button) findViewById(R.id.btn_goVia2);
        buttonVia2.setOnClickListener(new View.OnClickListener() {
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
                latVia2 = Double.parseDouble(hmPlace.get("lat"));
                Log.w("lat", latVia2 + "");
                // Getting longitude of the place
                lngVia2 = Double.parseDouble(hmPlace.get("lng"));
                Log.w("lng", lngVia2 + "");
                // Getting name
                String name = hmPlace.get("formatted_address");
                Log.w("name", name + "");
                LatLng latLng = new LatLng(latVia2, lngVia2);
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

    private void selectImage() {

        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(Via2Activity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, 1);
                } else if (options[item].equals("Choose from Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                File f = new File(Environment.getExternalStorageDirectory().toString());
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("temp.jpg")) {
                        f = temp;
                        break;
                    }
                }
                try {
                    Bitmap bitmap;
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

                    bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), bitmapOptions);

                    viewImageVia2.setImageBitmap(bitmap);

                    //pathVia2 = Environment.getExternalStorageDirectory().getAbsolutePath();
                    pathVia2 = "/sdcard/DCIM/Camera/";


                    f.delete();
                    OutputStream outFile = null;
                    File file = new File(pathVia2, String.valueOf(System.currentTimeMillis()) + ".jpg");
                    pathVia2 = pathVia2 + String.valueOf(System.currentTimeMillis()) + ".jpg";
                    try {
                        outFile = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outFile);
                        outFile.flush();
                        outFile.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == 2) {
                Uri selectedImage = data.getData();
                String[] filePath = {MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                pathVia2 = c.getString(columnIndex);
                c.close();
                Bitmap thumbnail = (BitmapFactory.decodeFile(pathVia2));
                Log.w("path of image", pathVia2 + "");
                viewImageVia2.setImageBitmap(thumbnail);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_via2, menu);
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
        final CharSequence[] options = {"Save", "Cancel"};

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(Via2Activity.this);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Save")) {

                    EditText notesVia = (EditText) findViewById(R.id.notePadVia2);
                    notesVia2 = notesVia.getText().toString();

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

                    String[] arrayVia1 = new String[4];
                    arrayVia1[0] = notesVia1;
                    arrayVia1[1] = pathVia1;
                    arrayVia1[2] = String.valueOf(latVia1);
                    arrayVia1[3] = String.valueOf(lngVia1);

                    String[] arrayVia2 = new String[4];
                    arrayVia2[0] = notesVia2;
                    arrayVia2[1] = pathVia2;
                    arrayVia2[2] = String.valueOf(latVia2);
                    arrayVia2[3] = String.valueOf(lngVia2);

                    String arrayStart_String = convertArrayToString(arrayStart);
                    String arrayEnd_String = convertArrayToString(arrayEnd);
                    String arrayVia1_String = convertArrayToString(arrayVia1);
                    String arrayVia2_String = convertArrayToString(arrayVia2);

                    Intent intent = new Intent(Via2Activity.this, RouteList.class);

                    db.open();

                    //need to include one more parameter
                    long id = db.insertRoute(titleSent, arrayStart_String, arrayEnd_String, arrayVia1_String, arrayVia2_String);
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
}
