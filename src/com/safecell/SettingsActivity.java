package com.safecell;

import com.safecell.utilities.FlurryUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class SettingsActivity extends Activity{
	private ToggleButton settingToggleButton;
	//private OnClickListener backButtononClickListener;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		getWindow().setWindowAnimations(R.anim.null_animation);
		this.intiUI();

		settingToggleButton.setOnCheckedChangeListener(settingToggleButtonListener);



	}//end on create 
	private void intiUI()
	{
		SharedPreferences sharedPreferences = getSharedPreferences("FAKE_LOCATION", MODE_PRIVATE);
		setContentView(R.layout.settings_layout);
		settingToggleButton = (ToggleButton)findViewById(R.id.settingSettingToggleButton);
		settingToggleButton.setChecked(sharedPreferences.getBoolean("fake_location", false));
		
		
		

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


	private OnCheckedChangeListener settingToggleButtonListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			SharedPreferences sharedPreferences = getSharedPreferences("FAKE_LOCATION", MODE_PRIVATE);
			Editor prefEditor = sharedPreferences.edit();			
			prefEditor.putBoolean("fake_location", isChecked);		
			prefEditor.commit();
			//FakeLocationManager.FakeLocation=isChecked;
			//Log.v("Safecell :"+"Fake Location",""+isChecked);	
		}
	};


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {

			Intent homeScreen = new Intent(SettingsActivity.this,HomeScreenActivity.class);
			homeScreen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(homeScreen);
			finish();
		}
		return false;

	}

}
