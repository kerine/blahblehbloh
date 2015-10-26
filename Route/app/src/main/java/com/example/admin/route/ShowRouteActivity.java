package com.example.admin.route;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class ShowRouteActivity extends Activity {

    MyDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_route);

        //Instantiate Database
        db = new MyDB(this);

        long routeID = getIntent().getLongExtra("routeID", 0);

        db.open();
        Cursor c = db.getRoute(routeID);
        c.moveToPosition(0);
        String title = c.getString(1);
        String note = c.getString(2);
        String photopath = c.getString(3);
        Toast.makeText(getBaseContext(), "title: " + title + ", note: " + note +
                ",photo_path: " + photopath, Toast.LENGTH_LONG).show();
        db.close();

        TextView routeName_View = (TextView)findViewById(R.id.TextView_routeName);
        routeName_View.setText(title);

        //Show Picture
        File imgFile = new  File(photopath);
        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            ImageView photo_View = (ImageView)findViewById(R.id.TextView_Image);
            photo_View.setImageBitmap(myBitmap);
        }

        TextView note_View = (TextView)findViewById(R.id.TextView_note);
        note_View.setText(note);
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
