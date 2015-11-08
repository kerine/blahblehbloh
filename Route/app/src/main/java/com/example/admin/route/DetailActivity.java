package com.example.admin.route;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DetailActivity extends Activity {

    double latStart, lngStart, latEnd, lngEnd, latVia1, lngVia1, latVia2, lngVia2;

    String titleSent, notesStartSent, notesEndSent, notesVia1, notesVia2, path, pathEnd, pathVia1, pathVia2;

    String currentPath, currentNotes;

    String id;

    ImageView viewImageDetail;
    Button b, bu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            id = extras.getString("id");
            currentPath = extras.getString("path");
            currentNotes = extras.getString("notesStartSent");
            latStart = extras.getDouble("latStart");
            latEnd = extras.getDouble("latEnd");
//            Toast.makeText(this, "Detail activity = " + currentPath + currentNotes + latStart + latEnd + id, Toast.LENGTH_LONG).show();

        }

        viewImageDetail = (ImageView) findViewById(R.id.viewImageDetail);
        EditText notes = (EditText) findViewById(R.id.notePadDetail);
        notes.setText(currentNotes);


        b = (Button) findViewById(R.id.btnSelectPhotoDetail);
        viewImageDetail = (ImageView) findViewById(R.id.viewImageDetail);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        bu = (Button) findViewById(R.id.submitDetail);
        bu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText notes = (EditText)findViewById(R.id.notePadDetail);
                currentNotes = notes.getText().toString();

                Bundle bundle = new Bundle();
                bundle.putString("id", id);
                bundle.putString("currentPath", currentPath);
                bundle.putString("currentNotes", currentNotes);
                bundle.putDouble("latStart", latStart);
                bundle.putDouble("latEnd", latEnd);
                Toast.makeText(DetailActivity.this, "return to current = " + currentPath + currentNotes + latStart + latEnd + id, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(DetailActivity.this, CurrentLocationActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });


        if (id == "m0") {

            currentPath = extras.getString("currentPath");
            currentNotes = extras.getString("currentNotes");
        }
    }

    private void selectImage() {

        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(DetailActivity.this);
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

                    viewImageDetail.setImageBitmap(bitmap);

                    currentPath = Environment.getExternalStorageDirectory().getAbsolutePath();

                    f.delete();
                    OutputStream outFile = null;
                    File file = new File(currentPath, String.valueOf(System.currentTimeMillis()) + ".jpg");
                    currentPath = currentPath + String.valueOf(System.currentTimeMillis()) + ".jpg";
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
                currentPath = c.getString(columnIndex);
                c.close();
                Bitmap thumbnail = (BitmapFactory.decodeFile(path));
                Log.w("path of image", currentPath + "");
                viewImageDetail.setImageBitmap(thumbnail);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
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

    /*
    public void onSubmit_Detail(View view) {

        EditText notes = (EditText)findViewById(R.id.notePadDetail);
        currentNotes = notes.getText().toString();

        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        bundle.putString("currentPath", currentPath);
        bundle.putString("currentNotes", currentNotes);
    }
    */
}
