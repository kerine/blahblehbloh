package com.example.admin.route;

import android.app.Activity;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class RouteList extends Activity {

    MyDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_list);

        db = new MyDB(this);
        populateListViewFromDB();
        registerListClickCallback();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_route_list, menu);
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


    public void populateListViewFromDB() {
        db.open();
        Cursor cursor = db.getAllRoute();

        //Allow Activity to manage lifetime of the cursor
        //DEPRECATED!
        startManagingCursor(cursor);

        //Setup Mapping from cursor to view Fields:
        String[] fromFieldNames = new String[]{MyDBHelper.columnName_routeName, MyDBHelper.columnName_routeStart, MyDBHelper.columnName_routeEnd};

        int[] toViewIDs = new int[]{R.id.route_name, R.id.start_loc, R.id.end_loc};


        //create adaptor to may columns of the database into elements of the UI
        SimpleCursorAdapter myCursorAdapter = new SimpleCursorAdapter(this,
                R.layout.item_layout, //Row layout template
                cursor,                 //cursor (set of DB records to map)
                fromFieldNames,         //DB column names
                toViewIDs);             //View IDs to put information in

        //set adapter for the list view
        ListView myList = (ListView) findViewById(R.id.routeList);
        myList.setAdapter(myCursorAdapter);
        db.close();
    }

    private void registerListClickCallback() {
        ListView myList = (ListView) findViewById(R.id.routeList);
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View viewClicked,
                                    int position, long idInDB) {

                // Getting the Container Layout of the ListView
                LinearLayout linearLayoutParent = (LinearLayout) viewClicked;

                // Getting Route Name
                TextView routeName = (TextView) linearLayoutParent.getChildAt(0);
                // Getting Route Name
                TextView routeStart = (TextView) linearLayoutParent.getChildAt(1);
                // Getting Route Name
                TextView routeEnd = (TextView) linearLayoutParent.getChildAt(2);

                Toast.makeText(getBaseContext(), routeName.getText().toString() + " " +
                        routeStart.getText().toString() + " " + routeEnd.getText().toString()
                        , Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void displayToastForId(long idInDB) {
//        Cursor cursor = db.getRoute(idInDB);
//        if (cursor.moveToFirst()) {
//            long idDB = cursor.getLong(MyDBHelper.columnName_routeID);
//            String name = cursor.getString(DBAdapter.COL_NAME);
//            int studentNum = cursor.getInt(DBAdapter.COL_STUDENTNUM);
//            String favColour = cursor.getString(DBAdapter.COL_FAVCOLOUR);
//
//            String message = "ID: " + idDB + "\n"
//                    + "Name: " + name + "\n"
//                    + "Std#: " + studentNum + "\n"
//                    + "FavColour: " + favColour;
//            Toast.makeText(RouteList.this, message, Toast.LENGTH_LONG).show();
//        }
//        cursor.close();
//    }

    public void onClick_BacktoCallingActivity(View view){

        finish();
    }

}
