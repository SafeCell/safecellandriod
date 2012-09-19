package com.safecell.networking;

import com.safecell.AddTripActivity;
import com.safecell.HomeScreenActivity;
import com.safecell.dataaccess.InteruptionRepository;
import com.safecell.dataaccess.TempTripJourneyWayPointsRepository;
import com.safecell.model.SCWayPoint;
import com.safecell.utilities.DateUtils;
import com.safecell.utilities.FlurryUtils;
import com.safecell.utilities.URLs;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * @author Sandeep
 * 
 */
public class SubmitNewTripJourney {

	private Resources resources;
	private JSONArray wayPointArray, jsonArray;
	Context context;
	String stringFile;
	String wayPointStringFromFile, interuptionStringFromFile;
	StringEntity stringEntity;
	JSONObject outerJsonObject;
	HttpResponse response;
	private int accountID;
	private int profileID;
	private String apiKey;
	private String tripName, startDateTime, endDateTime;
	int totalMiles;
	JSONArray interruptionsJsonArray;
	String profileName;

	private String TAG = SubmitNewTripJourney.class.getSimpleName();
	// JSONArray wayPointArray, interruptionsJsonArray;

	public SubmitNewTripJourney(Context context) {
		this.context = context;

	}

	public SubmitNewTripJourney(Context context, int accountID, int profileID, String apiKey, String tripName, int totalMiles, String profileName) {
		interruptionsJsonArray = new JSONArray();
		this.context = context;
		resources = context.getResources();
		this.accountID = accountID;
		this.profileID = profileID;
		this.apiKey = apiKey;
		this.tripName = tripName;
		this.totalMiles = totalMiles;
		this.profileName = profileName;

	}

	/**
	 * @return JSONArray of wavepoints stored in table
	 *         temp_trip_journey_wavepoints
	 * 
	 * */
	public JSONArray readWayPoints() {

		TempTripJourneyWayPointsRepository tempTripJourneyWayPointsRepository = new TempTripJourneyWayPointsRepository(context);
		InteruptionRepository ir = new InteruptionRepository(context);
		interruptionsJsonArray = ir.getInteruptions();
		// Log.v("Safecell :"+"Interuption Json",interruptionsJsonArray.toString());

		// interruptionsJsonArray.getJSONObject(index)
		Cursor cursor = tempTripJourneyWayPointsRepository.selectTrip();

		if (cursor.getCount() > 0) {
			wayPointArray = new JSONArray();
			cursor.moveToFirst();

			try {

				for (int i = 0; i < cursor.getCount(); i++) {
					JSONObject wayPoint = new JSONObject();
					wayPoint.put("estimatedSpeed", cursor.getFloat(4));
					wayPoint.put("longitude", cursor.getDouble(3));
					wayPoint.put("latitude", cursor.getDouble(2));

					wayPoint.put("timeStamp", DateUtils.getTimeStamp(Long.parseLong(cursor.getString(1))));
					wayPoint.put("background", Boolean.valueOf(cursor.getString(cursor.getColumnIndex("isBackground"))));

					wayPointArray.put(wayPoint);

					if (i == 0) {
						startDateTime = DateUtils.getTimeStamp(Long.parseLong(cursor.getString(1)));
					}
					if (i == (cursor.getCount() - 1)) {
						endDateTime = DateUtils.getTimeStamp(Long.parseLong(cursor.getString(1)));
					}

					cursor.moveToNext();
				}
				cursor.close();
			} catch (Exception e) {
				e.printStackTrace();
				if (cursor != null) {
					cursor.close();
				}
			}
		}

		return wayPointArray;
	}


	private void processWavePointArrayList(long intrStartTimeInMilliSec,
			long intrEndTimeInMilliSec, ArrayList<SCWayPoint> wayPointArrayList) throws JSONException {
		
		long wavePointTimeStamp;
		TempTripJourneyWayPointsRepository tempTripJourneyWayPointsRepository = new TempTripJourneyWayPointsRepository(context);
		
		for(SCWayPoint waypoint : wayPointArrayList) {
			wavePointTimeStamp = DateUtils.dateInMillSecond(waypoint.getTimeStamp());
//			Log.v("SaveCell/SubmitNewTripJourney:", "wavePointTimeStamp" + wavePointTimeStamp);
			
			if ((wavePointTimeStamp >= intrStartTimeInMilliSec) && (wavePointTimeStamp <= intrEndTimeInMilliSec)) {
				tempTripJourneyWayPointsRepository.updateWaypointBackgroundFlag(waypoint.getWayPointID(), true);
//				Log.v("Safecell:ID", "waypoint.getWayPointID()="+waypoint.getWayPointID()+"="+"true");
			} else {
				tempTripJourneyWayPointsRepository.updateWaypointBackgroundFlag(waypoint.getWayPointID(), false);
//				Log.v("Safecell:ID", "waypoint.getWayPointID()="+waypoint.getWayPointID()+"="+"false");
			}
		}
	}

