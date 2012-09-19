package com.safecell.networking;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.R.bool;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.provider.ContactsContract.RawContacts.Entity;
import android.util.Log;

import com.safecell.dataaccess.JourneyEventsRepository;
import com.safecell.dataaccess.TempTripJourneyWayPointsRepository;
import com.safecell.dataaccess.TripJourneyWaypointsRepository;
import com.safecell.dataaccess.TripJourneysRepository;
import com.safecell.dataaccess.TripRepository;
import com.safecell.model.SCJournyEvent;
import com.safecell.model.SCTrip;
import com.safecell.model.SCTripJourney;
import com.safecell.model.SCWayPoint;
import com.safecell.utilities.DateUtils;
import com.safecell.utilities.StreamToStringHelper;

public class SubmitNewTripJourneyResponceHandler {
	private Context context;
	public SubmitNewTripJourneyResponceHandler(Context context) {
		this.context =context;
	};

	private HttpResponse response;

	public void readResponce(HttpResponse httpResponse) throws SQLiteConstraintException,Exception {
		
			response = httpResponse;
		
			if (response != null) {
				InputStream in = response.getEntity().getContent();
				
				if (in != null) {

					String result = StreamToStringHelper.convertStreamToString(in);					
					
					/*
					FileOutputStream  fileOutputWrite = context.openFileOutput("Submit Trip Response",Context.MODE_APPEND);
					OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputWrite);
					outputStreamWriter.append(result);
					outputStreamWriter.flush();
					*/

					JSONObject JsonObject = new JSONObject(result);
					//Log.v("Safecell :"+"Responce",JsonObject.toString(4));
					JSONObject tripJsonObject = (JSONObject) JsonObject.get("trip");
					
					//Log.v("Safecell :"+"profile_id", tripJsonObject.getString("profile_id"));

					JSONArray journeysJsonArray = tripJsonObject.getJSONArray("journeys");
					JSONObject journeysJsonObject = (JSONObject) journeysJsonArray.get(0);
					JSONArray journey_eventsJasonArray = journeysJsonObject.getJSONArray("journey_events");
					JSONObject journey_eventsJsonObject = (JSONObject) journey_eventsJasonArray.get(0);
					int tripID = tripJsonObject.getInt("id");
					SCTrip scTrip = new SCTrip();
					scTrip.setName(tripJsonObject.getString("name"));
					scTrip.setTripId(tripJsonObject.getInt("id"));
					
					//Log.v("Safecell :"+"Trip Name:", tripJsonObject.getString("name"));
					//Log.v("Safecell :"+"id:", tripJsonObject.getString("id"));
					
					TripRepository tripRepository = new TripRepository(context);
					if (!tripRepository.tripExist(tripID)) {
						tripRepository.saveTrip(scTrip);
					}
					
					//tripRepository.SelectTrip();

					SCTripJourney scTripJourney = new SCTripJourney();

					scTripJourney.setTripId(journeysJsonObject
							.getInt("trip_id"));

					scTripJourney.setPoints(journeysJsonObject
							.getInt("total_points"));
					scTripJourney.setMiles(Float.parseFloat(journeysJsonObject
							.getString("miles_driven")));
					//scTripJourney.setMiles(Math.round(TrackingScreenActivity.DISTANCE_IN_MILES));

					scTripJourney.setTripDate(journeysJsonObject
							.getString("started_at"));
					scTripJourney.setJourneyId(journeysJsonObject.getInt("id"));
					scTripJourney.setEstimatedSpeed(10);

					
					TripJourneysRepository tripJourneysRepository = new TripJourneysRepository(context);
					boolean tripExistFlag = tripJourneysRepository.alreadyTripIdExit(scTripJourney.getJourneyId());
					if (!tripExistFlag) {
						tripJourneysRepository.insertTripJourneys(scTripJourney);
					 
//					Log.v("Safecell :"+"trip_id", journeysJsonObject.getString("trip_id"));
//					Log.v("Safecell :"+"ended_at", journeysJsonObject.getString("ended_at"));
//					Log.v("Safecell :"+"id", journeysJsonObject.getString("id"));
//					Log.v("Safecell :"+"total_points", journeysJsonObject
//							.getString("total_points"));
//					Log.v("Safecell :"+"miles_driven", journeysJsonObject
//							.getString("miles_driven"));
//					Log.v("Safecell :"+"started_at", journeysJsonObject
//							.getString("started_at"));
//
//					Log.v("Safecell :"+"journey_eventsJsonObject",
//					"_____________________________________________________");
//
//					Log.v("Safecell :"+"timestamp", ""+ journey_eventsJsonObject.getString("timestamp"));
//					Log.v("Safecell :"+"event_id", ""+ journey_eventsJsonObject.getInt("event_id"));
//					Log.v("Safecell :"+"points", ""+ journey_eventsJsonObject.getDouble("points"));
//					Log.v("Safecell :"+"id", "" + journey_eventsJsonObject.getInt("id"));
//					Log.v("Safecell :"+"journey_id", ""+ journey_eventsJsonObject.getInt("journey_id"));
//					Log.v("Safecell :"+"near", ""+ journey_eventsJsonObject.getString("near"));
//					Log.v("Safecell :"+"description", ""+ journey_eventsJsonObject.getString("description"));
//
//					Log.v("Safecell :"+"","_____________________________________________________");

					for(int i=0; i < journey_eventsJasonArray.length();i++)
					{
						journey_eventsJsonObject = (JSONObject) journey_eventsJasonArray.get(i);
						SCJournyEvent scJournyEvent = new SCJournyEvent();
						scJournyEvent.setTimeStamp( ""+Long.valueOf(DateUtils.dateInMillSecond(journey_eventsJsonObject.getString("timestamp"))));
						scJournyEvent.setId(journey_eventsJsonObject.getInt("id"));
						scJournyEvent.setPoints(journey_eventsJsonObject.getInt("points"));
						scJournyEvent.setJourneyId(journey_eventsJsonObject.getInt("journey_id"));
						scJournyEvent.setNear(journey_eventsJsonObject.getString("near"));
						scJournyEvent.setDescription(journey_eventsJsonObject.getString("description"));

						JourneyEventsRepository journeyEventsRepository = new JourneyEventsRepository(context);
						journeyEventsRepository.insertTripJourneys(scJournyEvent);
					}
					
					TempTripJourneyWayPointsRepository tempTripJourneyWayPointsRepository =new TempTripJourneyWayPointsRepository(context);
					Cursor waypointCursor = tempTripJourneyWayPointsRepository.selectTrip();
					if(waypointCursor.getCount()>0)
					{
					waypointCursor.moveToFirst();
					for (int i = 0; i < waypointCursor.getCount(); i++) {

						//JSONObject waypointJsonObject = waypointJsonArray.getJSONObject(i);
						SCWayPoint scWayPoint = new SCWayPoint();
						scWayPoint.setEstimatedSpeed(waypointCursor.getFloat(waypointCursor.getColumnIndex("estimated_speed")));
						scWayPoint.setJourneyID(scTripJourney.getTripId());
						scWayPoint.setLatitude(waypointCursor.getDouble(waypointCursor.getColumnIndex("latitude")));
						scWayPoint.setLongitude(waypointCursor.getDouble(waypointCursor.getColumnIndex("longitude")));
						scWayPoint.setTimeStamp(waypointCursor.getString(waypointCursor.getColumnIndex("timestamp")));
						scWayPoint.setBackground(Boolean.valueOf(waypointCursor.getString(waypointCursor.getColumnIndex("isBackground"))));
						
						TripJourneyWaypointsRepository waypointsRepository = new TripJourneyWaypointsRepository(context);
						waypointsRepository.insertWayPoint(scWayPoint);
						
						waypointCursor.moveToNext();
					}
					waypointCursor.close();
					}
					}
				} else {
					//Log.v("Safecell :"+"Responce Null", "Null");
				}
			}
		

	}

}
