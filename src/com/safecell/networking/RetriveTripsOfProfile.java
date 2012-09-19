package com.safecell.networking;

import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.safecell.dataaccess.JourneyEventsRepository;
import com.safecell.dataaccess.TripJourneysRepository;
import com.safecell.dataaccess.TripRepository;
import com.safecell.model.SCJournyEvent;
import com.safecell.model.SCTrip;
import com.safecell.model.SCTripJourney;
import com.safecell.utilities.DateUtils;
import com.safecell.utilities.StreamToStringHelper;
import com.safecell.utilities.URLs;

public class RetriveTripsOfProfile {
	//String result="";
	JSONObject jsonObject = new JSONObject();
	int profileID;
	String ApiKey="";
	Context context;
	TripJourneysRepository tripJourneysRepository;
	
	public RetriveTripsOfProfile(Context context,int profileID, String apiKey){
		this.profileID = profileID;
		this.ApiKey = apiKey;
		this.context = context;
		
	}
	public void retrive(){
		JSONArray tripJSONArray = new JSONArray();
		HttpClient client = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); // Timeout
		String url = Uri.decode(URLs.REMOTE_URL+"api/1/trips?profile_id="+profileID);
		//Log.v("Safecell :"+"Retrive Trips",""+url);
		
		try {
			
			HttpGet httpGet = new HttpGet(url);
			httpGet.setHeader("Content-Type", "Application/json");
			httpGet.setHeader("X-Api-Key",this.ApiKey);
		
			HttpResponse  response = client.execute(httpGet);
			handleResponce(response);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
	}
	
	private void  handleResponce(HttpResponse response)
	{
		
		JSONArray tripsJsonArray= new JSONArray();
		JSONObject jsonObject = new JSONObject();
		JSONObject tripJsonObject = new JSONObject();
		JSONArray journeysArray = new JSONArray();
		JSONObject journeyObject =  new JSONObject();
		
		//Log.v("Safecell :"+"Retrive Trip",response.getStatusLine().toString()+"" );
		if(response.getStatusLine().toString().equalsIgnoreCase("HTTP/1.1 200 OK")){
			/*InputStream instream = entity.getContent();
			result = StreamToStringHelper
					.convertStreamToString(instream);
			instream.close();*/
			
			
			try {
				InputStream instream = response.getEntity().getContent();
				String result=StreamToStringHelper.convertStreamToString(instream);
			//Log.v("Safecell :"+"Result= ",""+result);
			tripsJsonArray = new JSONArray(result);
			
			//Log.v("Safecell :"+"Retrive Trips",""+tripsJsonArray.toString(4));
			
			for(int i=0; i<tripsJsonArray.length();i++)
			{
				jsonObject = tripsJsonArray.getJSONObject(i);
				
				tripJsonObject = jsonObject.getJSONObject("trip");
				
				String tripName = tripJsonObject.getString("name");
				int tripId = tripJsonObject.getInt("id");
				int profile_id = tripJsonObject.getInt("profile_id");
				
				journeysArray = tripJsonObject.getJSONArray("journeys");
				//Log.v("Safecell :"+"Number Of Journeys= ",""+journeysArray.length());
				
				journeyObject = journeysArray.getJSONObject(0);
				
				int trip_id = journeyObject.getInt("trip_id");
				String ended_at = journeyObject.getString("ended_at");
				int jouneyId = journeyObject.getInt("id");
				int total_points = journeyObject.getInt("total_points");
				float miles_driven = (float)journeyObject.getDouble("miles_driven");
				String started_at = journeyObject.getString("started_at");
				
				JSONArray journeyEvents = journeyObject.getJSONArray("journey_events");
				//Log.v("Safecell :"+"Number on Journey Events= ",""+journeyEvents.length());
				//Log.v("Safecell :"+"Journey Events",journeyEvents.toString(4));
				
					for(int j=0;j<journeyEvents.length();j++)
					{
						JSONObject journeyEvent = journeyEvents.getJSONObject(j);
						
						String timestamp = journeyEvent.getString("timestamp");
						int event_id = journeyEvent.getInt("event_id");
						int points = journeyEvent.getInt("points");
						int jid = journeyEvent.getInt("id");
						int journey_id = journeyEvent.getInt("journey_id");
						String near= journeyEvent.getString("near");
						String description = journeyEvent.getString("description");
						
						SCJournyEvent scJournyEvent = new SCJournyEvent();
						scJournyEvent.setDescription(description);
						scJournyEvent.setId(event_id);
						scJournyEvent.setJourneyId(journey_id);
						scJournyEvent.setNear(near);
						scJournyEvent.setPoints(points);
						scJournyEvent.setTimeStamp(DateUtils.dateInMillSecond(timestamp)+"");				
						
						JourneyEventsRepository journeyEventsRepository = new JourneyEventsRepository(this.context);
						journeyEventsRepository.insertTripJourneys(scJournyEvent);
					
					}
				SCTrip scTrip = new SCTrip();
				scTrip.setName(tripName);
				scTrip.setTripId(tripId);
				
				TripRepository tripRepository = new TripRepository(this.context);
				tripRepository.saveTrip(scTrip);
				
				SCTripJourney  scTripJourney = new SCTripJourney();
				//scTripJourney.setEstimatedSpeed(estimatedSpeed);
				//scTripJourney.setInterruptions(interruptions);
				scTripJourney.setJourneyId(jouneyId);
				scTripJourney.setMiles(miles_driven);
				scTripJourney.setPoints(total_points);
				scTripJourney.setTripDate(started_at);
				scTripJourney.setTripId(tripId);
				scTripJourney.setTripName(tripName);
				
				tripJourneysRepository = new TripJourneysRepository(this.context);
				tripJourneysRepository.insertTripJourneys(scTripJourney);
				//scTripJourney.setWaypoints(waypoints);
				
				
				
				/*TripJourneysRepository tripJourneysRepository =new TripJourneysRepository(this.context);
				tripJourneysRepository.insertTripJourneys(scTripJourney);*/
				
				
			}
			
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
			
		}
		else
		{
			//Log.v("Safecell :"+"Retrive Trips","Bad Response");
		}
		
		
		
	}

}
