package com.safecell.model;

import java.util.ArrayList;

public class SCTripJourney {
	private int tripId;
	private int journeyId;
	private int points;
	private String tripName;
	private float miles;
	private String tripDate;
	private float estimatedSpeed;

	public int getTripId() {
		return tripId;
	}
	public void setTripId(int tripId) {
		this.tripId = tripId;
	}
	public int getJourneyId() {
		return journeyId;
	}
	public void setJourneyId(int journeyId) {
		this.journeyId = journeyId;
	}
	public int getPoints() {
		return points;
	}
	public void setPoints(int points) {
		this.points = points;
	}
	public String getTripName() {
		return tripName;
	}
	public void setTripName(String tripName) {
		this.tripName = tripName;
	}
	public float getMiles() {
		return miles;
	}
	public void setMiles(float miles) {
		this.miles = miles;
	}
	public String getTripDate() {
		return tripDate;
	}
	public void setTripDate(String tripDate) {
		this.tripDate = tripDate;
	}
	public float getEstimatedSpeed() {
		return estimatedSpeed;
	}
	public void setEstimatedSpeed(float estimatedSpeed) {
		this.estimatedSpeed = estimatedSpeed;
	}
	public ArrayList<Object> getWaypoints() {
		return waypoints;
	}
	public void setWaypoints(ArrayList<Object> waypoints) {
		this.waypoints = waypoints;
	}
	public ArrayList<Object> getInterruptions() {
		return interruptions;
	}
	public void setInterruptions(ArrayList<Object> interruptions) {
		this.interruptions = interruptions;
	}
	public ArrayList<Object> getJourneyEvents() {
		return journeyEvents;
	}
	public void setJourneyEvents(ArrayList<Object> journeyEvents) {
		this.journeyEvents = journeyEvents;
	}
	ArrayList<Object> waypoints = new ArrayList<Object>();
	ArrayList<Object> interruptions = new ArrayList<Object>();
	ArrayList<Object> journeyEvents = new ArrayList<Object>();

}
