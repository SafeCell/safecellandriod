package com.safecell;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.safecell.dataaccess.ContactRepository;
import com.safecell.model.SCContact;
import com.safecell.utilities.FlurryUtils;
import com.safecell.utilities.LocationSP;
import com.safecell.utilities.StateAddress;

public class EmergencyContactListActivity extends Activity {
	
	Button homeButton, btnMyTrips, settingsButton,rulesButton;
	private static final int INSERT=0;
	private static final int UPDATE=1;
	private static final String TAG = "EmergencyContactListActivity";
	public ListView listView;
	mListAdapter listAdapter;
	 /*private String[] name;
	private String[] number; */
	ArrayList<SCContact> contactArrayList = new ArrayList<SCContact>();
	// ArrayList contactInfo=new ArrayList<SCContact>();
	ContactRepository contactRepository;
	OnItemClickListener listener;
	//private boolean Update = true;
	//private boolean insertOrUpdate = true;
	EditText tvEmergencyContactName;
	EditText tvEmergencyContactNumber;
	int position;
	TextView tvLocation;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setWindowAnimations(R.anim.null_animation);

		setContentView(R.layout.emergency_contact_list_layout);
		listView = (ListView) findViewById(R.id.emergency_contact_ListView);
		contactRepository = new ContactRepository(EmergencyContactListActivity.this);
		listAdapter = new mListAdapter(EmergencyContactListActivity.this);
		
		contactArrayList = contactRepository.initialiseEmergencyContactArrayList();
		//Log.v("Safecell :"+TAG+"/onCreate", "contactArrayList size="+contactArrayList.size());
			

		
		 /* name=new String[]{"aa","bb"};
		  number=new String[]{"11","22"};*/
		 

		// fetchData();
		
		listView.setAdapter(listAdapter);

		listView.setOnItemClickListener(onItemClickListener);
		homeButton = (Button) findViewById(R.id.tabBarHomeButton);
		btnMyTrips = (Button) findViewById(R.id.tabBarMyTripsButton);
		rulesButton = (Button) findViewById(R.id.tabBarRulesButton);
		settingsButton = (Button) findViewById(R.id.tabBarSettingsButton);
		settingsButton.setBackgroundResource(R.drawable.settings_clicked);

		tvLocation=(TextView) findViewById(R.id.tabBarCurentLocationTextView);
		tvLocation.setText(LocationSP.LocationSP);
		
		TabControler tabControler =new TabControler(EmergencyContactListActivity.this);
		btnMyTrips.setOnClickListener(tabControler.getMyTripsOnClickListner());
		rulesButton.setOnClickListener(tabControler.getRulesOnClickListner());
		homeButton.setOnClickListener(tabControler.getHomeTabOnClickListner());
		settingsButton.setOnClickListener(tabControler.getSettingOnClickListener());


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

	OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			String name = null;
			String number=null;
			int status;
			position = arg2;
			//Log.v("Safecell :"+"Position", position + "");
			int id=contactArrayList.get(arg2).getId();
			//Log.v("Safecell :"+"name", "Name" + contactArrayList.get(arg2).getName() + "");
			
			if(id==-1)
			{
				
				status=INSERT;
			}else
			{
				 name = contactArrayList.get(arg2).getName();
				 number = String.valueOf(contactArrayList.get(arg2).getNumber());
				 status=UPDATE;
			}
				
			Displaydialog(name, number,status);
		}
	};

	void Displaydialog(String Name, String Number,final int status) {

		String updateInsert=null;
		updateInsert=(status==INSERT)?"Insert":"Update";
		
		LayoutInflater li = LayoutInflater.from(this);
		View categoryDetailView = li.inflate(R.layout.cutom_dialog, null);

	
		tvEmergencyContactName = (EditText) categoryDetailView.findViewById(R.id.emergencyContactListCustumChangedName);
		tvEmergencyContactNumber = (EditText) categoryDetailView.findViewById(R.id.emergencyContactListCustumChangedNumber);
		
		tvEmergencyContactName.setText(Name);
		tvEmergencyContactNumber.setText(Number);

		contactRepository = new ContactRepository(EmergencyContactListActivity.this);


		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(updateInsert+" Name And Number");
		builder.setView(categoryDetailView);
		// builder.setMessage("Are you sure you want to exit?");
		builder.setCancelable(false);
		String[] args ={};
		
		builder.setPositiveButton(updateInsert,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

						if (!tvEmergencyContactName.getText().toString().equalsIgnoreCase("")&& 
								!tvEmergencyContactNumber.getText().toString().equalsIgnoreCase(
										"")) {

							
								SCContact scContact = new SCContact();
								scContact.setName(tvEmergencyContactName.getText().toString());
								
								scContact.setNumber(tvEmergencyContactNumber
										.getText().toString());
								 scContact.setId(position+1);

								//Log.v("Safecell :"+"ed.getText().toString()", ""
//										+ tvEmergencyContactNumber.getText().toString());
								/*if (insertOrUpdate) {
									
									contactRepository = new ContactRepository(EmergencyContactListActivity.this);
									contactRepository.insertContact(scContact);
								} else {
									contactRepository = new ContactRepository(EmergencyContactListActivity.this);
									contactRepository.updateContact(scContact);
								}*/
								
								contactRepository = new ContactRepository(EmergencyContactListActivity.this);
								if(status==INSERT)
								{
									//Log.v("Safecell :"+TAG+"/Displaydialog", "in INSERT");
									
									contactRepository.insertContact(scContact);
								}else
								{
									//Log.v("Safecell :"+TAG+"/Displaydialog", "in UPDATE");
									contactRepository.updateContact(scContact);
								}
								
								
								// contactRepository.SelectMin_Contacts();

								contactArrayList = contactRepository.initialiseEmergencyContactArrayList();
								listView.setAdapter(listAdapter);
							
						}

					}
				});
		builder.setNegativeButton("cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	


	public class mListAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		public mListAdapter(Context context) 
		{
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {

			// return name.length;
			return contactArrayList.size();
		}

		@Override
		public Object getItem(int position) {

			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			TextView tvName, tvNumber;
			// ImageView icon;
			View view;
			view = mInflater.inflate(
					R.layout.emergency_contact_list_custom_row, null);
			
			tvName = (TextView) view
					.findViewById(R.id.emergencyContactListCustumRowName);
			tvNumber = (TextView) view
					.findViewById(R.id.emergencyContactListCustumRowPhoneNumber);
			// icon
			// =(ImageView)findViewById(R.id.emergencyContactListCustumRowIcon);
			
			if(contactArrayList.get(position).getId()==-1){
				tvName.setText("");
				tvNumber.setText("");
			}
			else{
				tvName.setText(contactArrayList.get(position).getName());
				tvNumber.setText(contactArrayList.get(position).getNumber() + "");
			}
			
			return view;

		}

	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {

			Intent homeScreen = new Intent(EmergencyContactListActivity.this,SettingScreenActivity.class);
			homeScreen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(homeScreen);
			finish();
		}
		return false;

	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		StateAddress.currentActivity = this;
	}

}

