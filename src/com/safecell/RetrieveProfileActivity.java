package com.safecell;

import com.safecell.utilities.FlurryUtils;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class RetrieveProfileActivity extends Activity{
	EditText etUserName, etPassword;
	Button btnRetriveProfiles;
	
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initUi();
	}
	void initUi(){
		setContentView(R.layout.retrieve_profiles_layout);
		etUserName = (EditText) findViewById(R.id.RetriveProfileUserNameEditText);
		etPassword = (EditText) findViewById(R.id.RetriveProfileUserNameEditText);
		btnRetriveProfiles = (Button)findViewById(R.id.RetriveProfilesRetriveProfileButton);
	}
	
	View.OnClickListener btnRetriveProfileOnclickListner= new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
		}
	};
		
		
}
