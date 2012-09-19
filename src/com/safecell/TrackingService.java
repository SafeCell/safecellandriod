package com.safecell;

import com.safecell.dataaccess.AccountRepository;
import com.safecell.dataaccess.InteruptionRepository;
import com.safecell.dataaccess.ProfilesRepository;
import com.safecell.dataaccess.SMSRepository;
import com.safecell.dataaccess.TempTripJourneyWayPointsRepository;
import com.safecell.model.SCProfile;
import com.safecell.model.SCSms;
import com.safecell.model.SCWayPoint;
import com.safecell.networking.ConfigurationHandler;
import com.safecell.networking.NetWork_Information;
import com.safecell.networking.SubmitNewTripJourney;
import com.safecell.networking.SubmitNewTripJourneyResponceHandler;
import com.safecell.receiver.LockReceiver;
import com.safecell.utilities.DateUtils;
import com.safecell.utilities.InformatonUtils;
import com.safecell.utilities.LocationSP;
import com.safecell.utilities.StateAddress;
import com.safecell.utilities.Util;

import org.apache.http.HttpResponse;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.GpsStatus.NmeaListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class TrackingService extends Service implements LocationListener, NmeaListener {
	
	public static final String GPS_PROVIDER_DISPLAY_TEXT = "GPS Satellite";
	public static final String NETWORK_PROVIDER_DISPLAY_TEXT = "Network";
	public static final String UNAVAILABLE_PROVIDER_DISPLAY_TEXT = "Unavailable";
	public static ArrayList<String> incomingNumberArrayList = new ArrayList<String>();
	public static final double WAYPOINT_TIME_CUT_OFF = 0.000833;
	static String TAG = TrackingService.class.getSimpleName();
	static TrackingService context;
	public static Location LOCATION_FOR_RULE = null;
	public static final int RESPONSE_NULL_WHILE_SAVING = 1;
	public static final int EXCEPTION_WHILE_SAVING = 2;
	public static String SELECTED_PROVIDER = null;
	private static int LOCATION_UPDATE_TIME_INTERVAL = 0; // 0 SEC
	private static int LOCATION_UPDATE_DISTANCE_INTERVAL = 2; // 2 METERS
	private static Double latitude = 0.0, longitude = 0.0;
	private static Location currentLocation;
	static boolean isTripDeleted;
	private TempTripJourneyWayPointsRepository tempTripJourneyWayPointsRepository;
	double speed = 0.0;
	private float lastDistanceInMiles = 0;
	private static int TRIP_CUT_OFF = 5;
	public static TrackingScreenActivity trackingScreenActivity = null;
	public static Context trackingScreenContext = null;
	private ArrayList<SCSms> smsArrayList = new ArrayList<SCSms>();
	private SMSRepository smsRepository;
	static boolean isTempTripDeleted = false;
	public static HomeScreenActivity homeScreenActivity;
	public static AddTripActivity addTripActivity = null;
	private boolean resultFlag;
	private ConfigurationRunnable configRunnable;
	
	public static boolean isTripRunning = false;
	private static final int IGNORE_WAYPOINT_SPEED_LIMIT = 150;
	SharedPreferences sharedPreferences;
	Handler handler;
	
	
	private static Handler handlerTimerTask;
	private static Runnable timerTaskRunner;
	InteruptionRepository ir;

	private static final int AUTO_TRIP_START_SPEED_MONITOR_PERIOD = 30; // In
	// seconds
	private static final int AUTO_TRIP_START_REQUIRED_MIN_UPDATE_INTERVAL = 6; // In
	// seconds
	private static final int WAYPOINT_FILTER_TIME = 3; // In seconds use -1 to
	// turn this filtering
	// off

	private static long lastUpdateReceivedOn = 0;

	private static Handler autoStartTripTimer;
	private static Runnable autoStarTripTimerRunner;

	static int AUTO_SAVE_DELAY_MINUTE = 5;

	private int show_dialog_counter = 0;

	public static double currentPDOP = -1;
	public static double currentHDOP = -1;
	public double currentVDOP;
	private static final String NMEA_DOP_SENTENCE = "$GPGSA";
	public static String gpsSource = UNAVAILABLE_PROVIDER_DISPLAY_TEXT;

	LocationSP locationSP;
	StateAddress stateAddress;

	public static boolean ignoreLocationUpdates = false;
	
	private static LocationManager locationManager;
	
	private LockReceiver receiverScreenOff;
	
	private OutgoingSMSDetector mSmsDetector;
	
	private OutgoingWebDetector mWebDetector;
	
	private OutgoingEmailDetector mEmailDetector;
	
	@Override
	public IBinder onBind(Intent intent) {
//	    Log.d(TAG, "Bind Service Called");
//	     // starting the tracking service
//        //ConfigurationHandler.getInstance().readResponse();
//        AUTO_SAVE_DELAY_MINUTE = ConfigurationHandler.getInstance().getConfiguration().getTripStopTime();
//        TrackingService.ignoreLocationUpdates = false;
//        //setting the auto timer always on
//        SharedPreferences preferences = getSharedPreferences("TripCheckBox", MODE_WORLD_WRITEABLE);
//        SharedPreferences.Editor editor= preferences.edit();
//        editor.putBoolean("isbackgroundtrip", true);
//        editor.commit();
		return null;
	}

	@Override
	public void onCreate() {
//		Log.v(TAG, "OnCreate");
		context = TrackingService.this;
		configureLocationManager();
				
		smsRepository = new SMSRepository(context);
		

		handler = new Handler(Looper.getMainLooper());
		ir = new InteruptionRepository(TrackingService.this);
		handlerTimerTask = new Handler();
		
		HomeScreenActivity.genereateTripUniqueID();
		
		addIntentFilterForScreenOff();
		

	}
	
	private void addIntentFilterForScreenOff(){
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        receiverScreenOff = new LockReceiver();
        registerReceiver(receiverScreenOff, filter);
	}

	public static void deleteLastInterruption(){
		if(context == null) {
//			Log.v("Safecell", "deleteLastInterruption falied : Tracking service static context is null");
			return;
		}
		
		InteruptionRepository interuptionRepository = new InteruptionRepository(context);
		interuptionRepository.deleteLastInterruption();
	}

	@Override
	public void onDestroy() {

		super.onDestroy();
		unregisterReceiver(receiverScreenOff);
		stopForeground(true);
		locationManager.removeUpdates(this);
		locationManager.removeNmeaListener(this);
		if(mSmsDetector != null){
		    mSmsDetector.stop();
		}
		if(mWebDetector != null){
		    mWebDetector.stop();
		}
		if(mEmailDetector != null){
		    mEmailDetector.stop();
		}
		
	}
 
	@Override
	public void onStart(Intent intent, int startId) {
		sharedPreferences = getSharedPreferences("TRIP", MODE_PRIVATE);
		tempTripJourneyWayPointsRepository = new TempTripJourneyWayPointsRepository(TrackingService.this);
		context = TrackingService.this;
		AddTripActivity.trackingService = TrackingService.this;
		Log.d(TAG, "On Start called");
		if (currentLocation != null) {
			//Log.v("Tracking service", "curren Location is not Null");
			latitude = currentLocation.getLatitude();
			longitude = currentLocation.getLongitude();

			LOCATION_FOR_RULE = currentLocation;

			locationSP = new LocationSP(currentLocation, TrackingService.this);
			if (locationSP != null) {
				locationSP.setAddressLine();
				stateAddress = new StateAddress();
				stateAddress.setTextLocation();
			}
		}
		
		// sms detector initialization
		mSmsDetector = new OutgoingSMSDetector(new Handler(), getApplicationContext());
		mWebDetector = new OutgoingWebDetector(new Handler(), getApplicationContext());
		mEmailDetector = new OutgoingEmailDetector(new Handler(), getApplicationContext());
		
		
		startTrip();

		super.onStart(intent, startId);
	}
	
	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	     super.onStartCommand(intent, flags, startId);
	     foregroundService();
	     return START_STICKY;
	}
	
	
	/**
     * 
     */
    private void foregroundService() {
        CharSequence text = getText(R.string.safe_cell);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.launch_icon, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, HomeScreenActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.safe_cell),
                       text, contentIntent);

        startForeground(R.string.safe_cell, notification);
    }

    private void autoStartTrip() {
        Log.d(TAG, "Auto Start Trip Started ");
//        Toast.makeText(getApplicationContext(), "Auto Start Trip Started", Toast.LENGTH_LONG).show();
		tempTripJourneyWayPointsRepository.deleteTrip();
		ir.deleteInteruptions();

		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean("isTripStarted", true);
		editor.commit();
		
		
		/** change unique ID for trip saving **/
		if (HomeScreenActivity.genereateTripUniqueID().equalsIgnoreCase("")) {
			HomeScreenActivity.editGenereateTripUniqueID(SCProfile.newUniqueDeviceKey());
		}
		
		//start the sms observer
		if(mSmsDetector != null){
		    mSmsDetector.start();
		}
		if(mWebDetector != null){
		    mWebDetector.start();
		}
		if(mEmailDetector != null){
		    mEmailDetector.start();
		}
		
		// disable Mobile network
		Util.setMobileDataEnabled(context, false);
//		Log.v("Safecell", "**autoStartTrip");
	}

	private static void cancelAutoTripStartTimer() {
		if (autoStartTripTimer != null) {
			autoStartTripTimer.removeCallbacks(autoStarTripTimerRunner);
			autoStartTripTimer = null;
//			Log.v("Safecell", "**Trip Autostart cancelled");
		}
	}

	private void setAutoStartTripTimer() {
		autoStarTripTimerRunner = new Runnable() {
			public void run() {
				if (lastUpdateReceivedOn == 0) {
//					Log.v("safecell", "WARNING: lastUpdateReceivedOn was zero. This should never reach here.");
					autoStartTripTimer = null;
					return;
				} else {

					long currentTime = new Date().getTime();

					if ((currentTime - lastUpdateReceivedOn) >= (AUTO_TRIP_START_REQUIRED_MIN_UPDATE_INTERVAL * 1000)) {

//						Log.v("safecell", "Auto Trip Start Timer fired but trip was not started: last update time too big.");

						autoStartTripTimer = null;
						return;
					} else if(!tempTripJourneyWayPointsRepository.isAvarageTimeDiffFeasible()) {
//						Log.v("safecell", "Auto Trip Start Timer fired but trip was not started: isAvarageTimeDiffFeasible is false.");
						autoStartTripTimer = null;
						return;
					}
				}
				autoStartTrip();
//				Log.v("Safecell", "Auto Trip Satrted");
				autoStartTripTimer = null;
			}

		};

		if (autoStartTripTimer != null) {
			cancelAutoTripStartTimer();
		}

		autoStartTripTimer = new Handler();
		autoStartTripTimer.postDelayed(autoStarTripTimerRunner, AUTO_TRIP_START_SPEED_MONITOR_PERIOD * 1000);
//		Log.v("Safecell", "**Trip Autostart Monitering started");
	}

	/** Set timer to auto stop trip **/

	private void setAutostopTripTimer() {
	    Log.d(TAG, "Auto time stop ");
		timerTaskRunner = new Runnable() {
			public void run() {
				try {
//					Log.v("Safecell", "Auto Save Trip Called");
					saveTrip(getApplicationContext());
				} catch (Exception e) {
					e.printStackTrace();
				}
				handlerTimerTask = null;
			}
		};

		if (handlerTimerTask != null) {
			handlerTimerTask.removeCallbacks(timerTaskRunner);
			handlerTimerTask = null;
//			Log.v("Safecell", "**Auostop Timer Canceled");
		}
		handlerTimerTask = new Handler();
		handlerTimerTask.postDelayed(timerTaskRunner, AUTO_SAVE_DELAY_MINUTE * 60 * 1000 - 2*1000);
//		Log.v("Tracking Service", "**Timer Started");

	}
	

    public void waiting(int n) {
        long t0, t1;
        t0 = System.currentTimeMillis();
        do {
            t1 = System.currentTimeMillis();
        } while (t1 - t0 < n);
    }
	

	public static void cancelTripStopTimer() {
		if (handlerTimerTask != null) {
			handlerTimerTask.removeCallbacks(timerTaskRunner);
			handlerTimerTask = null;
//			Log.v("Tracking Service", "**Timer Cancel");
		}
	}
	
	public void saveTripAsyncTask(final Context frontScreenActivity) {

		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean("isTripPaused", false);
		editor.putBoolean("isTripStarted", false);
		editor.commit();

		String apiKey = "";
		int accountID = 0;
		int profileID = 0;
		String tripName = "";
		tripName = "Trip On " + getTodaysDate();
		int totalMiles = getTotalDistance();
		HashMap<Object, Object> ApikeyAndAccountId = new HashMap<Object, Object>();

		AccountRepository accountRepository = new AccountRepository(TrackingService.this);
		ApikeyAndAccountId = accountRepository.selectApiKeyAndAccountID();
		apiKey = (String) ApikeyAndAccountId.get("ApiKey");
		accountID = Integer.valueOf(ApikeyAndAccountId.get("AccountId").toString());

		ProfilesRepository profilesRepository = new ProfilesRepository(TrackingService.this);
		profileID = profilesRepository.getId();

		TempTripJourneyWayPointsRepository tempWayPointRepo = new TempTripJourneyWayPointsRepository(TrackingService.this);
		InteruptionRepository interuptionRepository = new InteruptionRepository(TrackingService.this);
		
		SCProfile currentUser = profilesRepository.getCurrentProfile();
		String name = currentUser.getFirstName() +  " " + currentUser.getLastName();
		
		int TotalDistatnce = (int) tempWayPointRepo.getTotalDistance();

		if (!(TotalDistatnce < 1) && ConfigurationHandler.getInstance().getConfiguration().isLogWayPoints()) {
			TrackingScreenActivity.isTripSavingInProgress = true;

			insertIntoSMS();
			
			if(LockReceiver.wasLoacked) {
				TrackingService.deleteLastInterruption();
//				Log.v("SafeCell: ", "LockReceiver.wasLoacked : Yes - Deleted the last interruption before save trip");
			}
			
			String firstInterruptionTime = interuptionRepository.firstInterruptionStartTime();
			long tripStartTime = tempWayPointRepo.tripStartTime();
			
					
			if (firstInterruptionTime != null) {
				
				String tripStartTimeStr = DateUtils.getTimeStamp(tripStartTime);
				
				if(firstInterruptionTime.equalsIgnoreCase(tripStartTimeStr)){
					interuptionRepository.deleteFirstInterruption();
				}
			}
			
			

			SubmitNewTripJourney submitNewTripJourney = new SubmitNewTripJourney(getApplicationContext(), accountID, profileID, apiKey, tripName, totalMiles, name);

			submitNewTripJourney.createJson();
			HttpResponse httpResponse = submitNewTripJourney.postRequest();
			if (httpResponse == null) {

				// showNotification("Unexepted error occure while saving the trip.");
				//tripNotSaveDialog(frontScreenActivity, RESPONSE_NULL_WHILE_SAVING);
				resultFlag = false;

			} else {
				SubmitNewTripJourneyResponceHandler submitNewTripJourneyResponceHandler = new SubmitNewTripJourneyResponceHandler(TrackingService.this);
				try {
					submitNewTripJourneyResponceHandler.readResponce(httpResponse);
					resultFlag = true;
					tempTripJourneyWayPointsRepository.deleteTrip();
					TrackingScreenActivity.isTripSavingInProgress = false;
//					Toast.makeText(getApplicationContext(), "Trip Saved", Toast.LENGTH_LONG).show();
					Log.d(TAG, "Trip Saved ");
				} catch (Exception e) {
					resultFlag = false;
					e.printStackTrace();
				}
				
				

			}

		} else // when trip is small and timer fires
		{
			resultFlag = true;
			// showNotification("Auto Save: Trip is small to save");
			SharedPreferences.Editor editor1 = sharedPreferences.edit();
			editor1.putBoolean("isTripPaused", false);
			editor1.putBoolean("isTripStarted", false);
			editor1.commit();
		}
		

	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		boolean result = false;
		result = cm.getActiveNetworkInfo().isAvailable();
//		Log.v("result", "result =" + result);
		return result;

	}

	// @SuppressWarnings("unchecked")
	public void saveTrip(final Context context) {
	    TrackingService.ignoreLocationUpdates = true;
	    if(mSmsDetector != null){
	        mSmsDetector.stop();
	    }
	    if(mWebDetector != null){
	        mWebDetector.stop();
	    }
	    if(mEmailDetector != null){
	        mEmailDetector.stop();
	    }
        removeLocationUpdates();
	    Util.setMobileDataEnabled(context, true);
	    int count = 0;
	    Log.d(TAG, "Network Connections "+NetWork_Information.isNetworkAvailable(context));
        while (!NetWork_Information.isNetworkAvailable(context)) {
            if (count < 30) {
                waiting(2 * 1000);
                count++;
            } else {
                break;
            }
        }
//        do {
//            waiting(2*1000);
//        } while (!NetWork_Information.isNetworkAvailable(context));
	    
//		Log.v("Safecell", "startIgnoringLocationUpdates = true");
//		if (TrackingScreenActivity.isBackground) {
//			Intent intent = new Intent(TrackingService.this, TrackingScreenActivity.class);
//			intent.setFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
//			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			startActivity(intent);
//		}
		
		if (NetWork_Information.isNetworkAvailable(context)) {
		    Log.d(TAG, "Trip Saving");
//		    Toast.makeText(getApplicationContext(), "Saving Trip", Toast.LENGTH_LONG).show();
//			SaveTrip saveTrip = new SaveTrip();
//			saveTrip.execute();
		    saveTripAsyncTask(context);
		}
		startTrip();

	}

	public static void showNotification(String msg) {
		if(TrackingService.homeScreenActivity != null) {
			homeScreenActivity.showNotification(msg);
		}
	}

	String getTodaysDate() {
		String today = "";
		SimpleDateFormat formatter = new SimpleDateFormat("d MMM yyyy");
		Date currentTime_1 = new Date();
		today = formatter.format(currentTime_1);
		return today;
	}

	int getTotalDistance() {
		double totalDistance = 0d;
		TempTripJourneyWayPointsRepository tempTripJourneyWayPointsRepository = new TempTripJourneyWayPointsRepository(TrackingService.this);
		totalDistance = tempTripJourneyWayPointsRepository.getTotalDistance();

		return (int) totalDistance;
	}

    private void reset() {
        Log.d(TAG, "reset Service");
        TrackingScreenActivity.isTripSavingInProgress = false;
        TrackingService.ignoreLocationUpdates = false;
        TrackingScreenActivity.IS_TRIP_PAUSED = false;
        cancelAutoTripStartTimer();
        cancelTripStopTimer();
        mSmsDetector.stop();
        mWebDetector.stop();
        mEmailDetector.stop();
        System.gc();

    }
    
    private void set(){
        Log.d(TAG, "set Called ");
        ProfilesRepository profilesRepository = new ProfilesRepository(TrackingService.this);
        int profile_id = profilesRepository.getId();
        Log.d(TAG, "profile id : "+profile_id);

        configRunnable = new ConfigurationRunnable(profile_id);
        handler.postDelayed(configRunnable,2000);
        
        TrackingService.ignoreLocationUpdates = false;
        // setting the auto timer always on
        SharedPreferences preferences = getSharedPreferences("TripCheckBox", MODE_WORLD_WRITEABLE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isbackgroundtrip", true);
        editor.commit();

    }
	
    @Override
    public boolean onUnbind(Intent intent) {
//        Log.d(TAG, "onUnbind Service");
//        TrackingScreenActivity.isTripSavingInProgress = false;
//        TrackingService.ignoreLocationUpdates = false;
//        TrackingScreenActivity.IS_TRIP_PAUSED = false;
//        cancelAutoTripStartTimer();
//        cancelTripStopTimer();
//        System.gc();
        return super.onUnbind(intent);
    }

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
	}

	@Override
	public void onLocationChanged(Location location) {
//		Log.v("Location Changed", "Location Changed");
//	    Toast.makeText(getApplicationContext(), "On Location Up", Toast.LENGTH_SHORT).show();
	    Log.d(TAG, "Location Object = "+location);
//	    Toast.makeText(getApplicationContext(), "Location Update "+location.getLongitude(), Toast.LENGTH_LONG).show();
	    
		long currentTime = new Date().getTime();
		
		if (WAYPOINT_FILTER_TIME > 0) {
			if ((currentTime - lastUpdateReceivedOn) < (WAYPOINT_FILTER_TIME * 1000)) {
				return;
			} 
			  //else { Log.v("Safecell", "Ignored waypoint update"); }
			
		}

		lastUpdateReceivedOn = currentTime;

		if (TrackingService.ignoreLocationUpdates) {
//			Log.v("Safecell", "Ignored Location Update");
			return;
		}

		if (LocationManager.GPS_PROVIDER.equals(location.getProvider())) {
			gpsSource = GPS_PROVIDER_DISPLAY_TEXT;
		} else {
			gpsSource = NETWORK_PROVIDER_DISPLAY_TEXT;
		}
		
		if (lastDistanceInMiles == 0 || lastDistanceInMiles >= 1600 ) {
			
			locationSP = new LocationSP(location, TrackingService.this);
			if (locationSP != null) {
//				Log.v("Safecell", "Calling Geocoder: " + lastDistanceInMiles);
				locationSP.setAddressLine();
				stateAddress = new StateAddress();
				stateAddress.setTextLocation();
			}
			
			lastDistanceInMiles = 0;
		}
		if (currentLocation != null) {
			lastDistanceInMiles += (location.distanceTo(currentLocation));
//			Log.v("Safecell", "LAST_DISTANCE_IN_MILES: " + lastDistanceInMiles);
		}
		
		currentLocation = location;
		

		LOCATION_FOR_RULE = location;
//		if (TrackingScreenActivity.isTripSavingInProgress) {
//			return;
//		}
		
        

		if (!TrackingScreenActivity.IS_TRIP_PAUSED) {
			insertWaypoint(location);
		} else {
//			Log.v("Tracking Service", "InsertWayPoint *Trip Paused*");
		}

	}
	
	@Override
	public void onProviderDisabled(String provider) {
//		Log.v("Safecell", "onProviderDisabled: SELECTED_PROVIDER: " + SELECTED_PROVIDER + " provider " + provider);
		selectBestLocationProvider();

	}

	@Override
	public void onProviderEnabled(String provider) {
//		Log.v("Safecell", "onProviderEnabled: SELECTED_PROVIDER: " + SELECTED_PROVIDER + " provider " + provider);	
		selectBestLocationProvider();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
//		Log.v("Safecell", "onStatusChanged: SELECTED_PROVIDER: " + SELECTED_PROVIDER + " provider " + provider);
		if(provider.equals(SELECTED_PROVIDER)){
			switch (status) {
			case LocationProvider.OUT_OF_SERVICE:
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				//showNotification("GPS updates are unavailable.");
				break;
			case LocationProvider.AVAILABLE:
			    if(!InformatonUtils.isServiceRunning(getApplicationContext())){
			        Log.d(TAG, "Configuration");
			        configureLocationManager();
			        startTrip();
			    }
			    break;
			}
		}
	}
	
	
	private static void noGPSProviderAvailable() {
		SELECTED_PROVIDER = null;
		//showNotification("Location updates are disabled on your phone. Please enable location updates to use Safecell App.");
	}
	
	public static void selectBestLocationProvider() {
//		Log.v("SafeCell", "selectBestLocationProvider");
		if(context == null) {
			return;
		}
		if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			requestGPSUpdates();
		} else if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
			requestNetworkLocationUpdates();
		} else {
			noGPSProviderAvailable();
		}
	}
	
	private static void processLastKnownLocation() {
		currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (currentLocation != null) {
//			Log.v("Tracking service", "curren Location is not Null");
			latitude = currentLocation.getLatitude();
			longitude = currentLocation.getLongitude();

			LOCATION_FOR_RULE = currentLocation;
//			Log.v("current location", currentLocation.toString());

		}
	}
	
	private static void requestGPSUpdates() {
		if(LocationManager.GPS_PROVIDER.equals(SELECTED_PROVIDER)) {
			return;
		} else if(SELECTED_PROVIDER != null) {
			locationManager.removeUpdates(context);
		}
		
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATE_TIME_INTERVAL, LOCATION_UPDATE_DISTANCE_INTERVAL, context);
		SELECTED_PROVIDER = LocationManager.GPS_PROVIDER;
