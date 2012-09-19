package com.safecell.networking;

import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.safecell.dataaccess.AccountRepository;
import com.safecell.dataaccess.ProfilesRepository;
import com.safecell.model.SCAccount;
import com.safecell.model.SCProfile;
import com.safecell.utilities.StreamToStringHelper;

public class SubmitAccountDetailsReponseHandler 
{
	private HttpResponse response;
	private Context context;
	private String VersionName;
	
	public SubmitAccountDetailsReponseHandler(Context ctx, String versionName)
	{
		this.context = ctx;
		this.VersionName = versionName;
	}
	
	public String readAccountResponse(HttpResponse httpResponse)
	{
		String statusString="";
		
		try {
			response = httpResponse;
			if (response != null) {
				InputStream in = response.getEntity().getContent();
				if (in != null) {
					
					String result = StreamToStringHelper.convertStreamToString(in);
					
					JSONObject jsonObject = new JSONObject(result);
//					Log.v("SafeCell", jsonObject.toString(4));
					JSONObject accountJsonObject = jsonObject.getJSONObject("account");
					
					JSONArray profileJsonArray = accountJsonObject.getJSONArray("profiles");
					JSONObject profileJsonObject = profileJsonArray.getJSONObject(0);
					
					//Log.v("Safecell :"+"Account", jsonObject.toString(4));
					
					//Log.v("Safecell :"+"validation_code:", accountJsonObject.getString("validation_code"));
					//Log.v("Safecell :"+"master_profile_id:",""+ accountJsonObject.getInt("master_profile_id"));
					//Log.v("Safecell :"+"apikey", accountJsonObject.getString("apikey"));
					
					SCAccount scAccount = new SCAccount();
					scAccount.setAccountCode(accountJsonObject.getString("validation_code"));
					scAccount.setMasterProfileId(accountJsonObject.getInt("master_profile_id"));
					scAccount.setApiKey(accountJsonObject.getString("apikey"));
					scAccount.setAccountId(accountJsonObject.getInt("id"));
					scAccount.setChargity_id(accountJsonObject.getString("chargify_id"));
					scAccount.setActivated(accountJsonObject.getBoolean("activated"));
					scAccount.setArchived(accountJsonObject.getBoolean("archived"));
					scAccount.setStatus(accountJsonObject.getString("status"));
					scAccount.setPerksId(accountJsonObject.getString("perks_id"));
					
					
					AccountRepository accountRepository = new AccountRepository(context);
					accountRepository.insertAccount(scAccount);
					
					//Log.v("Safecell :"+"Profile", profileJsonArray.getString(0));
					/*Log.v("Safecell :"+"last_name", profileJsonObject.getString("last_name"));
					Log.v("Safecell :"+"id",""+ profileJsonObject.getInt("id"));
					Log.v("Safecell :"+"first_name", profileJsonObject.getString("first_name"));
					Log.v("Safecell :"+"account_id",""+ profileJsonObject.getInt("account_id"));
					Log.v("Safecell :"+"email", profileJsonObject.getString("email"));*/

					//Log.v("Safecell :"+"bus_driver", ""+profileJsonObject.getBoolean("bus_driver"));
					//Log.v("Safecell :"+"device_key", ""+profileJsonObject.getString("device_key"));
					
					SCProfile scProfile = new SCProfile();
					scProfile.setProfileId(profileJsonObject.getInt("id"));
					scProfile.setFirstName(profileJsonObject.getString("first_name"));
					scProfile.setLastName(profileJsonObject.getString("last_name"));
					scProfile.setEmail(profileJsonObject.getString("email"));
					scProfile.setPhone(profileJsonObject.getString("phone"));
					scProfile.setAccountID(profileJsonObject.getInt("account_id"));
					scProfile.setDeviceKey(profileJsonObject.getString("device_key"));
					scProfile.setLicenses(profileJsonObject.getString("license_class_key"));
					scProfile.setDeviceFamily("android");
					scProfile.setStatus(profileJsonObject.getString("status"));
					scProfile.setAppVersion(VersionName);
					scProfile.setExpiresOn(profileJsonObject.getString("expires_on"));
					
					ProfilesRepository profilesRepository = new ProfilesRepository(context);
					
					profilesRepository.insertProfile(scProfile);
					
				}
			} else {
				//Log.v("Safecell :"+"Account Response", "null");
				statusString="Invalid Input";
				return statusString;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return statusString;
		
	}
	
}