	private void correctBackgroundFlags() throws JSONException {

		TempTripJourneyWayPointsRepository tempTripJourneyWayPointsRepository = new TempTripJourneyWayPointsRepository(context);
	
		ArrayList<SCWayPoint> wayPointArrayList = new ArrayList<SCWayPoint>();
		
		// Log.v("Safecell :"+"Interuption Json",interruptionsJsonArray.toString());

		// interruptionsJsonArray.getJSONObject(index)
		Cursor cursor = tempTripJourneyWayPointsRepository.selectTrip();

		if (cursor.getCount() > 0) {
			
			cursor.moveToFirst();

			try {
				SCWayPoint scWayPoint = null;
				
				for (int i = 0; i < cursor.getCount(); i++) {
					scWayPoint = new SCWayPoint();
					
					scWayPoint.setWayPointID(cursor.getInt(0));
					
					scWayPoint.setTimeStamp(DateUtils.getTimeStamp(Long.parseLong(cursor.getString(1))));
					scWayPoint.setBackground(Boolean.valueOf(cursor.getString(cursor.getColumnIndex("isBackground"))));
										
					wayPointArrayList.add(scWayPoint);

					cursor.moveToNext();
				}
				cursor.close();
			} catch (Exception e) {
				e.printStackTrace();
				if (cursor != null) {
					cursor.close();
				}
			}
		}
		
		
		long startTimeInMilliSec;
		long endTimeInMilliSec;
		
		InteruptionRepository ir = new InteruptionRepository(context);
		JSONArray interruptionsArr = ir.getInteruptions();
	
		JSONObject intruptionJsonObject = null;
		
		for (int i = 0; i < interruptionsArr.length(); i++) {
			intruptionJsonObject = interruptionsArr.getJSONObject(i);

			startTimeInMilliSec = DateUtils.dateInMillSecond(intruptionJsonObject.getString("started_at"));
			endTimeInMilliSec = DateUtils.dateInMillSecond(intruptionJsonObject.getString("ended_at"));
//			Log.v("SaveCell/SubmitNewTripJourney:", "IntruptStartTime" + startTimeInMilliSec);
//			Log.v("SaveCell/SubmitNewTripJourney:", "IntruptEndTime" + endTimeInMilliSec);
			processWavePointArrayList(startTimeInMilliSec, endTimeInMilliSec,wayPointArrayList);
		}
		

	}

