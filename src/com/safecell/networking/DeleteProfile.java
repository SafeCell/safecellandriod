package com.safecell.networking;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import android.content.Context;

import com.safecell.dataaccess.AccountRepository;
import com.safecell.utilities.URLs;

public class DeleteProfile extends AbstractProxy {
	
	private int profileId;
	
	public DeleteProfile(Context context, int profileId) {
		super(context);
		this.profileId = profileId;
	}
	
	public boolean deleteRequest() {
		HttpClient client = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); // Timeout
		
		String url = URLs.REMOTE_URL + "api/1/profiles/" + profileId;
		
		HttpDelete deleteRequest = new HttpDelete(url);
		
		AccountRepository accountRepository = new AccountRepository(this.context);
		String currentAPIKey = accountRepository.currentAPIKey();
		
		deleteRequest.setHeader("Content-Type", "application/json");
		deleteRequest.setHeader("x-api-key", currentAPIKey);
		
		//Log.v("Safecell :"+"URL", url);
		//Log.v("Safecell :"+"Method", "DELETE");
		//Log.v("Safecell :"+"Content-Type", "application/json");
		//Log.v("Safecell :"+"x-api-key", currentAPIKey);
		
		boolean result = false;
		try {
			response = client.execute(deleteRequest);
			
			//Log.v("Safecell :"+"Response Status Line", response.getStatusLine().toString());
			//Log.v("Safecell :"+"Response Body", StreamToStringHelper.convertStreamToString(response.getEntity().getContent()));
		
			if (response.getStatusLine().toString().equalsIgnoreCase("HTTP/1.1 200 OK")) {
				result = true;
			} else {
				result = false;
				
				if(response.getStatusLine().getStatusCode() == 404)  {
					failureMessage = "The profile being deleted was not found";
				} else {
					failureMessage = "The profile deletion failed because of an unexpected error.";
				}
			}
		} catch(Exception e) {
			response = null;
			failureMessage = "The profile deletion failed because of an unexpected error.";
			e.printStackTrace();
		}
		
		return result;
	}
	
	public int getProfileId() {
		return profileId;
	}
}
