package com.safecell;


import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.safecell.dataaccess.LicenseRepository;
import com.safecell.networking.DeleteProfile;
import com.safecell.utilities.FlurryUtils;
import com.safecell.utilities.LocationSP;
import com.safecell.utilities.StateAddress;

public class DeleteProfileActivity extends ListActivity{
	
	String[] firstTitleLabelArray = { "First Name", "Last Name", "Email","Phone","License" };//License
	String[] secondTitleLabelArray = { "", "", "", "", "",""};
	private Button deleteProfileButton;
	 private	Button homeButton, btnMyTrips, settingsButton,rulesButton;
	TextView deleteProfileHeading;
	EditText dialogInputEditText;
	Context context;
	ProgressDialog dialog;
	ProgressDialog progressDialog;
	String message;
	String [] values;
	 private TextView tvLocation;
	private LicenseRepository licenseRepository;
	 
	String profileId, masterId;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setWindowAnimations(R.anim.null_animation);
		context = DeleteProfileActivity.this;
		dialog = new ProgressDialog(context);
		licenseRepository = new LicenseRepository(context);
		intiUI();
	}

	public void intiUI()
	{
		setContentView(R.layout.manage_profile);
		deleteProfileButton=(Button) findViewById(R.id.manageProfileUpdateProfileButton);
		deleteProfileHeading=(TextView) findViewById(R.id.ManageProfileHeadingTextView);
		homeButton = (Button) findViewById(R.id.tabBarHomeButton);
		btnMyTrips = (Button) findViewById(R.id.tabBarMyTripsButton);
		rulesButton = (Button) findViewById(R.id.tabBarRulesButton);
		settingsButton = (Button) findViewById(R.id.tabBarSettingsButton);
		settingsButton.setBackgroundResource(R.drawable.settings_clicked);
		tvLocation=(TextView) findViewById(R.id.tabBarCurentLocationTextView);
		tvLocation.setText(LocationSP.LocationSP);
		TabControler tabControler =new TabControler(DeleteProfileActivity.this);
		btnMyTrips.setOnClickListener(tabControler.getMyTripsOnClickListner());
		rulesButton.setOnClickListener(tabControler.getRulesOnClickListner());
		settingsButton.setOnClickListener(tabControler.getSettingOnClickListener());
		homeButton.setOnClickListener(tabControler.getHomeTabOnClickListner());
		deleteProfileHeading.setText("Delete Profile");
		deleteProfileButton.setText("Delete Profile");
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		values = bundle.getStringArray("scProfileAccount");
		
		String licenseName = licenseRepository.selectGetLicenseName(values[7]);
		//Log.v("Safecell :"+"licenseName", "Name = "+licenseName);
		if (licenseName.equalsIgnoreCase("")) {
			licenseName = values[7];
		}
		secondTitleLabelArray[0]=values[2];
		secondTitleLabelArray[1]=values[3];
		secondTitleLabelArray[2]=values[4];
		secondTitleLabelArray[3]=values[5];
		secondTitleLabelArray[4]=licenseName;
		
		profileId=values[1];
		String masterProfile=values[0];
		String profileId=values[1];
		String profileBeingUsed=values[6];
		deleteProfileButton.setVisibility(View.GONE);
		
		
		ListView listView = getListView();
		listView.setAdapter(new DeleteProfileListAdapter(DeleteProfileActivity.this));
		listView.setFocusable(true);
		listView.setCacheColorHint(Color.TRANSPARENT);
		listView.setSelector(R.drawable.transparent_row_bg);
		listView.setFocusableInTouchMode(true);
		listView.setEnabled(true);
		
		if (masterProfile.equals(profileBeingUsed)) {
			deleteProfileButton.setVisibility(View.VISIBLE);
			if (masterProfile.equals(profileId))
				deleteProfileButton.setVisibility(View.GONE);
		}
		
		
		deleteProfileButton.setOnClickListener(deleteButtonClickListener);
		
		//setListAdapter(new DeleteProfileListAdapter(DeleteProfileActivity.this));
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
	
	class DeleteProfileListAdapter extends BaseAdapter{

		ListActivity context;
		
		public DeleteProfileListAdapter(ListActivity context) {
			super();
			this.context = context;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return secondTitleLabelArray.length-1;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			
			
			
			LayoutInflater inflater = context.getLayoutInflater();
			View row = inflater.inflate(R.layout.manage_profile_custom_row,
					null);
				TextView firstLabel = (TextView) row
				.findViewById(R.id.ManageProfileCustomRowFirstTitleTextView);
				TextView secondLabel = (TextView) row
				.findViewById(R.id.ManageProfileCustomRowSecondTitleTextView);
				
			
				switch (position) {
				case 0:

					firstLabel.setText(firstTitleLabelArray[position]);
					secondLabel.setText(secondTitleLabelArray[position]);
					break;

				case 1:
					firstLabel.setText(firstTitleLabelArray[position]);
					secondLabel.setText(secondTitleLabelArray[position]);
					break;
				case 2:
					firstLabel.setText(firstTitleLabelArray[position]);
					secondLabel.setText(secondTitleLabelArray[position]);
					break;
				case 3:
					firstLabel.setText(firstTitleLabelArray[position]);
					secondLabel.setText(secondTitleLabelArray[position]);
					break;
				case 4:
					firstLabel.setText(firstTitleLabelArray[position]);
					secondLabel.setText(secondTitleLabelArray[position]);
					break;
				}
			
			return row;
		}
		
	}

	OnClickListener deleteButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			//Log.v("Safecell :"+"Click on Delete Button", "Deleted:" + profileId);
			final int profileId = Integer
					.parseInt(DeleteProfileActivity.this.profileId);
			DeleteProfile deleteProfile = new DeleteProfile(
					DeleteProfileActivity.this, profileId);
			boolean deleteProfileResult = deleteProfile.deleteRequest();

			String message = "Delete Profile (" + profileId
					+ ") Failed. Reason: " + deleteProfile.getFailureMessage();

			if (deleteProfileResult) {
				message = "Profile (" + profileId
						+ ") was deleted successfully";
			}

			new AlertDialog.Builder(DeleteProfileActivity.this).setMessage(
					message).setNeutralButton("OK",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {

							dialog.cancel();
							DeleteProfileActivity.this.finish();
							Intent mIntent = new Intent( DeleteProfileActivity.this,AccountActivity.class);
							mIntent.putExtra("Account_Activity_Calling", "From delete profile.");
							startActivity(mIntent);
							
						/*	Intent intent = new Intent();
							intent.putExtra("Delete_profileId", profileId);
							setResult(RESULT_OK,intent);
							finish();*/
						}
					}).show();
			
		}
		
	};
	
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		
		if(keyCode==KeyEvent.KEYCODE_BACK)
		{
			
			DeleteProfileActivity.this.finish();
			Intent mIntent = new Intent( DeleteProfileActivity.this,AccountActivity.class);
			mIntent.putExtra("Account_Activity_Calling", "From delete profile.");
			startActivity(mIntent);
			/*setResult(RESULT_CANCELED);
			finish();*/
		}
		return false;
  };
   
  @Override
protected void onResume() {
	// TODO Auto-generated method stub
	super.onResume();
	StateAddress.currentActivity = this;
}
}
