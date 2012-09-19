package com.safecell.dataaccess;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.safecell.model.SCTripJourney;
import com.safecell.utilities.DateUtils;

public class TripJourneysRepository extends DBAdapter {
	private int totalPoints;

	public static final String CREATE_TABLE_QUERY = "CREATE TABLE trip_journeys ("
			+ "trip_journey_id 	INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL ,"
			+ " points 			INTEGER NOT NULL,"
			+ " miles 			FLOAT NOT NULL,"
			+ " trip_date  		DATETIME NOT NULL,"
			+ " estimated_speed 	FLOAT,"
			+ " trip_id 			INTEGER)";
	private long dateTimeStampMills;
	private String insertQuery = "INSERT INTO trip_journeys ("
			+ " trip_journey_id,points, miles, trip_date, "
			+ "estimated_speed, trip_id " + ") VALUES (" + " ?,?, ?, ?, "
			+ "?, ? " + ")";

	public TripJourneysRepository(Context context) {
		super(context);
	}

	public void insertTripJourneys(SCTripJourney scTripJourney) {

		String dateString = scTripJourney.getTripDate();
		// DateUtils dateInMillSecond = new DateUtils();
		dateTimeStampMills = DateUtils.dateInMillSecond(dateString);

		Object[] args = {scTripJourney.getJourneyId(),
				scTripJourney.getPoints(), scTripJourney.getMiles(),
				dateTimeStampMills, scTripJourney.getEstimatedSpeed(),
				scTripJourney.getTripId()};

		//Log.v("Safecell :"+"insertTripJourneys", "_________________________________");
		//Log.v("Safecell :"+"Journey ID", "" + scTripJourney.getJourneyId());
		//Log.v("Safecell :"+"Points", "" + scTripJourney.getPoints());
		//Log.v("Safecell :"+"Miles", "" + scTripJourney.getMiles());
		//Log.v("Safecell :"+"Estimated Speed", "" + scTripJourney.getEstimatedSpeed());
		//Log.v("Safecell :"+"Trip Id", "" + scTripJourney.getTripId());

		this.Query(insertQuery, args);

	}

	public Cursor SelectTrip_journeys() {

		String selectQuery = "Select * from trip_journeys ORDER BY trip_journey_id DESC LIMIT 5";
		Cursor cursor = this.selectQuery(selectQuery, null);
		this.selectQuery(selectQuery, null).close();
		return cursor;
	}

	public Cursor sumOfPointsMiles() {
		String selectQuery = "Select SUM(points) as TotalPoints ,ROUND(SUM(miles)) as TotalMiles from trip_journeys ORDER BY trip_journey_id DESC LIMIT 5";
		Cursor cursor = this.selectQuery(selectQuery, null);
		this.selectQuery(selectQuery, null).close();
		return cursor;
	}

	public Cursor SelectAllTripJourney() {
		String[] args = new String[]{};

		// Cursor allTripJourney =
		// this.Query("select trip_journeys.trip_journey_id, trip_journeys.points, trip_journeys.miles,  trip_journeys.trip_date, trips.name from trip_journeys,trips where  trip_journeys.trip_id= trips.trip_id",
		// args) ;
		Cursor allTripJourney = this.selectQuery("select trip_journeys.trip_journey_id, trip_journeys.points, trip_journeys.miles,  " +
				"trip_journeys.trip_date, trips.name from trip_journeys,trips where  trip_journeys.trip_id= trips.trip_id order BY trip_journeys.trip_date desc",
						args);
		this.selectQuery("select trip_journeys.trip_journey_id, trip_journeys.points, trip_journeys.miles,  " +
				"trip_journeys.trip_date, trips.name from trip_journeys,trips where  trip_journeys.trip_id= trips.trip_id order BY trip_journeys.trip_date desc",
						args).close();
		return allTripJourney;
	}

	public Cursor SelectTripById(int id) {
		String[] args = new String[]{"" + id};
		Cursor cursor = this.selectQuery(
				"SELECT * FROM trip_journeys where trip_journey_id = ?", args);
		this.selectQuery(
				"SELECT * FROM trip_journeys where trip_journey_id = ?", args)
				.close();
		return cursor;
	}

	public int getTotalpoints() {
		Cursor cursor = sumOfPointsMiles();

		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			totalPoints = cursor.getInt(0);
			cursor.close();

		}
		return totalPoints;

	}

	public boolean alreadyTripIdExit(int tripID)
	{
		String selectQurey = "SELECT * from trip_journeys where trip_journey_id = "+tripID;
		Cursor cursor = this.selectQuery(selectQurey, null);
		this.selectQuery(selectQurey, null).close();
		
		if (cursor!=null && cursor.getCount() >0) {
			cursor.close();
			return true;
		}else 
			cursor.close();
			return false;
		
	}
	public int getPointsSum() {
		int totalPositivePoints = 0;
	  	String[] args = new String[]{};
		Cursor cursor = this.selectQuery("SELECT points FROM journey_events ORDER BY id ASC",args);
		this.selectQuery("SELECT points FROM journey_events ORDER BY id ASC", args).close();
		int tripPoints = 0;
		
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			for (int i = 0; i < cursor.getCount(); i++) {
				tripPoints = cursor.getInt(0);
				if(tripPoints > 0){
					totalPositivePoints += tripPoints;
				}
				cursor.moveToNext();				
			}
		}
		cursor.close();
		return totalPositivePoints;
	}
	
	public int getSafeMilePointsSum() {
		int PositivePoint = 0;
	   // int totalPositivePoints = 0;
		String[] args = new String[]{};
		Cursor cursor = this.selectQuery("SELECT points FROM trip_journeys ORDER BY  trip_journey_id ASC",
				args);
		this.selectQuery("SELECT points FROM trip_journeys", args).close();
		
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				 PositivePoint += cursor.getInt(0);	
				 if (PositivePoint < 0) PositivePoint = 0;
			} while (cursor.moveToNext());
			
		}
		cursor.close();
		return PositivePoint;
	}
	
	public int getTotalMiles(){
		int totalMiles=0;
		String[] args = new String[]{};
		Cursor cursor = this.selectQuery(
				"SELECT sum(Miles) FROM trip_journeys", args);
		this.selectQuery(
				"SELECT sum(Miles) FROM trip_journeys", args)
				.close();
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			totalMiles = cursor.getInt(0);
			cursor.close();
		}
		return totalMiles;
	}
}
