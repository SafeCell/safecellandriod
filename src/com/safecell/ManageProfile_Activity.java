package com.safecell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.safecell.dataaccess.AccountRepository;
import com.safecell.dataaccess.LicenseRepository;
import com.safecell.dataaccess.ProfilesRepository;
import com.safecell.model.SCAccount;
import com.safecell.model.SCLicense;
import com.safecell.model.SCProfile;
import com.safecell.networking.GetLicenseKey;
import com.safecell.networking.GetLicenseResponseHandler;
import com.safecell.networking.NetWork_Information;
import com.safecell.networking.UpdateAccountDetails;
import com.safecell.networking.UpdateAccountsDetailsResponseHandler;
import com.safecell.utilities.FlurryUtils;
import com.safecell.utilities.LocationSP;
import com.safecell.utilities.StateAddress;
import com.safecell.utilities.UIUtils;

public class ManageProfile_Activity extends ListActivity {

	private Button updateProfileButton;
    private	Button homeButton, btnMyTrips, settingsButton,rulesButton;
	String[] firstTitleLabelArray = { "First Name", "Last Name", "Email",
			"Phone","License" };
	String[] secondTitleLabelArray = { "", "", "", "", "", "", ""};
	String callingActivity ="";
	EditText dialogInputEditText;
	ArrayList<SCProfile> profilesArrayList = new ArrayList<SCProfile>();
	ArrayList<SCAccount> accountArrayList = new ArrayList<SCAccount>();

	Context context;
	ProgressDialog dialog;

	private String apiKey;
	private int account_id, profile_id;
	ProgressDialog progressDialog;
	String message;
	TextView tvLocation;
	
	Handler handler;
	private String licensekeyString;
	private ArrayList<SCLicense> scLicenseArrayList = new ArrayList<SCLicense>();
	private LicenseThread licenseThread;
	private int licensesSelectIndex = -1;
	private GetLicenseKey getLicenseKey;
	private ProgressDialog licenseProgressDialog;
	
	ProfilesRepository profilesRepository;
	private LicenseRepository licenseRepository;
	 private	boolean cancelLicenseProgressDialog = false;
	 private boolean cancelDialog = false;
	 HttpResponse profileResponse;
	 protected boolean dialogDismiss;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setWindowAnimations(R.anim.null_animation);

		Intent callingIntent =getIntent();
		callingActivity =  callingIntent.getStringExtra("CallingActivity");

		context = ManageProfile_Activity.this;
		dialog = new ProgressDialog(context);
		licenseRepository = new LicenseRepository(context);
		intializesecondTitleLabelArray();
		intiUI();

		progressDialog = new ProgressDialog(context);
		licenseProgressDialog = new ProgressDialog(context);
		licenseThread = new LicenseThread();
		
		handleMessageFromThread();
		
		setListAdapter(new manageProfileListAdapter(ManageProfile_Activity.this));
		TabControler tabControler =new TabControler(ManageProfile_Activity.this);
		homeButton.setOnClickListener(tabControler.getHomeTabOnClickListner());
		
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

