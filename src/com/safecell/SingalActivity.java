package com.safecell;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.safecell.utilities.FlurryUtils;
import com.safecell.utilities.LocationSP;
import com.safecell.utilities.StateAddress;

public class SingalActivity extends Activity{
	Context context;
	private Button homeButton,btnMyTrips,rulesButton,settingsButton;
	private TextView tvLocation;
	private TextView gpsDataText,podpText,hodpText,autoTripSaveDisableTextView;
	private CheckBox backgroundTrip;
	private SharedPreferences sharedPreferences;
	
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setWindowAnimations(R.anim.null_animation);
		context=SingalActivity.this;
		//startService(new Intent(SettingScreenActivity.this, LocationService.class));
		this.intiUI();
		
		sharedPreferences=getSharedPreferences("TripCheckBox", MODE_WORLD_READABLE);
		boolean isbackgroundtrip = sharedPreferences.getBoolean("isbackgroundtrip", true);
		backgroundTrip.setChecked(isbackgroundtrip);
		
	}
	
	
	private void intiUI()
	{
		setContentView(R.layout.signal_layout);
		backgroundTrip=(CheckBox)findViewById(R.id.TripCheckBox);
		
		settingsButton = (Button) findViewById(R.id.tabBarSettingsButton);
		settingsButton.setBackgroundResource(R.drawable.settings_clicked);
 		homeButton = (Button) findViewById(R.id.tabBarHomeButton);
		btnMyTrips = (Button) findViewById(R.id.tabBarMyTripsButton);
		rulesButton = (Button) findViewById(R.id.tabBarRulesButton);
 		TabControler tabControler =new TabControler(SingalActivity.this);
 		settingsButton.setOnClickListener(tabControler.getSettingOnClickListener());
 		btnMyTrips.setOnClickListener(tabControler.getMyTripsOnClickListner());
		rulesButton.setOnClickListener(tabControler.getRulesOnClickListner());
		homeButton.setOnClickListener(tabControler.getHomeTabOnClickListner());
		autoTripSaveDisableTextView = (TextView)findViewById(R.id.signalActivityAutoTripsDisableTextView);
		tvLocation=(TextView) findViewById(R.id.tabBarCurentLocationTextView);
 		 tvLocation.setText(LocationSP.LocationSP);
 		 
 		 gpsDataText=(TextView) findViewById(R.id.gpsDataTextView);
 		 
 		 if(TrackingService.context == null) {
 			 gpsDataText.setText("Unavailable");
 			backgroundTrip.setEnabled(false);
 			//autoTripSaveDisableTextView.setVisibility(View.GONE);
 		 } else {
 			 if(TrackingService.SELECTED_PROVIDER == LocationManager.GPS_PROVIDER) {
 				gpsDataText.setText("GPS Satellite");
 				backgroundTrip.setEnabled(true);
 				autoTripSaveDisableTextView.setVisibility(View.INVISIBLE);
 				
 			 } else if (TrackingService.SELECTED_PROVIDER == LocationManager.NETWORK_PROVIDER) {
 				gpsDataText.setText("Network");
 				backgroundTrip.setEnabled(false);
 				//autoTripSaveDisableTextView.setVisibility(View.GONE);
 			 } else {
 				gpsDataText.setText("Connecting...");
 				backgroundTrip.setEnabled(false);
 				//autoTripSaveDisableTextView.setVisibility(View.GONE);
 			 }
 		 }
 		 
 		 podpText=(TextView) findViewById(R.id.pdopTextView);
 		
 		 //podpText.setText("Postitional Strength  :"    +TrackingService.currentPDOP);
 		 hodpText=(TextView) findViewById(R.id.hdopTextView);
 		// hodpText.setText("Horizontal Strength  :"     +TrackingService.currentHDOP);
 		gpsValueCalculate();
 		
 		backgroundTrip.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SharedPreferences.Editor editor= sharedPreferences.edit();
				editor.putBoolean("isbackgroundtrip", isChecked);
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
	
	
	private void gpsValueCalculate()
	{
		 double  currentPDOP ;
		 double currentHDOP ;
		 NumberFormat formatter = new DecimalFormat("#0.00");

		 if(TrackingService.currentPDOP < 0) {
			 podpText.setText("Positional  Strength  "    +"Unavailable"); 
		 }else{
			 currentPDOP=(1/TrackingService.currentPDOP)*100;
			 String pDOPStr = formatter.format(currentPDOP);
			 //Log.v("currentPDOP", String.valueOf(currentPDOP));
			 podpText.setText("Positional  Strength  "    + pDOPStr +"%");
		 }
		 
		 if(TrackingService.currentHDOP < 0) {
			 hodpText.setText("Horizontal  Strength  "    +"Unavailable"); 
		 }else{
			 currentHDOP=(1/TrackingService.currentHDOP)*100;
			 String hDOPStr = formatter.format(currentHDOP);
			// Log.v("currentPDOP", String.valueOf(currentHDOP));
			 hodpText.setText("Horizontal  Strength  "     + hDOPStr +"%");
		 }
 		 
		 
		 
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {

			Intent homeScreen = new Intent(SingalActivity.this,SettingScreenActivity.class);
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
