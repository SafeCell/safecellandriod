package com.safecell;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
        Log.i("IntentReceiver", "Got intent " + intent.getAction() + " / data: " + intent.getDataString());

		try {
			//Log.v("Safecell :"+"Boot BroadcastReceiver","Received");
			
			
			Intent myStarterIntent = new Intent(context, SplashScreenActivity.class);
			myStarterIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        context.startActivity(myStarterIntent);
	        
	        // on boot up automatically start the service
            Intent mIntent = new Intent(context, TrackingService.class);
            context.startService(mIntent);
//            ServiceHandler.getInstance(context).bindService();
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}   
    }