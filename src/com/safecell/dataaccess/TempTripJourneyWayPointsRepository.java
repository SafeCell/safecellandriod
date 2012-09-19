package com.safecell.dataaccess;

import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.util.Log;

import com.safecell.TrackingService;
import com.safecell.model.SCWayPoint;
import com.safecell.utilities.DateUtils;
import com.safecell.utilities.DistanceAndTimeUtils;

public class TempTripJourneyWayPointsRepository extends DBAdapter {
    
    private  String TAG = TempTripJourneyWayPointsRepository.class.getSimpleName();
	
	public static final String CREATE_TABLE_QUERY = "CREATE TABLE temp_trip_journey_waypoints"
		+ "(waypoint_id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , " 
		+"timestamp DATETIME NOT NULL , latitude DOUBLE NOT NULL , " 
		+"longitude DOUBLE NOT NULL , estimated_speed DOUBLE NOT NULL," 
		+"distance DOUBLE default 0," 
		+"isBackground BOOLEAN default false" 
		+")";
	
	String insertQuery = "INSERT INTO temp_trip_journey_waypoints ("
		+ "timestamp, latitude, longitude, estimated_speed, distance,"
		+"isBackground"
		+ ") VALUES " +
				"( ?, ?, ?, ?, ?, ?)";

	public TempTripJourneyWayPointsRepository(Context context) {
		super(context);		
	}
	
	public void updateWaypointBackgroundFlag(int waypointId, boolean flag) {
		String query = "UPDATE temp_trip_journey_waypoints SET isBackground = ? WHERE waypoint_id = ?";
		
		Object[] args = {"" + flag, waypointId};

		this.Query(query, args);
	}
	
	public void  intsertWaypoint(SCWayPoint scWayPoint){		
		
	//	Log.v("Safecell :"+"TempWaypointRepository","insertWayPoint");
		float distanceBetweenPoints =0;
		distanceBetweenPoints = (float)getDistanceBetweenTwoPoints(scWayPoint);	
		Log.d(TAG, "Inserting Distance = "+distanceBetweenPoints);
		String dateString = scWayPoint.getTimeStamp();
		//DateUtils dateInMillSecond = new DateUtils();
		long dateTimeStampMills = DateUtils.dateInMillSecond(dateString);
		
		
		
		Object[] args = { dateTimeStampMills, 
							scWayPoint.getLatitude(),
							scWayPoint.getLongitude(), 
							scWayPoint.getEstimatedSpeed(),
							distanceBetweenPoints,
							""+scWayPoint.isBackground(),
							};

		this.Query(insertQuery, args);
		
	}
	public long tripStartTime(){
		long startTime = 0l;
		String selectQuery = "select timestamp from temp_trip_journey_waypoints order by waypoint_id asc limit 1";
		Cursor cursor = this.selectQuery(selectQuery, null);
		this.selectQuery(selectQuery, null).close();
		if (cursor != null && cursor.getCount()> 0) {
			cursor.moveToFirst();
			startTime = cursor.getLong(0);
		}
		cursor.close();
		return startTime;
	}
	
	double getDistanceBetweenTwoPoints(SCWayPoint firstWayPoint){
		
		SCWayPoint lastWayPoint = new SCWayPoint();
		
		String[] args ={};// 
		
		String sql = "Select * from temp_trip_journey_waypoints where WayPoint_id=(select max(WayPoint_id) from temp_trip_journey_waypoints)";	
		
		Cursor cursor =  this.selectQuery(sql, args);
		this.selectQuery(sql, args).close();
		
			if(cursor!=null && cursor.getCount()==1)
			{
				cursor.moveToFirst();
				lastWayPoint.setLatitude(cursor.getDouble(2));
				lastWayPoint.setLongitude(cursor.getDouble(3));
				cursor.close();
				
				return DistanceAndTimeUtils.distFrom(lastWayPoint.getLatitude(),lastWayPoint.getLongitude(),
						firstWayPoint.getLatitude(), firstWayPoint.getLongitude());
				
			}
		cursor.close();
	//	Log.v("Safecell :"+"Before Return","OK");
		
		return 0;
		
	}
	
