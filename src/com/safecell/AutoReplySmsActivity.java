package com.safecell;

import com.safecell.utilities.FlurryUtils;
import com.safecell.utilities.LocationSP;
import com.safecell.utilities.StateAddress;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class AutoReplySmsActivity extends Activity{
	
	private CheckBox autoReplycheckBox;
	private SharedPreferences sharedPreferences;
	private Button homeButton,btnMyTrips,rulesButton,settingsButton;
	private TextView tvLocation;
	Context context;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setWindowAnimations(R.anim.null_animation);
		setContentView(R.layout.sms_phone_enable);
		
		context=AutoReplySmsActivity.this;
		autoReplycheckBox = (CheckBox)findViewById(R.id.SmsPhoneAutoreplyCheckBox);
		settingsButton = (Button) findViewById(R.id.tabBarSettingsButton);
 		settingsButton.setBackgroundResource(R.drawable.settings_clicked);
 		
 		homeButton = (Button) findViewById(R.id.tabBarHomeButton);
		btnMyTrips = (Button) findViewById(R.id.tabBarMyTripsButton);
		rulesButton = (Button) findViewById(R.id.tabBarRulesButton);
 		
 		 tvLocation=(TextView) findViewById(R.id.tabBarCurentLocationTextView);
 		 tvLocation.setText(LocationSP.LocationSP);
 		 
 		TabControler tabControler =new TabControler(AutoReplySmsActivity.this);
 		settingsButton.setOnClickListener(tabControler.getSettingOnClickListener());
 		btnMyTrips.setOnClickListener(tabControler.getMyTripsOnClickListner());
		rulesButton.setOnClickListener(tabControler.getRulesOnClickListner());
		homeButton.setOnClickListener(tabControler.getHomeTabOnClickListner());
		
		 sharedPreferences = getSharedPreferences("SMSAutoReplyCheckBox", MODE_WORLD_READABLE);
	     boolean isAutoreply = sharedPreferences.getBoolean("isAutoreply", true);
	     autoReplycheckBox.setChecked(isAutoreply);
		
	     autoReplycheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SharedPreferences.Editor editor = sharedPreferences.edit();
			    editor.putBoolean("isAutoreply", isChecked);
			    editor.commit();
				
			}
		});
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
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {

			Intent homeScreen = new Intent(AutoReplySmsActivity.this,SettingScreenActivity.class);
			homeScreen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(homeScreen);
			finish();
		}
		return false;

	}
protected void onResume() {
		
		super.onResume();
		StateAddress.currentActivity = this;
	}

}
