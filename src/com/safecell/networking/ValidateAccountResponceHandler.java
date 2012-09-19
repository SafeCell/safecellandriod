package com.safecell.networking;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.safecell.dataaccess.AccountRepository;
import com.safecell.model.SCAccount;

public class ValidateAccountResponceHandler {
	
	Context context;

	public ValidateAccountResponceHandler(Context ctx) {
		this.context = ctx;
	}

	public void HandleResponce(String resultResponse) {

		try {
				JSONObject jsonObject = new JSONObject(resultResponse);

				JSONObject accountJsonObject = jsonObject.getJSONObject("account");

				//Log.v("Safecell :"+"validation_code:", accountJsonObject
//					.getString("validation_code"));
				//Log.v("Safecell :"+"master_profile_id:", ""
//					+ accountJsonObject.getInt("master_profile_id"));
				//Log.v("Safecell :"+"apikey", accountJsonObject.getString("apikey"));

				SCAccount scAccount = new SCAccount();
				scAccount.setAccountCode(accountJsonObject
					.getString("validation_code"));
				scAccount.setMasterProfileId(accountJsonObject
					.getInt("master_profile_id"));
				scAccount.setApiKey(accountJsonObject.getString("apikey"));
				scAccount.setAccountId(accountJsonObject.getInt("id"));

				AccountRepository accountRepository = new AccountRepository(context);
				accountRepository.insertAccount(scAccount);

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}
