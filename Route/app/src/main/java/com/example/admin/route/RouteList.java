package com.example.admin.route;

import android.app.Activity;
import android.content.Intent;
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
        String[] fromFieldNames = new String[]{MyDBHelper.columnName_routeName, MyDBHelper.columnName_routeStart,
                MyDBHelper.columnName_routeEnd ,  MyDBHelper.columnName_routeVia1, MyDBHelper.columnName_routeVia2};

        int[] toViewIDs = new int[]{R.id.route_name, R.id.start_loc, R.id.end_loc, R.id.via1_loc, R.id.via2_loc};


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
                // Getting Route Name
                TextView routeVia1 = (TextView) linearLayoutParent.getChildAt(3);
                // Getting Route Name
                TextView routeVia2 = (TextView) linearLayoutParent.getChildAt(4);

                Toast.makeText(getBaseContext(), routeName.getText().toString() + " " +
                        routeStart.getText().toString() + " " + routeEnd.getText().toString() + " " + routeVia1.getText().toString() + " " + routeVia2.getText().toString()
                        , Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onClick_BackToMain(View view){

        Intent myIntent = new Intent(this,MainActivity.class);
        startActivity(myIntent);
    }

}
