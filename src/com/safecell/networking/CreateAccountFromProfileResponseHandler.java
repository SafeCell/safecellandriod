package com.safecell.networking;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.safecell.dataaccess.ProfilesRepository;
import com.safecell.model.SCProfile;

public class CreateAccountFromProfileResponseHandler {

	private String result;
	private Context context;
	private String VersionName;
	public CreateAccountFromProfileResponseHandler(Context ctx, String versionName) {
		this.context = ctx;
		this.VersionName = versionName;
	}

	public void readAccountResponse(String resultResponse) {
		try {
			
			result = resultResponse;
			JSONObject jsonObject = new JSONObject(result);
			//Log.v("Safecell:", jsonObject.toString(4));
			JSONObject profileJsonObject = jsonObject.getJSONObject("profile");

			//Log.v("Safecell :"+"last_name", profileJsonObject.getString("last_name"));
			//Log.v("Safecell :"+"id", "" + profileJsonObject.getInt("id"));
			//Log.v("Safecell :"+"first_name", profileJsonObject.getString("first_name"));
			//Log.v("Safecell :"+"account_id", "" + profileJsonObject.getInt("account_id"));
			//Log.v("Safecell :"+"email", profileJsonObject.getString("email"));

			//Log.v("Safecell :"+"bus_driver", ""+ profileJsonObject.getBoolean("bus_driver"));

			//Log.v("Safecell :"+"license_class_key", ""+ profileJsonObject.getString("license_class_key"));
			//Log.v("Safecell :"+"device_key", profileJsonObject.getString("device_key"));

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

		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
