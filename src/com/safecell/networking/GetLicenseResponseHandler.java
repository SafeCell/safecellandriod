package com.safecell.networking;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.safecell.model.SCLicense;

public class GetLicenseResponseHandler {

	private String licenseJSONArrayStr;

	public GetLicenseResponseHandler(String licenseArrayStr) {

		this.licenseJSONArrayStr =licenseArrayStr;
	}
	
	public ArrayList<SCLicense> getLicenseKey()
	{
		ArrayList<SCLicense> licenses = null;
		
		try {
			JSONArray licenseJsonArray = new JSONArray(licenseJSONArrayStr);
			//Log.v("Safecell :"+"LicenseKey",licenseJsonArray.toString(4));
			
			licenses = new ArrayList<SCLicense>(licenseJsonArray.length());
			for (int i = 0; i < licenseJsonArray.length(); i++) {
				
				JSONObject licenseJsonObject = licenseJsonArray.getJSONObject(i);
				SCLicense license = SCLicense.licenseFromJSONObject(licenseJsonObject);
				licenses.add(license);
			}
		} catch (JSONException e) {
			licenses = null;
		}
		return licenses;
	}
}
