package com.example.testapp.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.Date;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "HEALTHNAV.db";
    public static final String TABLE_NAME = "LOCATION_HISTORY";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "LONGITUDE";
    public static final String COL_3 = "LATITUDE";
    public static final String COL_4 = "CREATED_DATE";
    public static final String COL_5 = "RESPONSE";


    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT,LONGITUDE REAL,LATITUDE REAL,CREATED_DATE TEXT,RESPONSE CLOB)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean insertData(double longitude, double latitude, Date date, String response){
        boolean returnBoolean = false;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("LONGITUDE", longitude);
        contentValues.put("LATITUDE", latitude);
        contentValues.put("CREATED_DATE", String.valueOf(date));
        contentValues.put("RESPONSE", response);
        long temp = db.insert(TABLE_NAME, null, contentValues);
        if(temp != -1){
            returnBoolean = true;
        }
        return returnBoolean;
    }

}
