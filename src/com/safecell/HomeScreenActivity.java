package com.safecell;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.safecell.dataaccess.InteruptionRepository;
import com.safecell.dataaccess.ProfilesRepository;
import com.safecell.dataaccess.TempTripJourneyWayPointsRepository;
import com.safecell.dataaccess.TripJourneysRepository;
import com.safecell.dataaccess.TripRepository;
import com.safecell.model.SCProfile;
import com.safecell.utilities.DateUtils;
import com.safecell.utilities.FlurryUtils;
import com.safecell.utilities.InformatonUtils;
import com.safecell.utilities.LocationSP;
import com.safecell.utilities.StateAddress;
import com.safecell.utilities.Util;

public class HomeScreenActivity extends ListActivity {
	/** Called when the activity is first created. **/
	Button startNewTripButton, homeButton, btnMyTrips, rulesButton,
			settingsButton;
	TextView tvTotalTrips, tvGrade, tvTotalMiles;
	TextView tvUserName, tvUserLevel;
	public static TextView tvLocation;
	StringBuilder strFile;
	static String[][] pointInfo;
	ImageView profileImageView;
	Uri outputFileUri;;
	String overallTotalPoints, overallTotalMiles;
	int arrayIndex = 0;
	String tripNameArray[] = new String[] {};
	int pointsArray[] = new int[] {};
	String milesArray[] = new String[] {};
	String tripRecordedDateArray[] = new String[] {};
	private int totalTrips, totalGrade;
	static Context contextHomeScreenActivity;
	private ProfilesRepository profilesRepository;
	private boolean isgameplay;
	private StateAddress stateAddress;
	int[] tripIdArray = new int[] {};
	private TextView noTripsSavedTextView;
	SharedPreferences sharedPreferences;
	private ServiceConnection mConnection;
	
	private  Handler gpsCheckTimerHandler;
	private  Runnable gpsCheckTimerHandlerTask;
	
	private static final int GPS_CHECK_TIMER_INTERVAL = 2; //seconds
	
	static final int AUTO_SAVE_DELAY_MINUTE = 5;
	

	private LinearLayout gradeLinearLayout, pointsLinearLayout;
	
	private NotificationManager mManager;
	
	private static final int APP_ID = 0;
	
	private String TAG = HomeScreenActivity.class.getSimpleName();
    private TabControler tabControler;	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setWindowAnimations(R.anim.null_animation);
		
		contextHomeScreenActivity = HomeScreenActivity.this;
		
		mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


		//profilesRepository = new ProfilesRepository(context);
		
//		Log.v("onCreate ", "oncreate");

		//startService(new Intent(HomeScreenActivity.this, TrackingService.class));

		profilesRepository = new ProfilesRepository(contextHomeScreenActivity);


		isgameplay = this.GamplayOnOff();
		if(!InformatonUtils.isServiceRunning(this)){
		    startService();
		}
//		ServiceHandler.getInstance(this).bindService();
		
		this.initUI();
		IsTripPaused();
		IsTripSaved();
		
		Log.d(TAG, "Setings = "+getSharedPreferences("SETTINGS", MODE_WORLD_READABLE).getBoolean("isDisabled", false));
		
		sharedPreferences = getSharedPreferences("TRIP", MODE_WORLD_READABLE);

		this.recentTripLog();

		deleteFile(WayPointStore.WAY_POINT_FILE);
		deleteFile(InterruptionStore.INTERRUPTION_POINT_FILE);

		//setListAdapter(new recentTripAdapater(HomeScreenActivity.this));

		

		  if(isAppTermited()) 
			  
		  { 
			 deleteLastTempTrip();
			  Intent mIntent = new Intent(HomeScreenActivity.this,
						TrackingScreenActivity.class);
			  mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			  
			  Log.v("HomeScreenActivity", "AppTerminated");
				startActivity(mIntent);
				
				
			  finish();
		// clearTrackingScreenPref();
		 }