	public  boolean isAvarageTimeDiffFeasible(){
		boolean isFesibleTimeDiff = false;
		String[] args={};
		double timeDiffInSecond = 0;
		double time;
		double[] timeArray= new double[5];
		
		Cursor cursor =this.selectQuery("SELECT timestamp FROM temp_trip_journey_waypoints order by waypoint_id desc limit(5)", args);
		if(cursor!= null && cursor.getCount()==5){
			cursor.moveToFirst();

			for(int i=0;i<cursor.getCount();i++)
			{
			timeArray[i] = cursor.getDouble(0);
				cursor.moveToNext();
			}
			
			for(int i=0;i<5;i++)
			{
				if(i>0)
				{
					time =   timeArray[i] - timeArray[i-1]; 
					timeDiffInSecond = Math.abs((time))/1000;
					//Log.v("Safecell :"+"Tracking Service","TimeDiffInSecond = "+timeDiffInSecond);
					if(timeDiffInSecond > 10)
					{
						isFesibleTimeDiff=false;
						break;
					}
					else {
						isFesibleTimeDiff =true;
					}
				}
			}
						
		}		
		cursor.close();
		return isFesibleTimeDiff;
	}
	
	public void deleteTrip(){		
		this.deleteQuery("Delete from temp_trip_journey_waypoints");		
	}
	
	public Cursor selectTrip(){
		String[] args={};
		Cursor tripCursor = this.selectQuery("Select * from temp_trip_journey_waypoints order by timestamp asc", args);		
		 this.selectQuery("Select * from temp_trip_journey_waypoints", args).close();
		return tripCursor;		
	}
	
	public  double getTotalDistance(){
	double totalDistance=0;
	String[] args={};
	Cursor cursor = this.selectQuery("select sum(distance) from temp_trip_journey_waypoints", args);
	this.selectQuery("select sum(distance) from temp_trip_journey_waypoints", args).close();
	if(cursor!= null && cursor.getCount()>0)
	{
		cursor.moveToFirst();
		totalDistance =cursor.getDouble(0);		
	}
	cursor.close();
	return totalDistance;
}
	public double getTotalTimeBetweenWayPoints(){
		String selectQuery = "select timestamp from  temp_trip_journey_waypoints";
		Cursor cursor = this.selectQuery(selectQuery, null);
		double totalTimeRequired = 0l;
		
		if (cursor!= null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			long startTime = cursor.getLong(0);
			cursor.moveToLast();
			long endTime = cursor.getLong(0);
			long diffDate = endTime - startTime;
			//Log.v("Temp Jounery diffDate", ""+diffDate);
			totalTimeRequired  = (double)diffDate/ (3600.0 * 1000);
		}
		cursor.close();
		return totalTimeRequired;
	}
	
	public float getAvarageSpeed(){
		double avgSpeed = 0;
		
		double time = getTotalTimeBetweenWayPoints();
		//Log.v("Temp Jounery Time", ""+time);
		double distance = getTotalDistance();
		//Log.v("Temp Jounery distance", ""+distance);
		
		if (time == 0 || distance ==0) {
			return 0;
		}
		avgSpeed = distance /time;
		
		return Math.round(avgSpeed);
	}
	
	public int getTotalWaypoints() {
		int count = 0;
		String[] args={};
		Cursor cursor = this.selectQuery("SELECT COUNT(waypoint_id) FROM temp_trip_journey_waypoints", args);
		if(cursor.getCount()>0)
		{
			cursor.moveToFirst();
			count = cursor.getInt(0);			
		}
		cursor.close();
		return count;
	}
	
