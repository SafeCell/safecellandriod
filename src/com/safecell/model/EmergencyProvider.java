/*******************************************************************************
* EmergencyProvider.java.java, Created: Apr 24, 2012
*
* Part of Muni Project
*
* Copyright (c) 2012 : NDS Limited
*
* P R O P R I E T A R Y &amp; C O N F I D E N T I A L
*
* The copyright of this code and related documentation together with any
* other associated intellectual property rights are vested in NDS Limited
* and may not be used except in accordance with the terms of the licence
* that you have entered into with NDS Limited. Use of this material without
* an express licence from NDS Limited shall be an infringement of copyright
* and any other intellectual property rights that may be incorporated with
* this material.
*
* ******************************************************************************
* ******     Please Check GIT for revision/modification history    *******
* ******************************************************************************
*/
package com.safecell.model;

import com.safecell.model.Emergency.Emergencies;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;

/**
 * @author uttama
 *
 */
public class EmergencyProvider extends ContentProvider {

    private static final String TAG = "EmergencyProvider";

    private static final String DATABASE_NAME = "emergency.db";

    private static final int DATABASE_VERSION = 1;

    private static final String EMERGENCY_TABLE_NAME = "emergency";

    public static final String AUTHORITY = "com.safecell.model";

    private static final UriMatcher sUriMatcher;

    private static final int EMERGENCY = 1;

    private static HashMap<String, String> notesProjectionMap;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + EMERGENCY_TABLE_NAME + " (" + Emergencies.EMERGENCY_ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," + Emergencies.NAME + " VARCHAR(50)," + Emergencies.NUMBER
                    + " VARCHAR(50)" + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
                    + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + EMERGENCY_TABLE_NAME);
            onCreate(db);
        }
    }

    private DatabaseHelper dbHelper;

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case EMERGENCY:
                count = db.delete(EMERGENCY_TABLE_NAME, where, whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case EMERGENCY:
                return Emergencies.CONTENT_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        if (sUriMatcher.match(uri) != EMERGENCY) { throw new IllegalArgumentException("Unknown URI " + uri); }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = db.insert(EMERGENCY_TABLE_NAME, Emergencies.NAME, values);
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(Emergencies.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
            case EMERGENCY:
                qb.setTables(EMERGENCY_TABLE_NAME);
                qb.setProjectionMap(notesProjectionMap);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case EMERGENCY:
                count = db.update(EMERGENCY_TABLE_NAME, values, where, whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, EMERGENCY_TABLE_NAME, EMERGENCY);

        notesProjectionMap = new HashMap<String, String>();
        notesProjectionMap.put(Emergencies.EMERGENCY_ID, Emergencies.EMERGENCY_ID);
        notesProjectionMap.put(Emergencies.NAME, Emergencies.NAME);
        notesProjectionMap.put(Emergencies.NUMBER, Emergencies.NUMBER);

    }

}
