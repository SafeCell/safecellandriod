package com.safecell.networking;

import java.net.SocketTimeoutException;
import java.util.ArrayList;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import android.content.Context;
import android.util.Log;

import com.safecell.dataaccess.AccountRepository;
import com.safecell.dataaccess.ProfilesRepository;
import com.safecell.model.SCProfile;
import com.safecell.utilities.QueryString;
import com.safecell.utilities.URLs;

public class CheckAccountStatus extends AbstractProxy{
	
	
	int accountID;
	int profileID;
	int device_key;
	private ArrayList<SCProfile> scProfileArrayList;
	public CheckAccountStatus(Context context){
	super(context);

		scProfileArrayList = new ArrayList<SCProfile>();
	}
	
	public String checkStatus()  
	{
		
			ProfilesRepository profilesRepository = new ProfilesRepository(context);
			scProfileArrayList = profilesRepository.getProfilesArrayList();
			SCProfile scProfile = scProfileArrayList.get(0);
			
			String result = null;
			
			if (scProfileArrayList.size()>0) {
				
				HttpClient httpclient = new DefaultHttpClient();
				HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), 10000); // Timeout
				HttpConnectionParams.setSoTimeout(httpclient.getParams(), 10000);
				String deviceFamilyStr =  scProfile.getDeviceFamily();
				String  appVersion = scProfile.getAppVersion();
				
//				Log.v("Safecell :"+"deviceFamilyStr", deviceFamilyStr);
//				Log.v("Safecell :"+"appVersion", appVersion);
				
				QueryString queryString = new QueryString();
				queryString.add("account_id", scProfile.getAccountID()+"");
				queryString.add("profile_id", scProfile.getProfileId()+"");
				queryString.add("device_key", scProfile.getDeviceKey());
				queryString.add("device_family", deviceFamilyStr);
				queryString.add("app_version", appVersion);
				
				//Log.v("Safecell :"+"account_id", scProfile.getAccountID()+"");
				//Log.v("Safecell :"+"profile_id", scProfile.getProfileId()+"");
				//Log.v("Safecell :"+"device_key", scProfile.getDeviceKey());
				
				
				AccountRepository accountRepository = new AccountRepository(this.context);
				String currentAPIKey = accountRepository.currentAPIKey();
//				Log.v("Safecell :"+"currentAPIKey", ""+currentAPIKey);
				String url = URLs.REMOTE_URL+"api/1/accounts/show?"+queryString.getQuery();
						
					Log.v("Safecell :"+"url",url);
					
					HttpGet httpget = new HttpGet(url);
					httpget.setHeader("Content-Type", "application/json");
					httpget.setHeader("x-api-key", currentAPIKey);
					
					try {
						
						response = httpclient.execute(httpget);
						Log.d("tag", "Status:[" + response.getStatusLine().toString()+ "]");
						result = getResponseBody();
						statusCode = response.getStatusLine().getStatusCode();
						
						if (statusCode != 200) {
							response = null;
							result = null;
							failureMessage = "The account downlaod failed because of an unexpected error.";
//							Log.v("safecell: response  error", failureMessage);
						}else if (statusCode == 404) {
							response = null;
							result = null;
							failureMessage = "Your profile is not found. It might have been deleted by master account owner.";
//							Log.v("safecell: response  error", failureMessage);
						}else if (statusCode == 400 && result.equalsIgnoreCase("Invalid Device Key")) {
							response = null;
							result = null;
							failureMessage = "This profile is already in use on another device.";
//							Log.v("safecell: response  error", failureMessage); 
						}
						
						
					}
					catch (SocketTimeoutException e1) {
						response = null;
						result = null;
//						Log.v("response  error", "TimeOut");
						failureMessage = "The account downlaod failed because of an unexpected error.";
					}
					catch (Exception e) {
						e.printStackTrace();
						response = null;
						result = null;
//						Log.v("response  error", "other");
						failureMessage = "The account downlaod failed because of an unexpected error.";
					}
					
				
			}
			return result;
			
	}
	
	
	
}
