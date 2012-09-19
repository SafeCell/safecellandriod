package com.safecell;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.safecell.dataaccess.RulesRepository;
import com.safecell.model.SCRule;
import com.safecell.networking.NetWork_Information;
import com.safecell.networking.RulesAccountRequest;
import com.safecell.utilities.FlurryUtils;
import com.safecell.utilities.LocationSP;
import com.safecell.utilities.StateAddress;
import com.safecell.utilities.UIUtils;

public class ViewRulesListActivity extends Activity {

	static final int ACTIVE_RULE = 0;
	static final int INACTIVE_RULE = 1000;
	static final String INACTIVE_RULE_TAG = "inactive";
	static final String ACTIVE_RULE_TAG = "active";
	private Location currentLocation;
	// private ListView ActiveListView, inActiveListView;
	private Context context;
	private List<SCRule> rulesArrayList = new ArrayList<SCRule>();
	private ArrayList<SCRule> inActiveRulesArrayList = new ArrayList<SCRule>();
	private ArrayList<SCRule> activeRulesArrayList = new ArrayList<SCRule>();
	private String message;
	private Button homeButton, btnMyTrips, settingsButton, rulesButton;
	private RulesRepository rulesRepository;
	private TextView activeTestView, inactiveTextView;
	
	private TextView tvLocation;
	private RulesAccountRequest rulesAccountRequest;
	// private LocationSP locationSP;
	private ProgressDialog progressDialog;
	private ProgressThread mThread;
	private Handler handler;
	private HttpResponse rulesHttpResponse;
	private ImageButton refreshButton;

	ArrayAdapter<String> listArrayAdapter;
	private boolean cancelRule = false;
	LinearLayout innerRuleLinearLayout;
	LayoutInflater vi;
	ruleListDisplyAsync displyAsync;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setWindowAnimations(R.anim.null_animation);
		context = ViewRulesListActivity.this;

		progressDialog = new ProgressDialog(context);

		this.initUI();

		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				progressDialog.dismiss();