		/* if(!isAppTermited()) { 
			 Log.v("SafeCell: Temp data", "Delete Last Data");
			 deleteLastTempTrip();
		  }*/

		
	}
	
	public static String genereateTripUniqueID() {
		SharedPreferences preferences = TrackingService.context.getSharedPreferences("TripJouneryUID", MODE_WORLD_WRITEABLE);
		String jouneryUniqueID = preferences.getString("UniqueIdForTrip","");
		//Log.v("SafeCell : HomeScreen","jouneryUniqueID ="+jouneryUniqueID);
		return jouneryUniqueID;
	}
	
	public static void editGenereateTripUniqueID(String uniqueId){
		SharedPreferences.Editor editorUniqueID = TrackingService.context.getSharedPreferences("TripJouneryUID", MODE_WORLD_WRITEABLE).edit();
		editorUniqueID.putString("UniqueIdForTrip", uniqueId);
		editorUniqueID.commit();
		//Log.v("Safecell : --UniqueIdForTrip", "ID = "+uniqueId);
	}
	
	
	@Override
	protected void onStart() {
		super.onStart();
//		Log.v("onStart", "onstart");
		FlurryUtils.startFlurrySession(this);
		if (!isgameplay) {
			gradeLinearLayout.setVisibility(View.GONE);
		}else {
			gradeLinearLayout.setVisibility(View.VISIBLE);
			//initUI();
		}

		setListAdapter(new recentTripAdapater(HomeScreenActivity.this));
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		FlurryUtils.endFlurrySession(this);
	}

	public void IsTripSaved() {

		// SharedPreferences sharedPreferences = getSharedPreferences("TRIP",
		// MODE_PRIVATE);
		/*
		 * if(sharedPreferences.getBoolean("isTrackingCrashed", false)) {
		 * SharedPreferences.Editor editorTripCrashed =
		 * sharedPreferences.edit();
		 * editorTripCrashed.putBoolean("isTrackingCrashed", false);
		 * editorTripCrashed.commit();
		 * 
		 * Intent mIntent = new Intent(HomeScreenActivity.this,
		 * TrackingScreenActivity.class); startActivity(mIntent);
		 * 
		 * 
		 * }
		 */
		
		/*
		 if(!sharedPreferences.getBoolean("isTripSaved", true)){ 
			Intent mIntent = new Intent(HomeScreenActivity.this, AddTripActivity.class);
		 	startActivity(mIntent); 
		 }
		 */
	}

	private void IsTripPaused() {

		SharedPreferences sharedPreferences = getSharedPreferences("TRIP",
				MODE_PRIVATE);
		if (sharedPreferences.getBoolean("isTripPaused", false)) {
			Intent mIntent = new Intent(HomeScreenActivity.this,
					TrackingScreenActivity.class);
			startActivity(mIntent);
			finish();

		}
	}

	private void startService() {

		Intent mIntent = new Intent(this,
				TrackingService.class);
		startService(mIntent);

	}
	


	    
	/*public void checkProvider() {
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Location currentLocation = locationManager.getLastKnownLocation("gps");

		
		 * String provider = TrackingService.SELECTED_PROVIDER;
		 * Log.v("Safecell: Provider", "provider = "+provider); AlertDialog d;
		 * if(!"gps".equalsIgnoreCase(provider))
		 
		if (currentLocation == null) {
			Toast.makeText(HomeScreenActivity.this,
					"Not using GPS provider for location updates.",
					Toast.LENGTH_LONG).show();
			
			 * final AlertDialog.Builder b = new
			 * AlertDialog.Builder(HomeScreenActivity.this); //final AlertDialog
			 * d =new AlertDialog(this); b.setTitle("Warning");b.setMessage(
			 * "This program requires a GPS provider. As of now your device does not have GPS service. "
			 * + "	Please enable the GPS service and restart the program.");
			 * 
			 * b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			 * 
			 * @Override public void onClick(DialogInterface dialog, int which)
			 * { // /dismiss(); //HomeScreenActivity.this.finish(); } }); d =
			 * b.create(); d.show();
			 
		}
	}*/
	
	private void launchGPSOptions() {
        final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

	void deleteLastTempTrip() {

		TempTripJourneyWayPointsRepository tempTripJourneyWayPointsRepository = new TempTripJourneyWayPointsRepository(
				HomeScreenActivity.this);
		tempTripJourneyWayPointsRepository.deleteTrip();

		InteruptionRepository interuptionRepository = new InteruptionRepository(
				HomeScreenActivity.this);
		interuptionRepository.deleteInteruptions();
	}

	private void recentTripLog() {
		TripJourneysRepository tripJourneysRepository = new TripJourneysRepository(
				HomeScreenActivity.this);
		TripRepository tripRepository = new TripRepository(
				HomeScreenActivity.this);

		Cursor cursorTripJounery = tripJourneysRepository.SelectTrip_journeys();
		startManagingCursor(cursorTripJounery);
		tripJourneysRepository.SelectTrip_journeys().close();

		float totalPositivePoints = tripJourneysRepository.getPointsSum();
		// Log.v("totalPositivePoints", ""+totalPositivePoints);
		float totalSafeMilePoints = tripJourneysRepository
				.getSafeMilePointsSum();
		// Log.v("totalSafeMilePoints", ""+totalSafeMilePoints);

		float grade = 0;

		

		if (totalPositivePoints > 0) {
			grade = totalSafeMilePoints / totalPositivePoints;

		}
		grade = grade * 100;
		//Log.v("grade", "" + grade);
		int ratioInt = Math.round(grade);
		if (ratioInt < 0) {
			ratioInt = 0;
		}

		Cursor cursorTotalPointsMiles = tripJourneysRepository
				.sumOfPointsMiles();
		startManagingCursor(cursorTotalPointsMiles);
		tripJourneysRepository.sumOfPointsMiles().close();
		cursorTotalPointsMiles.moveToFirst();

		ProfilesRepository profileRepository = new ProfilesRepository(
				HomeScreenActivity.this);
		tvUserName.setText(profileRepository.getName() + "");

		if (cursorTotalPointsMiles.getCount() > 0
				&& !cursorTotalPointsMiles.isNull(0)) {
			cursorTotalPointsMiles.moveToFirst();

			overallTotalPoints = cursorTotalPointsMiles.getString(0);

			totalTrips = tripRepository.getTripCount();
			tvTotalMiles.setText("" + (int) totalSafeMilePoints);
			tvTotalTrips.setText("" + totalTrips);
			tvGrade.setText("" + ratioInt + "%");
		}
		cursorTotalPointsMiles.close();

		pointsArray = new int[cursorTripJounery.getCount()];
		milesArray = new String[cursorTripJounery.getCount()];
		tripRecordedDateArray = new String[cursorTripJounery.getCount()];
		tripIdArray = new int[cursorTripJounery.getCount()];

		if (cursorTripJounery.getCount() > 0) {
			noTripsSavedTextView.setVisibility(View.INVISIBLE);
			cursorTripJounery.moveToFirst();
			do {
				int tripIdIndex = cursorTripJounery
						.getColumnIndex("trip_journey_id");
				int milesIndex = cursorTripJounery.getColumnIndex("miles");
				int pointsIndex = cursorTripJounery.getColumnIndex("points");
				int trip_dateIndex = cursorTripJounery
						.getColumnIndex("trip_date");

				int tripId = cursorTripJounery.getInt(tripIdIndex);
				String miles = ""
						+ Math.round(Double.valueOf(cursorTripJounery
								.getString(milesIndex)));
				int points = cursorTripJounery.getInt(pointsIndex);
				long tripDate = cursorTripJounery.getLong(trip_dateIndex);

				String formatTripDate = DateUtils.dateInString(tripDate);

				pointsArray[arrayIndex] = points;
				milesArray[arrayIndex] = miles + " Total Miles";
				tripRecordedDateArray[arrayIndex] = formatTripDate;
				tripIdArray[arrayIndex] = tripId;
				arrayIndex = arrayIndex + 1;

			} while (cursorTripJounery.moveToNext());

			cursorTripJounery.close();

			tripNameArray = tripRepository.SelectTripName();

		}

	}

	boolean isAppTermited() {
		boolean isAppTermited;
		InteruptionRepository interuptionRepository = new InteruptionRepository(
				HomeScreenActivity.this);
		isAppTermited = interuptionRepository.isAppTermited();
		return isAppTermited;
	}

	public boolean GamplayOnOff() {
		SharedPreferences sharedPreferences = getSharedPreferences(
				"GamePlayCheckBox", MODE_WORLD_READABLE);
		isgameplay = sharedPreferences.getBoolean("isGameplay", true);
		return isgameplay;

	}
	
	public void checkLocationProviderStatus() {
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			return; //We have GPS do nothing
		} else if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			showGPSStatusAlert(LocationManager.NETWORK_PROVIDER);
		} else {
			showGPSStatusAlert(null);
		}
	}
	
	public void showGPSStatusAlert(String provider) {
		
		//Log.v("Safecell", "" + provider);
		
		String title = "";
		String message = "";
		
		if(LocationManager.GPS_PROVIDER.equals(provider)) {
			return;
		} 
		
		if(provider == null) {
			title = "GPS is not enabled.";
			message = "GPS is not enabled. Please enable it.";
		} else {
			title = "GPS is not enabled.";
			message = "GPS is not enabled. Please enable it. \nMeanwhile, background trip tracking will be disabled.";
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setCancelable(false);
		
		
		builder.setPositiveButton("Launch Settings", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int id) {
			launchGPSOptions();
		}
		});
		
		if(LocationManager.NETWORK_PROVIDER.equals(provider)) {
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
		}
		
		
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private void initUI() {

		setContentView(R.layout.home_screen_layout);
//		startNewTripButton = (Button) findViewById(R.id.StartNewTripLayout_StartNewButton);

		homeButton = (Button) findViewById(R.id.tabBarHomeButton);
		btnMyTrips = (Button) findViewById(R.id.tabBarMyTripsButton);
		rulesButton = (Button) findViewById(R.id.tabBarRulesButton);
		settingsButton = (Button) findViewById(R.id.tabBarSettingsButton);
		homeButton.setBackgroundResource(R.drawable.home_clicked);
		noTripsSavedTextView = (TextView) findViewById(R.id.noTripsSavedTextView);
		tvTotalTrips = (TextView) findViewById(R.id.StartNewTripTripsTextView);
		tvGrade = (TextView) findViewById(R.id.StartNewTripGradeTextView);
		tvTotalMiles = (TextView) findViewById(R.id.StartNewTripTotalMilesTextView);

		profileImageView = (ImageView) findViewById(R.id.StartNewTripProileImageView);
		tvUserName = (TextView) findViewById(R.id.HomeScreenUserName);

		tvLocation = (TextView) findViewById(R.id.tabBarCurentLocationTextView);
		tvLocation.setText(LocationSP.LocationSP);

		gradeLinearLayout = (LinearLayout) findViewById(R.id.HomeScreenGradeLinearLayout);
		setProfileImage();
		/*if (!isgameplay) {
			gradeLinearLayout.setVisibility(View.GONE);
		}else gradeLinearLayout.setVisibility(View.VISIBLE);*/

		if (overallTotalMiles == null) {
			overallTotalMiles = "0";
		}
		tvTotalMiles.setText(overallTotalMiles + "");
		tvTotalTrips.setText(totalTrips + "");
		tvGrade.setText(totalGrade + "%");

		tabControler = new TabControler(HomeScreenActivity.this);
		btnMyTrips.setOnClickListener(tabControler.getMyTripsOnClickListner());
		rulesButton.setOnClickListener(tabControler.getRulesOnClickListner());

//		startNewTripButton.setOnClickListener(startTripOnClickListener);
		profileImageView.setOnClickListener(profileImageViewOnclickListener);
	}
	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onPostResume()
	 */
	@Override
	protected void onPostResume() {
	    super.onPostResume();
	    settingsButton.setOnClickListener(tabControler
	                .getSettingOnClickListener());

	    Log.d(TAG, "ON Resume");
	}
	
	private void setProfileImage() {

		byte[] profileImage = profilesRepository.getProfileImage();
		if (profileImage != null) {

			ByteArrayInputStream imageStream = new ByteArrayInputStream(profileImage);
			Bitmap Image = BitmapFactory.decodeStream(imageStream);
			profileImageView.setImageBitmap(Image);

		}
	}

	private OnClickListener startTripOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
                TempTripJourneyWayPointsRepository tempTripJourneyWayPointsRepository = new TempTripJourneyWayPointsRepository(
                        HomeScreenActivity.this);
                InteruptionRepository ir = new InteruptionRepository(HomeScreenActivity.this);

                TrackingService.cancelTripStopTimer();
                // Log.v("Safecell",
                // "Manual Trip Start Cancelled Auto Trip Start Timer if Set");

                /** Clear Shared Preference **/
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isTripPaused", false);
                editor.putBoolean("isTripStarted", true);
                editor.commit();

                /** change unique ID for trip saving **/
                editGenereateTripUniqueID(SCProfile.newUniqueDeviceKey());
