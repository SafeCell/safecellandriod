package com.safecell;

import java.util.ArrayList;

import org.apache.http.HttpResponse;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.safecell.dataaccess.ProfilesRepository;
import com.safecell.dataaccess.RulesRepository;
import com.safecell.model.SCRule;
import com.safecell.networking.RulesAccountRequest;
import com.safecell.utilities.DistanceAndTimeUtils;

public class RulesDownload {

	private String TAG = "RulesDownload";
	private Location lastRulesDownloadlocation;
	private final double RULE_UPDATE_RADIUS = 5;
	private double distanceSinceLastDownload;
	private RulesAccountRequest rulesAccountRequest;
	public ArrayList<SCRule> scRules = new ArrayList<SCRule>();
	private boolean isPhoneActive = false ;
	private boolean isSmsActive = false ;
	private SCRule scRule;
	private Context context;
	private RulesRepository rulesRepository; 
	private ProfilesRepository profilesRepository;
	private String currentProfileLicenseKey;
	private boolean ruleDownloadFailed = false;
	
	public RulesDownload(Context context) {
		this.context = context;
		rulesRepository = new RulesRepository(context);
		profilesRepository = new ProfilesRepository(context);
		currentProfileLicenseKey = profilesRepository.getLicenseKey();
		//scRules = new ArrayList<SCRule>();
	}

	public void loactionChangedForRule(Location location, Context context) {
		
		
		
		if (lastRulesDownloadlocation != null) {

			double distance = DistanceAndTimeUtils.distFrom(
					lastRulesDownloadlocation.getLatitude(),
					lastRulesDownloadlocation.getLongitude(), location
							.getLatitude(), location.getLongitude());
			//Log.v("Safecell :"+TAG + "distance", distance + "");

			distanceSinceLastDownload += distance;

			if (distanceSinceLastDownload >= RULE_UPDATE_RADIUS) {
				distanceSinceLastDownload = 0;
				lastRulesDownloadlocation = location;
				startDownloadThread();
				// downloadRule();
			
			}
			
			if (ruleDownloadFailed == true) {
				distanceSinceLastDownload = 0;
				lastRulesDownloadlocation = location;
				startDownloadThread();
				ruleDownloadFailed = false;
			}

		} else {

			distanceSinceLastDownload = 0;
			lastRulesDownloadlocation = location;
			startDownloadThread();
			// downloadRule();
		
		}
	}

