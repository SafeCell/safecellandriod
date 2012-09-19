package com.safecell.utilities;

import java.util.Calendar;

import android.content.Context;
import android.util.Log;


public class DistanceAndTimeUtils {
	
	
	public DistanceAndTimeUtils(Context context) {
		
		
		
	}
	public static double distFrom(double startLat, double startLong,
			double endLat, double endLong) {
		
	
		double earthRadius = 6371;
		double dLat = Math.toRadians(endLat - startLat);
		double dLng = Math.toRadians(endLong - startLong);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
		+ Math.cos(Math.toRadians(startLat))* Math.cos(Math.toRadians(endLat)) 
		* Math.sin(dLng / 2)* Math.sin(dLng / 2);
		
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = earthRadius * c;//in miles
		//Log.v("Safecell :"+"dist", dist+"");
		
		return (dist * .621371192);
	}
	
	 public static double distanceMiles(double lat1, double lon1, double lat2, double lon2) {
		 
		 
		 
		 double theta = lon1 - lon2;
		  double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		  dist = Math.acos(dist);
		  dist = rad2deg(dist);
		  dist = dist * 60 * 1.1515;
		 // Log.v("Safecell :"+"Distance in miles",""+dist +" Miles"  );
		 // Log.v("Safecell :"+"Distance in Kilometers",""+dist*1.6 +" Kms"  );
		  return (dist);
	         //return distance ;
	    }
	
		/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
		/*::  This function converts decimal degrees to radiant             :*/
		/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
		private static double deg2rad(double deg) {
		  return (deg * Math.PI / 180.0);
		}

		/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
		/*::  This function converts radiant to decimal degrees             :*/
		/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
		private static double rad2deg(double rad) {
		  return (rad * 180.0 / Math.PI);
		}



	public static double timeDifference(String timeFisrt, String timeSecond) {
		String timeIn,timeOut;
		
		if (timeFisrt.equalsIgnoreCase("0000-00-00 00:00:00")) {
			timeIn = timeFisrt;
			timeOut = timeSecond;
		} else {
			timeIn = timeFisrt;
			timeOut = timeSecond;
			Calendar calendarFirstTimeIn = Calendar.getInstance();
			Calendar calendarSecondTimeIn = Calendar.getInstance();

			calendarFirstTimeIn.set(Integer.parseInt(timeIn.substring(0, 4)),
					Integer.parseInt(timeIn.substring(5, 7)), Integer
					.parseInt(timeIn.substring(8, 10)), Integer
					.parseInt(timeIn.substring(11, 13)), Integer
					.parseInt(timeIn.substring(14, 16)), Integer
					.parseInt(timeIn.substring(17, 19)));

			calendarSecondTimeIn.set(Integer.parseInt(timeOut.substring(0, 4)),
					Integer.parseInt(timeOut.substring(5, 7)), Integer
					.parseInt(timeOut.substring(8, 10)), Integer
					.parseInt(timeOut.substring(11, 13)), Integer
					.parseInt(timeOut.substring(14, 16)), Integer
					.parseInt(timeOut.substring(17, 19)));

			double milliseconds1 = calendarFirstTimeIn.getTimeInMillis();
			double milliseconds2 = calendarSecondTimeIn.getTimeInMillis();
			double diffMilliSecond = milliseconds2 - milliseconds1;
	
			//Log.v("Safecell :"+"Time difference in mili second",""+diffMilliSecond);
			return diffMilliSecond;
		}
		return 0;

	}

	
}
