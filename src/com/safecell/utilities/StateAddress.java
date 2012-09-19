package com.safecell.utilities;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import com.safecell.R;

public class StateAddress {

	public static Activity currentActivity;
	
	public StateAddress() {
		
	}
	
	public void  setTextLocation()
	{
		if(currentActivity != null)
		{
			
			TextView tvlocation = (TextView)currentActivity.findViewById(R.id.tabBarCurentLocationTextView);
			if(tvlocation != null)
			{
			tvlocation.setText(LocationSP.LocationSP);
			tvlocation.postInvalidate();
			//Log.v("Safecell :"+"SateAdress","tvlocation="+LocationSP.LocationSP);
			}else
			{
				//Log.v("Safecell :"+"SateAdress","tvlocation=null");
			}
			
		}else{
			//Log.v("Safecell :"+"SateAdress","Currentactivtiy=null");
		}
	}
}