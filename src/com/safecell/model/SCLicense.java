package com.safecell.model;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.safecell.dataaccess.LicenseRepository;


public class SCLicense {

	private String name;
	private int id;
	private String description;
	private String key;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	public static SCLicense licenseFromJSONObject (JSONObject licenseJsonObject) throws JSONException
	{
		
		JSONObject propertiesObj = licenseJsonObject.getJSONObject("license_class");
		
		if(propertiesObj == null) {
			return null;
		}
		int id = propertiesObj.getInt("id");
		 String key = propertiesObj.getString("key");
		 String description = propertiesObj.getString("description");
		 String name = propertiesObj.getString("name");
		 
		// Log.v("Safecell :"+"name", ""+name);
		// Log.v("Safecell :"+"id", ""+id);
		// Log.v("Safecell :"+"description", ""+description);
		// Log.v("Safecell :"+"key", ""+key);
		
		 SCLicense license = new SCLicense();
		 
		 license.id = propertiesObj.getInt("id");
		 license.key = propertiesObj.getString("key");
		 license.description = propertiesObj.getString("description");
		 license.name = propertiesObj.getString("name");
		
		return license;
		
	}
	
	public static void insertOrUpdateLicenseKey(ArrayList<SCLicense> licenses, Context context) {
		
		boolean licenseIdPresent = false;
		
		for (int i = 0; i < licenses.size(); i++) {
			int id = licenses.get(i).getId();
			LicenseRepository licenseRepository = new LicenseRepository(context);
			licenseIdPresent = licenseRepository.licensesIdPresent(String.valueOf(id));
			
			if (licenseIdPresent) {
				licenseRepository.updateQuery(licenses.get(i)); 
				//Log.v("Safecell :"+"update", "update");
			}else {
				licenseRepository.insertQuery(licenses.get(i));
				//Log.v("Safecell :"+"insert", "insert");
			}
		}

	}
}
