package com.safecell;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.safecell.dataaccess.JourneyEventsRepository;
import com.safecell.dataaccess.TripJourneysRepository;
import com.safecell.utilities.DateUtils;
import com.safecell.utilities.FlurryUtils;
import com.safecell.utilities.LocationSP;
import com.safecell.utilities.StateAddress;

public class MyTripDiscriptionActivity extends Activity {
	TextView tvTotalPoints, tvPenalty, tvGrade, tvTotalMiles;
	private Button homeButton,btnMyTrips,rulesButton,settingsButton;
	TextView tvDate;
	ListView lvTripLog;
	int tripId=0;
	String TAG = "MyTripDiscriptionActivity";
	//String[] TripLogArray={};
	ArrayList<String> TripLogArray = new ArrayList<String>();
	ArrayList<Integer> pointsArray = new ArrayList<Integer>();
	//int[] pointsArray;
	int TotalPoints = 0;
	int TotalMiles = 0;
	String tripDate="";
	Bitmap bmSafe;
	Bitmap bmInteruption;
	int penalyPoints=0;
	int totalPositivePoints=0;
	int grade = 0;
	private LinearLayout penalityLinearLayout,tripsPointsLinearLayout,gradeLinearLayout;
	private boolean isgameplay;
	private  int listAdapterSize = 0;
	public static TextView tvLocation;

	String callingActivity="";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setWindowAnimations(R.anim.null_animation);
		
		Intent mIntent = getIntent();
		tripId = mIntent.getIntExtra("TripId", 0);
		//Log.v("Safecell :"+TAG ,"Trip ID= "+tripId);
		
		callingActivity = mIntent.getStringExtra("CallingActivity");
		
		isgameplay = this.GamplayOnOff();
		InitUi();
		fetchData();
		
		lvTripLog.setAdapter(new tripLogAdapter(MyTripDiscriptionActivity.this));
		bmSafe = BitmapFactory.decodeResource(getResources(), R.drawable.image_interruption);
		bmInteruption= BitmapFactory.decodeResource(getResources(), R.drawable.image_safe);
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
	
	
	void fetchData(){
		
		//Log.v("Safecell :"+TAG,"Fetch Data");
		
		JourneyEventsRepository journeyEventsRepository = new JourneyEventsRepository(MyTripDiscriptionActivity.this);
		Cursor cursor = journeyEventsRepository.getJourneyEventsById(tripId);		
		
		if(cursor.getCount()>0)
		{
			cursor.moveToFirst();
			/*TripLogArray = new String[cursor.getCount()];
			pointsArray = new int[cursor.getCount()];	*/		
			
			for(int i=0; i<cursor.getCount(); i++)
			{
				int tripPoints = cursor.getInt(cursor.getColumnIndex("points"));
				String desc = cursor.getString(cursor.getColumnIndex("description"));
				/*Log.v("tripPoints", "tripPoints :" +tripPoints);
				if (tripPoints == 0) {
					cursor.moveToNext();
					continue;
				}
				*/if (tripPoints !=0) {
					
				/*	TripLogArray[i] = desc;
					pointsArray[i] = tripPoints;*/
					TripLogArray.add(desc);
					pointsArray.add(tripPoints);
					listAdapterSize +=1;
//					Log.v("AFTER continue", tripPoints+"TripPOints");
					//Log.v("TripLogArray", TripLogArray.get(i));
				}
				
				cursor.moveToNext();
			}
		}
		cursor.close();
		
		//Log.v("Safecell :"+"MyTripDiscription","TripLogCount= "+TripLogArray.length);
		TripJourneysRepository tripJourneysRepository = new TripJourneysRepository(MyTripDiscriptionActivity.this);
		Cursor tripCursor = tripJourneysRepository.SelectTripById(tripId);
		startManagingCursor(tripCursor);
		
		penalyPoints = journeyEventsRepository.getPenaltyByID(tripId);
		totalPositivePoints = journeyEventsRepository.getTotalPositivePointsById(tripId);
		grade = gradeCalculation(totalPositivePoints, penalyPoints);
		
		if(tripCursor.getCount()>0)
		{
			//Log.v("Safecell :"+TAG,"Trip Cursor Count="+tripCursor.getCount());
			tripCursor.moveToFirst();
			TotalMiles = (int) Math.round(tripCursor.getDouble(tripCursor.getColumnIndex("miles")));
			TotalPoints = tripCursor.getInt(tripCursor.getColumnIndex("points"));	
			
			tripDate = DateUtils.dateInString(Long.valueOf(tripCursor.getString(tripCursor.getColumnIndex("trip_date"))));
			
		}
		tripCursor.close();
		
		tvTotalMiles.setText(TotalMiles+"");
		tvTotalPoints.setText(""+TotalPoints);
		tvPenalty.setText(penalyPoints+"");
		tvDate.setText("TRIP ON "+tripDate);
		tvGrade.setText(grade +" %");
		
	}
	private int gradeCalculation(float totalPositivePoints, float penaltyPoints) {
		
		float safetyPoints  = totalPositivePoints + penaltyPoints;
		
		if (safetyPoints <= 0 || totalPositivePoints <= 0) {
			return 0;
		}
		float ratio = (safetyPoints/ totalPositivePoints);
		ratio = ratio* 100;
		int ratioInt = (int)Math.round(ratio);
		
		return ratioInt;
	}
	public boolean GamplayOnOff()
	{
		 SharedPreferences sharedPreferences = getSharedPreferences("GamePlayCheckBox",MODE_WORLD_READABLE);
	     isgameplay = sharedPreferences.getBoolean("isGameplay", true);
	     return isgameplay;
		
	}
	public void InitUi(){		
		setContentView(R.layout.my_trips_description_layour);	
		homeButton = (Button) findViewById(R.id.tabBarHomeButton);
		btnMyTrips = (Button) findViewById(R.id.tabBarMyTripsButton);
		rulesButton = (Button) findViewById(R.id.tabBarRulesButton);
		settingsButton = (Button) findViewById(R.id.tabBarSettingsButton);
		btnMyTrips.setBackgroundResource(R.drawable.mytrips_clicked);
		tvTotalPoints =(TextView)findViewById(R.id.MyTripDescriptionTripPointsTextView);
		tvGrade =(TextView)findViewById(R.id.MyTripDescriptionGradeTextView);
		tvPenalty =(TextView)findViewById(R.id.MyTripDescriptionPenaltyTextView);
		tvTotalMiles = (TextView) findViewById(R.id.MyTripDescriptionTotalMilesTextView);
		tvLocation=(TextView) findViewById(R.id.tabBarCurentLocationTextView);
		tvLocation.setText(LocationSP.LocationSP);
			
		tvDate = (TextView)findViewById(R.id.MyTripDescriptionDateTextView);		
		lvTripLog = (ListView) findViewById(R.id.MyTripDescTripLogListView);
		
		tvGrade.setText("100%");
		tvPenalty.setText("0");
		
		TabControler tabControler =new TabControler(MyTripDiscriptionActivity.this);
		btnMyTrips.setOnClickListener(tabControler.getMyTripsOnClickListner());
		homeButton.setOnClickListener(tabControler.getHomeTabOnClickListner());
		rulesButton.setOnClickListener(tabControler.getRulesOnClickListner());
		settingsButton.setOnClickListener(tabControler.getSettingOnClickListener());
		penalityLinearLayout = (LinearLayout)findViewById(R.id.MyTripDescriptionPenaltyLinearLayout);
		tripsPointsLinearLayout = (LinearLayout)findViewById(R.id.MyTripDescriptionTripsPointsLinearLayout);
		gradeLinearLayout = (LinearLayout)findViewById(R.id.MyTripDescriptionGradeLinearLayout); 
	
		if (!isgameplay) {
			penalityLinearLayout.setVisibility(View.GONE);
			tripsPointsLinearLayout.setVisibility(View.GONE);
			gradeLinearLayout.setVisibility(View.GONE);
		}
	}
	
