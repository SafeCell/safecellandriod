package com.safecell.model;


public class SCInterruption {
    
    public static final String CALL = "PHONE";
    
    public static final String SMS = "SMS";
    
    public static final String WEB = "WEB";
    
    public static final String EMAIL = "EMAIL";

	/*
	private boolean terminated_app = false;
	private String started_at;
	private String ended_at;
	private float estimatedSpeed=0;
	private double latitude=0.0;
	private double longitude=0.0;
	private boolean paused = false;
	*/
	
	private boolean terminated_app = false;
	private String started_at;
	private String ended_at;
	private String estimatedSpeed;
	private String latitude;
	private String longitude;
	private boolean paused = false;
	private boolean isSchooleZoneActive =false;
	private boolean isPhoneRuleActive = false;
	private String type = "NONE";
    public boolean isSchooleZoneActive() {
		return isSchooleZoneActive;
	}

	public void setSchooleZoneActive(boolean isSchooleZoneActive) {
		this.isSchooleZoneActive = isSchooleZoneActive;
	}

	public boolean isPhoneRuleActive() {
		return isPhoneRuleActive;
	}

	public void setPhoneRuleActive(boolean isPhoneRuleActive) {
		this.isPhoneRuleActive = isPhoneRuleActive;
	}

	public boolean isSmsRuleActive() {
		return isSmsRuleActive;
	}

	public void setSmsRuleActive(boolean isSmsRuleActive) {
		this.isSmsRuleActive = isSmsRuleActive;
	}
	private boolean isSmsRuleActive= false;
	
	public SCInterruption(){
		terminated_app = false;
		started_at="";
		ended_at="";
		estimatedSpeed="0";
		latitude="0.0";
		longitude="0.0";
	}
	
	public boolean isTerminated_app() {
		return terminated_app;
	}
	public void setTerminated_app(boolean terminatedApp) {
		terminated_app = terminatedApp;
	}
	public String getStarted_at() {
		return started_at;
	}
	public void setStarted_at(String startedAt) {
		started_at = startedAt;
	}
	public String getEnded_at() {
		return ended_at;
	}
	public void setEnded_at(String endedAt) {
		ended_at = endedAt;
	}
	public String getEstimatedSpeed() {
		
		return estimatedSpeed;
	}
	public void setEstimatedSpeed(String Speed) {	
		
		this.estimatedSpeed = Speed;
		
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String lat) {
		
		this.latitude = lat;
		
	}
	public String getLongitude() {
		
		return longitude;
	}
	public void setLongitude(String lng) {
		
		this.longitude = lng;
		
		
	}
	public boolean isPaused() {
		return paused;
	}
	public void setPaused(boolean paused) {
		
		this.paused = paused;
		//Log.v("Safecell :"+"SCInteruption isPaused",""+this.paused);
	}
	
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

	
}
