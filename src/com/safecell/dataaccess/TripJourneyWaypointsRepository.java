package com.safecell.dataaccess;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.safecell.model.SCWayPoint;

public class TripJourneyWaypointsRepository extends DBAdapter {
	private long dateTimeStampMills;
	public static final String CREATE_TABLE_QUERY = "CREATE TABLE trip_journey_waypoints"
			+ "(waypoint_id INTEGER PRIMARY KEY  " +
					"AUTOINCREMENT  NOT NULL , " +
					"timestamp DATETIME NOT NULL , " +
					"latitude FLOAT NOT NULL , " +
					"longitude FLOAT NOT NULL , " +
					"estimated_speed FLOAT NOT NULL , " +
					"trip_journey_id INTEGER, " +
					"background boolean default false"+
					")";

	String insertQuery = "INSERT INTO trip_journey_waypoints ("
			+ "timestamp, latitude, longitude, estimated_speed, trip_journey_id, background "
			+ ") VALUES (" + "?, ?, ? ,?, ?, ?" + ")";

	
	public TripJourneyWaypointsRepository(Context context){
	super(context);
	
}
	public void insertWayPoint(SCWayPoint scWayPoint) {

		String dateString = scWayPoint.getTimeStamp();
		dateTimeStampMills = Long.parseLong(dateString);
		Object[] args = { dateTimeStampMills, scWayPoint.getLatitude(),
				scWayPoint.getLongitude(), scWayPoint.getEstimatedSpeed(),
				scWayPoint.getJourneyID(), "" + scWayPoint.isBackground()};

		this.Query(insertQuery, args);
	}
	
	public Cursor SelectTrip_journey_waypoints() {
		
		String selectQuery = "Select * from trip_journey_waypoints ";
		Cursor cursor = this.selectQuery(selectQuery, null);
		
		if (cursor.getCount()>0) {
			cursor.moveToFirst();
			do {
				//Log.v("Safecell :"+"Value From Cursor", "" + cursor.getString(1));
			} while (cursor.moveToNext());
		}
		return cursor;
	}
}
