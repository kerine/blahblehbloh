package com.example.admin.route;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDBHelper extends SQLiteOpenHelper {

    public static final int    databaseVersion                  = 1;
    public static final String databaseName                     = "routeDB";

    public static final String tableName                        = "routeTable";
    public static final String columnName_routeID               = "_id";
    public static final String columnName_routeName             = "routeName";
    public static final String columnName_routeStart            = "startLocation";
    public static final String columnName_routeEnd              = "endLocation";
    public static final String columnName_routeVia1             = "via1_location";
    public static final String columnName_routeVia2             = "via2_location";

    private static final String SQLite_CREATE =
            "CREATE TABLE " + tableName + "(" + columnName_routeID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + columnName_routeName + " TEXT NOT NULL," + columnName_routeStart + " TEXT NOT NULL,"
                    + columnName_routeEnd + " TEXT NOT NULL," + columnName_routeVia1 + " TEXT," + columnName_routeVia2 + " TEXT);";

    private static final String SQLite_DELETE = "DROP TABLE IF EXISTS " + tableName;

    public MyDBHelper(Context context) {
        super(context, databaseName, null, databaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQLite_CREATE);
    }

    // onUpgrade is called if the database version is increased in your application code
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQLite_DELETE);
        onCreate(db);
    }

}
