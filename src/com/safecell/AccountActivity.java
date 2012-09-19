package com.safecell;

import java.util.ArrayList;

import com.safecell.utilities.FlurryUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.safecell.dataaccess.ProfilesRepository;
import com.safecell.model.SCAccountDetails;
import com.safecell.model.SCProfile;
import com.safecell.networking.GetAccount;
import com.safecell.networking.GetAccountResponseHandler;
import com.safecell.networking.NetWork_Information;
import com.safecell.networking.SendActivateSubscriptionEmail;
import com.safecell.utilities.LocationSP;
import com.safecell.utilities.StateAddress;
import com.safecell.utilities.UIUtils;

public class AccountActivity extends Activity {

	String KEY_PROFILE_ID = "profile_ID";
	private Button homeButton, btnMyTrips, settingsButton, rulesButton;
	LinearLayout linearLayoutTrialMode, linearLayoutActivate;

	Context context;
	Button manageAccountActivateButton;
	TextView profileNameTextView, profileValueTextView;
	ListView accountInformationListView, profileListView;
	private GetAccountResponseHandler getAccountResponseHandler;
	private GetAccount getAccount;
	private TextView tvLocation;
	private String responseAccountStr,message;
	TableLayout tableLayout;
	private TextView vrificationCodeValue;
	ArrayList<SCProfile> profiles = new ArrayList<SCProfile>();
	
	TableRow profileNameTableRow;

	ArrayList<LinearLayout> viewArrayList = new ArrayList<LinearLayout>();
	
	SCAccountDetails scAccountDetails;
	ArrayList<String> info = new ArrayList<String>();
	ArrayList<SCProfile> profilesTableArrayList;

	SCProfile scProfile = new SCProfile();
	ProgressDialog progressDialog;
	ProgressThread mThread;
	Handler handler;
	private LinearLayout tabBarLinearLayout;
	private String callingFromActivity;
	/** Called when the activity is first created. */

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setWindowAnimations(R.anim.null_animation);
		context = AccountActivity.this;
		ProfilesRepository profilesRepository = new ProfilesRepository(context);
		scProfile = profilesRepository.getCurrentProfile();

		scAccountDetails = new SCAccountDetails();
		progressDialog = new ProgressDialog(context);
		mThread = new ProgressThread();

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		callingFromActivity = bundle.getString("Account_Activity_Calling");
		
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				progressDialog.dismiss();
				
