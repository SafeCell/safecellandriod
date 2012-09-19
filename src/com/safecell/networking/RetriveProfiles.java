package com.safecell.networking;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONObject;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.safecell.utilities.URLs;

public class RetriveProfiles extends AbstractProxy{

	String result="";
	JSONObject jsonObject = new JSONObject();
	
	public RetriveProfiles(Context context, JSONObject json){
		super(context);
		this.jsonObject =json;
	}
	public String  Retrive()
	{
		
		HttpClient client = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 60000); // Timeout
		String url = Uri.decode(URLs.REMOTE_URL+"api/1/user_session");
		
//		System.out.println(url);
		try {
			
			HttpPost post = new HttpPost(url);
			StringEntity stringEntity = new StringEntity(jsonObject.toString());
//			System.out.println("String Entity"+jsonObject.toString(4));
			post.setHeader("Content-Type", "application/json");
			post.setEntity(stringEntity);
			
			response = client.execute(post);
			result = getResponseBody();
			statusCode = response.getStatusLine().getStatusCode();
			//Log.v("Safecell :"+"StatusCode", "Response code = "+ statusCode);
			//Log.v("Safecell :"+"result", "Response result = "+ result);
			
			if (statusCode != 200) {
				response = null;
				result = null;
				failureMessage = "The Login failed because of an unexpected error.";
			}
			if (statusCode == 422) {
				response = null;
				result = null;
				failureMessage = "The username or password is incorrect.";
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			response = null;
			result = null;
			failureMessage = "The Login failed because of an unexpected error.";
		}
		return result;
				
	}
	
}
