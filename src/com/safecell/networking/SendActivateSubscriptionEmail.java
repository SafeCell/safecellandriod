package com.safecell.networking;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import android.content.Context;

import com.safecell.dataaccess.AccountRepository;
import com.safecell.utilities.URLs;

public class SendActivateSubscriptionEmail extends AbstractProxy {
	
	public SendActivateSubscriptionEmail(Context context) {
		super(context);
	}
	
	public boolean postRequest() {
		
		HttpClient client = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 60000); // Timeout
		
		String url = URLs.REMOTE_URL + "api/1/account_activation";
		
		HttpPost postRequest = new HttpPost(url);
		
		AccountRepository accountRepository = new AccountRepository(this.context);
		String currentAPIKey = accountRepository.currentAPIKey();
		
		postRequest.setHeader("Content-Type", "application/json");
		postRequest.setHeader("x-api-key", currentAPIKey);
		
		//Log.v("Safecell :"+"URL", url);
		//Log.v("Safecell :"+"Method", "POST");
		//Log.v("Safecell :"+"Content-Type", "application/json");
		//Log.v("Safecell :"+"x-api-key", currentAPIKey);
		
		boolean result = false;
		try {
			response = client.execute(postRequest);
			
			//Log.v("Safecell :"+"Response Status Line", response.getStatusLine().toString());
			//Log.v("Safecell :"+"Response Body", StreamToStringHelper.convertStreamToString(response.getEntity().getContent()));
		
			if (response.getStatusLine().toString().equalsIgnoreCase("HTTP/1.1 200 OK")) {
				result = true;
			} else {
				result = false;
				
				if(response.getStatusLine().getStatusCode() == 422)  {
					failureMessage = "The email is already associated with another account.";
				} else {
					failureMessage = "The activation email was not sent because of an unexpected error.";
				}
			}
		} catch(Exception e) {
			response = null;
			e.printStackTrace();
		}
		
		return result;
	}
	
}
