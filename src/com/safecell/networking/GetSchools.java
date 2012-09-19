package com.safecell.networking;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import android.content.Context;
import android.util.Log;

import com.safecell.dataaccess.AccountRepository;
import com.safecell.utilities.QueryString;
import com.safecell.utilities.URLs;

public class GetSchools extends AbstractProxy {
	
	private double latitude;
	private double longitude;
	private double radius;
	
	public GetSchools(Context context, double latitude, double longitude, double radius) {
		super(context);
		this.latitude = latitude;
		this.longitude = longitude;
		this.radius = radius;
	}
	
	public String getRequest() {
		HttpClient client = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); // Timeout
		
		QueryString queryString = new QueryString();
		queryString.add("lat", "" + latitude);
		queryString.add("lng", "" + longitude);
		queryString.add("distance", "" + radius);
	
		
		String url = URLs.REMOTE_URL + "api/1/schools?" + queryString.getQuery();
		
		HttpGet postRequest = new HttpGet(url);
		AccountRepository accountRepository = new AccountRepository(this.context);
		String currentAPIKey = accountRepository.currentAPIKey();
			
		//String currentAPIKey = "99a02e2836a750ca3f10d34eb690d34f9de362d5875fdd3ee906445cef6a16272bd8b8ac38ae5be94147ff6c7332766ef2be6ec2db98c2d61310e139d3174209";
		postRequest.setHeader("Content-Type", "application/json");
		postRequest.setHeader("x-api-key", currentAPIKey);
		
		//Log.v("Safecell :"+"URL", url);
		//Log.v("Safecell :"+"Method", "GET");
		//Log.v("Safecell :"+"Content-Type", "application/json");
		//Log.v("Safecell :"+"x-api-key", currentAPIKey);
		
		String result = null;
		
		try {
			response = client.execute(postRequest);
			result = getResponseBody();
			
			//Log.v("Safecell :"+"Response Status Line", response.getStatusLine().toString());
			//Log.v("Safecell :"+"  SCHOOOL :Response Body", result);
		
			if (response.getStatusLine().getStatusCode() != 200) {
				response = null;
				result = null;
				
				failureMessage = "The schools downlaod failed because of an unexpected error.";
			}
		} catch(Exception e) {
			response = null;
			e.printStackTrace();
		}
		
		return result;
	}

}
