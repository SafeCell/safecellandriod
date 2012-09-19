package com.safecell;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.safecell.dataaccess.TempTripJourneyWayPointsRepository;
import com.safecell.dataaccess.TripRepository;
import com.safecell.utilities.FlurryUtils;

public class AddTripActivity extends Activity {

	TextView milesTravelledTextView, estimateSpeedTextView;
	ListView listViewSaveTrip;
	Button saveButton;
	// TableRow tblRowViewMap;
	Context context;
	private String tripName = "Trip On May 8";
	String[] arr = new String[1];

	ProgressDialog progressDialog;
	Handler handler;
	String TRIP_ON = "Trip On";
	boolean tripExist = false;
	public static TrackingService trackingService;
	SharedPreferences sharedPreferences ;
    Handler handlerProgressBar;
    public static String TripName="";
    public static AddTripActivity addTripActivity;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		addTripActivity=this;
		//TrackingScreenActivity.isTripSavingInProgress = true;
		
		getWindow().setWindowAnimations(R.anim.null_animation);
		context = AddTripActivity.this;
		progressDialog = new ProgressDialog(context);
		TripName ="";
		
		handlerProgressBar = new Handler(Looper.getMainLooper());
		this.initUI();
		populateUI();
		
		//Log.v("Safecell :","isTripSavingInProgress = " +TrackingScreenActivity.isTripSavingInProgress);
		
		
		TrackingService.addTripActivity = AddTripActivity.this;
		sharedPreferences = getSharedPreferences("TRIP",MODE_WORLD_READABLE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean("isTripSaved", false);
		editor.commit();
	}

	private void initUI() {
		setContentView(R.layout.add_trip_layout);

		TripRepository tripRepository = new TripRepository(context);
		tripExist = (tripRepository.getTripCount() > 0 ? true : false);
		//Log.v("Safecell :"+"tripExist", "" + tripExist);
		if (tripExist) {
			arr = new String[2];
		}

		milesTravelledTextView = (TextView) findViewById(R.id.addTripMilesTravelledTextView);
		estimateSpeedTextView = (TextView) findViewById(R.id.addTripEstimateSpeedTextView);
		listViewSaveTrip = (ListView) findViewById(R.id.addTripLayoutSaveTripListView);
		saveButton = (Button) findViewById(R.id.addTripSaveButton);
		listViewSaveTrip.setAdapter(new listViewAdapter(AddTripActivity.this));
		listViewSaveTrip.setOnItemClickListener(listViewSaveTripOnItemClick);
		saveButton.setOnClickListener(saveButtonOnClickListener);

	}

