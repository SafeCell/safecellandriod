package com.safecell.model;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.safecell.utilities.DateUtils;

public class SCSchool {
	
	private int id;
	
	private String name;
	private String address;
	private String zipcode;
	private String city;
	private String state;
	
	private double latitude;
	private double longitude;
	private double distance;
	
	private Date createdAt;
	private Date updatedAt;
	
	
	public static SCSchool schoolFromJSONObject(JSONObject schoolJSONObject) throws JSONException {
		JSONObject propertiesObj = schoolJSONObject.getJSONObject("school");
		
		if(propertiesObj == null) {
			return null;
		}
		
		SCSchool school = new SCSchool();
		
		school.id = propertiesObj.getInt("id");
		
		school.name = propertiesObj.getString("name");
		school.address = propertiesObj.getString("address");
		school.zipcode = propertiesObj.getString("zipcode");
		school.city = propertiesObj.getString("city");
		school.state = propertiesObj.getString("state");
		
		school.latitude = propertiesObj.getDouble("latitude");
		school.longitude = propertiesObj.getDouble("longitude");
		school.distance = propertiesObj.getDouble("distance");
		
		String createdAt = propertiesObj.getString("created_at");
		school.createdAt = new Date(DateUtils.dateInMillSecond(createdAt));
		
		String updatedAt = propertiesObj.getString("updated_at");
		school.updatedAt = new Date(DateUtils.dateInMillSecond(updatedAt));
		
		return school;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int schoolId) {
		this.id = schoolId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getZipcode() {
		return zipcode;
	}
	
	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}
	
	public String getCity() {
		return city;
	}
	
	public void setCity(String city) {
		this.city = city;
	}
	
	public String getState() {
		return state;
	}
	
	public void setState(String state) {
		this.state = state;
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
	
	public double getDistance() {
		return distance;
	}
	
	public void setDistance(double distance) {
		this.distance = distance;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

}
