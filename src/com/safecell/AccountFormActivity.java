 package com.safecell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.safecell.dataaccess.AccountRepository;
import com.safecell.dataaccess.ProfilesRepository;
import com.safecell.model.SCLicense;
import com.safecell.model.SCProfile;
import com.safecell.networking.CreateAccountFromProfile;
import com.safecell.networking.CreateAccountFromProfileResponseHandler;
import com.safecell.networking.GetLicenseKey;
import com.safecell.networking.GetLicenseResponseHandler;
import com.safecell.networking.NetWork_Information;
import com.safecell.networking.SubmitAccountDetails;
import com.safecell.networking.SubmitAccountDetailsReponseHandler;
import com.safecell.utilities.FlurryUtils;
import com.safecell.utilities.UIUtils;

public class AccountFormActivity extends Activity  {
	private Button createAccountButton, licensesButton;
	EditText firstNameEditText, lastNameEditText, emailEditText, phoneEditText;
	String callingActivity="";
	protected boolean dialogDismiss;
	private String apiKey;
	private int account_id;
	Context context;
	String message;
	private ProgressDialog progressDialog,licenseProgressDialog;
	AsyncProgressHandler asyncProgressHandler;
	SubmitAccountDetails sumbitAccountDetails;
	Handler handler;
	private String licensekeyString;
	private ProfilesRepository profilesRepository;
	private ArrayList<SCLicense> scLicenseArrayList = new ArrayList<SCLicense>();
	private LicenseThread licenseThread;
// This is Trial Change
	private int licensesSelectIndex = -1;
	private GetLicenseKey getLicenseKey;
	 boolean sendActivationEmail= false;
    private	boolean cancelLicenseProgressDialog = false;
    private boolean cancelProgressDialog = false;
    private String versionName;
    SharedPreferences preferences;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setWindowAnimations(R.anim.null_animation);
		context = AccountFormActivity.this;
		this.initUI();
		profilesRepository = new ProfilesRepository(context);
		createAccountButton.setOnClickListener(createAccountButtonOnclickListenr);
		
		progressDialog = new ProgressDialog(context);
		licenseProgressDialog = new ProgressDialog(context);
		licenseThread = new LicenseThread();
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		callingActivity = bundle.getString("from");
		
