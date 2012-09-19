package com.safecell.model;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import android.graphics.Bitmap;

public class SCProfile extends SCAccount {

	private String deviceKey;
	private int accountID;
	private int profileId;
	
	private String phone;
	private String lastName;
	private boolean busDriver =false;
	private String points_earned;
	private String firstName;
	private String email;
	
	private ArrayList<Object> trips;
	private Bitmap userImage = null;
	private String licenses = null;
	private String deviceFamily = null;
	private String status = null;
	private String appVersion = null;
	private String expiresOn = null;
	
	static SCProfile profileFromMap(Map<Object, Object> profileMap)
	{
		return (new SCProfile());

	}
	
	public static String newUniqueDeviceKey() {
		UUID newUUID = UUID.randomUUID();
		String key = newUUID.toString();
		key = key.replace("-", "");
		return key;
	}
	
	public String JSONRepresentation(){
		
		return (new String());
		
	}

	public String JSONForPost(){
		
		return (new String());
		
	}
	
	
	public String getDeviceKey() {
		return deviceKey;
	}
	
	public void setDeviceKey(String deviceKey) {
		this.deviceKey = deviceKey;
	}
	
	
	public String getPoints_earned() {
		return points_earned;
	}
	public void setPoints_earned(String pointsEarned) {
		points_earned = pointsEarned;
	}
	public int getAccountID() {
		return accountID;
	}

	public void setAccountID(int accountID) {
		this.accountID = accountID;
	}

	public boolean isBusDriver() {
		return busDriver;
	}

	public void setBusDriver(boolean busDriver) {
		this.busDriver = busDriver;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public int getProfileId() {
		return profileId;
	}

	public void setProfileId(int profileId) {
		this.profileId = profileId;
	}

	public ArrayList<Object> getTrips() {
		return trips;
	}

	public void setTrips(ArrayList<Object> trips) {
		this.trips = trips;
	}

	public Bitmap getUserImage() {
		return userImage;
	}

	public void setUserImage(Bitmap userImage) {
		this.userImage = userImage;
	}

	public String getLicenses() {
		return licenses;
	}

	public void setLicenses(String licenses) {
		this.licenses = licenses;
	}

	public String getDeviceFamily() {
		return deviceFamily;
	}

	public void setDeviceFamily(String deviceFamily) {
		this.deviceFamily = deviceFamily;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public String getExpiresOn() {
		return expiresOn;
	}

	public void setExpiresOn(String expiresOn) {
		this.expiresOn = expiresOn;
	}

}