	void handleMessageFromThread()
	{
		handler = new Handler(){
			
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				licenseProgressDialog.dismiss();
				
				if (licensekeyString == null) {
					message = getLicenseKey.getFailureMessage();
					UIUtils.OkDialog(context, message);
					licenseThread=new LicenseThread();
				}
				selectLicenseFromDialog();
				
				if (licenseThread.isAlive()) {

					licenseThread =new LicenseThread();

				}
			}
		};
	}
	private void intializesecondTitleLabelArray() {

		profilesRepository = new ProfilesRepository(ManageProfile_Activity.this);

		profilesArrayList = profilesRepository
				.intialiseProfilesArrayList(profilesRepository.selectProfiles());
		profilesRepository.selectProfiles().close();
		String licenseName = licenseRepository.selectGetLicenseName(profilesArrayList.get(0).getLicenses());
		//Log.v("Safecell :"+"licenseName", "Name = "+licenseName);
		
		secondTitleLabelArray[0] = profilesArrayList.get(0).getFirstName();
		secondTitleLabelArray[1] = profilesArrayList.get(0).getLastName();
		secondTitleLabelArray[2] = profilesArrayList.get(0).getEmail();
		secondTitleLabelArray[3] = profilesArrayList.get(0).getPhone();
		secondTitleLabelArray[4] = licenseName;
		
		profile_id = profilesArrayList.get(0).getProfileId();
	}

	private void intiUI() {
		// TODO Auto-generated method stub
		setContentView(R.layout.manage_profile);
		updateProfileButton = (Button) findViewById(R.id.manageProfileUpdateProfileButton);
		updateProfileButton.setOnClickListener(updateProfileClickListener);
		
		homeButton = (Button) findViewById(R.id.tabBarHomeButton);
		btnMyTrips = (Button) findViewById(R.id.tabBarMyTripsButton);
		rulesButton = (Button) findViewById(R.id.tabBarRulesButton);
		settingsButton = (Button) findViewById(R.id.tabBarSettingsButton);
		settingsButton.setBackgroundResource(R.drawable.settings_clicked);
		tvLocation=(TextView) findViewById(R.id.tabBarCurentLocationTextView);
		tvLocation.setText(LocationSP.LocationSP);
		
		TabControler tabControler =new TabControler(ManageProfile_Activity.this);
		btnMyTrips.setOnClickListener(tabControler.getMyTripsOnClickListner());
		rulesButton.setOnClickListener(tabControler.getRulesOnClickListner());
		settingsButton.setOnClickListener(tabControler.getSettingOnClickListener());
	}

	OnClickListener updateProfileClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			
			if (NetWork_Information.isNetworkAvailable(context)) {

				HandleResponse handleResponse = new HandleResponse();
				handleResponse.execute(); 
			} else {
				NetWork_Information.noNetworkConnectiondialog(context);
			}
		}
	};

	private HttpResponse accountUpdatetoProfile() {

		AccountRepository accountRepository = new AccountRepository(context);

		accountArrayList = accountRepository
				.intialiseAccountArrayList(accountRepository.SelectAccount());
		accountRepository.SelectAccount().close();

		apiKey = accountArrayList.get(0).getApiKey();
		account_id = accountArrayList.get(0).getAccountId();

		HashMap<Object, Object> profileMap = new HashMap<Object, Object>();

		profileMap.put("first_name", secondTitleLabelArray[0]);
		profileMap.put("last_name", secondTitleLabelArray[1]);
		profileMap.put("email", secondTitleLabelArray[2]);
		//profileMap.put("bus_driver", false);
		profileMap.put("phone", secondTitleLabelArray[3]);
		String key = null;
		
		if (scLicenseArrayList.size()> 0) {
			for (int i = 0; i < scLicenseArrayList.size(); i++) {
				if (scLicenseArrayList.get(i).getName() == secondTitleLabelArray[4]) {
					key = scLicenseArrayList.get(i).getKey();
				}
			}
		}else
		{
			
			key = licenseRepository.selectGetLicenseKey(secondTitleLabelArray[4]);
		}
		//Log.v("Safecell :"+"License Key", "KEY = "+key);
		profileMap.put("license_class_key",key);
		profileMap.put("account_id", account_id);
		profileMap.put("id", profile_id);
		HttpResponse profileResponse = null;

		UpdateAccountDetails updateAccountDetails = new UpdateAccountDetails(
				context, profileMap, apiKey, profile_id);
		updateAccountDetails.updateAccountJson();

		profileResponse = updateAccountDetails.putRequest();
		message = updateAccountDetails.getFailureMessage();

		return profileResponse;

	}

	private boolean updateProfile() {
		
		
		profileResponse = accountUpdatetoProfile();
		dialogDismiss = false;
		if (profileResponse != null) {

			UpdateAccountsDetailsResponseHandler updateAccountsDetailsResponseHandler = new UpdateAccountsDetailsResponseHandler(
					context);
			
			updateAccountsDetailsResponseHandler
					.updateAccountResponse(profileResponse);
			dialogDismiss = true;
		} else {
			dialogDismiss = true;
			new AlertDialog.Builder(context).setMessage(message)
			.setNeutralButton("OK",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog,
								int which) {

							dialog.cancel();
						}
					}).show();
			
		}
		return dialogDismiss;
	}

	
	class manageProfileListAdapter extends BaseAdapter {
		Activity context;

		manageProfileListAdapter(Activity context) {
			this.context = context;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return firstTitleLabelArray.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			LayoutInflater inflater = context.getLayoutInflater();
			View row = inflater.inflate(R.layout.manage_profile_custom_row,
					null);

			TextView firstLabel = (TextView) row
					.findViewById(R.id.ManageProfileCustomRowFirstTitleTextView);
			TextView secondLabel = (TextView) row
					.findViewById(R.id.ManageProfileCustomRowSecondTitleTextView);

			switch (position) {
			case 0:

				firstLabel.setText(firstTitleLabelArray[position]);
				secondLabel.setText(secondTitleLabelArray[position]);
				break;

			case 1:
				firstLabel.setText(firstTitleLabelArray[position]);
				secondLabel.setText(secondTitleLabelArray[position]);
				break;
			case 2:
				firstLabel.setText(firstTitleLabelArray[position]);
				secondLabel.setText(secondTitleLabelArray[position]);
				break;
			case 3:
				firstLabel.setText(firstTitleLabelArray[position]);
				secondLabel.setText(secondTitleLabelArray[position]);
				break;
			case 4:
				firstLabel.setText(firstTitleLabelArray[position]);
				secondLabel.setText(secondTitleLabelArray[position]);
				break;
			
			case 5 :
				firstLabel.setText(firstTitleLabelArray[position]);
				secondLabel.setText(secondTitleLabelArray[position]);
				break;
			}
			return row;
		}

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		if (position<4) {
			displayDialog(firstTitleLabelArray[position],
					secondTitleLabelArray[position], position);
		}
		if (position == 4) {
			if (scLicenseArrayList.size()== 0) {
				if (NetWork_Information.isNetworkAvailable(context)) {
					licenseProgressDialog.setMessage("Loading Licenses...");
					licenseProgressDialog.show();
					licenseProgressDialog.setCancelable(cancelLicenseProgressDialog);
					licenseThread.start();
					licensesSelectIndex = -1;
					} else {
					NetWork_Information.noNetworkConnectiondialog(context);
				}
			}else {
				selectLicenseFromDialog();
			}
		}
	}

	private String getLicensesName(){
		getLicenseKey = new GetLicenseKey(context);
		licensekeyString = getLicenseKey.getRequest();
		return licensekeyString;
	}
	
	private synchronized void enterLicenseKey()
	{
		String key = getLicensesName();
		if (key != null) {
			GetLicenseResponseHandler licenseResponseHandler = new GetLicenseResponseHandler(key);
			scLicenseArrayList= licenseResponseHandler.getLicenseKey();
			SCLicense.insertOrUpdateLicenseKey(scLicenseArrayList, context);
		}
	}
	
	private void selectLicenseFromDialog()
	{
		if (licenseProgressDialog.isShowing() == false && scLicenseArrayList.size()>0) {
			
			
			final CharSequence[] items = new CharSequence[scLicenseArrayList.size()];
			for (int i = 0; i < scLicenseArrayList.size(); i++) {
				items[i]=scLicenseArrayList.get(i).getName();
			}
			
			new AlertDialog.Builder(context)
			.setTitle("Select Licenses")
			.setSingleChoiceItems(items, licensesSelectIndex, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			    	
			    	secondTitleLabelArray[4] = items[item].toString();
			    	licensesSelectIndex = item;
			    	setListAdapter(new manageProfileListAdapter(
					ManageProfile_Activity.this));
			        
			        dialog.cancel();
			    }
			}).show();
		}
	}
	void displayDialog(String title, String inputText, final int position) {

		LayoutInflater li = LayoutInflater.from(this);
		View dialogView = li.inflate(R.layout.dialog_edittext_input, null);
		  
		
		dialogInputEditText = (EditText) dialogView
				.findViewById(R.id.DialogEditTextInputEditText);
		dialogInputEditText.setText(inputText);
		dialogInputEditText.setInputType(setInputTypeKeyBoard(position));

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title).setInverseBackgroundForced(true).setView(
				dialogView).setCancelable(false).setPositiveButton("Ok",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						String text = dialogInputEditText.getText().toString();
						if (!text.equalsIgnoreCase("")) {
							if (position == 2) {
								if (validationForEmailAddress(text)) {
									setDialogValuesListArrayAdapter(position);
									dialog.cancel();
								}else{ dialog.cancel();}
							}else{
								setDialogValuesListArrayAdapter(position);
								dialog.cancel();
							}
						} else {
							Toast.makeText(context, "Blank not allowed.",
									Toast.LENGTH_SHORT).show();
							}

					}
				}).setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show(); 
		alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	}
	private void setDialogValuesListArrayAdapter(int position){
		secondTitleLabelArray[position] = dialogInputEditText
		.getText().toString();

		setListAdapter(new manageProfileListAdapter(
		ManageProfile_Activity.this));
		
	}
	private boolean validationForEmailAddress(String inputText){
		Pattern p = Pattern.compile("\\b[A-Z0-9._%-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b");
		  
		 Matcher m = p.matcher(inputText.toUpperCase());
		 boolean b1 = m.matches();
		 if (!b1) {
			 UIUtils.OkDialog(context, "Invalid Email id");
			 return false;
		}
		return true;
		
	}
	private int setInputTypeKeyBoard(int position) {
		int inputType = 0;
		switch (position) {
		case 0:

			inputType = InputType.TYPE_MASK_CLASS|InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;
			break;

		case 1:

			inputType = InputType.TYPE_MASK_CLASS|InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;
			break;

		case 2:
			inputType = InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
			break;

		case 3:
			inputType = InputType.TYPE_CLASS_PHONE;
			break;
		}

		return inputType;

	}

	private class HandleResponse extends AsyncTask<Void, Boolean, Boolean> {

		protected void onPreExecute() {

			dialog.setMessage("Loading Please Wait");

			dialog.show();
			dialog.setCancelable(cancelDialog);

		}


		protected Boolean doInBackground(Void... params) {
		
			try {
				Looper.prepare();
				
				publishProgress(updateProfile());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Looper.loop();
			
		
			return dialogDismiss;


		}
		
		protected void onProgressUpdate(Boolean... values) {
			if (dialogDismiss)
			{
				onPostExecute(dialogDismiss);
			}
			super.onProgressUpdate(values);
		}

		protected void onPostExecute(Boolean result) {

			if (dialog.isShowing() && dialogDismiss == true) {

				dialog.dismiss();

			}

		}

	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		StateAddress.currentActivity = this;
	}

	class LicenseThread extends Thread{
		
		@Override
		public void run() {
			super.run();
			enterLicenseKey();
			handler.sendEmptyMessage(0);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {

			if(callingActivity.equals("SettingScreenActivity")){
			Intent homeScreen = new Intent(ManageProfile_Activity.this,SettingScreenActivity.class);
			homeScreen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(homeScreen);
			finish();
			}
			else if(callingActivity.equals("AccountActivity"))
			{
				Intent homeScreen = new Intent(ManageProfile_Activity.this,AccountActivity.class);
				homeScreen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				homeScreen.putExtra("Account_Activity_Calling", "From manage profile activity");
				startActivity(homeScreen);
				finish();				
			}
		}
		return false;

	}
}
