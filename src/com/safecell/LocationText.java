package com.safecell;

import android.app.Activity;
import android.widget.TextView;

import com.safecell.utilities.LocationSP;

public class LocationText {
	public static Activity currentActivity;
	
	public LocationText(){
		
	}
	public void setText(){
		
		TextView tv = (TextView) currentActivity.findViewById(R.id.tabBarCurentLocationTextView);
		tv.setText(LocationSP.LocationSP);
	}

}
