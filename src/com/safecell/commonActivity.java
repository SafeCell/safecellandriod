package com.safecell;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.safecell.dataaccess.ProfilesRepository;
import com.safecell.model.SCProfile;
import com.safecell.model.SCSchool;
import com.safecell.networking.DeleteProfile;
import com.safecell.networking.GetAccount;
import com.safecell.networking.GetAccountResponseHandler;
import com.safecell.networking.GetSchools;
import com.safecell.networking.GetSchoolsResponseHandler;
import com.safecell.networking.SendActivateSubscriptionEmail;
import com.safecell.utilities.FlurryUtils;

public class commonActivity extends Activity{

	private Button deleteProfileButton;
	private Button sendActiavtionEmailButton;
	private Button downloadSchoolsButton;
	private Button getAccountButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		initUI();
	}

	private void initUI() {
		// TODO Auto-generated method stub
		setContentView(R.layout.common_screen);
		deleteProfileButton = (Button)findViewById(R.id.DeleteProfileButton);
		deleteProfileButton.setOnClickListener(deleteButtonClickListener);
		
		sendActiavtionEmailButton = (Button)findViewById(R.id.SendActivationEmailButton);
		sendActiavtionEmailButton.setOnClickListener(sendActiavtionEmailButtonClickListener);
		
		downloadSchoolsButton = (Button)findViewById(R.id.DownloadSchools);
		downloadSchoolsButton.setOnClickListener(downloadSchoolsButtonClickListener);
		
		getAccountButton = (Button)findViewById(R.id.GetAccount);
		getAccountButton.setOnClickListener(getAccountButtonClicked);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		FlurryUtils.startFlurrySession(this);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		FlurryUtils.endFlurrySession(this);
	}
	
	private OnClickListener deleteButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			
			// Random profile id used at the time of testing this method.
			// This should be replaced with appropriate value.
			int profileId = 439; 
			DeleteProfile deleteProfile = new DeleteProfile(commonActivity.this, profileId); 
			boolean deleteProfileResult = deleteProfile.deleteRequest();
			
			String message = "Delete Profile (" + profileId + ") Failed. Reason: " + deleteProfile.getFailureMessage();
			
			if(deleteProfileResult) {
				message =  "Profile (" + profileId + ") was deleted successfully";
			}
			
			new AlertDialog.Builder(commonActivity.this)
		      .setMessage(message)
		      .setNeutralButton("OK", new DialogInterface.OnClickListener() {

		          public void onClick(DialogInterface dialog, int which) {

		        	  dialog.cancel();;

		        }})
		      .show();
			
		}
	};
	
	
	private OnClickListener sendActiavtionEmailButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			
			SendActivateSubscriptionEmail sendActivateSubscriptionEmail = new SendActivateSubscriptionEmail(commonActivity.this); 
			boolean result = sendActivateSubscriptionEmail.postRequest();
			
			String message = "Email Sending Failed. Reason: " + sendActivateSubscriptionEmail.getFailureMessage();
			
			if(result) {
				message =  "The activation email was sent successfully.";
			}
			
			new AlertDialog.Builder(commonActivity.this)
		      .setMessage(message)
		      .setNeutralButton("OK", new DialogInterface.OnClickListener() {

		          public void onClick(DialogInterface dialog, int which) {

		        	  dialog.cancel();;

		        }})
		      .show();
			
		}
	};
	
	
	private OnClickListener downloadSchoolsButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			
			float latitude = 29.986666f;
			float longitude = -95.350418f;
			float radius = 5;
			
			GetSchools downloadSchools = new GetSchools(commonActivity.this, latitude, longitude, radius); 
			String result = downloadSchools.getRequest();
			
			String message = downloadSchools.getFailureMessage();
			
			if(result != null) {
				
				GetSchoolsResponseHandler getSchoolsResponseHandler = new GetSchoolsResponseHandler(result);
				
				ArrayList<SCSchool> schools = getSchoolsResponseHandler.handleGetSchoolsResponse();
				
				for(SCSchool school : schools) {
					//Log.v("Safecell :"+"id: ",  "" + school.getId());
					//Log.v("Safecell :"+"name: ", "" + school.getName());
				}
				
				message =  "The Schools were downloaded successfully.";
			}
			
			new AlertDialog.Builder(commonActivity.this)
		      .setMessage(message)
		      .setNeutralButton("OK", new DialogInterface.OnClickListener() {

		          public void onClick(DialogInterface dialog, int which) {

		        	  dialog.cancel();;

		        }})
		      .show();
			
		}
	};
	
	private OnClickListener getAccountButtonClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			
			ProfilesRepository profilesRepository = new ProfilesRepository(commonActivity.this);
			SCProfile currentProfile = profilesRepository.getCurrentProfile();
			
			GetAccount getAccount = new GetAccount(commonActivity.this, currentProfile);
			String response = getAccount.getRequest();
			String message = "Downloaded Account";
			
			if(response == null) {
				message = getAccount.getFailureMessage();
				
			}
			else {
				GetAccountResponseHandler getAccountResponseHandler = new GetAccountResponseHandler();
				getAccountResponseHandler.HandleGetAccountResponse(response);
				
			}
			
			
			new AlertDialog.Builder(commonActivity.this)
		      .setMessage(message)
		      .setNeutralButton("OK", new DialogInterface.OnClickListener() {

		          public void onClick(DialogInterface dialog, int which) {

		        	  dialog.cancel();;

		        }})
		      .show();
		}
	};
}
