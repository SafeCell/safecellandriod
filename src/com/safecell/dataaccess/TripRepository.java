package com.safecell.dataaccess;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.safecell.model.SCTrip;

public class TripRepository extends DBAdapter {

	public static final String CREATE_QUERY = "CREATE TABLE trips (trip_id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , name varchar NOT NULL );";

	public TripRepository(Context context) {
		super(context);
	}

	public void saveTrip(SCTrip scTrip) {
		
		String insertQuery = "Insert INTO trips (trip_id,name) VALUES (?, ?)";
		Object[] args = { scTrip.getTripId(),scTrip.getName() };
		this.Query(insertQuery, args);

	};

	public boolean tripExist(int tripID){
		String selectQuery = "SELECT  trip_id FROM trips where trip_id = "+tripID;
		Cursor cursor = this.selectQuery(selectQuery, null);
		this.selectQuery(selectQuery, null).close();
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			cursor.close();
			return true;
		}
		cursor.close();
		return false;
	}
	public int getTripCount() {

		String selectQuery = "SELECT COUNT(*) as count FROM trips";
		int tripCount=0;
		
		Cursor cursor = this.selectQuery(selectQuery, null);
		
		if (cursor != null) {
			cursor.moveToFirst();
			tripCount = cursor.getInt(0);
		}
		//Log.v("Safecell :"+"Trips Count", ""+tripCount);
		cursor.close();
		return tripCount;
	}
	
	public String[] SelectTripName() {
		
		String[] tripNames ={};
		String selectQuery = "Select name from trips ORDER BY trip_id DESC LIMIT 5";
		Cursor cursor = this.selectQuery(selectQuery, null);
		
		if (cursor != null) {
			cursor.moveToFirst();
			tripNames =new String[ cursor.getCount()];
			
			for(int i=0; i<cursor.getCount();i++)
			{
				tripNames[i] = cursor.getString(0);
				cursor.moveToNext();
			}
		}
		cursor.close();
		return tripNames ;
	}
	public String[] selectAllTripNames(){
		String[] tripNames ={};
		String selectQuery="Select name from trips";
		Cursor cursor = this.selectQuery(selectQuery, tripNames);
		if(cursor.getCount() > 0)
		{
			cursor.moveToFirst();
			tripNames = new String[cursor.getCount()];
			for(int i=0; i<cursor.getCount();i++)
			{
				tripNames[i] =cursor.getString(0);
				cursor.moveToNext();
			}
		}
		return tripNames;
	}
	
	public String[] SelectListOfTripName() {
		
		String[] tripNames ={};
		String selectQuery = "Select name from trips";
		Cursor cursor = this.selectQuery(selectQuery, null);
		
		if (cursor != null) {
			cursor.moveToFirst();
			tripNames =new String[ cursor.getCount()];
			
			for(int i=0; i<cursor.getCount();i++)
			{
				tripNames[i] = cursor.getString(0);
			}
		}
		cursor.close();
		return tripNames ;
	}
}
