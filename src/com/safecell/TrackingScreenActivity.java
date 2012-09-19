package com.safecell;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;

import com.safecell.dataaccess.ContactRepository;
import com.safecell.dataaccess.InteruptionRepository;
import com.safecell.dataaccess.SMSRepository;
import com.safecell.dataaccess.TempTripJourneyWayPointsRepository;
import com.safecell.model.SCInterruption;
import com.safecell.model.SCSms;
import com.safecell.receiver.LockReceiver;
import com.safecell.utilities.DateUtils;
import com.safecell.utilities.FlurryUtils;
import com.safecell.utilities.UIUtils;

public class TrackingScreenActivity extends Activity {

	private static boolean isTripPaused = false;
	private static TextView tvLatitude;
	private static TextView tvLongitude;
	private static TextView tvEstimatedSpeed;
	private static TextView tvTimeStamp;
	public static boolean INCOMING_CALL_OCCUER =false;
	private static Button btnPause, btnStop, btnTrackingView, btnDebugView,
			btnPlaceEmergencyCall;
	private Button raceToEndButton;
	private static ImageView phoneImageView, smsImageView, schoolImageView;
	private TextView trackingOnPausedTextView;
	public static Context context;
	private CharSequence[] contactName;
	private String[] contactNumber;
	static String[][] pointInfo;
	ToggleButton emailToggleButton;
	Resources resources;
	private boolean sendMailFlag = false;
	AssetManager assetManager;
	private boolean checkEmailFacilityFlag = false;
	SharedPreferences sharedPreferences;
	public static int incomingCallCounter;
	Handler handler;
	private ArrayList<SCSms> smsArrayList = new ArrayList<SCSms>();
	private SMSRepository smsRepository;
	// SharedPreferences sharedPreferences;
	ProgressDialog progressDialog;
	public static final String PREF_FILE_NAME = "TrackingScreenPref";
	InterruptionStore interruptionStore;
		
	static TempTripJourneyWayPointsRepository tempTripJourneyWayPointsRepository;
	public static boolean isTripSavingInProgress = false;
	public static boolean IS_TRIP_PAUSED = false;
	// PAUSE_BUTTON_FlAG
	private boolean isLastIntuption = false;

	InteruptionRepository interuptionRepository;

	public static MediaPlayer mediaPlayer;
	public static int songsFileArray[] = { R.raw.cell_phone, R.raw.texting,
			R.raw.school_zone };
	public static QueueSong queueSong;

	public static boolean smsRuleLight = false, phoneRuleLight = false,
			schoolRuleLight = false;

	static double latitude = 0;

	static double longitude = 0;
	BroadcastReceiver smsSendReceiver,smsDeliveredReceiver;
	private static boolean isNotificationSoundEnabled;

	public static boolean isBackground = false;
	
	
	
	static boolean isSchooleZoneActive = false;
	static boolean isPhoneRuleActive = false;
	static boolean isSMSRuleActive = false;
	PowerManager pm;
	protected PowerManager.WakeLock mWakeLock;
	// boolean isTrackingCrashed = true;
	ServiceConnection serviceConnection;
	TrackingService trackingService;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setWindowAnimations(R.anim.null_animation);

		queueSong = new QueueSong();
		context = TrackingScreenActivity.this;
		trackingService = new TrackingService();
		mediaPlayer = new MediaPlayer();

		isBackground = false;
		
		isSchooleZoneActive = false;
		isPhoneRuleActive = false;
		isSMSRuleActive = false;

		sharedPreferences = getSharedPreferences("TRIP", MODE_PRIVATE);
		handler = new Handler(Looper.getMainLooper());
		incomingCallCounter = 0;
		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		// this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
		// "My Tag");
		this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
				"My Tag");
		this.mWakeLock.acquire();

		tempTripJourneyWayPointsRepository = new TempTripJourneyWayPointsRepository(
				context);
		interuptionRepository = new InteruptionRepository(
				TrackingScreenActivity.this);

		resources = this.getResources();
		assetManager = resources.getAssets();

		isNotificationSoundEnabled = this.NotificationsoundOnOff();

		this.initUI();
		AudioManager aManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		aManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		TrackingService.isTripRunning = true;