	void populateUI() {
		TempTripJourneyWayPointsRepository tempTripJourneyWayPointsRepository = new TempTripJourneyWayPointsRepository(
				AddTripActivity.this);
		milesTravelledTextView.setText(""
				+ Math.round(tempTripJourneyWayPointsRepository
						.getTotalDistance()));
		estimateSpeedTextView.setText(""
				+ tempTripJourneyWayPointsRepository.getAvarageSpeed());
		tripName = getTodaysDate();

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

	String getTodaysDate() {
		String today = "";
		SimpleDateFormat formatter = new SimpleDateFormat("d MMM yyyy");
		Date currentTime_1 = new Date();
		today = formatter.format(currentTime_1);

		return today;
	}

	private OnItemClickListener listViewSaveTripOnItemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			int listItemPosition = arg2;
			if (listItemPosition == 0) {
				enterTextDialog();
			} else if (listItemPosition == 1 && tripExist) {

				Intent intent = new Intent(AddTripActivity.this,ExistingTripActivity.class);
				startActivityForResult(intent, 1);
			}
		}
	};
	private OnClickListener saveButtonOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			TrackingService.cancelTripStopTimer();
				saveTrip();
		}
	};

	private synchronized void saveTrip() {		
		/**Call saveTrip() function from tracking service**/
		/*if(!TrackingScreenActivity.isTripSavingInProgress)
		{*/			
		trackingService.saveTrip(AddTripActivity.addTripActivity);			
		/**Update Shared preference isTripStarted & isTripSaved**/
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean("isTripSaved", true);
		editor.commit();
		//}
	}

	int getTotalDistance() {
		double totalDistance = 0d;
		TempTripJourneyWayPointsRepository tempTripJourneyWayPointsRepository = new TempTripJourneyWayPointsRepository(
				AddTripActivity.this);
		totalDistance = tempTripJourneyWayPointsRepository.getTotalDistance();
		
		return (int)totalDistance;
	}

	private void enterTextDialog() {

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(LAYOUT_INFLATER_SERVICE);

		View layout = inflater.inflate(R.layout.dialog_input_text,
				(ViewGroup) findViewById(R.id.layout_root));

		final EditText editInput = (EditText) layout
				.findViewById(R.id.dialog_input_EditText);
		editInput.setInputType(InputType.TYPE_CLASS_TEXT);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Enter Trip name").setView(layout);
		final AlertDialog dialogAlert = builder.create();
		dialogAlert.setInverseBackgroundForced(false);

		dialogAlert.setButton("Ok", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				if (!editInput.getText().toString().equalsIgnoreCase("")) {
					listViewSaveTrip.setAdapter(new listViewAdapter(
							AddTripActivity.this));
					if (!(editInput.getText().toString().equals(""))) {
						tripName = editInput.getText().toString();
						TRIP_ON = "";
					}
					dialog.dismiss();
				}
			}
		});
		dialogAlert.setButton2("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();

			}
		});

		dialogAlert.show();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Please save your trip.").setCancelable(false)
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									
									//AddTripActivity.this.finish();
								}
							});

			AlertDialog alert = builder.create();
			alert.show();
			return super.onKeyDown(0, null);
		}

		return super.onKeyDown(keyCode, event);
	}

	class listViewAdapter extends ArrayAdapter<Object> {

		Activity context;

		listViewAdapter(Activity context) {
			super(context, R.layout.custom_listview_row, arr);
			this.context = context;

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			LayoutInflater inflater = context.getLayoutInflater();
			View row = inflater.inflate(R.layout.custom_listview_row, null);

			TextView label = (TextView) row.findViewById(R.id.TextView01);

			TextView label_small = (TextView) row.findViewById(R.id.TextView02);

			if (position == 0) {
				label.setText("New Trip");
				label_small.setText(TRIP_ON + " " + tripName);
				TripName = label_small.getText().toString();
			}
			if (tripExist) {

				if (position == 1) {
					label.setText("Pick From Existing Trips");
					label_small.setText(">");
				}
			}
			return (row);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				Bundle bundle = data.getExtras();
				tripName = bundle.getString("Trip_name");
				TRIP_ON = "";
				listViewSaveTrip.setAdapter(new listViewAdapter(
						AddTripActivity.this));
			}
		}
	}
	

	
	@Override
	protected void onPause() {
		TrackingService.addTripActivity = null;
		super.onPause();
	}
	@Override
	protected void onResume() {
		TrackingService.addTripActivity = AddTripActivity.this;
		// super.onPause();
		super.onResume();
	}
	@Override
	protected void onDestroy() {
		if(progressDialog.isShowing()){
		progressDialog.dismiss();
		}
		dismProgressDialog();
		TrackingService.addTripActivity = null;
		TrackingScreenActivity.isTripSavingInProgress = false;
		super.onDestroy();
	}
	public void dismProgressDialog()
	{
		handlerProgressBar.post(new Runnable(){public void run(){
			if(progressDialog.isShowing())
			progressDialog.dismiss();
		}
		});
	}

	public void showProgressBar(){
		
		handlerProgressBar.post(new Runnable() {

			public void run() {
			//Toast.makeText(getApplicationContext(), "I poop on you", Toast.LENGTH_LONG).show();
			progressDialog = ProgressDialog.show(AddTripActivity.this,    
                    "Please wait...", "Trip saving in progress.", true);
			progressDialog.show();
			}
			});
		
	}
	
	class saveTrip extends  AsyncTask<Void, Void, Void>
	{
		@Override
		protected void onPreExecute() {
			showProgressBar();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			saveTrip();
			return null;
		}
		
		 protected void onPostExecute(Long result) {
	         dismProgressDialog();
	     }

	}
	/*void tripNotSaveDialog() {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		final AlertDialog alert;

		builder.setMessage(
				"Unexepted error occure while saving the trip.")
				.setCancelable(false).setPositiveButton("Retry",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(
									DialogInterface dialog,
									int which) {
								dialog.cancel();
								trackingService.saveTrip(context);
							}
						})
				//
				.setNegativeButton("Delete Trip",
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialog, int id) {
								*//** Delete Temporary Trip **//*
								
									dialog.cancel();
									trackingService.callActivityAfterTripSave();
									tempTripJourneyWayPointsRepository.deleteTrip();
									if (addTripActivity != null) {
										addTripActivity.finish();
									}
									
							
							}
						});

		alert = builder.create();
		alert.show();
		
			
			if (addTripActivity != null) {
				addTripActivity.dismProgressDialog();
				}
		
	}*/

}
