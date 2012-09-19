package com.safecell.receiver;

import com.safecell.TrackingScreenActivity;
import com.safecell.TrackingService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class LockReceiver extends BroadcastReceiver {
	public static boolean wasLoacked = false;

	@Override
	public void onReceive(Context context, Intent intent) {
		
		SharedPreferences sharedPreferences = context.getSharedPreferences("TRIP", 1);
		boolean isTripStarted = sharedPreferences.getBoolean("isTripStarted", false);
		
//		Log.v("SafeCell :", "LockReceiver : isTripStarted : " + isTripStarted);
//		Log.v("SafeCell :", "LockReceiver : Tracking Screen IsBackground : " + TrackingScreenActivity.isBackground);


		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
//			Log.v("SafeCell :", "ACTION_SCREEN_OFF");
			wasLoacked = true;
		} else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
			
			if (wasLoacked) {
				if(isTripStarted && !(TrackingScreenActivity.isBackground))
				TrackingService.deleteLastInterruption();
			}
			
//			Log.v("SafeCell :", "ACTION_SCREEN_ON");
			wasLoacked = false;
		}

	}
}
