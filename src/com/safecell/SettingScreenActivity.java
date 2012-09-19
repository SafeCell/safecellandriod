package com.safecell;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;

import android.widget.ListView;
import android.widget.TextView;

import com.safecell.utilities.FlurryUtils;
import com.safecell.utilities.LocationSP;
import com.safecell.utilities.StateAddress;

public class SettingScreenActivity extends ListActivity {
	
	Button homeButton, btnMyTrips, settingsButton,rulesButton;
	TextView tvLocation;
	



	int []imageArray={R.drawable.gameplay_icon,R.drawable.manage_profile_icon,R.drawable.manage_account_devices_icon,R.drawable.emergency_numbers_icon,R.drawable.sounds,R.drawable.auto_reply,R.drawable.signal_icon};
	String labelTextViewArray []={"Gameplay Settings & Score", "Manage Profile", "Add/Delete Account Devices", "Emergency Numbers", "Notification Sound","Auto Reply","GPS & Backgrounding"};
	private HomeScreenActivity homeScreenActivity;


	/*int []imageArray={R.drawable.gameplay_icon,R.drawable.manage_profile_icon,R.drawable.manage_account_devices_icon,R.drawable.emergency_numbers_icon,android.R.drawable.stat_notify_more,android.R.drawable.ic_delete};
	String labelTextViewArray []={"Gameplay Settings & Score", "Manage Profile", "Add/Delete Account Devices","Emergency Numbers","Development settings for GPS","Common Screen"};*/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setWindowAnimations(R.anim.null_animation);
		
		//startService(new Intent(SettingScreenActivity.this, LocationService.class));
		this.intiUI();
		setListAdapter(new SettingListAdapter(SettingScreenActivity.this));
		TabControler tabControler =new TabControler(SettingScreenActivity.this);
		homeButton.setOnClickListener(tabControler.getHomeTabOnClickListner());
		
		//LocationService.setAct =this;

	}
	private void intiUI() {

		setContentView(R.layout.setting_layout);
		homeButton = (Button) findViewById(R.id.tabBarHomeButton);
		btnMyTrips = (Button) findViewById(R.id.tabBarMyTripsButton);
		rulesButton = (Button) findViewById(R.id.tabBarRulesButton);
		settingsButton = (Button) findViewById(R.id.tabBarSettingsButton);
		settingsButton.setBackgroundResource(R.drawable.settings_clicked);
		tvLocation=(TextView) findViewById(R.id.tabBarCurentLocationTextView);
		tvLocation.setText(LocationSP.LocationSP);
		
		TabControler tabControler =new TabControler(SettingScreenActivity.this);
		btnMyTrips.setOnClickListener(tabControler.getMyTripsOnClickListner());
		rulesButton.setOnClickListener(tabControler.getRulesOnClickListner());
	//	settingsButton.setOnClickListener(tabControler.getSettingOnClickListener());
		//set disable
		disableSettings();

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

	class SettingListAdapter extends ArrayAdapter<Object>
	{
		Activity context;

		public SettingListAdapter(Activity context) {
			super(context, R.layout.setting_layout_listrow,labelTextViewArray);
			this.context = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = context.getLayoutInflater();
			View row = inflater.inflate(R.layout.setting_layout_listrow, null);

			ImageView settingListRowImageView = (ImageView)row.findViewById(R.id.SettingsListRowImageView);
			TextView  settingListRowTextView = (TextView)row.findViewById(R.id.SettingsListRowTextView);

			

			switch (position) {
			case 0:
				settingListRowImageView.setBackgroundResource(imageArray[position]);
				settingListRowTextView.setText(labelTextViewArray[position]);
				break;

			case 1://Manage Profile
				settingListRowImageView.setBackgroundResource(imageArray[position]);
				settingListRowTextView.setText(labelTextViewArray[position]);
				break;

			case 2:
				settingListRowImageView.setBackgroundResource(imageArray[position]);
				settingListRowTextView.setText(labelTextViewArray[position]);
				break;

			
			case 3://Emergency Number
				settingListRowImageView.setBackgroundResource(imageArray[position]);
				settingListRowTextView.setText(labelTextViewArray[position]);
				break;
			case 4:
				settingListRowImageView.setBackgroundResource(imageArray[position]);
				settingListRowTextView.setText(labelTextViewArray[position]);
				break;
			case 5:
				settingListRowImageView.setBackgroundResource(imageArray[position]);
				settingListRowTextView.setText(labelTextViewArray[position]);
				break;
			case 6:
				settingListRowImageView.setBackgroundResource(imageArray[position]);
				settingListRowTextView.setText(labelTextViewArray[position]);
				break;
			}

			return row;
		}

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);

		switch (position) {
		case 0:
			Intent gamePlay = new Intent(SettingScreenActivity.this,GamePlayActivity.class);
			startActivity(gamePlay);
			break;

		case 1:
			Intent intent_ManageProfile = new Intent(SettingScreenActivity.this,ManageProfile_Activity.class);
			intent_ManageProfile.putExtra("CallingActivity", "SettingScreenActivity");
			startActivity(intent_ManageProfile);
			
			break;

		case 2:
			Intent intentAccount = new Intent(SettingScreenActivity.this,AccountActivity.class);
			intentAccount.putExtra("Account_Activity_Calling", "From setting activity");
			startActivity(intentAccount);
			break;

		
		case 3://Emergency Number
				//Intent intent = new Intent(SettingScreenActivity.this,EmergencyContactsActivity.class);
			Intent intent = new Intent(SettingScreenActivity.this,EmergencyContactListActivity.class);
				startActivity(intent);
		break;
		case 4://sound notification
			Intent intentSoundScreen = new Intent(SettingScreenActivity.this,NotificationSoundActivity.class);
			startActivity(intentSoundScreen);
	break;
		case 5:
			Intent intentSMSscreen= new Intent(SettingScreenActivity.this,AutoReplySmsActivity.class);
			startActivity(intentSMSscreen);
	        break;
		case 6:
			Intent intentSignal= new Intent(SettingScreenActivity.this,SingalActivity.class);
			startActivity(intentSignal);
	        break;
		
		}
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			
			//finish();
			
			if (homeScreenActivity != null) { 
				 homeScreenActivity.finish();
			}
			Intent homeScreen = new Intent(SettingScreenActivity.this, HomeScreenActivity.class);
			homeScreen.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			startActivity(homeScreen);			
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
/*	@Override
	public void locationChanged(String address) {
		// TODO Auto-generated method stub
		Log.v("Safecell :"+"SettingScreenActivity","Location ChangeCallBack Called");
		if(tvLocation != null)
		{
			tvLocation.setText(address);
		}
		
	}*/
	
	@Override
	protected void onResume() {
		//LocationService.setAct = this;
		super.onResume();
		StateAddress.currentActivity = this;
		
		
	}
	
	private void disableSettings() {
	    SharedPreferences preferences = getSharedPreferences("SETTINGS", MODE_WORLD_WRITEABLE);
        SharedPreferences.Editor editor= preferences.edit();
        editor.putBoolean("isDisabled", true);
        editor.commit();
	    
	}
}
