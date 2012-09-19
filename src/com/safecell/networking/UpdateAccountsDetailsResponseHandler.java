package com.safecell.networking;


import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.safecell.dataaccess.ProfilesRepository;
import com.safecell.model.SCProfile;
import com.safecell.utilities.StreamToStringHelper;

public class UpdateAccountsDetailsResponseHandler {
	
	private HttpResponse response;
	Context context;
	
	public UpdateAccountsDetailsResponseHandler(Context ctx)
	{
		context=ctx;
	}
	
	public void updateAccountResponse(HttpResponse httpResponse)
	{
		try{
		response=httpResponse;
		
		if(response!=null)
		{
			
				InputStream ins=response.getEntity().getContent();
				
				if( ins != null)
				{
					String result = StreamToStringHelper.convertStreamToString(ins);
					JSONObject jsonObject=new JSONObject(result);
					JSONObject profileJsonObject=jsonObject.getJSONObject("profile");
					//Log.v("Safecell :"+"last_name", profileJsonObject.getString("last_name"));
					//Log.v("Safecell :"+"id", "" + profileJsonObject.getInt("id"));
					//Log.v("Safecell :"+"first_name", profileJsonObject.getString("first_name"));
					//Log.v("Safecell :"+"account_id", "" + profileJsonObject.getInt("account_id"));
					//Log.v("Safecell :"+"email", profileJsonObject.getString("email"));
					//Log.v("Safecell :"+"bus_driver", ""+ profileJsonObject.getBoolean("bus_driver"));

					
					SCProfile scProfile=new SCProfile();
					scProfile.setProfileId(profileJsonObject.getInt("id"));
					scProfile.setFirstName(profileJsonObject.getString("first_name"));
					scProfile.setLastName(profileJsonObject.getString("last_name"));
					scProfile.setEmail(profileJsonObject.getString("email"));
					scProfile.setPhone(profileJsonObject.getString("phone"));
					scProfile.setAccountID(profileJsonObject.getInt("account_id"));
					scProfile.setLicenses(profileJsonObject.getString("license_class_key"));
					
					ProfilesRepository profilesRepository=new ProfilesRepository(context);
					profilesRepository.updateProfile(scProfile);
					
				}
			} else {
				//Log.v("Safecell :"+"Account Response", "null");
				}
			
		} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}

}