	public void createJson() {
		
		try {
			correctBackgroundFlags();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		jsonArray = readWayPoints();
		
		// jsonArray = wayPointArray
		// getTotalDistance();
		// JSONArray wayPointArray, interruptionsJsonArray;
		outerJsonObject = new JSONObject();
		JSONObject tripJsonObject = new JSONObject();
		JSONObject jounaryJsonObject = new JSONObject();

		JSONArray outerjounaryJsonArray = new JSONArray();

		try {
		    Log.d(TAG, "JSon Size "+interruptionsJsonArray.length());
//		    Toast.makeText(context, "Interruptions Count = "+interruptionsJsonArray.length(), Toast.LENGTH_LONG).show();
			jounaryJsonObject.put("waypoints_attributes", jsonArray);
			jounaryJsonObject.put("ended_at", endDateTime);
			jounaryJsonObject.put("started_at", startDateTime);
			jounaryJsonObject.put("interruptions_attributes", interruptionsJsonArray);
			jounaryJsonObject.put("miles_driven", totalMiles);
			jounaryJsonObject.put("total_points", "null");
			// interruptJsonObject.put("interruptions_attributes",interruptionsJsonArray);
			if (!HomeScreenActivity.genereateTripUniqueID().equalsIgnoreCase("")) {
				String uniqueTripId = HomeScreenActivity.genereateTripUniqueID();
//				Log.v("Safecell: SubmitNewTripjourney", "" + uniqueTripId);
				jounaryJsonObject.put("originator_token", uniqueTripId);
			}

			outerjounaryJsonArray.put(jounaryJsonObject);

			if (!AddTripActivity.TripName.equals("")) {
				tripName = AddTripActivity.TripName;
			}
			tripJsonObject.put("name", tripName);
			tripJsonObject.put("journeys_attributes", outerjounaryJsonArray);

			outerJsonObject.put("trip", tripJsonObject);
			Log.d(TAG, "Json "+outerjounaryJsonArray.toString());
			
			/*
			stringFile = outerJsonObject.toString(4);
			
			
			Log.v("Safecell :" + "StringFile", stringFile);

			FileOutputStream fileOutputWrite = context.openFileOutput("Submit Trip Request", Context.MODE_APPEND);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputWrite);
			outputStreamWriter.append(stringFile);
			outputStreamWriter.flush();
			*/
			
		} catch (JSONException e6) {

			e6.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	private void logSaveTripFailureToFlurry(int statusCode) {

		Date date = new Date();
		String utcDate = DateUtils.getTimeStamp(date.getTime());

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("accountId", "" + accountID);
		parameters.put("profileId", "" + profileID);
		parameters.put("name", profileName);
		parameters.put("timestamp", utcDate);

		switch (statusCode) {
		case 500:
			FlurryUtils.logEvent("Trip Save Failed : 500 Internal Server error", parameters);
			break;
		case 502:
			FlurryUtils.logEvent("Trip Save Failed : 502 App Failed To Respond", parameters);
			break;
		case 504:
			FlurryUtils.logEvent("Trip Save Failed : 504 Backlog Too Deep", parameters);
		case 422:
			FlurryUtils.logEvent("Trip Save Failed : 422 Maintenance Mode", parameters);
		case 503:
			FlurryUtils.logEvent("Trip Save Failed : 503 Heroku Ouchie Page", parameters);
			break;
		default:
			FlurryUtils.logEvent("Trip Save Failed : " + statusCode + " Unexpected Error", parameters);
			break;
		}
	}

	void GZIpfileSend() {

	}

	public HttpResponse postRequest() {

		DefaultHttpClient client = new DefaultHttpClient();
		
		client.addRequestInterceptor(new HttpRequestInterceptor() {

			public void process(final HttpRequest request,
					final HttpContext context) throws HttpException,
					IOException {
				if (!request.containsHeader("Accept-Encoding")) {
//					Log.v("Header", "Accept-Encoding");
					request.addHeader("Accept-Encoding", "gzip");
				}
			}

		});

		client.addResponseInterceptor(new HttpResponseInterceptor() {

			public void process(final HttpResponse response,
					final HttpContext context) throws HttpException,
					IOException {
				HttpEntity entity = response.getEntity();
				Header ceheader = entity.getContentEncoding();
				if (ceheader != null) {
					HeaderElement[] codecs = ceheader.getElements();
					for (int i = 0; i < codecs.length; i++) {
						if (codecs[i].getName().equalsIgnoreCase("gzip")) {
//							Log.v("SafeCell: GZipInStream", "GzipDecompressingEntity");
							response.setEntity(new GzipDecompressingEntity(response.getEntity()));
							return;
						}
					}
				}
			}

		});

		HttpConnectionParams.setConnectionTimeout(client.getParams(), 60000); // Timeout

		String url = URLs.REMOTE_URL + "api/1/trips?account_id=" + accountID + "&profile_id=" + profileID;
		HttpPost post = new HttpPost(url);
//		System.out.println(url);

		try {

			ByteArrayOutputStream arr = new ByteArrayOutputStream();

			OutputStream zipper = new GZIPOutputStream(arr);
			// Messaged on the file
//			Log.d(TAG, "Trip Name = "+tripName);
//			generateMessageOnSD(tripName,outerJsonObject.toString());
			
			zipper.write(outerJsonObject.toString().getBytes());
			zipper.close();

			ByteArrayEntity entity = new ByteArrayEntity(arr.toByteArray());
			entity.setContentEncoding("gzip");

			// stringEntity = new StringEntity(outerJsonObject.toString());
			post.setHeader("Content-Type", "application/json");
			post.setHeader("x-api-key", apiKey);
//			System.out.println(apiKey);

			post.setEntity(entity);
			response = client.execute(post);
			// Log.v("Safecell :"+"Trip Response",
			// response.getStatusLine().toString());
//			System.out.println("----------------------------------------");
//			System.out.println(response.getStatusLine());
//			System.out.println(response.getLastHeader("Content-Encoding"));
//			System.out.println(response.getLastHeader("Content-Length"));
//			System.out.println("----------------------------------------");
			
			if (response.getStatusLine().toString().equalsIgnoreCase("HTTP/1.1 200 OK")) {
				return response;
			} else {
//				Log.v("safecell", response.getStatusLine().toString());
				logSaveTripFailureToFlurry(response.getStatusLine().getStatusCode());
				response = null;
			}
			
			return response;
		} catch (SocketTimeoutException e1) {
//			Log.v("SaveTrip", "SocketTimeoutException");
			response = null;
			post.abort();

		} catch (Exception e) {
			e.printStackTrace();
			response = null;
			post.abort();
		}
		return response;
	}
	
	/**
	 * 
	 * 
	 */
	
    public void generateMessageOnSD(String sFileName, String sBody) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "SafeCell");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName+ new Date().getTime());
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
            Log.d(TAG, "Dumped into the file");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
