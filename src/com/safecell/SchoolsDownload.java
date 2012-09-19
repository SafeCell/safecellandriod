package com.safecell;

import java.util.ArrayList;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.safecell.model.SCSchool;
import com.safecell.networking.GetSchools;
import com.safecell.networking.GetSchoolsResponseHandler;
import com.safecell.utilities.DistanceAndTimeUtils;

public class SchoolsDownload {

	private String TAG = "SchoolsDownload";
	private Location lastSchoolDownLoadLocation;
	private final double SCHOOL_UPDATE_RADIUS = 5;
	private final double SCHOOL_NEAR_RANGE = 300;
	private double distanceSinceLastDownload;
	
	private GetSchools downloadSchools;
	private GetSchoolsResponseHandler getSchoolsResponseHandler;
	public ArrayList<SCSchool> schools = new ArrayList<SCSchool>();
	
	private Context context;
	
	public SchoolsDownload() {
		// TODO Auto-generated constructor stub
		
	}
	
	
	
	public void locationChangedForSchool(Location location, Context context)
	{
		
		this.context = context;
		//Log.v("Safecell :"+TAG+"lastSchoolDownLoadLocation", "Location == "+lastSchoolDownLoadLocation);
		
		if (lastSchoolDownLoadLocation != null) {
			
			double distance = DistanceAndTimeUtils.distFrom(lastSchoolDownLoadLocation.getLatitude(), lastSchoolDownLoadLocation.getLongitude(),
					location.getLatitude(), location.getLongitude());
			//Log.v("Safecell :"+TAG+ "distance", distance+"");
			
			distanceSinceLastDownload += distance ;
			//Log.v("Safecell :"+"distanceSinceLastDownload", ""+distanceSinceLastDownload);
			
			if (distanceSinceLastDownload>=SCHOOL_UPDATE_RADIUS) {
				distanceSinceLastDownload = 0;
				lastSchoolDownLoadLocation = location;
				
				startDownloadThread();
				
			}
						
		}
		
		else{
			
			distanceSinceLastDownload = 0;
			lastSchoolDownLoadLocation = location;
			//Log.v("Safecell :"+TAG+"lastSchoolDownLoadLocation", "Location ELSE == "+lastSchoolDownLoadLocation);
			startDownloadThread();
			
		}
		
	}
	
	private void startDownloadThread() {
		Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				downloadSchool();
				
			}
		};
		
		Thread t = new Thread(runnable);
		t.start();
	}
	
	
	
	
	private void downloadSchool()
	{
		
		downloadSchools = new GetSchools(context, lastSchoolDownLoadLocation.getLatitude(), lastSchoolDownLoadLocation.getLongitude(), SCHOOL_UPDATE_RADIUS); 
		String result = downloadSchools.getRequest();
		
		//String message = downloadSchools.getFailureMessage();
		
		if(result != null) {
			
			getSchoolsResponseHandler = new GetSchoolsResponseHandler(result);
			synchronized (schools) {
				schools = getSchoolsResponseHandler.handleGetSchoolsResponse();
				
			}
			
		}
		
		
		
	}
	
	public boolean schoolZoneActive(Location location)
	{ 
		
		synchronized (schools) {
//			Log.v("Safecell :"+"no of school", "Number of schools "+schools.size());
			if (schools.size()>0) {
				//Log.v("Safecell :"+"size school", ""+schools.size());
				for(SCSchool school : schools) {
					// Log.v("Safecell :"+"id: ",  "" + school.getId());
					//Log.v("Safecell :"+"name: ", "" + school.getName());
					//Log.v("Safecell :"+"latitude",school.getLatitude()+"");
					//Log.v("Safecell :"+"longitude", school.getLongitude()+"");
					
					double distanceFromSchool = DistanceAndTimeUtils.distFrom(location.getLatitude(), location.getLongitude(),
							school.getLatitude(), school.getLongitude());
					double distance = (1609.344)*distanceFromSchool;
					// 1 mile = 1 609.344 meters
				//	Log.v("Safecell :"+"distanceFromSchool",distance+"");
					if (distance<SCHOOL_NEAR_RANGE) {
						
						return true;
					}
				}
			}
		}
		return false;
		
	}
	
}
