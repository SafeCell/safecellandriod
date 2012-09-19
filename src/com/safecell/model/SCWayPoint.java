 package com.safecell.model;


import org.json.JSONException;
import org.json.JSONObject;

public class SCWayPoint{
	private int wayPointID;
	private int journeyID;
	private String timeStamp;
	private double latitude;
	private double longitude;
	private double estimatedSpeed;
	private boolean isBackground = false;
	//WayPointStore pointStore = new WayPointStore();
	//private Context context;
	
	public boolean isBackground() {
		return isBackground;
	}
	public void setBackground(boolean isBackground) {
		this.isBackground = isBackground;
	}
	public SCWayPoint()
	{	
		
	}
	public SCWayPoint(JSONObject waypointJsonObject) throws JSONException
	{
		this.setLatitude(waypointJsonObject.getDouble("latitude"));
		this.setLongitude(waypointJsonObject.getDouble("longitude"));
		this.setTimeStamp(waypointJsonObject.getString("timeStamp"));
		this.setEstimatedSpeed(waypointJsonObject.getLong("estimatedSpeed"));
		
	}
	public SCWayPoint(int wayPointID, int journeyID, String timeStamp,
			double latitude, double longitude, double estimatedSpeed, boolean isBackground ){
		super();
		this.wayPointID = wayPointID;
		this.journeyID = journeyID;
		this.timeStamp = timeStamp;
		this.latitude = latitude;
		this.longitude = longitude;
		this.estimatedSpeed = estimatedSpeed;
		this.isBackground =isBackground;
		
	}
	
	public double getEstimatedSpeed() {
		return estimatedSpeed;
	}
	public void setEstimatedSpeed(double estimatedSpeed) {
		this.estimatedSpeed = estimatedSpeed;
	}
	public int getWayPointID() {
		return wayPointID;
	}
	public void setWayPointID(int wayPointID) {
		this.wayPointID = wayPointID;
	}
	public int getJourneyID() {
		return journeyID;
	}
	public void setJourneyID(int journeyID) {
		this.journeyID = journeyID;
	}
	public String getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	
	
	
	

}
