package com.safecell;

import android.os.Bundle;

import com.google.android.maps.MapActivity;
import com.safecell.utilities.FlurryUtils;

public class Trial extends MapActivity {
	
	@Override
	protected void onStart() {
		super.onStart();
		FlurryUtils.startFlurrySession(this);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		FlurryUtils.endFlurrySession(this);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_layout);
		
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

}
