package com.safecell.networking;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import com.safecell.utilities.URLs;
import android.content.Context;
import android.util.Log;

import com.safecell.dataaccess.AccountRepository;
import com.safecell.model.SCProfile;
import com.safecell.utilities.QueryString;

public class GetAccount extends AbstractProxy {
	
	private SCProfile profile;
	
	public GetAccount(Context context, SCProfile profile) {
		super(context);
		this.profile = profile;
	}
	
	public String getRequest() {
		HttpClient client = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); // Timeout
		
		String url = URLs.REMOTE_URL + "api/1/accounts/" + profile.getAccountId() + "?";
		
		QueryString queryString = new QueryString();
		queryString.add("profile_id", "" + profile.getProfileId());
		queryString.add("device_key", "" + profile.getDeviceKey());
		
		url += queryString.getQuery();
		
		AccountRepository accountRepository = new AccountRepository(this.context);
		String currentAPIKey = accountRepository.currentAPIKey();
		
		HttpGet getRequest = new HttpGet(url);
		getRequest.setHeader("Content-Type", "application/json");
		getRequest.setHeader("x-api-key", currentAPIKey);
		
		//Log.v("Safecell :"+"URL", url);
		//Log.v("Safecell :"+"Method", "GET");
		//Log.v("Safecell :"+"Content-Type", "application/json");
		//Log.v("Safecell :"+"x-api-key", currentAPIKey);
		
		String result = null;
		
		try {
			response = client.execute(getRequest);
			
			result = getResponseBody();
			//Log.v("Safecell :"+"Account Info",""+result);
			//Log.v("Safecell :"+"Response Status Line", response.getStatusLine().toString());
			//Log.v("Safecell :"+"Response Body", getResponseBody());
		
			if (response.getStatusLine().getStatusCode() != 200) {
				response = null;
				result = null;
				
				if(response.getStatusLine().getStatusCode() == 404)  {
					failureMessage = "The account was not found.";
					response = null;
					result = null;
				} else {
					failureMessage = "The account information could not be retrieved because of an unexpected error.";
					response = null;
					result = null;
				}	
			}
		} catch(Exception e) {
			response = null;
			result = null;
			failureMessage = "The account information could not be retrieved because of an unexpected error.";
			e.printStackTrace();
		}
		
		return result;
	}

	public SCProfile getProfile() {
		return profile;
	}
	
}
