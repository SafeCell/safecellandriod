package com.safecell.networking;

import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONException;
import org.json.JSONObject;

import com.safecell.model.SCProfile;
import com.safecell.utilities.URLs;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

public class SubmitAccountDetails extends AbstractProxy
{
	private JSONObject outerJsonObject;
	private StringEntity stringEntity ;
	private HttpResponse response;
	HashMap<Object, Object>profileMap = new HashMap<Object, Object>();
	
	
	public SubmitAccountDetails(Context context,HashMap<Object, Object> map ) {
		
		super(context);
		profileMap = map;
	}
	
	
	public void createAccountJson()
	{
		outerJsonObject = new JSONObject();
		JSONObject accountJsonObject = new JSONObject();
		String uniqueID = context.getSharedPreferences("AccountUniqueID", context.MODE_WORLD_WRITEABLE).getString("AccountUID",SCProfile.newUniqueDeviceKey());
		
		JSONObject masterProfileAttributesJsonObject = new JSONObject(profileMap); 
		
		try {
			accountJsonObject.put("master_profile_attributes", masterProfileAttributesJsonObject);
			accountJsonObject.put("originator_token", uniqueID);
			outerJsonObject.put("account", accountJsonObject);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	public HttpResponse postRequest(){
		HttpClient client = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); // Timeout
		
		String url = Uri
				.decode(URLs.REMOTE_URL+"api/1/accounts");
//		System.out.println(url);
		try {
			
		
			HttpPost post = new HttpPost(url);
			stringEntity = new StringEntity(outerJsonObject.toString());
			
//			System.out.println("String Entity"+outerJsonObject.toString(4));
			post.setHeader("Content-Type", "application/json");
			
			post.setEntity(stringEntity);
//			System.out.println("String Entity"+stringEntity.toString());
			
			response = client.execute(post);
			//Log.v("Safecell :"+"Responce Status",response.getStatusLine().toString());
			
			 if (response.getStatusLine().toString().equalsIgnoreCase("HTTP/1.1 200 OK"))
			 {
				 return response;
			 }
			 else
				{
				 if(response.getStatusLine().getStatusCode() == 422)  {
						failureMessage = "Invalid Email id.";
					} else 
					{
						failureMessage = "Create Account is failed because of an unexpected error.";
					}
				 response = null;
				}

		} catch (Exception e) {
			response = null;
			failureMessage = "Create Account is failed because of an unexpected error.";
			e.printStackTrace();
		}
		return response;
	}
	
}
