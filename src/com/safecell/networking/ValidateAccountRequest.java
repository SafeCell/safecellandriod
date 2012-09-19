package com.safecell.networking;

import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.safecell.utilities.StreamToStringHelper;
import com.safecell.utilities.URLs;

public class ValidateAccountRequest extends AbstractProxy {

	private String accountCode;
	String result =null;
	public ValidateAccountRequest(Context context,String accCode) {
		
		super(context);
		accountCode = accCode;
	}

	public String Request() {
		 
		String url = Uri.decode(URLs.REMOTE_URL+"api/1/accounts/"+
		 accountCode + "/validate");
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);
		HttpResponse response;

		try {
			response = httpclient.execute(httpget);
			Log.d("tag", "Status:[" + response.getStatusLine().toString()+ "]");
			
			HttpEntity entity = response.getEntity();
            
			if (response.getStatusLine().toString().equalsIgnoreCase("HTTP/1.1 200 OK")) {
            	if (entity != null) {

    				InputStream instream = entity.getContent();
    				result = StreamToStringHelper
    						.convertStreamToString(instream);
    				instream.close();
    				//Log.v("Safecell :"+"Validate Account Response", ""+result);
    				return result;
    			}
			}
			else{
        		
        		failureMessage = "Invalid Verification Code";
        		
        		result = null;
        	}
            
            
		} catch (Exception e) {

			failureMessage = "Invalid Verification Code ";
    		result = null;
			e.printStackTrace();

		}
		return result;
	}

}
