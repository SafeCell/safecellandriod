package com.safecell.networking;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.safecell.model.SCAccountDetails;
import com.safecell.model.SCProfile;

public class GetAccountResponseHandler {

	SCAccountDetails scAccountDetails ;
	public GetAccountResponseHandler() {
		scAccountDetails = new SCAccountDetails();
	}
	
	public ArrayList<SCProfile> HandleGetAccountResponse(String result)
	{
		ArrayList<SCProfile> scProfileArrayList = new ArrayList<SCProfile>();
		SCProfile scProfile = new SCProfile();
		try {
			JSONObject jsonObject = new JSONObject(result);
			//Log.v("Safecell :"+"jsonObject", jsonObject.toString(4));
		
			JSONObject accountJsonObject = jsonObject.getJSONObject("account");
			
			String master_profileID = accountJsonObject.getString("master_profile_id");
		/*	
			Log.v("Safecell :"+"account_status", accountJsonObject.getString("account_status"));
			Log.v("Safecell :"+"activated", accountJsonObject.getBoolean("activated")+"");
			Log.v("Safecell :"+"is_trial", accountJsonObject.getBoolean("is_trial")+"");
			Log.v("Safecell :"+"master_profile_id", master_profileID);
			Log.v("Safecell :"+"perks_id", accountJsonObject.getString("perks_id"));
			Log.v("Safecell :"+"point_balance", accountJsonObject.getInt("point_balance")+"");
			Log.v("Safecell :"+"valid_until", accountJsonObject.get("valid_until")+"");
			Log.v("Safecell :"+"validation_code", accountJsonObject.getString("validation_code"));*/
			
			//scAccountDetails.setAcccountId(acccountId)
			SCAccountDetails accountDetails = new SCAccountDetails();
					
			
			accountDetails.setActivated(accountJsonObject.getBoolean("activated"));
			accountDetails.setApikey(accountJsonObject.getString("apikey"));
			accountDetails.setMaster_profile_id(Integer.parseInt(master_profileID));
			accountDetails.setPerks_id(accountJsonObject.getString("perks_id"));
			accountDetails.setPoint_balance(accountJsonObject.getInt("point_balance"));
			accountDetails.setValidation_code(accountJsonObject.getString("validation_code"));		
			accountDetails.setChargify_id(accountJsonObject.getString("chargify_id"));
			accountDetails.setArchived(accountJsonObject.getBoolean("archived"));
			accountDetails.setStatus(accountJsonObject.getString("status"));
			accountDetails.setPerks_id(accountJsonObject.getString("perks_id"));
			
			scAccountDetails = accountDetails;
			
			JSONArray profileJsonArray = accountJsonObject.getJSONArray("profiles");
			
			/*Log.v("Safecell :"+"validation_code", validation_code);
			Log.v("Safecell :"+"is_trial", ""+is_trial);
			Log.v("Safecell :"+"master_profileID", master_profileID);*/
			
			for (int i = 0; i < profileJsonArray.length(); i++) {
				
				SCProfile tempScProfile = new SCProfile();
								
				JSONObject profileJsonObject = profileJsonArray.getJSONObject(i);
				
				
				tempScProfile.setProfileId(profileJsonObject.getInt("id"));
				tempScProfile.setFirstName(profileJsonObject.getString("first_name"));
				tempScProfile.setDeviceKey(profileJsonObject.getString("device_key"));
				tempScProfile.setPhone(profileJsonObject.getString("phone"));
				tempScProfile.setAccountID(profileJsonObject.getInt("account_id"));
				tempScProfile.setEmail(profileJsonObject.getString("email"));
				tempScProfile.setPoints_earned(profileJsonObject.getString("points_earned"));
				tempScProfile.setLastName(profileJsonObject.getString("last_name"));
				tempScProfile.setMasterProfileId(Integer.parseInt(master_profileID));
				tempScProfile.setLicenses(profileJsonObject.getString("license_class_key"));	
				tempScProfile.setDeviceFamily(profileJsonObject.getString("device_family"));
				tempScProfile.setExpiresOn(profileJsonObject.getString("expires_on"));
				tempScProfile.setAppVersion(profileJsonObject.getString("app_version"));
				tempScProfile.setStatus(profileJsonObject.getString("status"));
				
				scProfile = tempScProfile;
				
				scProfileArrayList.add(scProfile);
			}
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
//			Log.v("Safecell : ", "Exception occured: " + e.getMessage());
			e.printStackTrace();
		}
		return scProfileArrayList;
		
	}
	
	public  SCAccountDetails getAccountDetailsModel ()
	{
		
		return scAccountDetails;
		
	}
}