	class tripLogAdapter extends BaseAdapter{
		Activity context;
		public tripLogAdapter(Activity context) {
		
			this.context =context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
		
			LayoutInflater inflater = context.getLayoutInflater();
			View row = inflater.inflate(R.layout.trip_history_tirp_log_row, null);
			
			LinearLayout pointsLineraLinearLayout = (LinearLayout)row.findViewById(R.id.TripHistoryTripLogRowPointsBackground);
			TextView tvTripLog 			=(TextView) row.findViewById(R.id.TripHistoryTripLogRowTripLogTextView);
			TextView textViewPoints  	=(TextView) row.findViewById(R.id.TripHistoryTripLogRowPoints1TextView);
			ImageView iv =(ImageView) row.findViewById(R.id.TripHistoryTripLogRowImageView);
			TextView pointsLabelTextView = (TextView)row.findViewById(R.id.TripHistoryTripLogRowPointsTextView);
			
			tvTripLog.setText(TripLogArray.get(position));
			textViewPoints.setText(pointsArray.get(position)+"");
			
			if (!isgameplay) {
				pointsLineraLinearLayout.setVisibility(View.GONE);
			}
			if(pointsArray.get(position)<0)
			{
				textViewPoints.setTextColor(Color.RED);
				pointsLabelTextView.setTextColor(Color.RED);
			}
			if(pointsArray.get(position)<0)
			{
				iv.setImageBitmap(bmInteruption);
				row.setBackgroundColor(Color.GRAY);
				//ll.setBackgroundColor(Color.GRAY);
			}
			else
			{
				iv.setImageBitmap(bmSafe);
			}
			
			return row;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return listAdapterSize;
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
		
		
	}
	@Override
	public void onBackPressed() {
		
		if(callingActivity == null) {
			callingActivity = "";
		}
		
		if(callingActivity.equals("HomeScreenActivity"))
		{
			if(TrackingService.homeScreenActivity !=null)
			{
				TrackingService.homeScreenActivity.finish();
			}
			
			Intent mIntent = new Intent(MyTripDiscriptionActivity.this,HomeScreenActivity.class);
			startActivity(mIntent);
			finish();
			
		}
		else{
		Intent mIntent = new Intent(MyTripDiscriptionActivity.this,MyTripsActivity.class);
		startActivity(mIntent);
		finish();
		}
		super.onBackPressed();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		StateAddress.currentActivity = this;
	}

}
