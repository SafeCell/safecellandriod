package com.safecell.networking;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import android.content.Context;
import android.util.Log;

import com.safecell.utilities.URLs;

public class GetTermsConditions extends AbstractProxy{

	public GetTermsConditions(Context context) {
		super(context);
		
	}

	public String getTermsConditionsStr() {
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 60000); // Timeout
		//http://safecell-test.heroku.com/api/1/site_settings
		String url = URLs.REMOTE_URL +"api/1/site_settings";
		String result = null;
		HttpGet httpGet = new HttpGet(url);
		
		try {
			response = httpClient.execute(httpGet);
			result = getResponseBody();
			//Log.v("Safecell :"+"Terms Result",""+result);
			statusCode = response.getStatusLine().getStatusCode();
			//Log.v("Safecell :"+"Response Status Line", response.getStatusLine().toString());
			if (statusCode != 200) {
				response = null;
				result = null;
			}
		} catch (Exception e) {
			response = null;
			result = null;
		}
		return result;
	}
	
}
