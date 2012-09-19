package com.safecell;

import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.safecell.dataaccess.AccountRepository;
import com.safecell.dataaccess.ProfilesRepository;
import com.safecell.model.SCAccount;
import com.safecell.model.SCProfile;
import com.safecell.networking.NetWork_Information;
import com.safecell.networking.RetriveProfiles;
import com.safecell.networking.RetriveTripsOfProfile;
import com.safecell.networking.UpdateAccountDetails;
import com.safecell.networking.UpdateAccountsDetailsResponseHandler;
import com.safecell.utilities.FlurryUtils;
import com.safecell.utilities.UIUtils;

public class LoginActivity extends Activity {
	EditText etUserName;
	EditText etPassword;
	Button btnRetrivaProfiles;
	String uName;
	String callingActivity="";
	String pwd;
	String loginResponce="";
	String[] profileName={};
	int profileIDArray[] ={};
	JSONArray profilesJA;
	JSONObject selectedProfile;
	JSONObject profileJO;
	JSONObject accountJO;
	ProgressThread mThread;
	ProgressThread1 mThread1;
	Handler handler ;
	Handler handler1 ;
	Context context;
	ProgressDialog progressDialog;
	private SCAccount scAccount;
	private SCProfile scProfile;
	private String message;
	private boolean cancelExisitingProfileProgressDialog=false;
	 private boolean cancelSelectProfile=false;
	 private String versionName;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.login_layout);	
		
		getWindow().setWindowAnimations(R.anim.null_animation);
		InitUi();
		context=LoginActivity.this;
		progressDialog = new ProgressDialog(context);
		mThread = new ProgressThread();
		mThread1 = new ProgressThread1();
		PackageManager pm = getPackageManager();
        try {
            //---get the package info---
            PackageInfo pi =  pm.getPackageInfo("com.safecell", 0);
           // Log.v("Version Code", "Code = "+pi.versionCode);
            //Log.v("Version Name", "Name = "+pi.versionName); 
            versionName = pi.versionName;
           
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	
      handler = new Handler(){
			
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				progressDialog.dismiss();
				if(mThread.isAlive())
				{
					 mThread = new ProgressThread();
				}
			}
		};
		   handler1 = new Handler(){
				
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					progressDialog.dismiss();
					if(mThread1.isAlive())
					{
						mThread1 = new ProgressThread1();
					}
				}
			};
	}
	
	void InitUi(){
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		etUserName = (EditText)findViewById(R.id.LoginUserNameEditText);
		etPassword =(EditText)findViewById(R.id.LoginPasswordEditText);
		btnRetrivaProfiles = (Button) findViewById(R.id.LoginRetriveProfileButton);
		btnRetrivaProfiles.setOnClickListener(retriveProfileOnclickListner);
		
		
		
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
	
	OnClickListener retriveProfileOnclickListner = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			/* uName = etUserName.getText().toString().trim(); 
			 pwd = etPassword.getText().toString().trim();*/
			
			
				if (NetWork_Information.isNetworkAvailable(LoginActivity.this)) 
				{
					 progressDialog.setMessage("Loading Please Wait");
					   
					 progressDialog.show();
					 
					 progressDialog.setCancelable(cancelExisitingProfileProgressDialog);
					
					 mThread.start();
				}else{
					 
					NetWork_Information.noNetworkConnectiondialog(LoginActivity.this);
					
				}
		}
	};
			
	private synchronized void varification() {
	           uName = etUserName.getText().toString().trim(); 
	           pwd = etPassword.getText().toString().trim();
				
				 try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("username", uName);
				jsonObject.put("password", pwd);
				
				JSONObject userSesionJsonObject = new JSONObject();
				userSesionJsonObject.put("user_session", jsonObject);		
				
				RetriveProfiles retriveProfiles =new RetriveProfiles(context,userSesionJsonObject);
				loginResponce = retriveProfiles.Retrive();
				
				int statusCode = retriveProfiles.getStatusCode();
				if (statusCode == 200) {
					showDialog();
				}else{
					String errorMessage = retriveProfiles.getFailureMessage();
					UIUtils.OkDialog(context, errorMessage);
				}
				
			
		} catch (Exception e) {
		e.printStackTrace();
		}
		}
		
	
	
	void showDialog(){
		try {		
			JSONObject loginResponceJsonObject = new JSONObject(loginResponce);
			
			//Log.v("Safecell:", "JSONObject " +loginResponceJsonObject.toString(4));
		accountJO = loginResponceJsonObject.getJSONObject("account");
		profilesJA = accountJO.getJSONArray("profiles");
		
		profileName = new String[profilesJA.length()];
		profileIDArray = new int[profilesJA.length()];
		
		for(int i=0;i<profilesJA.length();i++)
		{
			profileJO = profilesJA.getJSONObject(i);
			profileName[i] =profileJO.getString("first_name")+" "+profileJO.getString("last_name");			
			profileIDArray[i] =profileJO.getInt("id");		
		}
		
		selectedProfile= new JSONObject();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select Profile");
		builder.setItems(profileName, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	if (NetWork_Information.isNetworkAvailable(LoginActivity.this)) 
				{
					 progressDialog.setMessage("Loading Please Wait");
					   
					 progressDialog.show();
					
					 progressDialog.setCancelable(cancelSelectProfile);
					 try {
						 selectedProfile = profilesJA.getJSONObject(item);
						// Log.v("Safecell :"+"Profile", selectedProfile.toString(4));
					 
					 } catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					 mThread1.start();				
					
				
				}else{
					 
					NetWork_Information.noNetworkConnectiondialog(LoginActivity.this);
					
				}
		    	
		    } 
		});
		AlertDialog alert = builder.create();
		alert.show();
		
	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
		public synchronized void  SaveProfile()
		{
			try {
				
			
			String validationCode = accountJO.getString("validation_code");
			int masterProfileId = accountJO.getInt("master_profile_id");
			//accountJO.getString("valid_until");
			String apiKey = accountJO.getString("apikey");
			int MasterAccountId = accountJO.getInt("id");
			
			int accountId =selectedProfile.getInt("account_id");
			int profileId = selectedProfile.getInt("id");
			String phone = selectedProfile.getString("phone");
			String lastName = selectedProfile.getString("last_name");
			String firstName = selectedProfile.getString("first_name");
			String email = selectedProfile.getString("email");
			String licenses = selectedProfile.getString("license_class_key");
			String deviceKey = selectedProfile.getString("device_key");
			
			
		scAccount = new SCAccount();
		scAccount.setAccountCode(validationCode);
		scAccount.setAccountId(MasterAccountId);
		scAccount.setMasterProfileId(masterProfileId);
		scAccount.setApiKey(apiKey);
		scAccount.setChargity_id(accountJO.getString("chargify_id"));
		scAccount.setActivated(accountJO.getBoolean("activated"));
		scAccount.setArchived(accountJO.getBoolean("archived"));
		scAccount.setStatus(accountJO.getString("status"));
		scAccount.setPerksId(accountJO.getString("perks_id"));
		
		AccountRepository accountRepository= new AccountRepository(LoginActivity.this);
		accountRepository.insertAccount(scAccount);
		
		scProfile =new SCProfile();
		scProfile.setAccountID(accountId);
		scProfile.setProfileId(profileId);
		scProfile.setEmail(email);
		scProfile.setFirstName(firstName);
		scProfile.setLastName(lastName);
		scProfile.setPhone(phone);
		scProfile.setLicenses(licenses);
		scProfile.setDeviceKey(deviceKey);
		scProfile.setDeviceFamily("android");
		scProfile.setExpiresOn(selectedProfile.getString("expires_on"));
		scProfile.setAppVersion(versionName);
		
		scProfile.setDeviceKey(SCProfile.newUniqueDeviceKey());
		
		updateProfile();
		
		
		
		
		
		
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		private HttpResponse accountUpdateProfileResponse(){
			
			HashMap<Object, Object> profileMap = new HashMap<Object, Object>();
			profileMap.put("first_name",scProfile.getFirstName());
			profileMap.put("last_name", scProfile.getLastName());
			profileMap.put("email", scProfile.getEmail());
			profileMap.put("phone",scProfile.getPhone());
			profileMap.put("license_class_key",scProfile.getLicenses());
			profileMap.put("account_id", scProfile.getAccountID());
			profileMap.put("id", scProfile.getProfileId());
			profileMap.put("device_key", scProfile.getDeviceKey());
			
			HttpResponse profileResponse = null;
			UpdateAccountDetails updateAccountDetails = new UpdateAccountDetails(
					context, profileMap, scAccount.getApiKey(), scProfile.getProfileId());
			updateAccountDetails.updateAccountJson();

			profileResponse = updateAccountDetails.putRequest();
			message = updateAccountDetails.getFailureMessage();
			return profileResponse;
		}
		
		private synchronized void updateProfile(){
			
			HttpResponse profileResponse;
			profileResponse = accountUpdateProfileResponse();
			if (profileResponse != null) {

				UpdateAccountsDetailsResponseHandler updateAccountsDetailsResponseHandler = new UpdateAccountsDetailsResponseHandler(
						context);

				updateAccountsDetailsResponseHandler
						.updateAccountResponse(profileResponse);
				
				RetriveTripsOfProfile retriveTripsOfProfile = new RetriveTripsOfProfile(LoginActivity.this, scProfile.getProfileId(),scAccount.getApiKey());	
				retriveTripsOfProfile.retrive();
				
				ProfilesRepository profilesRepository = new ProfilesRepository(LoginActivity.this);
				profilesRepository.insertProfile(scProfile);
				
				progressDialog.dismiss();
				
				Intent mIntent = new Intent(LoginActivity.this,HomeScreenActivity.class);
				startActivity(mIntent);
				finish();
	
			} else {
				
				new AlertDialog.Builder(context).setMessage(message)
				.setNeutralButton("OK",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {

								dialog.cancel();
							}
						}).show();
			}
		}
		
	private class ProgressThread extends Thread {
			ProgressThread() {
			}

			public void run() {		
				try {
					Looper.prepare();
					
				
						varification();
					
					
				       
				} catch (Exception e) {
					// TODO: handle exception
				}
				 
		                handler.sendEmptyMessage(0);
		              Looper.loop();	              
		            }
		}	
	
	
	
	private class ProgressThread1 extends Thread {
		ProgressThread1() {
		}

		public void run() {		
			try {
				Looper.prepare();
							
				SaveProfile();
				
			       
			} catch (Exception e) {
				// TODO: handle exception
			}
			 
	                handler1.sendEmptyMessage(0);
	              Looper.loop();	              
	            }
	}	
	
	
}	
	 
	 

