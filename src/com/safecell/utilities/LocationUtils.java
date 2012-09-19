package com.safecell.utilities;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class LocationUtils extends Activity implements LocationListener {


	private String bestProvider;
	private Location currentLocation;
	public static String CurrenLocationName;

	public LocationUtils() {

		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		bestProvider = locationManager.getBestProvider(criteria, true);

		if (bestProvider != null) {
			locationManager.requestLocationUpdates(bestProvider, 60000, // 1min
					100, // 100m
					LocationUtils.this);
		}

		currentLocation = locationManager.getLastKnownLocation(bestProvider);

		if (currentLocation == null) {
			//Log.v("Safecell :"+"if (location == null)", "yes");
			LocationManager lmNetwork;
			lmNetwork = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			lmNetwork.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
					0, 0, LocationUtils.this);
			currentLocation = lmNetwork
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}

	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		currentLocation = location;
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

}