//                Intent mIntent = new Intent(getApplicationContext(), TrackingService.class);
//                getApplicationContext().stopService(mIntent);
//                getApplicationContext().startService(mIntent);
                
                ServiceHandler.getInstance(getApplicationContext()).unBind();
                ServiceHandler.getInstance(getApplicationContext()).bindService();

                // Intent callActivity = new Intent(HomeScreenActivity.this,
                // TrackingScreenActivity.class);
                // startActivity(callActivity);
//                finish();

                tempTripJourneyWayPointsRepository.deleteTrip();
                ir.deleteInteruptions();
            }
	};

	private OnClickListener profileImageViewOnclickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			dialogMessage();

		}
	};// Library

	private void dialogMessage() {
		Dialog dialog = new AlertDialog.Builder(HomeScreenActivity.this)
				.setMessage("Select Profile Picture").setPositiveButton(
						"Photo Library", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								Intent intent = new Intent();
								intent.setType("image/*");
								intent.setAction(Intent.ACTION_GET_CONTENT);
								startActivityForResult(intent, 1);

							}
						}).setNegativeButton("New Photo",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								Intent nintent = new Intent(
										MediaStore.ACTION_IMAGE_CAPTURE);
								File file = new File(Environment
										.getExternalStorageDirectory(), String
										.valueOf(System.currentTimeMillis())
										+ ".jpg");

								outputFileUri = Uri.fromFile(file);
								nintent.putExtra(MediaStore.EXTRA_OUTPUT,
										outputFileUri);
								startActivityForResult(nintent, 2);
							}
						}).create();
		dialog.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 1) {

			if (resultCode == Activity.RESULT_OK) {

				Uri selectedImage = data.getData();
				//Log.v("Safecell :" + "selectedImage", "imagePath = "
//						+ selectedImage);
				profileImageView.setImageBitmap(getImageFromURI(selectedImage));

			}
		}// //End Request code = 1

		if (requestCode == 2) {
			if (resultCode == -1) {

				Uri selectedImage = Uri.parse(outputFileUri.getPath());
//				Log.v("Safecell :" + "selectedImage", "imagePath = "
//						+ selectedImage);
				profileImageView.setImageBitmap(getImageFromURI(selectedImage));

			}
		}

	}// end on result

	Bitmap getImageFromURI(Uri uri) {
		Bitmap resizedBitmap = null;
		String abc = null;
		if (uri != null) {
			String str = uri.toString();
			abc = str.substring(0, 1);
			//Log.v("Safecell :" + "abc", str);
		}

		if (uri != null && abc.equalsIgnoreCase("c")) {
			Uri selectedImage = uri;
			//Log.v("Safecell :" + "Uri", selectedImage.toString());

			String[] proj = { MediaColumns.DATA };
			Cursor cursor = managedQuery(selectedImage, proj, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
			cursor.moveToFirst();

			String path = cursor.getString(column_index);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 4;
			resizedBitmap = BitmapFactory.decodeFile(path, options);
			imageStoreInDatabase(resizedBitmap);
			
			cursor.close();

			return resizedBitmap;
		} else if (uri != null && abc.equalsIgnoreCase("/")) {

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 4;
			resizedBitmap = BitmapFactory.decodeFile(uri.getPath(), options);
			imageStoreInDatabase(resizedBitmap);
			return resizedBitmap;

		}
		return resizedBitmap;

	}

	public void imageStoreInDatabase(Bitmap imageBitmap) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		byte[] b = baos.toByteArray();
		profilesRepository.updateProfileImage(b);

	}
	
	/*
	private void cancelGPSCheckTimer() {
		if (gpsCheckTimerHandler != null) {
			gpsCheckTimerHandler.removeCallbacks(gpsCheckTimerHandlerTask);
			gpsCheckTimerHandler = null;
			Log.v("Safecell", "**GPSCheckTimer cancelled");
		}
	}
	
	private void createGPSCheckTimer() {
		cancelGPSCheckTimer();
		gpsCheckTimerHandler = new Handler();
		gpsCheckTimerHandler.postDelayed(gpsCheckTimerHandlerTask, GPS_CHECK_TIMER_INTERVAL * 1000);
		Log.v("Safecell", "**GPSCheckTimer started");
	}
	
	private void setGPSCheckTimer() {
		gpsCheckTimerHandlerTask = new Runnable() {
			public void run() {
				
				if(TrackingService.context == null) {
					Log.v("Safecell", "**static_this is null : GPSCheckTimer will check again");
					createGPSCheckTimer();
					return;
				}
				
				cancelGPSCheckTimer();
				
				showGPSStatusAlert(TrackingService.SELECTED_PROVIDER);
			}
		};

		if (gpsCheckTimerHandler != null) {
			cancelGPSCheckTimer();
		}
		
		if(TrackingService.context == null) {
			createGPSCheckTimer();
		} else {
			Log.v("Safecell", "Traking service ready. No Timer.");
			showGPSStatusAlert(TrackingService.SELECTED_PROVIDER);
		}
	}
	*/

	class recentTripAdapater extends ArrayAdapter<Object> {

		Activity context;

		recentTripAdapater(Activity context) {
			super(context, R.layout.start_new_trip_listrow, milesArray);
			this.context = context;

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			LayoutInflater inflater = context.getLayoutInflater();
			View row = inflater.inflate(R.layout.start_new_trip_listrow, null);
			TextView pointsNumberTextView = (TextView) row
					.findViewById(R.id.StartNewTripRowPointsNumber);
			TextView totalMilesTextView = (TextView) row
					.findViewById(R.id.StartNewTripRowTotalMilesTextView);
			TextView dateTextView = (TextView) row
					.findViewById(R.id.StartNewTripRowDateTimeTextView);
			TextView tripNameTextView = (TextView) row
					.findViewById(R.id.StartNewTripRowTripNameTextView);
			TextView pointsLabelTextView = (TextView) row
					.findViewById(R.id.StartNewTripRowPointsText);

			pointsLinearLayout = (LinearLayout) row
					.findViewById(R.id.StartNewTripPointsLinearLayout);

			if (!isgameplay) {
				pointsLinearLayout.setVisibility(View.GONE);

			}
			switch (position) {
			case 0:
				pointsNumberTextView.setText(String
						.valueOf(pointsArray[position]));
				if (pointsArray[position] < 0) {
					pointsNumberTextView.setTextColor(Color.RED);
					pointsLabelTextView.setTextColor(Color.RED);
				}
				totalMilesTextView.setText(milesArray[position]);
				dateTextView.setText(tripRecordedDateArray[position]);
				tripNameTextView.setText(tripNameArray[position]);
				break;
			case 1:

				pointsNumberTextView.setText(String
						.valueOf(pointsArray[position]));
				if (pointsArray[position] < 0) {
					pointsNumberTextView.setTextColor(Color.RED);
					pointsLabelTextView.setTextColor(Color.RED);
				}
				totalMilesTextView.setText(milesArray[position]);
				dateTextView.setText(tripRecordedDateArray[position]);
				tripNameTextView.setText(tripNameArray[position]);
				break;
			case 2:

				pointsNumberTextView.setText(String
						.valueOf(pointsArray[position]));
				if (pointsArray[position] < 0) {
					pointsNumberTextView.setTextColor(Color.RED);
					pointsLabelTextView.setTextColor(Color.RED);
				}
				totalMilesTextView.setText(milesArray[position]);
				dateTextView.setText(tripRecordedDateArray[position]);
				tripNameTextView.setText(tripNameArray[position]);
				break;
			case 3:

				pointsNumberTextView.setText(String
						.valueOf(pointsArray[position]));
				if (pointsArray[position] < 0) {
					pointsNumberTextView.setTextColor(Color.RED);
					pointsLabelTextView.setTextColor(Color.RED);
				}
				totalMilesTextView.setText(milesArray[position]);
				dateTextView.setText(tripRecordedDateArray[position]);
				tripNameTextView.setText(tripNameArray[position]);
				break;
			case 4:

				pointsNumberTextView.setText(String
						.valueOf(pointsArray[position]));
				if (pointsArray[position] < 0) {
					pointsNumberTextView.setTextColor(Color.RED);
					pointsLabelTextView.setTextColor(Color.RED);
				}
				totalMilesTextView.setText(milesArray[position]);
				dateTextView.setText(tripRecordedDateArray[position]);

				break;

			}

			return (row);
		}

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		int TripId = tripIdArray[position];

		Intent mIntent = new Intent(HomeScreenActivity.this,
				MyTripDiscriptionActivity.class);
		mIntent.putExtra("TripId", TripId);
		mIntent.putExtra("CallingActivity", "HomeScreenActivity");
		startActivityForResult(mIntent, 17);

		super.onListItemClick(l, v, position, id);
	}

