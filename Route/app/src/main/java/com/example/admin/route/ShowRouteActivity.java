package com.example.admin.route;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class ShowRouteActivity extends Activity {

    MyDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_route);

        //Instantiate Database
        db = new MyDB(this);

        long routeID = getIntent().getLongExtra("routeID", 0);

        System.out.println("routeID is: " +routeID);

        db.open();

        Cursor c = db.getRoute(routeID);

        startManagingCursor(c);

        c.moveToPosition(0);
        String columnCount = String.valueOf(c.getColumnCount());
        System.out.println("Column count is: " + columnCount);
        String title = c.getString(1);
        String startLoc = c.getString(2);
        String endLoc = c.getString(3);
        String via1Loc = c.getString(4);
        String via2Loc = c.getString(5);

        stopManagingCursor(c);
        db.close();

        TextView routeName_View = (TextView)findViewById(R.id.TextView_routeName);
        routeName_View.setText(title);

        TextView startLocation_View = (TextView)findViewById(R.id.startLocation);
        startLocation_View.setText(startLoc);

        TextView endLocation_View = (TextView)findViewById(R.id.endLocation);
        endLocation_View.setText(endLoc);

        TextView via1Location_View = (TextView)findViewById(R.id.via1Location);
        via1Location_View.setText(via1Loc);

        TextView via2Location_View = (TextView)findViewById(R.id.via2Location);
        via2Location_View.setText(via2Loc);

//        //Show Picture
//        File imgFile = new  File(photopath);
//        if(imgFile.exists()){
//            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
//            ImageView photo_View = (ImageView)findViewById(R.id.TextView_Image);
//            photo_View.setImageBitmap(myBitmap);
//        }
//
//        TextView note_View = (TextView)findViewById(R.id.TextView_note);
//        note_View.setText(note);
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

    public static String strSeparator = "__,__";

    public static String[] convertStringToArray(String str){
        String[] arr = str.split(strSeparator);
        return arr;
    }
}
