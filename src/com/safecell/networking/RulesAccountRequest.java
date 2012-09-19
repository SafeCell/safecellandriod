package com.safecell.networking;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.safecell.dataaccess.AccountRepository;
import com.safecell.model.SCRule;
import com.safecell.utilities.QueryString;
import com.safecell.utilities.URLs;

public class RulesAccountRequest extends AbstractProxy {
	
	private final String TAG = "JSONhelper";
	private String API_KEY;
	

	private final String HEADER = "x-api-key";
	private double latitude;
	private double longitude;
	private double radius;
	
	ArrayList<SCRule> ruleArrayList = new ArrayList<SCRule>();

	public RulesAccountRequest(Context context, double latitude, double longitude,double radius) {
	
		super(context);
		this.latitude = latitude;
		this.longitude = longitude;
		this.radius = radius;
		
		AccountRepository accountRepository = new AccountRepository(context);
		API_KEY = accountRepository.currentAPIKey();
	}

	public HttpResponse ruleRequest() {
		
		HttpClient httpclient = new DefaultHttpClient();
		
		QueryString queryString = new QueryString();
		queryString.add("lat", "" + latitude);
		queryString.add("lng", "" + longitude);
		queryString.add("distance", "" + radius);
		
		String url = URLs.REMOTE_URL + "api/1/rules?" +queryString.getQuery();
		
		HttpGet httpget = new HttpGet(url);
		HttpResponse response;
		httpget.addHeader(HEADER, API_KEY); 
		
		try {
			response = httpclient.execute(httpget);
			//Log.v("Safecell :"+"Rules Response", response.getStatusLine().toString());
			//Log.v("Safecell :"+"Response", response+"response");
			if (response.getStatusLine().toString().equalsIgnoreCase("HTTP/1.1 200 OK"))
			 {
				//Log.v("Safecell :"+"Response", response+"response");
				 return response;
			 }
			 else
				{
				
					failureMessage = "Rule download failed because of an unexpected error.";
					//Log.v("Safecell :"+"failureMessage", failureMessage);
					response = null;
				}
			
		} catch (Exception e) {
			failureMessage = "Rule download failed because of an unexpected error.";
			e.printStackTrace();
			//Log.v("Safecell :"+"catch", "catch");
			response = null;
		}
		
		return response;
		
	}
	
	public ArrayList<SCRule> handleGetResponseSCRule(HttpResponse httpResponse) { 

		String rulesJsonArrayStr = null;
		JSONArray ruleJSONArray = null;
		SCRule ruleModel = new SCRule();
		SCRule tempRuleModel;
		
		
			try {
				rulesJsonArrayStr = EntityUtils.toString(httpResponse.getEntity());
				ruleJSONArray = new JSONArray(rulesJsonArrayStr);
				
				//Log.v("Safecell :"+TAG, ruleJSONArray.toString(4));
				
				for (int i = 0; i < ruleJSONArray.length(); i++) {
					
					JSONObject ruleJsonObject1 = ruleJSONArray.getJSONObject(i);
					JSONObject ruleJsonObject = ruleJsonObject1.getJSONObject("rule");

					tempRuleModel = new SCRule();

					tempRuleModel.setWhen_enforced(ruleJsonObject.getString("when_enforced"));
										
					tempRuleModel.setLabel(ruleJsonObject.getString("label"));
					tempRuleModel.setCreated_at(ruleJsonObject.getString("created_at"));
					tempRuleModel.setBusdriver(ruleJsonObject.getBoolean("busdriver"));
					tempRuleModel.setNovice(ruleJsonObject.getBoolean("novice"));
					tempRuleModel.setUpdated_at(ruleJsonObject.getString("updated_at"));
					tempRuleModel.setPrimary(ruleJsonObject.getBoolean("primary"));
					tempRuleModel.setCrash_collection(ruleJsonObject
							.getBoolean("crash_collection"));
					tempRuleModel.setZone_id(ruleJsonObject.getInt("zone_id"));
					
					tempRuleModel.setId(ruleJsonObject.getInt("id"));
					
					tempRuleModel.setRule_type(ruleJsonObject
							.getString("rule_type"));
					tempRuleModel.setPreemption(ruleJsonObject
							.getBoolean("preemption"));
					tempRuleModel.setDetail(ruleJsonObject.getString("detail"));
					tempRuleModel.setZone_name(ruleJsonObject
							.getString("zone_name"));
					tempRuleModel.setLicenses(ruleJsonObject.getString("licenses"));
					
					String allDriversStr = ruleJsonObject.getString("alldrivers");
					boolean isAllDrivers = false;
					
					try {
						isAllDrivers = ruleJsonObject.getBoolean("alldrivers");
					} catch (Exception e) {
						//Log.v("Safecell :"+"RULES--alldrivers", "Caught Exception");
						isAllDrivers = false;
					}
					
					if (allDriversStr.equalsIgnoreCase("null")) {
						 isAllDrivers = false;
					}
					
					tempRuleModel.setAlldrivers(isAllDrivers);
					
					ruleModel = tempRuleModel;
					ruleArrayList.add(ruleModel);
				}

			} catch (ParseException e) {
				//Log.v("Safecell :"+TAG, "ParseException");
				e.printStackTrace();
			} catch (IOException e) {
				//Log.v("Safecell :"+TAG, "IOException");
				e.printStackTrace();
			} catch (JSONException e) {
				//Log.v("Safecell :"+TAG, "JSONException");
				e.printStackTrace();
			}
		
		return ruleArrayList;
	}

}