	private synchronized void startDownloadThread() {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				downloadRule();

			}
		};

		Thread t = new Thread(runnable);
		t.start();
	}

	private void downloadRule() {
		rulesAccountRequest = new RulesAccountRequest(context,
				lastRulesDownloadlocation.getLatitude(),
				lastRulesDownloadlocation.getLongitude(), RULE_UPDATE_RADIUS);
		HttpResponse rulesHttpResponse = rulesAccountRequest.ruleRequest();

		if (rulesHttpResponse != null) {
			ruleDownloadFailed = false;
			synchronized (scRules) {
				scRules = rulesAccountRequest
						.handleGetResponseSCRule(rulesHttpResponse);
			} 			
		}else {
			ruleDownloadFailed = true;
		}
		rulesInsertionOrUpdation();
	}

	private boolean isSMSOrEmailRule(int index) { 
		String rule_type = scRules.get(index).getRule_type();

		if (rule_type.equalsIgnoreCase("sms")
				|| rule_type.equalsIgnoreCase("email")) {

			return true;
		}
		return false;

	}

	private boolean isSchoolZoneOnly(int index) {

		String isSchoolZone = scRules.get(index).getWhen_enforced();
		
		if (isSchoolZone.equalsIgnoreCase("always")) {
			return true;
		}

		return false;
	}

	private boolean isPhoneRule(int index) {

		String isPhone = scRules.get(index).getRule_type();
		if (isPhone.equalsIgnoreCase("phone")) {
			return true;
		}
		return false;
	}

	
	public void updateRulesStatusAsPerSchoolZone(boolean schoolZoneActive) {

		synchronized (scRules) {

			if (scRules.size() == 0) {
				
				ruleApplyOnTrackingScreen();
				return;

			} else {

				boolean phoneRuleFound = false;
				boolean smsOrEmailRuleFound = false;
				boolean smsRuleApplicableForAllZones = false;
				boolean phoneRuleApplicableForAllZones = false;

				for (int i = 0; i < scRules.size(); i++) {
				
					if (!(scRules.get(i).appliesToLicenseClass(currentProfileLicenseKey))) {
						// Log.v("Safecell :"+"Rule does not apply to"+ scRules.get(i).getLicenses(), currentProfileLicenseKey);
						continue;
					}
					
					//Log.v("Safecell :"+"Rule_type", scRules.get(i).getRule_type());
					
					if (isSMSOrEmailRule(i)) {

						smsOrEmailRuleFound = true;
						// Log.v("Safecell :"+"RuleSMs", "smsOrEmailRuleFound");

						if (isSchoolZoneOnly(i)) {
							smsRuleApplicableForAllZones = true;
						}
					}// end if email

					if (isPhoneRule(i)) {

						phoneRuleFound = true;
						// Log.v("Safecell :"+"RulePhone", "phoneRuleFound");
						if (isSchoolZoneOnly(i)) {
							phoneRuleApplicableForAllZones = true;
						}
					}// end if phone

				}//end for loop

				if (smsOrEmailRuleFound) {

					if (smsRuleApplicableForAllZones) {
						isSmsActive = true; 
						// Log.v("Safecell :"+"RuleSMs", "ApplyToall--smsOrEmailRuleFound");
					} else if ((!smsRuleApplicableForAllZones)
							&& schoolZoneActive) {
						// Log.v("Safecell :"+"RuleSMs", "SchoolZone--smsOrEmailRuleFound");
						isSmsActive = true; 
					} else {
						
						isSmsActive = false; 
					}
				} else {
					isSmsActive = false; 
					// Log.v("Safecell :"+"sms", "smsRule Not Found");
				}// end smsOrEmailRuleFound

				if (phoneRuleFound) {

					if (phoneRuleApplicableForAllZones) {
						// Log.v("Safecell :"+"RulePhone", "ApplyToall--PhoneRuleFound");
						isPhoneActive = true;
						isSmsActive = true; 
					} else if ((!phoneRuleApplicableForAllZones)
							&& schoolZoneActive) {
						// Log.v("Safecell :"+"RulePhone", "SchoolZone--PhoneRuleFound");
						isPhoneActive = true;
						isSmsActive = true; 
					} else {
						// Log.v("Safecell :"+"phone", "phoneRule Not Found");
						isPhoneActive = false;
					}
				} else {

					isPhoneActive = false;
					// Log.v("Safecell :"+"phone", "phoneRule Not Found");
				}// end phoneRuleFound
				
				 //Log.v("Safecell", "phone: " + isPhoneActive + " sms: " + isSmsActive + " school: " + schoolZoneActive);
				ruleApplyOnTrackingScreen();
				
			}// first if else
		}// end synchronized
	
	}

	public void ruleApplyOnTrackingScreen (){
		TrackingScreenActivity.updateRulesUI(isPhoneActive, isSmsActive);
	}
	
	
	public void rulesInsertionOrUpdation() {
		
		rulesRepository.updateInActive();
		
		boolean ruleIdPresent = false;
		for (int i = 0; i < scRules.size(); i++) {

			int ruleID = scRules.get(i).getId();
			ruleIdPresent = rulesRepository.ruleIdPresentInTable(String.valueOf(ruleID));
			
			if (ruleIdPresent)

			{
				rulesRepository.updateRules(scRules.get(i));
				//Log.v("Safecell :"+"update Rule", "update");
				//activeRulesArrayList.add(rulesArrayList.get(i));

			} else {

				rulesRepository.insertRules(scRules.get(i));
				//activeRulesArrayList.add(scRules.get(i));
				//Log.v("Safecell :"+"insert Rule", "insert");
			}
		}
	}
}
