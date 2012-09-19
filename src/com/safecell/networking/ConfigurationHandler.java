/*******************************************************************************
* ConfigurationHandler.java.java, Created: Apr 19, 2012
*
* Part of Muni Project
*
* Copyright (c) 2012 : NDS Limited
*
* P R O P R I E T A R Y &amp; C O N F I D E N T I A L
*
* The copyright of this code and related documentation together with any
* other associated intellectual property rights are vested in NDS Limited
* and may not be used except in accordance with the terms of the licence
* that you have entered into with NDS Limited. Use of this material without
* an express licence from NDS Limited shall be an infringement of copyright
* and any other intellectual property rights that may be incorporated with
* this material.
*
* ******************************************************************************
* ******     Please Check GIT for revision/modification history    *******
* ******************************************************************************
*/
package com.safecell.networking;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.safecell.model.Configuration;
import com.safecell.utilities.StreamToStringHelper;
import com.safecell.utilities.URLs;

import android.net.Uri;
import android.util.Log;

import java.io.InputStream;

/**
 * 
 * @author uttama
 *
 */
public class ConfigurationHandler {
    
    /** single instance*/
    private static ConfigurationHandler handler;
    
    /** Configuration */
    private Configuration configuration;
    
    private final String TAG = ConfigurationHandler.class.getSimpleName();
    
    
    /** private constructor*/
    private ConfigurationHandler(){
        this.configuration = new Configuration();
    }
    
    public static final ConfigurationHandler getInstance(){
        if(handler == null){
            handler = new ConfigurationHandler();
        }
        return handler;
    }
    
    
 
    public void readResponse(int id) {
        try {
            String response = request(id);
            Log.d(TAG, " full configuration responce ; :"+response);
            if(response != null && !response.isEmpty())
            {
	            JSONObject jsonObject = new JSONObject(response);
	           // Log.v("Tracking service", " full configuration responce ; :");
	            JSONArray configObj = jsonObject.getJSONArray("configurations");
	          
	            Log.d(TAG, " full configuration responce ; :"+configObj.getJSONObject(0).getJSONArray("TST").getJSONObject(0).getString("value"));
	           // configuration.setTripStartName(configObj.getJSONObject(0).getJSONArray("TSN").getJSONObject(0).getString("value"));
	           String str="";
	           if(!configObj.getJSONObject(0).getJSONArray("TSN").getJSONObject(0).getString("value").equals(str))
               {
	               int startSpeed = Integer.parseInt(configObj.getJSONObject(0).getJSONArray("TSN").getJSONObject(0).getString("value"));
	               Log.d(TAG, "Start Speed = "+startSpeed);
                   configuration.setTripStartSpeed(startSpeed < 1 ? configuration.getTripStartSpeed() : startSpeed);
               }
	            if(!configObj.getJSONObject(0).getJSONArray("TST").getJSONObject(0).getString("value").equals(str))
	            {
	                int stopTime = Integer.parseInt(configObj.getJSONObject(0).getJSONArray("TST").getJSONObject(0).getString("value"));
	                Log.d(TAG, "Stop Time = "+stopTime);
	            	configuration.setTripStopTime(stopTime < 1 ? configuration.getTripStopTime() : stopTime);
	            }
	            if(!configObj.getJSONObject(0).getJSONArray("DEMAIL").getJSONObject(0).getString("value").equals(str))
	            {
	            	configuration.setDisableEmail(Integer.parseInt(configObj.getJSONObject(0).getJSONArray("DEMAIL").getJSONObject(0).getString("value"))!=0);
	            
	            }
	            if(!configObj.getJSONObject(0).getJSONArray("DCALL").getJSONObject(0).getString("value").equals(str))
	            {
	                boolean disable = Integer.parseInt(configObj.getJSONObject(0).getJSONArray("DCALL").getJSONObject(0).getString("value"))!=0;
	                Log.d(TAG, "Disabled Call = "+disable);
	            	configuration.setDisableCall(disable);
	            }
	            if(!configObj.getJSONObject(0).getJSONArray("DTEXTING").getJSONObject(0).getString("value").equals(str))
	            {
	            	configuration.setDisableTexting(Integer.parseInt(configObj.getJSONObject(0).getJSONArray("DTEXTING").getJSONObject(0).getString("value"))!=0);
	            }
	            if(!configObj.getJSONObject(0).getJSONArray("DWEB").getJSONObject(0).getString("value").equals(str))
	            {
	            	configuration.setDisableWeb(Integer.parseInt(configObj.getJSONObject(0).getJSONArray("DWEB").getJSONObject(0).getString("value"))!=0);
	            }
	            if(!configObj.getJSONObject(0).getJSONArray("LOGWEBPOINTS").getJSONObject(0).getString("value").equals(str))
	            {
	                boolean enable = Integer.parseInt(configObj.getJSONObject(0).getJSONArray("LOGWEBPOINTS").getJSONObject(0).getString("value"))!=0;
	                Log.d(TAG, "Log Web Points = "+enable);
	                configuration.setLogWayPoints(enable);
	            }
	            
	            Log.d(TAG, "Configuration :  "+configuration.toString());
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception Occurred ");
        }
    }
    
    public Configuration getConfiguration(){
        return configuration;
    }
    
    private String request(int id) {
        
        String url = Uri.decode(URLs.CONFIG_URL+id);
        String result = null;
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(url);
        Log.d(TAG, "Url = "+url);
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
                    return result;
                }
            }
            else{
                
                
                result = null;
            }
            
            
        } catch (Exception e) {
            Log.d(TAG, "Exception");
            result = null;
            e.printStackTrace();

        }
        return result;
    }


}