//		Log.v("SafeCell--Tracking Activity", "oncreate");
		serviceConnect();
		
		
	}// end onCreate

	
	
	public void serviceConnect(){
		serviceConnection = new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName name) {
				//Log.v("SafeCell : service", "Service connected");

			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				//Log.v("SafeCell : service", "Service Disconnected");

			}

		};
		// for testing service
		Intent serviceIntent = new Intent(TrackingScreenActivity.this,
				TrackingService.class);
		context.bindService(serviceIntent, serviceConnection,
				Context.BIND_AUTO_CREATE);
	}
	public static void onLocationChangedUpdateUi(Location location) {
		//Log.v("Safecell :" + "Location", "Langitude = "
		//		+ location.getLongitude() + " Latitude= "
		//		+ location.getLatitude());

		updateUi(location);
	}

	public boolean NotificationsoundOnOff() {
		SharedPreferences sharedPreferences = getSharedPreferences(
				"NotificationSoundCheckBox", MODE_WORLD_READABLE);
		isNotificationSoundEnabled = sharedPreferences.getBoolean(
				"isNotificationSound", true);
		return isNotificationSoundEnabled;
	}

	private void initUI() {

		setContentView(R.layout.trip_recording_layout);

		tvLatitude = (TextView) findViewById(R.id.tripRecordingLatitudeTextView);
		tvLongitude = (TextView) findViewById(R.id.tripRecordingLongitudeTextView);
		btnPause = (Button) findViewById(R.id.tripRecordingPauseButton);
		btnStop = (Button) findViewById(R.id.tripRecordingStopButton);
		btnTrackingView = (Button) findViewById(R.id.tripRecordingInformationButton);
		btnDebugView = (Button) findViewById(R.id.tripRecordingSecondViewInformationButton);
		tvEstimatedSpeed = (TextView) findViewById(R.id.tripRecordingEstimateSpeedView);
		tvTimeStamp = (TextView) findViewById(R.id.tripRecordingTimeStampTextView);
		btnPlaceEmergencyCall = (Button) findViewById(R.id.tripRecordingEmergencyCallButton);
		raceToEndButton = (Button) findViewById(R.id.tripRecordingSecondViewRaceToEndButton);
		emailToggleButton = (ToggleButton) findViewById(R.id.tripRecordingEmailToggleButton);
		btnPause.setOnClickListener(OnPauseButtonClickListener);
		btnStop.setOnClickListener(StopButtonClickedListener);
		// btnTrackingView.setOnClickListener(TrackingViewButtononClickListener);
		// btnDebugView.setOnClickListener(DebugViewButtononClickListener);
		btnPlaceEmergencyCall
				.setOnClickListener(placeEmergencyCallOnClickListener);
		emailToggleButton.setOnClickListener(emailToggleButtonOnClickListener);

		smsImageView = (ImageView) findViewById(R.id.tripRecordingSmsImageView);
		phoneImageView = (ImageView) findViewById(R.id.tripRecordingPhoneImageView);
		schoolImageView = (ImageView) findViewById(R.id.tripRecordingSchoolImageView);

		trackingOnPausedTextView = (TextView) findViewById(R.id.tripRecordingTrackingOnPausedTextView);

		// SharedPreferences sharedPreferences = getSharedPreferences("TRIP",
		// MODE_PRIVATE);
		if (sharedPreferences.getBoolean("isTripPaused", false)) {
//			Log.v("Safecell :" + "Tracking Screen", "Trip Was Paused");
			btnPause.setBackgroundResource(R.drawable.start_button);
			trackingOnPausedTextView.setText("Tracking Paused");
			Toast.makeText(TrackingScreenActivity.this, "Trip Was Paused",
					Toast.LENGTH_LONG);
		}
	}

	public static void updateRulesUI(boolean phone, boolean sms) {
		//Log.v("Safecell :", "phone = " + phone + "  sms = " + sms);
		if (!TrackingService.isTripRunning) {
			return;
		}
		isPhoneRuleActive = phone;
		isSMSRuleActive = sms;

		if (smsImageView != null) {

			if (phone) {
				phoneImageView.setBackgroundResource(R.drawable.phone_active);
				if (phoneRuleLight == false) {
					//Log.v("Safecell :", "adding Phone sound");
					queueSong.enqueue(songsFileArray[0]);

					phoneRuleLight = true;
				} else
					return;

			} else {
				//Log.v("phone Light", "Light off");
				phoneImageView.setBackgroundResource(R.drawable.phone_unclick);
				phoneRuleLight = false;
			}// end phone if

			if (sms) {
				smsImageView.setBackgroundResource(R.drawable.sms_active);
				//Log.v("Safecell :", "smsRuleLight : " + smsRuleLight);
				if (smsRuleLight == false) {
					//Log.v("Safecell :", "adding SMS sound");
					queueSong.enqueue(songsFileArray[1]);
					smsRuleLight = true;
				} else if (!phone && sms)
					return;

			} else if (!sms) {
				//Log.v("sms Light", "Light off");
				smsImageView.setBackgroundResource(R.drawable.sms_unclick);
				smsRuleLight = false;
				// lastTimesmsRuleLight = false;
			}// end sms if
			if (sms || phone ) {
				playsong();
			}
			
		}
	}

	public static void updateSchoolUI(boolean school) {
		if (!TrackingService.isTripRunning) {
			return;
		}
		// Log.v("Safecell :"+"Tracking Screen ","School UI");
		isSchooleZoneActive = school;
		if (schoolImageView != null) {
			if (school) {
				schoolImageView.setBackgroundResource(R.drawable.school_active);

				if (schoolRuleLight == false) {

					schoolRuleLight = true;
					//Log.v("Safecell :", "adding School sound");
					if (!queueSong.isEmpty()) {
						queueSong.enqueue(songsFileArray[2]);

					} else {
						queueSong.enqueue(songsFileArray[2]);
						playsong();
						//Log.v("Safecell :" + "School--Add file",
//								"School--Add file");
					}
				} else
					return;
			} else {
				schoolImageView
						.setBackgroundResource(R.drawable.school_unclick);
				schoolRuleLight = false;
			}
		}
	}

	public static void playsong() {

		if (TrackingService.isTripRunning) {
			try {
				if (!mediaPlayer.isPlaying()) {

					if (!queueSong.isEmpty() && checkRuleIsActive()
							&& isNotificationSoundEnabled) {

						mediaPlayer = new MediaPlayer();
						mediaPlayer = MediaPlayer.create(context, queueSong
								.peek());

						mediaPlayer.start();
						mediaPlayer.setLooping(false);
						//Log.v("Safecell :" + "Player", "StartPlaying");

						mediaPlayer
								.setOnCompletionListener(new OnCompletionListener() {

									@Override
									public void onCompletion(MediaPlayer mp) {

										mediaPlayer.stop();
//										Log.v("Safecell :" + "Oncomplete",
//												"Song play complete");
										mediaPlayer.stop();
										int dequeued = queueSong.dequeue();

										if (dequeued == -1) {
											return;
										}

//										System.out.println("remove: "
//												+ dequeued);

										if (!queueSong.isEmpty()) {

//											Log.v("Safecell :" + "Oncomplete",
//													"Next Song play");
											playsong();

										}
									}
								});

					}/** !queueSong.isEmpty() **/
					else if (!queueSong.isEmpty()) {
//						Log.v("Safecell :" + "playsong", "else----------1");
						int dequeued = queueSong.dequeue();
						if (dequeued == -1) {
							return;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (!queueSong.isEmpty()) {
//			Log.v("Safecell :" + "playsong", "else----------1");
			int dequeued = queueSong.dequeue();
			if (dequeued == -1) {
				return;
			}
		}// queueSong.

	}

	public static boolean checkRuleIsActive() {
		if (!queueSong.isEmpty()) {

			switch (queueSong.peek()) {
			case R.raw.cell_phone:
				if (phoneRuleLight)
					return true;
				break;
			case R.raw.texting:
				if (smsRuleLight)
					return true;
				break;
			case R.raw.school_zone:
				if (schoolRuleLight)
					return true;
				break;
			}
		}
		return false;

	}

	public static void updateUi(Location location) {
		// Log.v("Safecell :"+"Tracking Screen ","Update UI");
		latitude = location.getLatitude();
		longitude = location.getLongitude();

		if (!isTripPaused) {

			if (tvLatitude != null) {

				tvLatitude.setText("" + location.getLatitude());
				tvLatitude.postInvalidate();

				tvLongitude.setText("" + location.getLongitude());
				tvLongitude.postInvalidate();

				tvEstimatedSpeed.setText("" + location.getSpeed());
				tvEstimatedSpeed.postInvalidate();

				tvTimeStamp.setText(getTimeStamp(System.currentTimeMillis()));
				tvTimeStamp.postInvalidate();

			}
		}
	}

	private static String getTimeStamp(long timeInMillSecond) {
		Date date = new Date(timeInMillSecond);
		SimpleDateFormat simpleDate = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss'Z'");
		String dateString = simpleDate.format(date);
		
		return dateString;
	}

	private OnClickListener OnPauseButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (!isTripSavingInProgress) {
				if (!IS_TRIP_PAUSED) {
//					Log.v("Safecell :" + "Tracking Screen", "Tracking Paused");
					btnPause.setBackgroundResource(R.drawable.start_button);
					trackingOnPausedTextView.setText("Tracking Paused");
					TrackingService.cancelTripStopTimer();
					if (!isTripSavingInProgress) {

						IS_TRIP_PAUSED = true;
						/** Update Shared Preference **/

						SharedPreferences.Editor editor = sharedPreferences
								.edit();
						editor.putBoolean("isTripPaused", true);
						editor.commit();
						/** Start Interruption **/
						insertPausedInterruptionStarted();
					} else {
						TripSaveInprigressDialog();
					}
				} else {

					btnPause.setBackgroundResource(R.drawable.pause_button);
					trackingOnPausedTextView.setText("Tracking On");
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putBoolean("isTripPaused", false);
					editor.commit();
					/** End Interruption **/
					updateInteruptionStoped();
				}
			} else {
				TripSaveInprigressDialog();
			}
		}
	};

	private void insertPausedInterruptionStarted() {

		TempTripJourneyWayPointsRepository tempTripJourneyWayPointsRepository = new TempTripJourneyWayPointsRepository(
				TrackingScreenActivity.this);
		double estimatedSpeed = tempTripJourneyWayPointsRepository
				.getAvarageEstimatedSpeedForAutoTripStart();
		String startedAt = DateUtils.getTimeStamp(new Date().getTime());

		SCInterruption scInterruption = new SCInterruption();
		scInterruption.setStarted_at(startedAt);
		scInterruption.setLatitude(latitude + "");
		scInterruption.setLongitude(longitude + "");
		scInterruption.setPaused(IS_TRIP_PAUSED);
//		Log.v("Safecell :" + "Tracking Screen isPaused", "" + IS_TRIP_PAUSED);
		scInterruption.setEstimatedSpeed(estimatedSpeed + "");
		scInterruption.setSchooleZoneActive(isSchooleZoneActive);
		scInterruption.setSmsRuleActive(isSMSRuleActive);
		scInterruption.setPhoneRuleActive(isPhoneRuleActive);

		InteruptionRepository interuptionRepository = new InteruptionRepository(
				TrackingScreenActivity.this);
		interuptionRepository.insertInterupt(scInterruption);
	}

	private void updateInteruptionStoped() {
		/** Update Interruption End **/
		InteruptionRepository interuptionRepository = new InteruptionRepository(
				TrackingScreenActivity.this);
		String endedAt = DateUtils.getTimeStamp(new Date().getTime());
		interuptionRepository.updateEndedAt(endedAt);

		SharedPreferences sharedPreferences = getSharedPreferences("TRIP",
				MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean("isTripPaused", false);
		editor.commit();

		IS_TRIP_PAUSED = false;
	}

	private OnClickListener StopButtonClickedListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			
			if (!isTripSavingInProgress) {
				TempTripJourneyWayPointsRepository tempTripRepo = new TempTripJourneyWayPointsRepository(
						TrackingScreenActivity.this);
				int totalDistance = (int) tempTripRepo.getTotalDistance();

				if (!(totalDistance < 1)) {
					dialogMessage();
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							TrackingScreenActivity.this);
					builder.setMessage("This trip is too short to save.");
					builder.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							});
					builder.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									/*
									 * SharedPreferences sharedPreferences =
									 * getSharedPreferences( "TRIP",
									 * MODE_PRIVATE);
									 */
									TrackingService.trackingScreenActivity = null;
									TrackingService.isTripRunning = false;
									

									SharedPreferences.Editor editor = sharedPreferences
											.edit();
									editor.putBoolean("isTripPaused", false);
									editor.putBoolean("isTripStarted", false);
									editor.commit();
									IS_TRIP_PAUSED = false;

									TempTripJourneyWayPointsRepository tempTripJourneyWayPointsRepository = new TempTripJourneyWayPointsRepository(
											TrackingScreenActivity.this);
									tempTripJourneyWayPointsRepository
											.deleteTrip();
									interuptionRepository.deleteInteruptions();
									
//									Log.v("Safecell", "Short Trip Deleted : Cleared Trip and Interruptions");
									
									if (TrackingService.homeScreenActivity != null) {
										TrackingService.homeScreenActivity
												.finish();
									}

									Intent mIntent = new Intent(
											TrackingScreenActivity.this,
											HomeScreenActivity.class);
									startActivity(mIntent);
									TrackingScreenActivity.this.finish();
									
//									Log.v("Safecell", "Short Trip Deleted : Tracking Finish Called");
									
								}
							})

					.setCancelable(false);

					AlertDialog dialog = builder.create();
					dialog.show();

				}
			}
		}
	};

	private OnClickListener TrackingViewButtononClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			ViewFlipper vf = (ViewFlipper) findViewById(R.id.ViewFlipper01);
			vf.setAnimation(AnimationUtils.loadAnimation(v.getContext(),
					R.anim.push_up_in));
			vf.showNext();
		}
	};

	private OnClickListener DebugViewButtononClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (!isTripSavingInProgress) {
				ViewFlipper vf = (ViewFlipper) findViewById(R.id.ViewFlipper01);
				/*
				 * vf.setAnimation(AnimationUtils.loadAnimation(v.getContext(),
				 * R.anim.push_up_out));
				 */
				vf.showPrevious();
			} else {
				TripSaveInprigressDialog();
			}
		}
	};

	private OnClickListener placeEmergencyCallOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			ContactRepository contactRepository = new ContactRepository(
					TrackingScreenActivity.this);
			Cursor cursorContact = contactRepository.SelectContacts();
			if (cursorContact.getCount() > 0) {
				cursorContact.moveToFirst();
				int i = 0;
				contactName = new CharSequence[cursorContact.getCount()];
				contactNumber = new String[cursorContact.getCount()];
				int nameIndex = cursorContact.getColumnIndex("name");
				int phoneNIndex = cursorContact.getColumnIndex("number");

				do {
					contactName[i] = cursorContact.getString(nameIndex);
					contactNumber[i] = cursorContact.getString(phoneNIndex);
					i = i + 1;
				} while (cursorContact.moveToNext());
			}
			if (cursorContact.getCount() > 0) {
				contactDialog(contactName);
			} else {
				UIUtils.OkDialog(TrackingScreenActivity.this,
						"No Emergency Contact ");
			}
		}
	};

	private OnClickListener emailToggleButtonOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (emailToggleButton.isChecked()) {
				sendMailFlag = true;

			} else {
				sendMailFlag = false;
			}

		}
	};

	private void dialogMessage() {

		Dialog dialog = new AlertDialog.Builder(context).setMessage(
				"Do you want to stop?").setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						try {
							/*
							 * if (mediaPlayer.isPlaying()) { mediaPlayer.stop(); }
							 */
							mediaPlayer.release();
						} catch (Exception e) {
							e.printStackTrace();
						}
						/** Clear Preferences **/
					
				TrackingScreenActivity.isTripSavingInProgress = true;

						SharedPreferences.Editor editor = sharedPreferences
								.edit();
						editor.putBoolean("isTripPaused", false);
						editor.putBoolean("isTripStarted", false);
						editor.commit();
						IS_TRIP_PAUSED = false;

						isLastIntuption = true;

						Intent AddTripScreen = new Intent(
								TrackingScreenActivity.this,
								AddTripActivity.class);
						startActivity(AddTripScreen);

						TrackingScreenActivity.this.finish();

					}
				})

		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.dismiss();
			}
		}).create();
		dialog.show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			if (!isTripSavingInProgress) {
				moveTaskToBack(true);
				return super.onKeyDown(0, null);
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	public void TripSaveInprigressDialog() {
		Dialog dialog = new AlertDialog.Builder(context).setMessage(
				"Trip saving in progerss...").setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						dialog.cancel();

					}
				}).create();

		dialog.show();
	}

	private void sendMail() {

		TempTripJourneyWayPointsRepository tempTripJourneyWayPointsRepository = new TempTripJourneyWayPointsRepository(
				TrackingScreenActivity.this);
		Cursor cursor = tempTripJourneyWayPointsRepository.getTrip();
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		String jsonString = "";
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			try {

				for (int i = 0; i < cursor.getCount(); i++) {
					jsonObject.put("estimatedSpeed", cursor.getDouble(4));
					jsonObject.put("longitude", cursor.getDouble(3));
					jsonObject.put("latitude", cursor.getDouble(2));
					jsonObject.put("timeStamp", cursor.getString(1));

					jsonArray.put(jsonObject);
				}
				jsonString = jsonArray.toString(4);

			} catch (Exception e) {
				// TODO: handle exception
			}
		}

		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		String[] recipients = new String[] { "" };
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Test");
		emailIntent
				.putExtra(android.content.Intent.EXTRA_TEXT, "" + jsonString);
		emailIntent.setType("text/plain");
		startActivityForResult(Intent
				.createChooser(emailIntent, "Send mail..."), 1);
	}

	public void contactDialog(final CharSequence[] items) {

		AlertDialog.Builder builder = new AlertDialog.Builder(
				TrackingScreenActivity.this);
		builder.setTitle("Emergency Call");

		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {

				callDialog(contactNumber[item]);
				startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
						+ contactNumber[item])));
			}

		});

		AlertDialog alert = builder.create();
		alert.setButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		alert.show();
	}

	private void callDialog(final String phoneNumber) {
		AlertDialog dialog = new AlertDialog.Builder(context).setMessage(
				"would you like to call?").setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						/* User clicked OK so do some stuff */
						try {
							startActivity(new Intent(Intent.ACTION_CALL, Uri
									.parse("tel:" + phoneNumber)));

						} catch (NullPointerException e) {
							e.printStackTrace();
						}

					}
				})

		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		}).create();
		dialog.show();
	}

	private void updateIsTerminated() {
		interuptionRepository.updateIsTerminated(true);
	}

	public void tripAutoSavedDialog() {
//		Log.v("Safecell :" + "TrackingScreenActivity",
//				"Show trip saved Dialog 1");
		AlertDialog dialog = new AlertDialog.Builder(context).setMessage(
				"Trip Auto Saved").create();
		dialog.show();
//		Log.v("Safecell :" + "TrackingScreenActivity",
//				"Show trip saved Dialog 2 ");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1) {
			Intent AddTripScreen = new Intent(TrackingScreenActivity.this,
					AddTripActivity.class);
			startActivity(AddTripScreen);
			finish();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		isBackground = true;
		
//		Log.v("Safecell :", "Tracking Screen Activity: " + "onPause");
		
		if (!isTripSavingInProgress) {
			if (!IS_TRIP_PAUSED) {
				if (!isLastIntuption &&!INCOMING_CALL_OCCUER) {
					insertPausedInterruptionStarted();
				}
			}
			// moveTaskToBack(true);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
//		Log.v("Safecell :" + "Tracking Screen Activity", "OnResume");
		
		isBackground = false;
		
		TrackingService.trackingScreenActivity = TrackingScreenActivity.this;
		
		SharedPreferences sharedPreferences = getSharedPreferences("TRIP",
				MODE_WORLD_READABLE);
		IS_TRIP_PAUSED = sharedPreferences.getBoolean("isTripPaused", false);

		if (!IS_TRIP_PAUSED) {
			/** End Interruption **/
			updateInteruptionStoped();
		}

		/** Update Is Terminated **/
		updateIsTerminated();
		
		INCOMING_CALL_OCCUER = false;
		
//		if (LockReceiver.wasLoacked) {
//			Log.v("SafeCell :", "was  locked, deleting last interrpution");
//			
//			TrackingService.deleteLastInterruption();
//		}
//		
//		LockReceiver.wasLoacked = false;
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// wl.release();
		this.mWakeLock.release();
		TrackingService.trackingScreenActivity = null;
		TrackingService.isTripRunning = false;
		TrackingService.incomingNumberArrayList = new ArrayList<String>();
//		Log.v("Safecell :" + "Tracking Screen Act OnDestroy()", "Called");
		smsRuleLight = false;
		phoneRuleLight = false;
		schoolRuleLight = false;

		AudioManager aManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		aManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

		insertIntoSMS();
		TrackingService.cancelTripStopTimer();

		SharedPreferences.Editor editor = sharedPreferences.edit();
		// editor.putBoolean("isTripPaused", false);
		editor.putBoolean("isTripStarted", false);
		editor.commit();
		// Last Change
		context.unbindService(serviceConnection);
		if (smsSendReceiver!= null) {
			context.unregisterReceiver(smsSendReceiver);
		}
		if (smsDeliveredReceiver!= null) {
			context.unregisterReceiver(smsDeliveredReceiver);
		}
		
		isBackground = false;
	}

	public void dismProgressDialog() {
		handler.post(new Runnable() {
			public void run() {
				if (progressDialog.isShowing())
					progressDialog.dismiss();
			}
		});
	}

	public void showProgressBar() {
		handler.post(new Runnable() {
			public void run() {
				// Toast.makeText(getApplicationContext(), "I poop on you",
				// Toast.LENGTH_LONG).show();
				progressDialog = ProgressDialog.show(
						TrackingScreenActivity.this, "Please wait...",
						"Trip saving in progress.", true);
				progressDialog.show();
			}
		});

	}

	private void insertIntoSMS() {
		Cursor c = context.getContentResolver().query(
				Uri.parse("content://sms"), null, null, null, null);
		c.moveToFirst();

		if (smsRepository == null) {
//			Log.v("Safecell :" + "NULL: ", "smsRepository");
			smsRepository = new SMSRepository(context);
		}

		smsArrayList = smsRepository.scSmsArrayList();

		if (smsArrayList == null) {
//			Log.v("Safecell :" + "NULL: ", "smsArrayList");
			return;
		}

//		Log.v("Safecell :" + "smsArrayList.size()", "size = "
//				+ smsArrayList.size());

		boolean smsPresent = false;
		int noOfSmses = smsArrayList.size();

		if (smsArrayList.size() == 0) {
			smsPresent = false;
		} else {

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
				values.put("reply_path_present", smsArrayList.get(i)
						.getReply_path_present());
				values.put("subject", smsArrayList.get(i).getSubject());
				values.put("body", smsArrayList.get(i).getBody());
				values.put("service_center", smsArrayList.get(i)
						.getService_center());
				values.put("locked", smsArrayList.get(i).getLocked());

				context.getContentResolver().insert(Uri.parse("content://sms"),
						values);
			}
			smsRepository.deleteSms();
			smsPresent = true;
		}

		boolean callPresent = false;

		CharSequence contentTitle2 = "Calls during trip";
		CharSequence contentText2 = incomingCallCounter
				+ " missed calls during trip.";

		if (incomingCallCounter > 0) {
			callPresent = true;
		}

		String sms = (noOfSmses == 1) ? " text " : " texts ";
		String calls = (incomingCallCounter == 1) ? " incoming call "
				: " incoming calls ";

		boolean showNotification = false;

		if (smsPresent && callPresent) {
			contentTitle2 = "Blocked texts and Incoming Calls";

			contentText2 = "SafeCellApp blocked " + noOfSmses + sms
					+ "(available in your SMS Inbox) and "
					+ incomingCallCounter + calls + "during your trip.";

			showNotification = true;
		} else if (smsPresent) {
			contentTitle2 = "Blocked Texts";
			contentText2 = "SafeCellApp blocked " + noOfSmses + sms
					+ "during your trip, that "
					+ ((noOfSmses == 1) ? "is" : "are")
					+ " now available in your SMS Inbox.";

			showNotification = true;
		} else if (callPresent) {
			contentTitle2 = "Blocked Incoming Calls";

			contentText2 = "SafeCellApp blocked " + incomingCallCounter + calls
					+ "during your trip.";

			showNotification = true;
		}

		if (showNotification) {
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			Notification notification = new Notification(
					R.drawable.launch_icon, contentTitle2, System
							.currentTimeMillis());

			RemoteViews contentView = new RemoteViews(getPackageName(),
					R.layout.sms_notification);
			contentView.setImageViewResource(R.id.NotificationImage,
					R.drawable.launch_icon);
			contentView.setTextViewText(R.id.SmsTextNotification, contentText2);

			notification.contentView = contentView;
			Intent notificationIntent = new Intent();
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
					notificationIntent, 0);
			notification.contentIntent = contentIntent;
			notificationManager.notify(0, notification);
		}

	}

	/*
	 * void tripNotSaveDialog() {
	 * 
	 * AlertDialog.Builder builder = new AlertDialog.Builder(context); final
	 * AlertDialog alert;
	 * 
	 * builder.setMessage( "Unexepted error occure while saving the trip.")
	 * .setCancelable(false).setPositiveButton("Retry", new
	 * DialogInterface.OnClickListener() {
	 * 
	 * @Override public void onClick( DialogInterface dialog, int which) {
	 * dialog.cancel(); trackingService.saveTrip(context); } }) //
	 * .setNegativeButton("Delete Trip", new DialogInterface.OnClickListener() {
	 * public void onClick( DialogInterface dialog, int id) {
	 *//** Delete Temporary Trip **/
	/*
	 * 
	 * dialog.cancel(); trackingService.callActivityAfterTripSave();
	 * 
	 * } });
	 * 
	 * alert = builder.create(); alert.show();
	 * 
	 * if (TrackingScreenActivity.currentInstance != null) {
	 * dismProgressDialog(); }
	 * 
	 * 
	 * }
	 */
	
	public void sendSMS(String phoneNumber, String message)
    {        
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";
        
        if(context == null) {
        	context = TrackingScreenActivity.this;
        }
        
        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
            new Intent(SENT), 0);
 
        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
            new Intent(DELIVERED), 0);
 
        //---when the SMS has been sent---
        context.registerReceiver(smsSendReceiver, new IntentFilter(SENT));
 
        //---when the SMS has been delivered---
        context.registerReceiver(smsDeliveredReceiver,new IntentFilter(DELIVERED));    
 
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);        
    }

	BroadcastReceiver SmsDeliveredBroadcastReceiver(){
		return smsDeliveredReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
            	String Toastmessage = "";
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                      
                        Toastmessage = "SMS delivered";
                        showToastMessage(context, Toastmessage);
                        break;
                    case Activity.RESULT_CANCELED:
                       
                        Toastmessage = "SMS not delivered";
                        showToastMessage(context, Toastmessage);
                        break;                        
                }
            }
        };    
	}
	BroadcastReceiver SmsSendBroadcastReceiver(){
		return smsSendReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
            	String Toastmessage = "";
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        
                        Toastmessage = "SMS sent";
                        showToastMessage(context, Toastmessage);
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                       
                        Toastmessage = "Generic failure";
                        showToastMessage(context, Toastmessage);
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                      
                        Toastmessage = "No service";
                        showToastMessage(context, Toastmessage);
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toastmessage = "Null PDU";
                        showToastMessage(context, Toastmessage);
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                    	Toastmessage = "Radio off";
                        showToastMessage(context, Toastmessage);
                        break;
                }
            }
        };
	}
	
	void showToastMessage(Context context, String message){
		/*Toast.makeText(context, message, 
                Toast.LENGTH_LONG)
                .show();*/
	}
	
}
