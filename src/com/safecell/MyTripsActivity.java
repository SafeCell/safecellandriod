package com.safecell;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.safecell.dataaccess.TripJourneysRepository;
import com.safecell.utilities.DateUtils;
import com.safecell.utilities.FlurryUtils;
import com.safecell.utilities.LocationSP;
import com.safecell.utilities.StateAddress;

public class MyTripsActivity extends ListActivity{
	
	String tripNameArray[] = new String[] {};
	int pointsArray[] = new int[] {};
	String milesArray[] = new String[]{};
	String tripRecordedDateArray[] = new String[] {};
	int[] tripIdArray = new int[]{};
	ListView lvTrips;
	TextView tvLocation;
	//String[] tripNameArray = tripNameArray[]{};

	private boolean isgameplay =false;
	Button startNewTripButton, homeButton, btnMyTrips, statisticsButton,
	settingsButton,rulesButton;

	
	int arrayIndex = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setWindowAnimations(R.anim.null_animation);
		//Intent mIntent = getIntent();
		
		fetchTripsData();
		isgameplay = this.GamplayOnOff();
		InitUi();
		
		setListAdapter(new recentTripAdapater(MyTripsActivity.this));
		//setContentView(R.layout.my_trips_layour);
	}	
	

	public boolean GamplayOnOff()
	{
		 SharedPreferences sharedPreferences = getSharedPreferences("GamePlayCheckBox",MODE_WORLD_READABLE);
	     isgameplay = sharedPreferences.getBoolean("isGameplay", true);
	     return isgameplay;
		
	}
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
		int TripId =  tripIdArray[position];
		
		Intent mIntent = new Intent(MyTripsActivity.this, MyTripDiscriptionActivity.class);
		mIntent.putExtra("TripId", TripId);
		startActivityForResult(mIntent, 0);	
	
		
		super.onListItemClick(l, v, position, id);
	}
	
	void InitUi(){
		
		setContentView(R.layout.my_trips_layout);
		startNewTripButton = (Button) findViewById(R.id.StartNewTripLayout_StartNewButton);
		homeButton = (Button) findViewById(R.id.tabBarHomeButton);
		tvLocation=(TextView) findViewById(R.id.tabBarCurentLocationTextView);
		tvLocation.setText(LocationSP.LocationSP);
		
		btnMyTrips = (Button) findViewById(R.id.tabBarMyTripsButton);
		rulesButton = (Button) findViewById(R.id.tabBarRulesButton);
		settingsButton = (Button) findViewById(R.id.tabBarSettingsButton);
		btnMyTrips.setBackgroundResource(R.drawable.mytrips_clicked);
		
		TabControler tabControler =new TabControler(MyTripsActivity.this);
		//btnMyTrips.setOnClickListener(tabControler.getMyTripsOnClickListner());
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
	
	void fetchTripsData(){
		
		TripJourneysRepository tripJourneysRepository = new TripJourneysRepository(MyTripsActivity.this);
		//TripRepository tripRepository = new TripRepository(MyTripsActivity.this);
		
		Cursor cursorTripJounery = tripJourneysRepository.SelectAllTripJourney();
		tripJourneysRepository.SelectAllTripJourney().close();
		//Log.v("Safecell :"+"cursorTripJounery Count",""+cursorTripJounery.getCount());
		//Cursor cursorTotalPointsMiles = tripJourneysRepository.sumOfPointsMiles();
		tripIdArray = new int[cursorTripJounery.getCount()];
        pointsArray = new int[cursorTripJounery.getCount()];
		milesArray = new String[cursorTripJounery.getCount()];
		tripRecordedDateArray = new String[cursorTripJounery.getCount()];
		tripNameArray = new String[cursorTripJounery.getCount()] ;

		if (cursorTripJounery.getCount() > 0) {
			cursorTripJounery.moveToFirst();
			for (int i=0;i<cursorTripJounery.getCount();i++)
			{
				int tripJourneyId= cursorTripJounery.getColumnIndex("trip_journey_id");				
				
				int milesIndex = cursorTripJounery.getColumnIndex("miles");
				int pointsIndex = cursorTripJounery.getColumnIndex("points");
				int trip_dateIndex = cursorTripJounery.getColumnIndex("trip_date");
				int tripNameIndex = cursorTripJounery.getColumnIndex("name");

				int tripId = cursorTripJounery.getInt(tripJourneyId);
				String miles = ""+Math.round(Double.valueOf(cursorTripJounery.getString(milesIndex)));
				int points = cursorTripJounery.getInt(pointsIndex);
				long tripDate = cursorTripJounery.getLong(trip_dateIndex);
				String tripName = cursorTripJounery.getString(tripNameIndex);
				
				String formatTripDate =DateUtils.dateInString(tripDate);

				pointsArray[i] = points;
				milesArray[i] = miles + " Total Miles";
				tripRecordedDateArray[i] = formatTripDate;
				tripIdArray[i] =tripId;
				tripNameArray[i]= tripName;
				
				cursorTripJounery.moveToNext();	
			}		
					
			cursorTripJounery.close();		
	}
}
	class recentTripAdapater extends ArrayAdapter<Object> {

		Activity context;
		
		recentTripAdapater(Activity context) {
			super(context, R.layout.start_new_trip_listrow, tripNameArray);
			this.context = context;

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//Log.v("Safecell :"+"Get View","Called"+position);
			LayoutInflater inflater = context.getLayoutInflater();
			View row = inflater.inflate(R.layout.start_new_trip_listrow, null);
			
			
			 LinearLayout pointsLinearLayout = (LinearLayout)row.findViewById(R.id.StartNewTripPointsLinearLayout);
			
			TextView pointsNumberTextView = (TextView)row.findViewById(R.id.StartNewTripRowPointsNumber);
			TextView totalMilesTextView = (TextView)row.findViewById(R.id.StartNewTripRowTotalMilesTextView);
			TextView dateTextView = (TextView)row.findViewById(R.id.StartNewTripRowDateTimeTextView);
			TextView tripNameTextView = (TextView)row.findViewById(R.id.StartNewTripRowTripNameTextView);	
			TextView pointsLabelTextView = (TextView)row.findViewById(R.id.StartNewTripRowPointsText);
			
				
			
				pointsNumberTextView.setText(String.valueOf(pointsArray[position]));
				if(pointsArray[position]<0)
				{
					pointsNumberTextView.setTextColor(Color.RED);
					pointsLabelTextView.setTextColor(Color.RED);
				}
				totalMilesTextView.setText(milesArray[position]);
				dateTextView.setText(tripRecordedDateArray[position]);
				tripNameTextView.setText(tripNameArray[position]);
				
				if (!isgameplay) {
					pointsLinearLayout.setVisibility(View.GONE);
				}
			
				
			return (row);
		}

	}
	

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		StateAddress.currentActivity = this;
		isgameplay = this.GamplayOnOff();
		setListAdapter(new recentTripAdapater(MyTripsActivity.this));
	}
	
	@Override
	public void onBackPressed() {
	
	
		super.onBackPressed();
	}
}
