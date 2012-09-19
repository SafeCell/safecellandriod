package com.safecell.networking;

import java.io.InputStream;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.safecell.utilities.StreamToStringHelper;
import com.safecell.utilities.URLs;

public class CreateAccountFromProfile extends AbstractProxy{

	private JSONObject outerJsonObject;
	private StringEntity stringEntity;
	private HttpResponse response;
	private String profileResponse;
	HashMap<Object, Object> profileMap = new HashMap<Object, Object>();
	private String apikeyPost;

	public CreateAccountFromProfile(Context context, HashMap<Object, Object> map, String apiKey) {
		super(context);
		profileMap = map;
		apikeyPost = apiKey;
	}

	public void createProfileJson() {
		outerJsonObject = new JSONObject();

		JSONObject profileAttributesJsonObject = new JSONObject(profileMap);

		try {
			outerJsonObject.put("profile", profileAttributesJsonObject);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public String postRequest() {
		HttpClient client = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); // Timeout

		String url = URLs.REMOTE_URL + "api/1/profiles";
//		System.out.println(url);
		try {

			HttpPost post = new HttpPost(url);
			
			stringEntity = new StringEntity(outerJsonObject.toString());
			post.setHeader("Content-Type", "application/json");
			post.setHeader("x-api-key", apikeyPost);
			post.setEntity(stringEntity);
			//Log.v("Safecell :"+"String Entity", stringEntity.toString());

			response = client.execute(post);
			//Log.v("Safecell :"+" Responce =",response.getStatusLine().toString());
			
			if (response.getStatusLine().toString().equalsIgnoreCase(
					"HTTP/1.1 200 OK")) {

				InputStream in = response.getEntity().getContent();
				if (in != null) {
					profileResponse = StreamToStringHelper
							.convertStreamToString(in);
					//Log.v("Safecell :"+"Profile Responce =", profileResponse);
					return profileResponse;
				}
			}

				else
				{
					if(response.getStatusLine().getStatusCode() == 422)  {
						failureMessage = "Invalid Email id.";
						//Log.v("Safecell :"+"failureMessage",failureMessage);
					} else 
					{
						failureMessage = "Create Account is failed because of an unexpected error.";
						//Log.v("Safecell :"+"failureMessage",failureMessage);
					}
					profileResponse = null;
				}
		} catch (Exception e) {
			// TODO: handle exception
			profileResponse = null;
			failureMessage = "Create Account is failed because of an unexpected error.";
			e.printStackTrace();
		}
		return profileResponse;
	}

}
