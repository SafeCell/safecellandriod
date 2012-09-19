package com.safecell.utilities;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.util.Log;

public class InformatonUtils {

	public InformatonUtils()
	{
		
	}
	public static int totalGrade(float totalMiles, float penaltyPoint) {

		float safetyPoint = totalMiles + penaltyPoint;
		float totalGrade = (safetyPoint / totalMiles) * 100;
		return Math.round(totalGrade);

	}
	
	public static boolean isServiceRunning(Context context) {
	        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	            Log.d("InformatonUtils", "Service Name = "+service.service.getClassName());
	            if ("com.safecell.TrackingService".equals(service.service.getClassName())) {
	                return true;
	            }
	        }
	        return false;
	    }

}
