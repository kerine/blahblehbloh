package com.example.admin.route;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class MyDB {

    com.example.admin.route.MyDBHelper DBHelper;
    SQLiteDatabase db;
    final Context context;

    public MyDB(Context ctx) {
        this.context = ctx;
        DBHelper = new com.example.admin.route.MyDBHelper(this.context);
    }


    public MyDB open() {
        db = DBHelper.getWritableDatabase();

        return this;
    }

    public void close() {
        DBHelper.close();
    }

    public long insertRoute(String route_str, String notes_str, String path_str) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(MyDBHelper.columnName_routeName, route_str);
        initialValues.put(MyDBHelper.columnName_routeStart, notes_str);
        initialValues.put(MyDBHelper.columnName_routeEnd, path_str);

        return db.insert(MyDBHelper.tableName, null, initialValues);
    }

    public int deleteRoute(long id) {
        return  db.delete(MyDBHelper.tableName, MyDBHelper.columnName_routeID + "=" + id, null);
    }

    public int deleteAllRoute() {
        return db.delete(MyDBHelper.tableName, "1", null);    // delete all records
    }

    public int updateRoute(long id, String route_str, String notes_str, String path_str) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(MyDBHelper.columnName_routeName, route_str);
        initialValues.put(MyDBHelper.columnName_routeStart, notes_str);
        initialValues.put(MyDBHelper.columnName_routeEnd, path_str);
        return db.update(MyDBHelper.tableName, initialValues, MyDBHelper.columnName_routeID + "=" + id, null);
    }

    public Cursor getAllRoute() {
        return db.query(
                MyDBHelper.tableName,
                new String[]{
                        MyDBHelper.columnName_routeID,
                        MyDBHelper.columnName_routeName,
                        MyDBHelper.columnName_routeStart,
                        MyDBHelper.columnName_routeEnd},
                null, null, null, null, null);
    }


    public Cursor getRoute(long id) {
        Cursor mCursor = db.query(MyDBHelper.tableName,
                new String[] {
                        MyDBHelper.columnName_routeID,
                        MyDBHelper.columnName_routeName,
                        MyDBHelper.columnName_routeStart,
                        MyDBHelper.columnName_routeEnd},
               MyDBHelper.columnName_routeID+"="+id,
                null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

}