//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
//					HomeScreenActivity.this);
//			dialogBuilder.setMessage("Are you sure you want to exit?")
//					.setCancelable(false).setPositiveButton("Yes",
//							new DialogInterface.OnClickListener() {
//								public void onClick(DialogInterface dialog,
//										int id) {
//
////									SharedPreferences sharedPreferences = getSharedPreferences(
////											"TRIP", MODE_WORLD_READABLE);
////									SharedPreferences.Editor editor = sharedPreferences
////											.edit();
////									editor.putBoolean("isTripStarted", false);
////									editor.commit();
////
////									TempTripJourneyWayPointsRepository tempTripJourneyWayPointsRepository = new TempTripJourneyWayPointsRepository(
////											HomeScreenActivity.this);
////									tempTripJourneyWayPointsRepository
////											.deleteTrip();
//
//									HomeScreenActivity.this.finish();
//								}
//							}).setNegativeButton("No",
//							new DialogInterface.OnClickListener() {
//								public void onClick(DialogInterface dialog,
//										int id) {
//									dialog.cancel();
//								}
//							});
//			if (!dialogBuilder.create().isShowing())
//				dialogBuilder.create().show();
//			return false;
//		}
//		return super.onKeyDown(keyCode, event);
//	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		// LocationService.setAct = this;
		super.onResume();
		StateAddress.currentActivity = this;
		
		TrackingService.selectBestLocationProvider();
		
		checkLocationProviderStatus();
		
		TrackingService.homeScreenActivity = HomeScreenActivity.this;
		// this.recentTripLog();
		// setListAdapter(new recentTripAdapater(HomeScreenActivity.this));
		// setListAdapter(new recentTripAdapater(HomeScreenActivity.this));
		isgameplay = this.GamplayOnOff();
		if (!isgameplay && (gradeLinearLayout.getVisibility() == View.VISIBLE)) {
			gradeLinearLayout.setVisibility(View.GONE);
		}
		
		TrackingService.ignoreLocationUpdates = false;
		//Log.v("Safecell", "startIgnoringLocationUpdates = false");
		

	}

	@Override
	protected void onDestroy() {
		TrackingService.homeScreenActivity = null;
		// TODO Auto-generated method stub
		super.onDestroy();
//		ServiceHandler.getInstance(this).unBind();
	}

	
	public void showNotification(String msg) {
		Notification notification = new Notification(R.drawable.launch_icon, "Notify", System.currentTimeMillis());
		notification.setLatestEventInfo(HomeScreenActivity.this, "SafeCell", msg, PendingIntent.getActivity(HomeScreenActivity.this.getBaseContext(), 0, null, PendingIntent.FLAG_CANCEL_CURRENT));
		mManager.notify(APP_ID, notification);
	}

	

}// end

