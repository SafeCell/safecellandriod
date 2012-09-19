package com.safecell.networking;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import android.content.Context;

import com.safecell.utilities.URLs;


public class GetLicenseKey extends AbstractProxy{

	public  GetLicenseKey(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	
	public String getRequest()
	{
		HttpClient client = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); // Timeout
	
		String url = URLs.REMOTE_URL +"api/1/license_classes" ;
		
		HttpGet postRequest = new HttpGet(url);
		postRequest.setHeader("Content-Type", "application/json");
		
		String result = null;
		try {
			response = client.execute(postRequest);
			result = getResponseBody();
			
			if (response.getStatusLine().getStatusCode() != 200) {
				response = null;
				result = null;
				failureMessage = "The licenses downlaod failed because of an unexpected error.";
			}
			
		} catch (Exception e) {
			response = null;
			result = null;
			failureMessage = "The licenses downlaod failed because of an unexpected error.";
		}
		
		return result;
		
	}
	
}
