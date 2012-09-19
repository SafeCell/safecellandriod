package com.safecell;


import com.safecell.utilities.FlurryUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;


public class AccountScreenActivity extends Activity {
	
	 Button joinSafeCellButton, activateDevicesButton;
	 @Override
	  public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	       	        
	        this.intiUI();
	        joinSafeCellButton.setOnClickListener(joinButtonOnClickListener);
	        activateDevicesButton.setOnClickListener(activateButtonOnClickListener);
	        
	       	     
	 }
	 
	 private void intiUI()
	 {
		 setContentView(R.layout.home_screen_layout_);	    
		 joinSafeCellButton =(Button)findViewById(R.id.flashScrenJoinSafecellButton);
		 activateDevicesButton=(Button)findViewById(R.id.flashScrenActivateDeviceButton);
	     //DBAdapter.context = getApplicationContext();
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
	 
	 private OnClickListener joinButtonOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent mIntent = new Intent(AccountScreenActivity.this,AccountFormActivity.class);
	        mIntent.putExtra("checkNewAccountOrValidateAccount", true);
	        finish();
	        startActivity(mIntent);
	       		
		}
	};
	 
	private OnClickListener activateButtonOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			Intent mIntent = new Intent(AccountScreenActivity.this,AccountVerificatonActivity.class);
			finish();
			startActivity(mIntent);
		}
	};

}
