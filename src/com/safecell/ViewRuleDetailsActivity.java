package com.safecell;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.safecell.dataaccess.LicenseRepository;
import com.safecell.model.SCRule;
import com.safecell.utilities.FlurryUtils;
import com.safecell.utilities.LocationSP;
import com.safecell.utilities.StateAddress;

public class ViewRuleDetailsActivity extends Activity{

	TextView labelTextView, whenEnforcedTextView,groupAffectedByThisLawTextView,detailsTextView;
	TextView tvLocation;
	TextView rulesDetailsHeadingTextView, secondaryRuleTextView;
	ImageView secondaryRuleImageView,crashCollectioImageView,preemptionImageView;
	Button startNewTripButton, homeButton, btnMyTrips, settingsButton,rulesButton;
	private Context context;
	private LicenseRepository licenseRepository;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setWindowAnimations(R.anim.null_animation);
		context = ViewRuleDetailsActivity.this;
		licenseRepository = new LicenseRepository(context);
		initUI();
	}

	public void initUI(){
		setContentView(R.layout.view_rule_details_layout);
		
		homeButton = (Button) findViewById(R.id.tabBarHomeButton);
		btnMyTrips = (Button) findViewById(R.id.tabBarMyTripsButton);
		rulesButton = (Button) findViewById(R.id.tabBarRulesButton);
		rulesButton.setBackgroundResource(R.drawable.rules_clicked);
		settingsButton = (Button) findViewById(R.id.tabBarSettingsButton);
		secondaryRuleTextView = (TextView) findViewById(R.id.ViewRuleDetailIsPrimary);
		tvLocation=(TextView) findViewById(R.id.tabBarCurentLocationTextView);
		tvLocation.setText(LocationSP.LocationSP);
		
		TabControler tabControler =new TabControler(ViewRuleDetailsActivity.this);
		homeButton.setOnClickListener(tabControler.getHomeTabOnClickListner());
		btnMyTrips.setOnClickListener(tabControler.getMyTripsOnClickListner());
		rulesButton.setOnClickListener(tabControler.getRulesOnClickListner());
		settingsButton.setOnClickListener(tabControler.getSettingOnClickListener());
		
		Intent intent =getIntent();
		SCRule rule=(SCRule)intent.getSerializableExtra("scRule");
		 
		labelTextView=(TextView) findViewById(R.id.RuleNameDetailsTextView);
		whenEnforcedTextView=(TextView) findViewById(R.id.whenEnforcedTextView);
		groupAffectedByThisLawTextView=(TextView) findViewById(R.id.GroupsAffectedByLawTextView);
		detailsTextView=(TextView) findViewById(R.id.Details);
		rulesDetailsHeadingTextView=(TextView) findViewById(R.id.RuleDetailsHeadingTextView);
		
		
		secondaryRuleImageView=(ImageView) findViewById(R.id.SecondaryRuleCheckedImageView);
		crashCollectioImageView=(ImageView) findViewById(R.id.CrashCollectionCheckedImageView);
		preemptionImageView=(ImageView) findViewById(R.id.PreemptionCheckedImageView);
		
		
		labelTextView.setText(rule.getLabel());
		whenEnforcedTextView.setText((rule.getWhen_enforced().equalsIgnoreCase("always"))? "Always" :"School Zone");
		
		String groupAffectLawStr = splitLicensesStr(rule.getLicenses());
		
		groupAffectedByThisLawTextView.setText(groupAffectLawStr);
		detailsTextView.setText(rule.getDetail());
		
		String headingLine = rule.getRule_type()+" usage banned in "+rule.getZone_name();
		rulesDetailsHeadingTextView.setText(headingLine);
		//
		secondaryRuleTextView.setText((rule.isPrimary()== true)?"Primary Rules":"Secondary Rules");
		
		secondaryRuleImageView.setBackgroundResource(R.drawable.rule_item_ckecked);
		crashCollectioImageView.setBackgroundResource((rule.isCrash_collection()==true)?R.drawable.rule_item_ckecked:R.drawable.rule_item_unckecked);
		preemptionImageView.setBackgroundResource((rule.isPreemption()==true)?R.drawable.rule_item_ckecked:R.drawable.rule_item_unckecked);
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

	private String splitLicensesStr(String licenses)
	{
		String displayLicense = "";
		//String licenses = "car| bus| class";
		licenses = licenses.replace("|", "/");
		String[]vaStrings=licenses.split("/");
		 
		  for (int i = 0; i < vaStrings.length; i++) {
//		   Log.v("Safecell :"+""+i,vaStrings[i].trim());
		   
		   String TempdisplayLicense = vaStrings[i].trim();
		   if (TempdisplayLicense.equalsIgnoreCase("all")) {
			   displayLicense ="All";
		   }else{
			   String licensesName = licenseRepository.selectGetLicenseName(TempdisplayLicense);
			   displayLicense += (i == 0 ? licensesName : ", "+licensesName);
		   }
		  
		  
		  }
//		Log.v("Safecell :"+"displayLicense ", displayLicense);
		
		return displayLicense;
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {

			Intent homeScreen = new Intent(ViewRuleDetailsActivity.this,ViewRulesListActivity.class);
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
