package com.safecell.networking;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

public class GetTermsConditionsResponseHandler {

	public GetTermsConditionsResponseHandler() {
		// TODO Auto-generated constructor stub
	}
	
	public String termsResponseStr (String result){
		
		String values = null;
		try {
			
			JSONArray termsJsonArray = new JSONArray(result);
			//Log.v("Safecell :"+"jsonObject", termsJsonArray.toString(4));
			
			JSONObject valuesObject = termsJsonArray.getJSONObject(0);
			values = valuesObject.getString("value");
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return values;
	}
}
