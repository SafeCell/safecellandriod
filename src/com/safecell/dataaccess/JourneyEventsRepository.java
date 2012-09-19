package com.safecell.dataaccess;

import android.content.Context;
import android.database.Cursor;
import android.sax.StartElementListener;
import android.util.Log;

import com.safecell.model.SCJournyEvent;

public class JourneyEventsRepository extends DBAdapter{

	public static final String CREATE_TABLE_QUERY = "CREATE TABLE journey_events (id INTEGER  NOT NULL, journey_id INTEGER NOT NULL ,points FLOAT NOT NULL ,near VARCHAR,description VARCHAR,timestamp DATETIME NOT NULL);";
	//PRIMARY KEY AUTOINCREMENT
	private long dateTimeStampMills;
	private String insertQuery = "INSERT INTO journey_events ("
		+"id, journey_id, points, near, "
		+"description, timestamp "
		+") VALUES ("
		+"?, ?, ?, ?, "
		+"?, ? "
		+")";
		
		public JourneyEventsRepository(Context context){
			super(context);
		}
	
		public void insertTripJourneys(SCJournyEvent scJournyEvent){
			
			String dateString = scJournyEvent.getTimeStamp();
			//DateUtils dateInMillSecond = new DateUtils();
			//Log.v("Safecell :"+"Insert Trip Journey","OK");
			
			
			//dateTimeStampMills = DateUtils.dateInMillSecond(dateString);
			dateTimeStampMills = Long.valueOf(dateString);//(dateString);
			//Log.v("Safecell :"+"Insert Trip Journey","OK1");
			
		Object[] args = { 	scJournyEvent.getId(), 
							scJournyEvent.getJourneyId(),
							scJournyEvent.getPoints(), 
							scJournyEvent.getNear(),
							scJournyEvent.getDescription(), 
							dateTimeStampMills};
		
		//Log.v("Safecell :"+"JourneyEvent","______________________" );
		//Log.v("Safecell :"+"Event ID",""+scJournyEvent.getId() );
		//Log.v("Safecell :"+"JourneyID",""+scJournyEvent.getJourneyId());
		//Log.v("Safecell :"+"Points",""+scJournyEvent.getPoints());
		//Log.v("Safecell :"+"Near",""+scJournyEvent.getNear());
		//Log.v("Safecell :"+"Description",""+scJournyEvent.getDescription());
		//Log.v("Safecell :"+"dateTimeStampMills",""+dateTimeStampMills);
		
			this.Query(insertQuery, args);
			
		}
		
		public Cursor getJourneyEventsById(int id)
		{
			String[] argv=  {""+id};
			Cursor cursor = this.selectQuery("SELECT * FROM journey_events where journey_id=?", argv);			
			return cursor;
		}
		
		public int getPenaltyByID(int id)
		{
			int penaltyPoints = 0;
			String[] argv=  {""+id};
			Cursor cursor = this.selectQuery("SELECT sum(points) from journey_events where points<0 AND journey_id = ?", argv);
			this.selectQuery("SELECT sum(points) from journey_events where points < 0 AND journey_id = ?", argv).close();
			if(cursor.getCount()>0)
			{
				cursor.moveToFirst();
				penaltyPoints = cursor.getInt(0);
			}
			cursor.close();
			return penaltyPoints;
			
		}
		
		public int getTotalPositivePointsById(int id)
		{
			int totalPositivePoints=0;
			String[] argv=  {""+id};
			Cursor cursor = this.selectQuery("SELECT sum(points) from journey_events where points>0 AND journey_id = ?", argv);
			if(cursor.getCount()>0)
			{
				cursor.moveToFirst();
				totalPositivePoints = cursor.getInt(0);
			}
			cursor.close();
			return totalPositivePoints;
		}
		
		

	
}
