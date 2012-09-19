package com.safecell.networking;

import java.net.SocketTimeoutException;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONException;
import org.json.JSONObject;

import com.safecell.utilities.URLs;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

public class UpdateAccountDetails extends AbstractProxy {
	
	private JSONObject accountJsonObject;
	private StringEntity stringEntity ;
	
	//private HttpResponse response;
	
	HashMap<Object, Object>profileMap = new HashMap<Object, Object>();
	private String apikeyPut;
	private int profileID;
	
	public UpdateAccountDetails(Context context)
	{
		super(context);
	}
	
	public  UpdateAccountDetails(Context context, HashMap<Object, Object>map,String apiKey,int profileID)
	{
		super(context);
		
		profileMap = map;
		apikeyPut = apiKey; 
		this.profileID = profileID;
		
		
	}
	
	public void updateAccountJson()
	{
		
		 accountJsonObject=new JSONObject();
		JSONObject masterProfileAttributesJsonObject=new JSONObject(profileMap);
		
		try {
			accountJsonObject.put("profile", masterProfileAttributesJsonObject);
			
			//Log.v("Safecell :"+"JSON BODY",accountJsonObject.toString(4) );
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public HttpResponse putRequest() 
	{
		HttpClient client=new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
		String url=Uri.decode(URLs.REMOTE_URL+"api/1/profiles/"+profileID);
//		System.out.println(url);
		
		//HttpPut put=new HttpPut(url);
		try {
			HttpPut put=new HttpPut(url);
			stringEntity=new StringEntity(accountJsonObject.toString());
			put.setHeader("Content-Type", "application/json");
			put.setHeader("x-api-key", apikeyPut);
			put.setEntity(stringEntity);
			
			response = client.execute(put);
		
			if (response.getStatusLine().toString().equalsIgnoreCase("HTTP/1.1 200 OK")){	
				return response;				 
			 } else{
					
					failureMessage = "Profile updation failed because of an unexpected error.";
					response = null;
				}
			
		}
		catch (SocketTimeoutException e1) {
			// TODO: handle exception
			failureMessage = "Profile updation failed because of an unexpected error.";
			response = null;
		}
		
		catch (Exception e) {
			// TODO Auto-generated catch block
			
			failureMessage = "Profile updation failed because of an unexpected error.";
			response = null;
			e.printStackTrace();
		}
		
		
		return response;
		
		
	}

}
