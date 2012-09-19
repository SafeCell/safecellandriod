package com.safecell;

import com.safecell.dataaccess.AccountRepository;
import com.safecell.dataaccess.DBAdapter;
import com.safecell.dataaccess.ProfilesRepository;
import com.safecell.model.SCAccount;
import com.safecell.model.SCAccountDetails;
import com.safecell.model.SCProfile;
import com.safecell.networking.CheckAccountStatus;
import com.safecell.networking.GetAccountResponseHandler;
import com.safecell.networking.NetWork_Information;
import com.safecell.networking.RetriveTripsOfProfile;
import com.safecell.networking.UpdateAccountDetails;
import com.safecell.utilities.DateUtils;
import com.safecell.utilities.FlurryUtils;
import com.safecell.utilities.UIUtils;

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
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class SplashScreenActivity extends Activity implements Runnable,OnClickListener {

	private boolean exitProfile = false;
	Context context;
	private CheckAccountStatus accountStatus;
	private GetAccountResponseHandler accountResponseHandler;
	private SCAccountDetails accountDetails;
	private int statusResponseCode;
	private String message="";
	private int profileId;
	private String lastDayStr = "Last day of your trial.";
	private String remainingDayStr =" more days remaining.";
	
	public static final String PREFS_NAME_MASTER = "MyPrefsFileForMasterProfiles";
	private static final String KEY_FOR_MASTER_PROFILE = "key_for_master_profile";
	public static final String PREFS_NAME_MOVE_PROFILES = "MyPrefsFileForMoveProfiles";
	private static final String KEY_FOR_MOVE_PROFILE = "key_for_move_profile";

	private SCProfile scProfile = new SCProfile();
	private CharSequence [] profileNameList = new CharSequence [1];
	static boolean dialogForSubProfileShown,dialogForMasterProfileShown;
	private AlertDialog profileInUseAlertDialog;
	private ProgressDialog progressDialog;
	private String displayProfileName;
	private ProfilesRepository profilesRepository;
	private AccountRepository accountRepository;
	private SCAccount scAccount;
	ProgressDialog progressDialogDownloadAcc;
	private ArrayList<SCProfile> scProfilesArrayList = new ArrayList<SCProfile>();
	private String versionName = null;
	private AlertDialog profileFailureDialog;
	private LinearLayout linearLayout;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
//	    clearApplicationData();
		super.onCreate(savedInstanceState);
		getWindow().setWindowAnimations(R.anim.null_animation);
		setContentView(R.layout.splash_screen_layout);
		linearLayout = (LinearLayout)findViewById(R.id.splashProgessDialogLinearLayout);
		
		
		context = SplashScreenActivity.this;
		//DBAdapter.context = getApplicationContext();
		accountDetails = new SCAccountDetails();
		progressDialogDownloadAcc = new ProgressDialog(context);
		progressDialog = new ProgressDialog(context);
		progressDialog.setMessage("Loading wait...");
		accountRepository = new AccountRepository(context);
		scAccount = accountRepository.getAccountInformation();
		//Log.v("model", "model = "+Build.MODEL);
	
		
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
		
		profilesRepository = new ProfilesRepository(SplashScreenActivity.this);
		scProfile = profilesRepository.getCurrentProfile();
		
		if (scProfile != null) {
			
				exitProfile = true;
				profileId = scProfile.getProfileId();
				displayProfileName = scProfile.getFirstName()+ "  " + scProfile.getLastName();
				if (versionName.equalsIgnoreCase("")) {
					profilesRepository.updateAppVersionProfile(versionName);
				}
		}
		/*boolean isTripStarted =getSharedPreferences("TRIP", MODE_WORLD_READABLE).getBoolean("isTripStarted", false);
		
		if (isTripStarted) {			
				Intent mIntent = new Intent(SplashScreenActivity.this, TrackingScreenActivity.class);
				mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(mIntent);
				finish();
		}*/
		
		
		if (NetWork_Information.isNetworkAvailable(context) && exitProfile) {
			
				downloadAccount account = new downloadAccount();
				account.execute();
			
			
		}else if (!exitProfile) {
			
		    Intent intent = new Intent(this,LoginActivity.class);
	        startActivity(intent);
			SplashScreenActivity.this.finish();
		} 
		else {
			linearLayout.setVisibility(View.INVISIBLE);
			NetWork_Information.noNetworkConnectiondialog(context);
			
		}
		//getAcccountInformation();
		//getAccountStatus();
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		FlurryUtils.startFlurrySession(this);
	}
	
    
    public void clearApplicationData() {
        File cache = getCacheDir();
        File appDir = new File(cache.getParent());
        Log.d("TAG", "App Directory Exists" + appDir.getAbsolutePath());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                    Log.i("TAG", "**************** File /data/data/APP_PACKAGE/" + s
                            + " DELETED *******************");
                }
            }
        }
        System.gc();
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }

    @Override
	protected void onStop() {
		super.onStop();
		FlurryUtils.endFlurrySession(this);
	}
	
	private synchronized String getAcccountInformation(){
		accountStatus = new CheckAccountStatus(context);
		String result = accountStatus.checkStatus();
		statusResponseCode = accountStatus.getStatusCode();
		
		message = accountStatus.getFailureMessage();
//		Log.v("safecell:", "CheckAccountStatus : returned"); 
		return result;
	}
	private void getAccountStatus(){
		
			String result = getAcccountInformation();
			
			if (null != result) {
				accountResponseHandler = new GetAccountResponseHandler();
				scProfilesArrayList = accountResponseHandler.HandleGetAccountResponse(result);
				
				accountDetails = accountResponseHandler.getAccountDetailsModel();
				
				/*if (accountDetails.getStatus().equalsIgnoreCase("open")) {
					
					for (SCProfile scProfile : scProfilesArrayList) {
						
						if (scProfile.getProfileId() != profileId) {
							Log.v("Safecell :"+"Profile ID ", " = "+scProfile.getProfileId());
							continue;
						}
						if (scProfile.getStatus().equalsIgnoreCase("open")) {
							Intent mIntent = new Intent(SplashScreenActivity.this,HomeScreenActivity.class);
							startActivity(mIntent);
							SplashScreenActivity.this.finish();
						}
					}
					
				}// OK end 
				
				*/			
				Intent mIntent = new Intent(SplashScreenActivity.this,HomeScreenActivity.class);
				startActivity(mIntent);
				SplashScreenActivity.this.finish();
				
			}
			/*else{
				checkStatusResponse();
			}*/
			
	}
	
	private void checkStatusResponse() {
		if (statusResponseCode==400) {
			linearLayout.setVisibility(View.INVISIBLE);
			profileInUseDialog();
			return;
		}
		if (statusResponseCode==404) {
			linearLayout.setVisibility(View.INVISIBLE);
			profileNotFoundDialog();
			return;
		}
		if (statusResponseCode!=200) {
			linearLayout.setVisibility(View.INVISIBLE);
			 UIUtils.OkDialog(context, message);
			 
			return;
		}
	}

	private void profileNotFoundDialog() {
		new AlertDialog.Builder(context)
	      .setMessage("Your profile was not found. It might have been deleted by master account owner.")// 
	      .setTitle("Profile Not Found")
	      .setPositiveButton("Quit", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
				finish();
			}
		})
		.setNegativeButton("Start Over", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
				profileStartoverDialog();
				
			}
		}).create().show();
	
	}
	
	private void profileStartoverDialog(){
		
		new AlertDialog.Builder(context)
	      .setMessage("This will delete all data in the safecell application. Do you want to proceed?")
	      .setTitle("Profile Not Found")
	      .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				deleteDatabase(DBAdapter.DATABASE_NAME);
				dialog.cancel();
				DBAdapter dbAdapter = new DBAdapter(context);
				dbAdapter.open();
				dbAdapter.closeDatabase();
				gamePlaySettingChange();
				Intent intent = new Intent(SplashScreenActivity.this, TrialOrAlreadyAccountActivity.class);
				startActivity(intent);
				finish();
			}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				finish();
			}
		}).create().show();
	}
	
	private void profileInUseDialog() {
			
			LayoutInflater li = LayoutInflater.from(this);
		    View view = li.inflate(R.layout.profile_in_use_layout, null);
		    
		    Button quitButton = (Button)view.findViewById(R.id.profileInUseQuitButton);
		    Button deleteProfileButton = (Button)view.findViewById(R.id.profileDeleteProfileButton);
		    Button moveProfile = (Button)view.findViewById(R.id.profileMoveProfileButton);

		   boolean buttonForMoveProfilesShown=isButtonForMoveProfilesShown();
		   if(buttonForMoveProfilesShown)
		    {
		    	 moveProfile.setVisibility(View.GONE);
		    }
		    
		   AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    builder.setView(view).setTitle("Profile in Use").setMessage("The Profile is already in use on another device.");
		   
		    profileInUseAlertDialog = builder.create();
		    profileInUseAlertDialog.show();
		    
		    quitButton.setOnClickListener(this);
		    deleteProfileButton.setOnClickListener(this);
		    moveProfile.setOnClickListener(this);
	}
	
	private boolean isButtonForMoveProfilesShown() {
		SharedPreferences showTimeOfMoveProfilesButton = getSharedPreferences(PREFS_NAME_MOVE_PROFILES, MODE_WORLD_WRITEABLE);
	       long previousTimeInMilliSec = showTimeOfMoveProfilesButton.getLong(KEY_FOR_MOVE_PROFILE, 0);
	       long currentTime=System.currentTimeMillis();
	       boolean flag=((currentTime-previousTimeInMilliSec)<24*60*60*1000)?true:false;//24*60*60*1000
	       //Log.v("Safecell :"+"3", "3 "+flag);

		return flag;
	}

	private long remainingDayForTrialMode(){
		
		//String valid_untilDate= accountDetails.getValid_until();
		long valid_untilMillsec = DateUtils.dateInMillSecond("");
		
		Date date = new Date();
		long currentDateMills = date.getTime();
		long diffDate = valid_untilMillsec - currentDateMills;

		long remainingDay = diffDate / (24 * 60 * 60 * 1000);
		return remainingDay;
	}
	
	private void trailAccountDialogOnlyMasterProfile(){
		dialogForMasterProfileShown = isDialogForProfilesShown();
		String dialogMessage = remainingDayForTrialMode()== 0 ? lastDayStr : (remainingDayForTrialMode()+1) + remainingDayStr;
		if(!dialogForMasterProfileShown)
		{
			
			new AlertDialog.Builder(context)
	      .setMessage(dialogMessage)
	      .setTitle("Trial Acccount")
	      .setNeutralButton("OK", new DialogInterface.OnClickListener() {

	          public void onClick(DialogInterface dialog, int which) {
	        	  
	        	  SharedPreferences showTimeOfMasterProfile = getSharedPreferences(PREFS_NAME_MASTER, MODE_WORLD_WRITEABLE);
	              SharedPreferences.Editor editor = showTimeOfMasterProfile.edit();
	              editor.putLong(KEY_FOR_MASTER_PROFILE, System.currentTimeMillis());
	              editor.commit();
	        	  dialog.cancel();
	        	  Intent mIntent = new Intent(SplashScreenActivity.this,HomeScreenActivity.class);
	  				startActivity(mIntent);
	  				SplashScreenActivity.this.finish();

	        }})
	        .setPositiveButton("Activate", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					
					 SharedPreferences showTimeOfMasterProfile = getSharedPreferences(PREFS_NAME_MASTER, MODE_WORLD_WRITEABLE);
		              SharedPreferences.Editor editor = showTimeOfMasterProfile.edit();
		              editor.putLong(KEY_FOR_MASTER_PROFILE, System.currentTimeMillis());
		              editor.commit();
					dialog.cancel();
					Intent intent = new Intent(SplashScreenActivity.this,AccountActivity.class);
					intent.putExtra("Account_Activity_Calling", " From trial mode activate");
					startActivity(intent);
					finish();
				}
			}).create().show();
		}
		else {
			 Intent mIntent = new Intent(SplashScreenActivity.this,HomeScreenActivity.class);
				startActivity(mIntent);
				SplashScreenActivity.this.finish();
		}
			
	}
	
	private boolean isDialogForProfilesShown() {
		SharedPreferences showTimeOfMasterProfile = getSharedPreferences(PREFS_NAME_MASTER, 0);
	       long previousTimeInMilliSec = showTimeOfMasterProfile.getLong(KEY_FOR_MASTER_PROFILE, 0);
	       long currentTime=System.currentTimeMillis();
	       boolean flag=((currentTime-previousTimeInMilliSec)<24*60*60*1000)?true:false;//24*60*60*1000
	       //Log.v("Safecell :"+"3", "3 "+flag);

		return flag;
	}

	private void showTrialAccountRemainingDayDialog()
	{
		if (profileId == accountDetails.getMaster_profile_id()) {
			trailAccountDialogOnlyMasterProfile();
		} else {
			String dialogMessage = remainingDayForTrialMode()== 0 ? lastDayStr : (remainingDayForTrialMode()+1) + remainingDayStr;
			
			dialogForSubProfileShown = isDialogForProfilesShown();
			if(!dialogForSubProfileShown)
			{
				
				new AlertDialog.Builder(context)
				.setMessage(dialogMessage)
				.setTitle("Trial Acccount")
				.setNeutralButton("OK", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {

						SharedPreferences showTimeOfMasterProfile = getSharedPreferences(PREFS_NAME_MASTER, MODE_WORLD_WRITEABLE);
			              SharedPreferences.Editor editor = showTimeOfMasterProfile.edit();
			              editor.putLong(KEY_FOR_MASTER_PROFILE, System.currentTimeMillis());
			              editor.commit();
						dialog.cancel();
						Intent mIntent = new Intent(SplashScreenActivity.this,HomeScreenActivity.class);
						startActivity(mIntent);
						SplashScreenActivity.this.finish();
					}}).create().show();
			}
			else
			{
				Intent mIntent = new Intent(SplashScreenActivity.this,HomeScreenActivity.class);
				startActivity(mIntent);
				SplashScreenActivity.this.finish();
			}
		}
	}
	
	private void showLoginToWebAccountForRenewalMessage(){
		
	      String message = "Your subscription has Expired. Please login to http://my.safecellapp.com/ to renew the subscription.";
	      String title="Trial Expired";
	      quitDialog(title, message);
	}
	
	private void showLoginToWebAccountForTrialMessage() {
		
		String message = "Your subscription has Expired. Please login to http://my.safecellapp.com/ to purchase a subscription.";
		String title ="Trial Expired";
		quitDialog(title, message);
	}

	
	private void showTrailExpiredActivateMessage() {
		
		if (profileId == accountDetails.getMaster_profile_id()) {
			new AlertDialog.Builder(context)
		      .setMessage("Your trial peroid has expired. Please activate your account.")
		      .setTitle("Trial Mode")
		      .setPositiveButton("Quit", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					finish();
				}
			})
			.setNegativeButton("Activate", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					Intent nIntent = new Intent(SplashScreenActivity.this, AccountActivity.class);
					nIntent.putExtra("Account_Activity_Calling", "Master profile trial mode expire");
					startActivity(nIntent);
					finish();
				}
			}).create().show();
			
		} else {
			
			String messsge = "Your trial period has expired. Please ask the account owner to purchase to activate subscription. The app will now terminate. Please restart the app after activation.";
			String title ="Trail Mode";
			quitDialog(title, messsge);
		}
	}
	
	private void showAccountTrialMessage()
	{
		if (accountDetails.isActivated()) {
			showLoginToWebAccountForTrialMessage();
		} else {
			showTrailExpiredActivateMessage();
		}
	}
	

	private void quitDialog(String title, String message){
		
		new AlertDialog.Builder(context)
	      .setMessage(message)
	      .setTitle(title)
	      .setNeutralButton("Quit", new DialogInterface.OnClickListener() {

	          public void onClick(DialogInterface dialog, int which) {

	        	  dialog.cancel();
	        	  finish();

	        }}).show();
	}
	
	public void run() {
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		if (!exitProfile) {
			/*Intent mIntent = new Intent(SplashScreenActivity.this,
					AccountScreenActivity.class);*/
			Intent mIntent = new Intent(SplashScreenActivity.this,HomeScreenActivity.class);
			startActivity(mIntent);
			SplashScreenActivity.this.finish();
		} else {
			Intent mIntent = new Intent(SplashScreenActivity.this,HomeScreenActivity.class);
			startActivity(mIntent);
			SplashScreenActivity.this.finish();

		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.profileInUseQuitButton:
			profileInUseAlertDialog.cancel();
			finish();
			break;
		case R.id.profileDeleteProfileButton:
			deleteDatabase(DBAdapter.DATABASE_NAME);
			DBAdapter dbAdapter = new DBAdapter(context);
			dbAdapter.open();
			dbAdapter.closeDatabase();
			profileInUseAlertDialog.cancel();
			gamePlaySettingChange();
			Intent intent = new Intent(SplashScreenActivity.this, TrialOrAlreadyAccountActivity.class);
			startActivity(intent);
			finish();
			break;
		case R.id.profileMoveProfileButton:
			profileInUseAlertDialog.cancel();
			
			SharedPreferences showTimeOfMoveProfilesButton = getSharedPreferences(PREFS_NAME_MOVE_PROFILES, MODE_WORLD_WRITEABLE);
            SharedPreferences.Editor editor = showTimeOfMoveProfilesButton.edit();
            editor.putLong(KEY_FOR_MOVE_PROFILE, System.currentTimeMillis());
            editor.commit();
			//progressDialog.show();
			//profileListThread listThread = new profileListThread();
			//listThread.start();
			showDialogListofProfile(); 
			break;
		
		}
	}

	
	private synchronized void downloadProfile(){
	
		AccountRepository accountRepository = new AccountRepository(context);
		String apiKey = accountRepository.currentAPIKey();
		try {
			RetriveTripsOfProfile retriveTripsOfProfile = new RetriveTripsOfProfile(SplashScreenActivity.this, profileId,apiKey);	
			retriveTripsOfProfile.retrive();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	
		
	}
	
	Handler profileHandler = new Handler(){
		
		public void handleMessage(android.os.Message msg) {
			progressDialog.dismiss();
			try {
				Intent mIntent = new Intent(SplashScreenActivity.this,HomeScreenActivity.class);
				startActivity(mIntent);
				finish();
			} catch (Exception e) {
				// TODO: handle exception
			}
			
		};
	};
	
	private void gamePlaySettingChange(){
		SharedPreferences sharedPreferences = getSharedPreferences("GamePlayCheckBox", MODE_WORLD_READABLE);
    	SharedPreferences.Editor editor = sharedPreferences.edit();
	    editor.putBoolean("isGameplay", false);
	    editor.commit();
	}
	private  void showDialogListofProfile(){
		
		profileNameList[0] = displayProfileName;
		
		new AlertDialog.Builder(context)
		.setTitle("Profile")
		.setSingleChoiceItems(profileNameList, -1, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	dialog.cancel();
		    	progressDialog.show();
		    	deleteDatabase(DBAdapter.DATABASE_NAME);
				DBAdapter dbAdapter = new DBAdapter(context);
				dbAdapter.open();
				dbAdapter.closeDatabase();
				// Log.v("Safecell:", "old Device Key" +scProfile.getDeviceKey());
				 
				scProfile.setDeviceKey(SCProfile.newUniqueDeviceKey());
				HashMap<Object, Object> profileMap = new HashMap<Object, Object>();
				profileMap.put("first_name", scProfile.getFirstName());
				profileMap.put("last_name", scProfile.getLastName());
				profileMap.put("email", scProfile.getEmail());
				//profileMap.put("bus_driver", false);
				profileMap.put("phone", scProfile.getPhone());
				
				profileMap.put("device_key", scProfile.getDeviceKey());
				profileMap.put("account_id",scProfile.getAccountID());
				profileMap.put("id", scProfile.getProfileId());
				
				
				UpdateAccountDetails updateAccountDetails = new UpdateAccountDetails(
						context, profileMap, scAccount.getApiKey(),scProfile.getProfileId());
				updateAccountDetails.updateAccountJson();
				
			   //Log.v("Safecell:", "New Device Key" +scProfile.getDeviceKey());
               
				 updateAccountDetails.putRequest();
				message = updateAccountDetails.getFailureMessage();
                 if(!"".equals(message))
                 {
                	 progressDialog.dismiss();
                	 AlertDialog.Builder builder = new AlertDialog.Builder(context)
       		      .setMessage("Profile updation failed because of an unexpected error.")
       		      .setTitle("Profile Update Failed")
                	 .setCancelable(false)
 					.setPositiveButton("Ok",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									
 									
 								}
 							});
                	 AlertDialog alert = builder.create();
                	 alert.show();
                 }else{
                	 profilesRepository = new ProfilesRepository(context);
     		    	profilesRepository.insertProfile(scProfile);
     				
     		    	accountRepository = new AccountRepository(context);
     		    	accountRepository.insertAccount(scAccount);
     		    	
     		    	gamePlaySettingChange();
     		    	profileListThread listThread = new profileListThread();
     		    	listThread.start();
                	 
                 }
				

		    	
		    }
		}).create().show();

	}
	
	private class profileListThread extends Thread{
		
		public profileListThread() {
			
		}
		@Override
		public void run() {
			super.run();
			
			try {
				downloadProfile();
			} catch (Exception e) {
				
			}
			profileHandler.sendEmptyMessage(0);
		}
	}
	
	class downloadAccount extends AsyncTask<Void, Void, Void>{
		
		
		@Override
		protected void onPreExecute() {
			
			super.onPreExecute();
			/*progressDialogDownloadAcc.setMessage("Loading wait...");
			progressDialogDownloadAcc.show();
			
			progressDialogDownloadAcc.setCancelable(false);*/
		}
		@Override
		protected Void doInBackground(Void... params) {
			
			try {
				getAccountStatus();
				} 
			catch (Exception e) {
				//e.printStackTrace();
				onPostExecute(null);
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			
			super.onPostExecute(result);
			 
			if (accountStatus.getStatusCode()!= 200) {
				checkStatusResponse();
			}
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		finish();
		return super.onKeyDown(keyCode, event);
		
	}
}