				if (responseAccountStr == null) {
					message = getAccount.getFailureMessage();
					UIUtils.OkDialog(context, message);
				}else {
					vrificationCodeValue.setText(scAccountDetails.getValidation_code());
					
						
				
						
					if (getAccount != null) {
							getAccount.getResponse();
							setProfilesDynamically();
						}
				}
				if (mThread.isAlive()) {
					mThread = new ProgressThread();
				}
			}
		};

		/*
		 * info.add("Verification Code"); info.add("Valid Until");
		 */

		this.initUI();
		
		// linearLayoutActivate.setVisibility(View.GONE);
		// activateSubscription.setVisibility(View.GONE);
		 if (NetWork_Information.isNetworkAvailable(context)) {
			 progressDialog.setMessage("Loading Please Wait");
			 progressDialog.show();
			 runOnUiThread(mThread);
		} else {
			NetWork_Information.noNetworkConnectiondialog(context);
			finish();
			
		}
		
			
			
	}

	private synchronized void downloadProfile() {
		ProfilesRepository profilesRepository = new ProfilesRepository(context);
		SCProfile currentProfile = profilesRepository.getCurrentProfile();

		getAccount = new GetAccount(context, currentProfile);
		responseAccountStr = getAccount.getRequest();
		
		if (responseAccountStr != null)  {

			getAccountResponseHandler = new GetAccountResponseHandler();
			
			profilesTableArrayList = getAccountResponseHandler
					.HandleGetAccountResponse(responseAccountStr);
			synchronized (scAccountDetails) {
				scAccountDetails = getAccountResponseHandler
				.getAccountDetailsModel();

	
			linearLayoutTrialMode.setVisibility(View.VISIBLE);

		if (!scAccountDetails.isActivated()) {
			if (scProfile.getProfileId() == scAccountDetails
					.getMaster_profile_id());
				// info.add("Activate Subscription");
				//linearLayoutActivate.setVisibility(View.VISIBLE);
		}
			}
			
		}
	}

	public  void initUI() {

		setContentView(R.layout.settings1_account);

		
		
		profileListView = (ListView) findViewById(R.id.ProfileListView);
		accountInformationListView = (ListView) findViewById(R.id.AccountInformationListView);
		homeButton = (Button) findViewById(R.id.tabBarHomeButton);
		btnMyTrips = (Button) findViewById(R.id.tabBarMyTripsButton);
		rulesButton = (Button) findViewById(R.id.tabBarRulesButton);
		settingsButton = (Button) findViewById(R.id.tabBarSettingsButton);
		settingsButton.setBackgroundResource(R.drawable.settings_clicked);
		tableLayout = (TableLayout) findViewById(R.id.SettingManageAccountTableLayout01);

		tvLocation = (TextView) findViewById(R.id.tabBarCurentLocationTextView);
		tvLocation.setText(LocationSP.LocationSP);

		TabControler tabControler = new TabControler(AccountActivity.this);
		homeButton.setOnClickListener(tabControler.getHomeTabOnClickListner());
		btnMyTrips.setOnClickListener(tabControler.getMyTripsOnClickListner());
		rulesButton.setOnClickListener(tabControler.getRulesOnClickListner());
		settingsButton.setOnClickListener(tabControler
				.getSettingOnClickListener());
		vrificationCodeValue = (TextView) findViewById(R.id.SettingManageAccountVerificationCodeTextViewValue);
		
		manageAccountActivateButton = (Button) findViewById(R.id.SettingManageAccountActivateButton);
		
		
		
		// linearLayoutActivate=(LinearLayout)findViewById(R.id.SettingManageLinearLayoutActivateSubscription);
		//this.downloadProfile();
		tabBarLinearLayout = (LinearLayout)findViewById(R.id.SettingAccountTabBarLinearLayout);

		if (callingFromActivity.equalsIgnoreCase("Master profile trial mode expire")) {
			tabBarLinearLayout.setVisibility(View.GONE);
		} else {
			tabBarLinearLayout.setVisibility(View.VISIBLE);
		}
		manageAccountActivateButton
				.setOnClickListener(activateButtonClickListener);
	}

	public  void setProfilesDynamically() {
		profiles=profilesTableArrayList;
		
		
		View.OnClickListener rowClickListener=new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TableRow tableRow=(TableRow)v;
				int id=tableRow.getId();
				SCProfile profile = profilesTableArrayList.get(id-100);

				String profileBiengUsed = scProfile.getProfileId() + "";

				String[] values = { profile.getMasterProfileId() + "",
						profile.getProfileId() + "", profile.getFirstName(),
						profile.getLastName(), profile.getEmail(),
						profile.getPhone(), profileBiengUsed, profile.getLicenses() };
				Intent mIntent = new Intent(AccountActivity.this,
						DeleteProfileActivity.class);
				mIntent.putExtra("scProfileAccount", values);
				startActivityForResult(mIntent, 1);
                
			}    
		};
		
		for(int i =0 ;i<profiles.size();i++)
		{
			profileNameTableRow=new TableRow(this);
			profileNameTableRow.setLayoutParams(new LayoutParams
					(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		    
			profileNameTableRow.setId(i+100);
			profileNameTableRow.addView(getView(i));
			
			profileNameTableRow.setOnClickListener(rowClickListener);
			
			tableLayout.addView(profileNameTableRow);
		}
	
	}

	View getView(int position) {

		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		TextView firstNameTextView, pointEarnsTextView;
		View customRow = inflater.inflate(R.layout.profile_list_row, null);

		firstNameTextView = (TextView) customRow
				.findViewById(R.id.ProfileNameTextView);

		pointEarnsTextView = (TextView) customRow
				.findViewById(R.id.ProfilePointsTextView);

		firstNameTextView.setText(profiles.get(position).getFirstName()
				.toString()
				+ " " + profiles.get(position).getLastName().toString());

		pointEarnsTextView
				.setText(profiles.get(position).getPhone().toString());
		return  customRow;

	}

	
	OnClickListener activateButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			String message = "Further information will be emailed to '"
					+ scProfile.getEmail()
					+ "'. Is this the correct email address?";
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage(message).setTitle("Activate Subscription")
					.setCancelable(false).setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {

									dialog.cancel();
									Intent mIntent = new Intent(AccountActivity.this,ManageProfile_Activity.class);
									mIntent.putExtra("CallingActivity", "AccountActivity");
									startActivity(mIntent);
									
									
								}
							})
							.setPositiveButton("Send Email",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
									SendActivateSubscriptionEmail sendActivateSubscriptionEmail = new SendActivateSubscriptionEmail(
											AccountActivity.this);
									boolean result = sendActivateSubscriptionEmail
											.postRequest();

									String message = "Email Sending Failed. Reason: "
											+ sendActivateSubscriptionEmail
													.getFailureMessage();

									if (result) {
										message = "An email which contains further instructions to complete the activation has been sent to '"
												+ scProfile.getEmail()
												+ "'. Thank You.";
									}

									new AlertDialog.Builder(
											AccountActivity.this)
											.setTitle("Activation Email Sent")
											.setMessage(message)
											.setNeutralButton(
													"OK",
													new DialogInterface.OnClickListener() {

														public void onClick(
																DialogInterface dialog,
																int which) {

															dialog.cancel();
															;

														}
													}).show();
								}
							});
			AlertDialog alert = builder.create();
			alert.show();
		}
	};

	protected void onResume()

	{
		super.onResume();
		StateAddress.currentActivity = this;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {

				Bundle bundle = data.getExtras();
				String profileid = bundle.getString("Delete_profileId");

				for (SCProfile profilesArrayList : profilesTableArrayList) {
					if (profilesArrayList.getProfileId() == Integer
							.parseInt(profileid)) {
						profilesTableArrayList.remove(profilesArrayList);
					}
				}

				setProfilesDynamically();

			}// ok

			if (resultCode == RESULT_CANCELED) {
				//Log.v("Safecell :"+"resultCode", "RESULT_CANCELED");
			}
		}
	}

	private class ProgressThread extends Thread {
		public ProgressThread() {

		}
		
		public void run() {
			try {

				downloadProfile();
			
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			handler.sendEmptyMessage(0);
		}
			
		}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && !callingFromActivity.equalsIgnoreCase("Master profile trial mode expire")) {

			Intent homeScreen = new Intent(AccountActivity.this,SettingScreenActivity.class);
			homeScreen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(homeScreen);
			finish();
		}
		return false;

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

}