//		Log.v("Safecell", "SELECTED_PROVIDER : " + SELECTED_PROVIDER);
		
		processLastKnownLocation();
	}
	
	private static void requestNetworkLocationUpdates() {
		if(LocationManager.NETWORK_PROVIDER.equals(SELECTED_PROVIDER)) {
			return;
		} else if(SELECTED_PROVIDER != null) {
			locationManager.removeUpdates(context);
		}
		
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_UPDATE_TIME_INTERVAL, LOCATION_UPDATE_DISTANCE_INTERVAL, context);

		currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		SELECTED_PROVIDER = LocationManager.NETWORK_PROVIDER;
//		Log.v("Safecell", "SELECTED_PROVIDER : " + SELECTED_PROVIDER);
		
		processLastKnownLocation();
	}

	private void configureLocationManager() {
	    
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.addNmeaListener(this);
		
//		Criteria criteria = new Criteria();
//		criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
//		criteria.setCostAllowed(true);
//		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		
		selectBestLocationProvider();
	}
	
	/**
	 * Removing the update listener
	 * 
	 */
	private void removeLocationUpdates(){
	    if(locationManager != null && context != null){
	        locationManager.removeUpdates(context);
	        locationManager.removeNmeaListener(context);
	        SELECTED_PROVIDER = null;
	    }
	}

	private void insertWaypoint(Location location) {
		// InteruptionRepository
	    TempTripJourneyWayPointsRepository tempTripJourneyWayPointsRepository = new TempTripJourneyWayPointsRepository(this);
//	    Log.d(TAG, "Size = "+tempTripJourneyWayPointsRepository.selectTrip().getCount());
		double distanceInMiles = tempTripJourneyWayPointsRepository.getDistanceDifference(location);
//		Log.d(TAG, "Distance Differences = "+distanceInMiles);
		long currentTime = new Date().getTime();
		
		double timeDifference = tempTripJourneyWayPointsRepository.getTimeDiffernce(currentTime);
		double avarageSpeed = tempTripJourneyWayPointsRepository.getAvarageEstimatedSpeedForAutoTripStart();
		Log.d(TAG, "Average Speed "+avarageSpeed);
		Log.d(TAG, "Distance Travelled = "+tempTripJourneyWayPointsRepository.getTotalDistance());
		SharedPreferences sharedPreferences = getSharedPreferences("TRIP", MODE_WORLD_READABLE);
//		double locationSpeed = location.getSpeed();
		boolean isTripStarted = sharedPreferences.getBoolean("isTripStarted", false);
//		Log.v("Safecell: Tracking Service", "*isTripStarted= " + isTripStarted + " Avg Speed= " + avarageSpeed);
 
		
//		if (isTripStarted) {
//			// if (isTripStarted && tempWayPointCount >= 5) { 
//			if (trackingScreenActivity == null) {
//				Intent mIntent = new Intent(TrackingService.this, TrackingScreenActivity.class);
//				mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				startActivity(mIntent);
//			}
//		}
		Log.d(TAG, "is Trip Started "+isTripStarted);
		if (avarageSpeed > TRIP_CUT_OFF && isTripStarted == false && autoStartTripTimer == null) {
			SharedPreferences tripAutoStartSharedPref = getSharedPreferences("TripCheckBox", MODE_WORLD_READABLE);
			boolean startAutoTrips = tripAutoStartSharedPref.getBoolean("isbackgroundtrip", true);
			Log.d(TAG, "Start Auto Trips = "+startAutoTrips);
//			Log.v("Safecell", "startAutoTrips : " + startAutoTrips);
//			Log.v("Safecell", "SELECTED_PROVIDER : " + SELECTED_PROVIDER);
			
			if (startAutoTrips && (SELECTED_PROVIDER == LocationManager.GPS_PROVIDER)) {
				setAutoStartTripTimer();
			} else {
//				Log.v("Safecell", "AutoTrip start : Disabled " + SELECTED_PROVIDER);
			}

			/*
			 * if
			 * (tempTripJourneyWayPointsRepository.isAvarageTimeDiffFeasible())
			 * { // Log.v("Tracking Service", "isTripStarted= " +
			 * isTripStarted); Log.v("safecell: Tracking Service",
			 * "***Tracking Started***"); } else {
			 * Log.v("safecell: Tracking Service",
			 * "isAvarageTimeDiffFeasible returned false"); }
			 */
		} else if (avarageSpeed < TRIP_CUT_OFF && isTripStarted == false && autoStartTripTimer != null) {
			cancelAutoTripStartTimer();
		}

		if (timeDifference != 0) {
			speed = distanceInMiles / timeDifference;
		}
//		Log.d(TAG, "Speed = "+speed);
//		Log.d(TAG, "time Difference = "+timeDifference);

//		Log.v("Safecell:", "distanceInMiles : " + distanceInMiles + " timeDifference : " + timeDifference + " Speed : " + speed);
		
		if (speed < IGNORE_WAYPOINT_SPEED_LIMIT || timeDifference == 0) {
			
			if (timeDifference == 0) {
//				Log.v("timeDifference", "adjusted for zero");
			}
			
			boolean background = false;
			
			if(TrackingScreenActivity.isBackground && !(LockReceiver.wasLoacked)) {
				background = true;
			}
			
//			Log.v("Safecell:", " isBackground: " + TrackingScreenActivity.isBackground 
//					+ " isWaypointBackground: " + background);

			if (!isTripStarted) {

				SCWayPoint wayPoint = new SCWayPoint(0, 0, DateUtils.getTimeStamp(currentTime), 
													location.getLatitude(), location.getLongitude(), speed, 
													background);
				Log.d(TAG, "Inserting way points");
				tempTripJourneyWayPointsRepository.intsertWaypoint(wayPoint);
				
//				Log.v("Tracking Service", "Insert WayPoint");

			}

			if (isTripStarted)

			{
				AudioManager aManager = (AudioManager) getSystemService(AUDIO_SERVICE);
				aManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);

				//Log.v("Tracking Service", "Insert WayPoint");
				//Log.v("Tracking Service", "isTripStarted= " + isTripStarted);

				// if (timeDifference >= WAYPOINT_TIME_CUT_OFF || timeDifference
				// == 0.0) {
				
				if(timeDifference*60 >= AUTO_SAVE_DELAY_MINUTE){
				    saveTrip(context);
				    return;
				}
				

				SCWayPoint wayPoint = new SCWayPoint(0, 0, DateUtils.getTimeStamp(currentTime), 
													location.getLatitude(), location.getLongitude(), speed, 
													background);
				
				tempTripJourneyWayPointsRepository.intsertWaypoint(wayPoint);

				/*
				 * } else{ Log.v("Insert waypoint igonare", timeDifference+"");
				 * }
				 */
				
//				Log.v("Safecell", "avarageSpeed = " + avarageSpeed);
//				Log.v("Safecell", "handlerTimerTask = " + handlerTimerTask);
				Log.d(TAG, "Before Call ");
				if (avarageSpeed < TRIP_CUT_OFF) {
					if (handlerTimerTask == null) {
						setAutostopTripTimer();
//						Log.v("Safecell", "Trip Stop Timer Set: Yes");
					}
				} else {
					if (handlerTimerTask != null) {
						handlerTimerTask.removeCallbacks(timerTaskRunner);
						handlerTimerTask = null;
//						Log.v("Safecell", "Trip Stop Timer Cancelled");
					}
				}

			}// if trip started
		}

		// trackingScreenActivity.dismProgressDialog();
	}

	private void insertIntoSMS() {
		Cursor c = context.getContentResolver().query(Uri.parse("content://sms"), null, null, null, null);
		c.moveToFirst();

		smsArrayList = smsRepository.scSmsArrayList();
//		Log.v("smsArrayList.size()", "size = " + smsArrayList.size());

		for (int i = 0; i < smsArrayList.size(); i++) {
			ContentValues values = new ContentValues();
			values.put("thread_id", smsArrayList.get(i).getThread_id());
			values.put("address", smsArrayList.get(i).getAddress());
			values.put("person", smsArrayList.get(i).getPerson());
			values.put("date", smsArrayList.get(i).getDate());
			values.put("protocol", smsArrayList.get(i).getProtocol());
			values.put("read", smsArrayList.get(i).getRead());
			values.put("status", smsArrayList.get(i).getStatus());
			values.put("type", smsArrayList.get(i).getType());
			values.put("reply_path_present", smsArrayList.get(i).getReply_path_present());
			values.put("subject", smsArrayList.get(i).getSubject());
			values.put("body", smsArrayList.get(i).getBody());
			values.put("service_center", smsArrayList.get(i).getService_center());
			values.put("locked", smsArrayList.get(i).getLocked());
			context.getContentResolver().insert(Uri.parse("content://sms"), values);
		}
		smsRepository.deleteSms();
	}

	class SaveTrip extends AsyncTask<Void, Void, Boolean> {

		private Context context;
		

		@Override
		protected void onPreExecute() {
			resultFlag = false;

			if (trackingScreenActivity != null) {

				trackingScreenActivity.showProgressBar();
				context = getApplicationContext();

			} else if (addTripActivity != null) {
				addTripActivity.showProgressBar();
				context = AddTripActivity.addTripActivity;

			}
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			try {
			    Log.d(TAG, "Do In Background");
				Looper.prepare();
				TrackingScreenActivity.isTripSavingInProgress = true;
				saveTripAsyncTask(context);
				Log.d(TAG, "Saved On Server");
				onPostExecute(resultFlag);

			} catch (Exception e) {
				e.printStackTrace();
				TrackingScreenActivity.isTripSavingInProgress = false;
				resultFlag = false;
				onPostExecute(false);

			}
			Looper.loop();

			return resultFlag;
		}

		protected void onPostExecute(Boolean result) {
//			Log.v("Tracking Service", "onPostExecute");
//			if (!result) {
//				try {
//					tripNotSaveDialog(context, EXCEPTION_WHILE_SAVING);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//
//			} else
//				callActivityAfterTripSave();
		    // start trip again
		    Log.d(TAG, "onPostExecute");
		    startTrip();
//
		}
		    

	}

	public void callActivityAfterTripSave() {
		try {

			if (trackingScreenActivity != null) {
				trackingScreenActivity.dismProgressDialog();
				trackingScreenActivity.finish();
			}
			if (addTripActivity != null) {
				addTripActivity.dismProgressDialog();
				addTripActivity.finish();
			}
			
			/** change unique ID for trip saving **/
			HomeScreenActivity.editGenereateTripUniqueID("");
			
			if (homeScreenActivity != null) { 
				homeScreenActivity.finish();
			}

			
			TempTripJourneyWayPointsRepository tempTripJourneyWayPointsRepository = new TempTripJourneyWayPointsRepository(TrackingService.this);
			tempTripJourneyWayPointsRepository.deleteTrip();
			ir.deleteInteruptions();
			
			
			Intent homeScreen = new Intent(TrackingService.this, HomeScreenActivity.class);
			homeScreen.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(homeScreen);			
			TrackingScreenActivity.isTripSavingInProgress = false;

		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}
	
	/**
	 * Start the service again
	 * 
	 */
	private void startTrip(){
        /** change unique ID for trip saving **/
	    configureLocationManager();
        HomeScreenActivity.editGenereateTripUniqueID("");
        TempTripJourneyWayPointsRepository tempTripJourneyWayPointsRepository = new TempTripJourneyWayPointsRepository(TrackingService.this);
        tempTripJourneyWayPointsRepository.deleteTrip();
        ir.deleteInteruptions();
        TrackingScreenActivity.isTripSavingInProgress = false;
        SharedPreferences.Editor editor1 = sharedPreferences.edit();
        editor1.putBoolean("isTripStarted", false);
        editor1.commit();
        
        if(!InformatonUtils.isServiceRunning(this)){
            Intent mIntent = new Intent(getApplicationContext(), TrackingService.class);
            startService(mIntent);
        }
      
//      Intent mIntent = new Intent(getApplicationContext(), TrackingService.class);
//      stopService(mIntent);
//      startService(mIntent);
//      ServiceHandler.getInstance(this).unBind();
//      ServiceHandler.getInstance(this).bindService();
        reset();
        set();
        
	}
	
	
	/* (non-Javadoc)
	 * @see android.content.ContextWrapper#stopService(android.content.Intent)
	 */
	@Override
	public boolean stopService(Intent name) {
	    Log.d(TAG, "Stopping Service");
	    TrackingScreenActivity.isTripSavingInProgress = false;
	    TrackingService.ignoreLocationUpdates = false;
	    TrackingScreenActivity.IS_TRIP_PAUSED = false;
	    cancelAutoTripStartTimer();
	    cancelTripStopTimer();
	    System.gc();
	    return super.stopService(name);
	}

	private void tripNotSaveDialog(final Context frontScreenActivity, final int whichBlock) {

		String errorMessage = "Unexepted error occure while saving the trip.";
		if (show_dialog_counter == 4) {
			deleteTripDialog(frontScreenActivity, errorMessage);
			return;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(frontScreenActivity);
		final AlertDialog alert;

		builder.setMessage(errorMessage).setCancelable(false).setPositiveButton("Retry", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				saveTrip(frontScreenActivity);
			}
		})
		//
		.setNegativeButton("Delete Trip", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				/** Delete Temporary Trip **/
				switch (whichBlock) {
				case RESPONSE_NULL_WHILE_SAVING:
					dialog.cancel();
					tempTripJourneyWayPointsRepository.deleteTrip();
					if (addTripActivity != null) {
						addTripActivity.finish();
					}
					break;
				case EXCEPTION_WHILE_SAVING:
					dialog.cancel();
					callActivityAfterTripSave();
					break;

				}

			}
		});

		alert = builder.create();
		alert.show();
		show_dialog_counter += 1;
		if (trackingScreenActivity != null) {
			trackingScreenActivity.dismProgressDialog();
		}
		if (addTripActivity != null) {
			addTripActivity.dismProgressDialog();
		}

	}

	void deleteTripDialog(Context context, String errorMessage) {
		new AlertDialog.Builder(context).setMessage(errorMessage).setPositiveButton("Delete Trip", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				callActivityAfterTripSave();
				show_dialog_counter = 0;
			}
		}).create().show();
		if (trackingScreenActivity != null) {
			trackingScreenActivity.dismProgressDialog();
		}
		if (addTripActivity != null) {
			addTripActivity.dismProgressDialog();
		}
	}

	@Override
	public void onNmeaReceived(long timestamp, String nmea) {
		// TODO Auto-generated method stub

		if (nmea != null && nmea.length() > 0) {
			// Get rid of checksum
			nmea = nmea.replaceAll("\\*..$", "");

			String[] nmeaSplit = nmea.split(",");

			if (nmeaSplit != null && nmeaSplit.length == 18) {
				if (nmeaSplit[0] != null && nmeaSplit[0].equals(NMEA_DOP_SENTENCE))
					;

				try {
					currentPDOP = Float.parseFloat(nmeaSplit[15]);
				} catch (NumberFormatException nfe) {
					currentPDOP = 0.0;
				}

				try {
					currentHDOP = Float.parseFloat(nmeaSplit[16]);
				} catch (NumberFormatException nfe) {
					currentHDOP = 0.0;
				}

				try {
					currentVDOP = Float.parseFloat(nmeaSplit[17]);
				} catch (NumberFormatException nfe) {
					currentVDOP = 0.0;
				}

//				Log.v("SafecellcurrentPDOP", nmeaSplit[15]);
//				Log.v("SafecellcurrentHDOP", nmeaSplit[16]);
//				Log.v("SafecellcurrentVDOP", nmeaSplit[17]);
			}

		} else {
//			Log.v("Safecell:", "NEMANULL");
		}

	}
	
    /**
     * Configuration 
     * 
     * @author uttama
     */
    private class ConfigurationRunnable implements Runnable {
        
        private int mProfileId;

        private ConfigurationRunnable(int mProfileId) {
            this.mProfileId = mProfileId;
        }

        public void run() {
         // starting the tracking service
            ConfigurationHandler.getInstance().readResponse(mProfileId);
            AUTO_SAVE_DELAY_MINUTE = ConfigurationHandler.getInstance().getConfiguration()
                    .getTripStopTime();
            TRIP_CUT_OFF = ConfigurationHandler.getInstance().getConfiguration().getTripStartSpeed();
            Log.d(TAG, "Auto Stop Delay = "+AUTO_SAVE_DELAY_MINUTE);
            Log.d(TAG, "TRIP START SPEED = "+TRIP_CUT_OFF);
        }

    }


}