	public double getTimeDiffernce(long timestamp2){
		double timeDifference=0;
		long timeStamp1=0;
		String[] args={};
		Cursor cursor = this.selectQuery("SELECT timestamp FROM temp_trip_journey_waypoints where waypoint_id= (select max(waypoint_id ) from  temp_trip_journey_waypoints)", args);
		if(cursor!= null && cursor.getCount()>0)			
		{
			cursor.moveToFirst();
			timeStamp1=cursor.getLong(0);	
			timeDifference = timestamp2 - timeStamp1;
			String Seconds = ""+(timeDifference/1000) ;
			//Log.v("Safecell :"+"Time difference in Second",Seconds +" Seconds");
			String Hours =""+ timeDifference / 3600000; 
			timeDifference = Double.valueOf(Hours);
			
		}	
		cursor.close();
		return timeDifference;
	}
	
	public double getDistanceDifference(Location location)
	{
		double distanceDiff=0.0;
		double latitude=0,longitude=0;
		
		String[] args={};
		Cursor cursor = this.selectQuery("SELECT latitude, longitude FROM temp_trip_journey_waypoints where waypoint_id= (select max(waypoint_id) from  temp_trip_journey_waypoints)",args);
		if(cursor!= null && cursor.getCount()>0)
		{
			cursor.moveToFirst();
			latitude = cursor.getDouble(0);
			longitude =cursor.getDouble(1);			
			
			distanceDiff = DistanceAndTimeUtils.distFrom(latitude, longitude, location.getLatitude(), location.getLongitude());
			//distanceDiff = loc.distanceTo(location);
			//distanceDiff *= 0.000621371192;
			//loc.
			//distanceDiff =  DistanceAndTimeUtils.distanceMiles(latitude, longitude, location.getLatitude(), location.getLongitude());			
		}
		cursor.close();
		return distanceDiff;
	}
	
	public Cursor getTrip()
	{
		String[] args={};
		Cursor cursor = this.selectQuery("Select  * from temp_trip_journey_waypoints", args);		
		return cursor;
	}
	
	
//	public double getAvarageEstimatedSpeed(){
//		double avarageEstimatedSpeed=0;
//		double speed=0;
//		String[] args={};
//		Cursor cursor =this.selectQuery("SELECT estimated_speed FROM temp_trip_journey_waypoints order by waypoint_id desc limit(5)", args);
//		if(cursor != null && cursor.getCount()>0){
//			cursor.moveToFirst();
//
//			for(int i=0;i<cursor.getCount();i++)
//			{
//				speed += cursor.getDouble(0);
//			//	Log.v("Safecell :"+"for loop speed ="+i,""+speed);
//				cursor.moveToNext();
//			}
//			avarageEstimatedSpeed = speed/cursor.getCount();			
//		}		
//		cursor.close();
//		return avarageEstimatedSpeed;		
//	}
	
	public double getAvarageEstimatedSpeedForAutoTripStart() {
		
		double totalDistance = 0;
		double averageSpeed = 0;
		
		String query = "SELECT distance, timestamp, waypoint_id FROM temp_trip_journey_waypoints order by waypoint_id desc limit(5)";
		String args[] = {};
		Cursor cursor = this.selectQuery(query, args);
		
		boolean firstIteration = true;
		
		long endTimeStamp = 0;
		long startTimeStamp = 0;
		
		if(cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();

			for(int i = 0; i < cursor.getCount(); i++) {
				double currentDistance =  cursor.getDouble(0);
				//Log.v("Safecell", cursor.getInt(2) + ". currentDistance: " + currentDistance);
				
				totalDistance += currentDistance;
				if(firstIteration) {
					endTimeStamp = cursor.getLong(1);
					firstIteration = false;
				} else {
					startTimeStamp = cursor.getLong(1);
				}
				
				cursor.moveToNext();
			}
		}
		
		cursor.close();
		
		if(endTimeStamp == 0 || startTimeStamp == 0) {
			return 0;
		}
		
		long timeDifference = endTimeStamp - startTimeStamp;
		
		if(timeDifference == 0) {
			return 0;
		}
		
		double timeDifferenceInHours = timeDifference / 3600000.0;
		
		averageSpeed = totalDistance / timeDifferenceInHours;
		//Log.v("Safecell", totalDistance + " / " + timeDifferenceInHours + " = " + averageSpeed);
		return averageSpeed;
	}
}
