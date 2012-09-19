package com.safecell.networking;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.safecell.model.SCSchool;

public class GetSchoolsResponseHandler {
	
	private String schoolsJSONArrayStr;
	
	public GetSchoolsResponseHandler(String schoolsJSONArr) {
		this.schoolsJSONArrayStr = schoolsJSONArr;
	}
	
	public ArrayList<SCSchool> handleGetSchoolsResponse() {
		ArrayList<SCSchool> schools = new ArrayList<SCSchool>();
		
		try {
			JSONArray schoolsJSONObjects = new JSONArray(schoolsJSONArrayStr);
			
			schools = new ArrayList<SCSchool>(schoolsJSONObjects.length());
			
			for(int i = 0; i < schoolsJSONObjects.length(); i++) {
				JSONObject schoolJSONObject = schoolsJSONObjects.getJSONObject(i);
				SCSchool school = SCSchool.schoolFromJSONObject(schoolJSONObject);
				schools.add(school); 
			}
			
		} catch (JSONException e) {

			e.printStackTrace();
			//schools = null;
			
		}
		
		return schools;
	}

	public String getSchoolsJSONArr() {
		return schoolsJSONArrayStr;
	}
}
