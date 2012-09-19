package com.safecell.utilities;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

public class LocationSP {

	Context context;
	public static String LocationSP = "";
	protected String message = "";
	Location location;

	public LocationSP(Location location, Context ctx) {
		this.context = ctx;
		this.location = location;
	}

	public void setAddressLine() {

		Geocoder gc = new Geocoder(this.context);
		String address = "";

		List<Address> mylist;
		try {
			mylist = gc.getFromLocation(location.getLatitude(), location
					.getLongitude(), 1);

			if (!mylist.isEmpty()) {
				Address ad = mylist.get(0);
				// Log.v("Safecell :"+"street_address",""+ad.getAddressLine(0));
				// Log.v("Safecell :"+"city_zipcode",""+ad.getAddressLine(1));
			/*	for (int i = 0; i <= ad.getMaxAddressLineIndex(); i++) {
					// Log.v("Safecell :"+"getAddressLine"+i,ad.getAddressLine(i));
				}
				// Log.v("Safecell :"+"ad.getAdminArea()",ad.getAdminArea());
*/				// Log.v("Safecell :"+"ad.getCountryCode()",ad.getCountryCode());
				// Log.v("Safecell :"+"ad.getCountryName()",ad.getCountryName());
				// Log.v("Safecell :"+"ad.getFeatureName()",ad.getFeatureName());
				// Log.v("Safecell :"+"ad.getLocality()",ad.getLocality());
				// Log.v("Safecell :"+"ad.getSubAdminArea()",ad.getSubAdminArea());

				/*address += ad.getLocality() + ", " + ad.getSubAdminArea()
						+ ", " + ad.getAdminArea();*/
				if(ad != null)
				{
					
				if (ad.getLocality()!=null){
					String locality = ad.getLocality();
					
					if(locality != null) {
						address += locality;
					}
				}
				if (ad.getSubAdminArea()!=null){
					if(address.length() > 0) {
						address += ", ";
					}
					address += ad.getSubAdminArea();
					
				}
				if (ad.getAdminArea()!=null){
					if(address.length() > 0) {
						address += ", ";
					}
					
					address += ad.getAdminArea();
				
				}
				}
				if (null == address) {
					LocationSP = "Resolving...";
//					Log.v("Safecell :"+"failureMessage", LocationSP);
				} else {
					LocationSP = address;
//					Log.v("Safecell :"+"Location Addresss", LocationSP);
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}

	}

}
