package com.safecell;



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

import com.safecell.utilities.FlurryUtils;
import com.safecell.utilities.LocationSP;
import com.safecell.utilities.StateAddress;

public class GamePlayActivity extends Activity {
	
	Context context;
	private CheckBox gamePlay ;
	private Button homeButton,btnMyTrips,rulesButton,settingsButton;
	private SharedPreferences sharedPreferences;
	private TextView tvLocation;
	String[] firstTitleLabelArray = { "Toggle GamePlay" };
	String[] secondTitleLabelArray = {"Participate in SafeCell game play"};
	
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setWindowAnimations(R.anim.null_animation);
        context=GamePlayActivity.this;
        this.initUI();
        
        sharedPreferences = getSharedPreferences("GamePlayCheckBox", MODE_WORLD_READABLE);
        boolean isgameplay = sharedPreferences.getBoolean("isGameplay", true);
        gamePlay.setChecked(isgameplay);

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
 
    
     private void initUI()
     {
    	 setContentView(R.layout.gameplay);
    	 gamePlay = (CheckBox)findViewById(R.id.GamePlayCheckBox);
    	 settingsButton = (Button) findViewById(R.id.tabBarSettingsButton);
 		settingsButton.setBackgroundResource(R.drawable.settings_clicked);
 		homeButton = (Button) findViewById(R.id.tabBarHomeButton);
		btnMyTrips = (Button) findViewById(R.id.tabBarMyTripsButton);
		rulesButton = (Button) findViewById(R.id.tabBarRulesButton);
 		TabControler tabControler =new TabControler(GamePlayActivity.this);
 		settingsButton.setOnClickListener(tabControler.getSettingOnClickListener());
 		btnMyTrips.setOnClickListener(tabControler.getMyTripsOnClickListner());
		rulesButton.setOnClickListener(tabControler.getRulesOnClickListner());
		homeButton.setOnClickListener(tabControler.getHomeTabOnClickListner());
    	 tvLocation=(TextView) findViewById(R.id.tabBarCurentLocationTextView);
 		 tvLocation.setText(LocationSP.LocationSP);
    	 gamePlay.setOnCheckedChangeListener(onCheckedChangeListener);
     }
     
    OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			
			SharedPreferences.Editor editor = sharedPreferences.edit();
		    editor.putBoolean("isGameplay", isChecked);
		    editor.commit();
			
		} 
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {

			Intent homeScreen = new Intent(GamePlayActivity.this,SettingScreenActivity.class);
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
