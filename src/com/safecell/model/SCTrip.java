package com.safecell.model;

import java.util.HashMap;

public class SCTrip {
	private int tripId;
	private String name;

	static SCTrip tripWithMap(HashMap<Object, Object> tripMap) {
		
		SCTrip trip = new SCTrip();
		trip.tripId = ((Integer) tripMap.get("id")).intValue();
		trip.name = ((String) tripMap.get("name")).toString();

		return trip;
	}

	public int getTripId() {
		return tripId;
	}

	public void setTripId(int tripId) {
		this.tripId = tripId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