				if (rulesHttpResponse == null) {

					UIUtils.OkDialog(context, message);
				}
				getRuleListFromDataBase();
				// setListArrayAdapter();
				if (mThread.isAlive()) {
					mThread = new ProgressThread();
				}
			}
		};

	}

	public void initUI() {

		setContentView(R.layout.view_rules_list);
		vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		homeButton = (Button) findViewById(R.id.tabBarHomeButton);
		btnMyTrips = (Button) findViewById(R.id.tabBarMyTripsButton);
		rulesButton = (Button) findViewById(R.id.tabBarRulesButton);
		settingsButton = (Button) findViewById(R.id.tabBarSettingsButton);

		rulesButton.setBackgroundResource(R.drawable.rules_clicked);
		
		
		inactiveTextView = new TextView(context);
		inactiveTextView.setText("Inactive Rules");
		inactiveTextView.setTextColor(Color.WHITE);
		inactiveTextView.setPadding(5, 0, 0, 0);
		inactiveTextView.setGravity(Gravity.CENTER_VERTICAL);
		inactiveTextView.setBackgroundResource(R.drawable.header_background);
		activeTestView = new TextView(context);
		activeTestView.setText("Active Rules");
		activeTestView.setBackgroundResource(R.drawable.header_background);
		activeTestView.setPadding(5, 0, 0, 0);
		activeTestView.setTextColor(Color.WHITE);
		activeTestView.setGravity(Gravity.CENTER_VERTICAL);
		innerRuleLinearLayout = (LinearLayout) findViewById(R.id.LinearLayoutInnerRule);
		
		tvLocation = (TextView) findViewById(R.id.tabBarCurentLocationTextView);
//		Log.v("Safecell", "Lcation: " + LocationSP.LocationSP);
		tvLocation.setText(LocationSP.LocationSP);
		
		refreshButton = (ImageButton) findViewById(R.id.ViewRulesRefreshButton);

		TabControler tabControler = new TabControler(ViewRulesListActivity.this);
		homeButton.setOnClickListener(tabControler.getHomeTabOnClickListner());
		btnMyTrips.setOnClickListener(tabControler.getMyTripsOnClickListner());

		displyAsync = new ruleListDisplyAsync();
		displyAsync.execute();

		refreshButton.setOnClickListener(eventTitleOnClickListner);
		

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

	public synchronized void getRuleListFromDataBase() {
		rulesRepository = new RulesRepository(context);

		inActiveRulesArrayList = rulesRepository.intialiseRulesArrayList(rulesRepository.SelectInActiveRule());
		rulesRepository.SelectInActiveRule().close();

		activeRulesArrayList = rulesRepository.intialiseRulesArrayList(rulesRepository.SelectActiveRule());
		rulesRepository.SelectActiveRule().close();
		// ActiveListView.setEnabled(true);
		// ActiveListView.setFocusable(true);
		if (activeRulesArrayList.size() == 0) {

			// ActiveListView.setVisibility(View.GONE);
			// *****ruleNotFoundTextView.setVisibility(View.VISIBLE);
			message = "No Rules found.";
//			Log.v("Safecell: ", message);
			SCRule scRule = new SCRule();
			scRule.setLabel("No rules found for this area.");
			// activeRulesArrayList.add(scRule);

		} else {
			// ActiveListView.setVisibility(View.VISIBLE);
			// *****ruleNotFoundTextView.setVisibility(View.GONE);
			message = " Rules found.";
//			Log.v("Safecell: ", activeRulesArrayList.size() + message);
		}
	}

	/*
	 * public void setListArrayAdapter() {
	 * 
	 * ActiveListView.setAdapter(new MyListAdapter(this, activeRulesArrayList));
	 * inActiveListView.setAdapter(new
	 * MyListAdapter(this,inActiveRulesArrayList));
	 * 
	 * if (inActiveRulesArrayList.size() > 0) {
	 * 
	 * inActiveListView.setVisibility(View.VISIBLE);
	 * inactiveTextView.setVisibility(View.VISIBLE); }
	 * 
	 * }
	 */
	
	private LinearLayout noRulesFoundLayout(String message) {
		final LinearLayout linearLayoutMain = new LinearLayout(this);
		linearLayoutMain.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		// linearLayoutMain.setBackgroundResource(R.drawable.event_row_background);
		linearLayoutMain.setOrientation(0);
		linearLayoutMain.setGravity(Gravity.CENTER_VERTICAL);
		linearLayoutMain.setPadding(0, 2, 0, 0);
		
		TextView ruleName = new TextView(this);
		ruleName.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		ruleName.setText(message);
		ruleName.setTextColor(Color.WHITE);
		ruleName.setGravity(Gravity.CENTER_VERTICAL);
		ruleName.setPadding(5, 10, 5, 10);
		
		linearLayoutMain.addView(ruleName);
		
		return linearLayoutMain;
	}
	
	private LinearLayout drawRules(int eventTitleId,
			ArrayList<SCRule> ruleListArrayList1) {

		// Main
		final LinearLayout linearLayoutMain = new LinearLayout(this);
		linearLayoutMain.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		
		linearLayoutMain.setOrientation(0);
		linearLayoutMain.setId(eventTitleId);
		linearLayoutMain.setBackgroundResource(R.drawable.rules_row_background);
		linearLayoutMain.setGravity(Gravity.CENTER_VERTICAL);
		linearLayoutMain.setPadding(0, 2, 0, 0);
		linearLayoutMain.setOnClickListener(eventTitleOnClickListner);
		
		
		final LinearLayout linearLayoutInner = new LinearLayout(this);
		linearLayoutInner.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		linearLayoutInner.setOrientation(1);
		linearLayoutInner.setId(eventTitleId);
		linearLayoutInner.setGravity(Gravity.CENTER_VERTICAL);

		final LinearLayout linearLayoutRow1 = new LinearLayout(this);
		linearLayoutRow1.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		linearLayoutRow1.setOrientation(0);
		linearLayoutRow1.setId(eventTitleId);
		linearLayoutRow1.setGravity(Gravity.CENTER_VERTICAL);
		linearLayoutRow1.setPadding(0, 0, 0, 3);
		
		final LinearLayout linearLayoutRow2 = new LinearLayout(this);
		linearLayoutRow2.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		// linearLayoutMain.setBackgroundResource(R.drawable.event_row_background);
		linearLayoutRow2.setOrientation(0);
		linearLayoutRow2.setId(eventTitleId);
		linearLayoutRow2.setGravity(Gravity.CENTER_VERTICAL);
		
		final LinearLayout linearLayoutRow3 = new LinearLayout(this);
		linearLayoutRow3.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	
		linearLayoutRow3.setOrientation(0);
		linearLayoutRow3.setId(eventTitleId);
		linearLayoutRow3.setGravity(Gravity.CENTER_VERTICAL);
	

		String primaryRulesLabel = (ruleListArrayList1.get(eventTitleId).isPrimary() == true ? "Primary Rules: " : "Secondary Rules: ");
		String crashCollection = (ruleListArrayList1.get(eventTitleId).isCrash_collection() == true ? "Yes," : "No,");
		String preemption = (ruleListArrayList1.get(eventTitleId).isPreemption() == true ? "Yes," : "No,");
		String when_forced = ruleListArrayList1.get(eventTitleId).getWhen_enforced();

		TextView ruleName = new TextView(this);
		ruleName.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		ruleName.setText(ruleListArrayList1.get(eventTitleId).getLabel());
		ruleName.setTextColor(Color.WHITE);
		ruleName.setGravity(Gravity.CENTER_VERTICAL);
		
		TextView primaryRuleLabelTextView = new TextView(this);
		primaryRuleLabelTextView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		primaryRuleLabelTextView.setText(primaryRulesLabel);
		primaryRuleLabelTextView.setTextColor(Color.WHITE);
		primaryRuleLabelTextView.setGravity(Gravity.CENTER_VERTICAL);
		int textSize = 10;
		primaryRuleLabelTextView.setTextSize(textSize);
		

		TextView primaryRuleOutputTextView = new TextView(this);
		primaryRuleOutputTextView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		primaryRuleOutputTextView.setText("Yes, ");
		primaryRuleOutputTextView.setTextColor(Color.WHITE);
		primaryRuleOutputTextView.setGravity(Gravity.CENTER_VERTICAL);
		primaryRuleOutputTextView.setTextSize(textSize);
		
		TextView crashCollectionLabelTextView = new TextView(this);
		crashCollectionLabelTextView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		crashCollectionLabelTextView.setText(" Crash Collection: ");
		crashCollectionLabelTextView.setTextColor(Color.WHITE);
		crashCollectionLabelTextView.setGravity(Gravity.CENTER_VERTICAL);
		crashCollectionLabelTextView.setTextSize(textSize);
		

		TextView crashCollectionOutputTextView = new TextView(this);
		crashCollectionOutputTextView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		crashCollectionOutputTextView.setText(crashCollection);
		crashCollectionOutputTextView.setTextColor(Color.WHITE);
		crashCollectionOutputTextView.setGravity(Gravity.CENTER_VERTICAL);
		crashCollectionOutputTextView.setTextSize(textSize);
		
		TextView preemptionLabelTextView = new TextView(this);
		preemptionLabelTextView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		preemptionLabelTextView.setText("Preemption: ");
		preemptionLabelTextView.setTextColor(Color.WHITE);
		preemptionLabelTextView.setGravity(Gravity.CENTER_VERTICAL);
		preemptionLabelTextView.setTextSize(textSize);
		

		TextView preemptionOutPutTextView = new TextView(this);
		preemptionOutPutTextView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		preemptionOutPutTextView.setText(preemption);
		preemptionOutPutTextView.setTextColor(Color.WHITE);
		preemptionOutPutTextView.setGravity(Gravity.CENTER_VERTICAL);
		preemptionOutPutTextView.setTextSize(textSize);

		TextView whenEnforcedLabelTextView = new TextView(this);
		whenEnforcedLabelTextView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		whenEnforcedLabelTextView.setText(" When Enfored: ");
		whenEnforcedLabelTextView.setTextColor(Color.WHITE);
		whenEnforcedLabelTextView.setGravity(Gravity.CENTER_VERTICAL);
		whenEnforcedLabelTextView.setTextSize(textSize);
	
		TextView whenEnforcedOutputTextView = new TextView(this);
		whenEnforcedOutputTextView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		whenEnforcedOutputTextView.setText((when_forced.equalsIgnoreCase("always")) ? "Always" : "School Zone");
		whenEnforcedOutputTextView.setTextColor(Color.WHITE);
		whenEnforcedOutputTextView.setGravity(Gravity.CENTER_VERTICAL);
		whenEnforcedOutputTextView.setTextSize(textSize);
		
		linearLayoutRow1.addView(ruleName);
		linearLayoutRow2.addView(primaryRuleLabelTextView);
		linearLayoutRow2.addView(primaryRuleOutputTextView);
		linearLayoutRow2.addView(crashCollectionLabelTextView);
		linearLayoutRow2.addView(crashCollectionOutputTextView);

		linearLayoutRow3.addView(preemptionLabelTextView);
		linearLayoutRow3.addView(preemptionOutPutTextView);
		linearLayoutRow3.addView(whenEnforcedLabelTextView);
		linearLayoutRow3.addView(whenEnforcedOutputTextView);

		linearLayoutInner.addView(linearLayoutRow1);
		linearLayoutInner.addView(linearLayoutRow2);
		linearLayoutInner.addView(linearLayoutRow3);

		linearLayoutMain.addView(linearLayoutInner);
		
		return linearLayoutMain;

	}

	OnClickListener eventTitleOnClickListner = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			if (id == R.id.ViewRulesRefreshButton) {

				if (NetWork_Information.isNetworkAvailable(context)) {
					removeAllRulesFromScreen();
					displyAsync = new ruleListDisplyAsync();
					displyAsync.execute();
					return;
				} else {
					NetWork_Information.noNetworkConnectiondialog(context);
					return;
				}
			}
			if (id < 1000) {
				SCRule scRuleActive = activeRulesArrayList.get(v.getId() - ACTIVE_RULE);
				Intent activeRuleIntent = new Intent(ViewRulesListActivity.this, ViewRuleDetailsActivity.class);
				activeRuleIntent.putExtra("scRule", scRuleActive);
				startActivity(activeRuleIntent);
			}
			if (id >= 1000) {
				SCRule scRuleInactive = inActiveRulesArrayList.get(v.getId() - INACTIVE_RULE);
				Intent inactiveRuleIntent = new Intent(ViewRulesListActivity.this, ViewRuleDetailsActivity.class);
				inactiveRuleIntent.putExtra("scRule", scRuleInactive);
				startActivity(inactiveRuleIntent);
			}

		}
	};

	public synchronized void downloadRules() {

		currentLocation = TrackingService.LOCATION_FOR_RULE;
		inActiveRulesArrayList = new ArrayList<SCRule>();
		activeRulesArrayList = new ArrayList<SCRule>();

		if (currentLocation != null) {

			rulesAccountRequest = new RulesAccountRequest(context, currentLocation.getLatitude(), currentLocation.getLongitude(), 5);

			rulesHttpResponse = rulesAccountRequest.ruleRequest();
			message = rulesAccountRequest.getFailureMessage();

			if (rulesHttpResponse != null) {
				rulesArrayList = rulesAccountRequest.handleGetResponseSCRule(rulesHttpResponse);
			} else {
				SCRule scRule = new SCRule();
				scRule.setLabel("No rules found for this area.");
				activeRulesArrayList.add(scRule);
				progressDialog.dismiss();
			}
			rulesInsertionOrUpdation();
			getRuleListFromDataBase();
		} else {
			message = "Current location is not resolved as of yet. Please try again later.";

		}

	}

	public void rulesInsertionOrUpdation() {

		rulesRepository.updateInActive();

		boolean ruleIdPresent = false;
		for (int i = 0; i < rulesArrayList.size(); i++) {

			int ruleID = rulesArrayList.get(i).getId();
			ruleIdPresent = rulesRepository.ruleIdPresentInTable(String.valueOf(ruleID));

			if (ruleIdPresent) {
				rulesRepository.updateRules(rulesArrayList.get(i));
			} else {

				rulesRepository.insertRules(rulesArrayList.get(i));

			}
		}
	}

	
	private class ProgressThread extends Thread {

		ProgressThread() {
		}

		public void run() {
			try {

				downloadRules();

			} catch (Exception e) {
				// TODO: handle exception
			}

			handler.sendEmptyMessage(0);
		}
	}

	@Override
	protected void onResume() {

		super.onResume();
		StateAddress.currentActivity = this;
	}

	class ruleListDisplyAsync extends AsyncTask<Void, Void, Void> {

		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(ViewRulesListActivity.this);
			dialog.setMessage("Loading wait...");
			dialog.show();
			dialog.setCancelable(cancelRule);

			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {

			try {
				downloadRules();
			} catch (NullPointerException e) {

			} catch (Exception e) {
				// TODO: handle exception
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			super.onPostExecute(result);
			if (rulesHttpResponse == null) {

				UIUtils.OkDialog(context, message);
			}
			getRuleListFromDataBase();
			// setListArrayAdapter();
			setRulesOnScreen();

			dialog.cancel();

		}

	}
	
	private void removeAllRulesFromScreen() {
		innerRuleLinearLayout.removeAllViews();
	}

	private void setRulesOnScreen() {
		
//		Log.v("activeRulesArrayList", "activeRulesArrayList" + activeRulesArrayList.size());
//		Log.v("activeRulesArrayList", "inactiveRulesArrayList" + inActiveRulesArrayList.size());
		
		LinearLayout rulesLinearLayout;
		
		innerRuleLinearLayout.addView(activeTestView);
		if (activeRulesArrayList.size() > 0) {
			

			for (int i = 0; i < activeRulesArrayList.size(); i++) {

				rulesLinearLayout = drawRules(i, activeRulesArrayList);
				innerRuleLinearLayout.addView(rulesLinearLayout);
				rulesLinearLayout.setId(ACTIVE_RULE + i);
			}
		} else {
			rulesLinearLayout = noRulesFoundLayout("No active rules found");
			innerRuleLinearLayout.addView(rulesLinearLayout);
		}
		
		innerRuleLinearLayout.addView(inactiveTextView);
		if (inActiveRulesArrayList.size() > 0) {
			

			for (int i = 0; i < inActiveRulesArrayList.size(); i++) {
				rulesLinearLayout = drawRules(i, inActiveRulesArrayList);
				innerRuleLinearLayout.addView(rulesLinearLayout);
				int id = i + INACTIVE_RULE;
				rulesLinearLayout.setId(id);
			}
		} else {
			rulesLinearLayout = noRulesFoundLayout("No inactive rules found");
			innerRuleLinearLayout.addView(rulesLinearLayout);
		}
	}
}