		handler = new Handler(){
			
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				licenseProgressDialog.dismiss();
				if (licenseThread.isAlive()) {
					licenseThread.interrupt();
					licenseThread = new LicenseThread();
				}
				if (licensekeyString == null) {
					message = getLicenseKey.getFailureMessage();
					UIUtils.OkDialog(context, message);
					return;
					}
				
				selectLicenseFromDialog();
			}
		};
			
		PackageManager pm = getPackageManager();
        try {
            //---get the package info---
            PackageInfo pi =  pm.getPackageInfo("com.safecell", 0);
            //Log.v("Version Code", "Code = "+pi.versionCode);
            //Log.v("Version Name", "Name = "+pi.versionName); 
            versionName = pi.versionName;
           
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
       preferences = getSharedPreferences("AccountUniqueID", MODE_WORLD_WRITEABLE);
       preferences.getString("AccountUID","");
       createAccountUniqueID(SCProfile.newUniqueDeviceKey());
       
	}

	private void initUI() {
		setContentView(R.layout.account_form_layout);
		createAccountButton = (Button) findViewById(R.id.accountFormCreateAccountButton);
		firstNameEditText = (EditText) findViewById(R.id.accountFormFirstNameEditText);
		lastNameEditText = (EditText) findViewById(R.id.accountFormLastNameEditText);
		emailEditText = (EditText) findViewById(R.id.accountFormEmailEditText);
		phoneEditText = (EditText) findViewById(R.id.accountFormPhoneEditText);
		licensesButton = (Button) findViewById(R.id.accountFormSelectLicenseButton);
		
		licensesButton.setOnClickListener(licensesButtonClickListener);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		}
	private void createAccountUniqueID(String uniqueID){
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("AccountUID", uniqueID);
		// Log.v("uniqueID", "uniqueID ="+uniqueID);
		editor.commit();
	}
	 OnClickListener licensesButtonClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			 
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
	};
	
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
			
			new AlertDialog.Builder(AccountFormActivity.this)
			.setTitle("Select Licenses")
			.setSingleChoiceItems(items, licensesSelectIndex, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			        licensesButton.setText(items[item]);
			        licensesSelectIndex = item;
			        dialog.cancel();
			    }
			}).show();
		}
	}
	boolean enterAllFieldData(){
		
		if (!firstNameEditText.getText().toString().equalsIgnoreCase("")
				&& !lastNameEditText.getText().toString().equalsIgnoreCase(
						"")
				&& !emailEditText.getText().toString().equalsIgnoreCase("")
				&& !phoneEditText.getText().toString().equalsIgnoreCase("")
				&& !licensesButton.getText().toString().equalsIgnoreCase("")){
			
			if (!validationForEmailAddress(emailEditText.getText().toString())) {
				return false;
			}else return true;
			
		}else  return false;
		
		
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
	private OnClickListener createAccountButtonOnclickListenr = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (enterAllFieldData()) {
				if (NetWork_Information.isNetworkAvailable(context)){
								
					asyncProgressHandler = new AsyncProgressHandler();
					asyncProgressHandler.execute();
				}else {
					NetWork_Information.noNetworkConnectiondialog(context);
				}
			}
	}};

	private HttpResponse accountCreateByAccount() {

		HashMap<Object, Object> profileMap = new HashMap<Object, Object>();
		profileMap.put("last_name", lastNameEditText.getText().toString()
				.trim());
		profileMap.put("first_name", firstNameEditText.getText().toString()
				.trim());
		profileMap.put("email", emailEditText.getText().toString().trim());
		//profileMap.put("bus_driver", false);
		profileMap.put("phone", phoneEditText.getText().toString().trim());
		profileMap.put("license_class_key", scLicenseArrayList.get(licensesSelectIndex).getKey());
		String newDeviceKey = SCProfile.newUniqueDeviceKey();
		//license
		profileMap.put("device_key", newDeviceKey);
		
		//Log.v("Safecell :"+"device_key", newDeviceKey);
		
		HttpResponse httpResponse = null;
		sumbitAccountDetails = new SubmitAccountDetails(
				context, profileMap);
		sumbitAccountDetails.createAccountJson();
		
		
		httpResponse = sumbitAccountDetails.postRequest();
		message = sumbitAccountDetails.getFailureMessage();
		//sumbitAccountDetails.getStatusCode()
		return httpResponse;
		
	}
	public boolean accountResponse()
	{
		HttpResponse accountHttpResponse = accountCreateByAccount();
		dialogDismiss = false;
		if (accountHttpResponse != null) {
			
			
			SubmitAccountDetailsReponseHandler sumbitAccountDetailsReponseHandler = new SubmitAccountDetailsReponseHandler(
					AccountFormActivity.this, versionName);
			
			synchronized (profilesRepository) {
				sumbitAccountDetailsReponseHandler
				.readAccountResponse(accountHttpResponse);

				/*SendActivateSubscriptionEmail sendActivateSubscriptionEmail = new SendActivateSubscriptionEmail(
						AccountFormActivity.this);
				sendActivationEmail = sendActivateSubscriptionEmail
						.postRequest();*/
				
				dialogDismiss = true;
				
				
				Intent mIntent = new Intent(AccountFormActivity.this,
						HomeScreenActivity.class);
				startActivity(mIntent);
				createAccountUniqueID("");
				finish();	
				
			}
		} else {
			 dialogDismiss = true;
				new AlertDialog.Builder(context)
				.setMessage(message)
						.setNeutralButton(
						"OK", new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog, int which) {

								dialog.cancel();

							}
							
						}).show();
			}
		return dialogDismiss;
		
	}

	
	private String accountCreateByProfiles() {
		
			AccountRepository accountRepository = new AccountRepository(AccountFormActivity.this);
			HashMap<Object, Object> ApikeyAndAccountId = new HashMap<Object, Object>();
			ApikeyAndAccountId =accountRepository.selectApiKeyAndAccountID();
		//	ApikeyAndAccountId= accountRepository.selectApiKeyAndAccountID();
			
			account_id = (Integer) ApikeyAndAccountId.get("AccountId");
			apiKey = (String) ApikeyAndAccountId.get("ApiKey");
			
			HashMap<Object, Object> profileMap = new HashMap<Object, Object>();
			profileMap.put("last_name", lastNameEditText.getText());
			profileMap.put("first_name", firstNameEditText.getText());
			profileMap.put("email", emailEditText.getText());
			//profileMap.put("bus_driver", false);
			profileMap.put("phone", phoneEditText.getText());
			profileMap.put("account_id", account_id);
			profileMap.put("id", 0);
			profileMap.put("license_class_key", scLicenseArrayList.get(licensesSelectIndex).getKey());
			
			String newDeviceKey = SCProfile.newUniqueDeviceKey();
			profileMap.put("device_key", newDeviceKey);
			//Log.v("Safecell :"+"device_key", newDeviceKey);

			CreateAccountFromProfile createAccountFromProfile = new CreateAccountFromProfile(context,
					profileMap, apiKey);
			
			createAccountFromProfile.createProfileJson();
                     
			String profileResponse = createAccountFromProfile.postRequest();
			message = createAccountFromProfile.getFailureMessage();
			
			return profileResponse;

			
	}
	
	public boolean accountNewProfileResponse()
	{
		String createAccountFromProfile = null;
		createAccountFromProfile = accountCreateByProfiles();
		dialogDismiss = false;
		
		if (createAccountFromProfile != null) {
			
			CreateAccountFromProfileResponseHandler createAccountFromProfileResponseHandler = new CreateAccountFromProfileResponseHandler(AccountFormActivity.this, versionName);
			createAccountFromProfileResponseHandler.readAccountResponse(createAccountFromProfile);
			dialogDismiss = true;
			
			finish();
			Intent mIntent = new Intent(AccountFormActivity.this,HomeScreenActivity.class);
			startActivity(mIntent);
			
		}
		else {
			
			 dialogDismiss = true;
		     
			new AlertDialog.Builder(context).setMessage(message)
					.setNeutralButton(
					"OK", new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {

							dialog.cancel();

						}
						
					}).show();
		}
		return dialogDismiss;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			
			
			Intent intent = new Intent(AccountFormActivity.this,
					TrialOrAlreadyAccountActivity.class);
			startActivity(intent);
			finish();
		}
		return super.onKeyDown(keyCode, event);
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
	
	private class AsyncProgressHandler extends AsyncTask<Void, Boolean, Boolean> {

		
		
		protected Boolean doInBackground(Void... params) {
			try{
				Looper.prepare();
				if (callingActivity.equals("trial")) {
					
					publishProgress(accountResponse());
				
				} else {
					
					
					publishProgress(accountNewProfileResponse());
					
				}
			}catch(Exception e)
			{
				e.printStackTrace(); 
			}
			Looper.loop();
			
			return dialogDismiss;

		}
		@Override
		protected void onProgressUpdate(Boolean... values) {
			
			if (dialogDismiss)
			{
				onPostExecute(dialogDismiss);
			}
			
			super.onProgressUpdate(values);
		}

		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			
			if (progressDialog.isShowing() && dialogDismiss == true) {

				try {
					progressDialog.dismiss();
				} 
				catch (Exception e) {
					// TODO: handle exception
				}
				}
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			progressDialog.setMessage("Loading Please Wait");
			progressDialog.show();
			progressDialog.setCancelable(cancelProgressDialog);
			

		}
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		
		return super.dispatchKeyEvent(event);
		
	}
	class LicenseThread extends Thread{
		
		@Override
		public void run() {
			super.run();
			enterLicenseKey();
			handler.sendEmptyMessage(0);
		}
		
	}
}

