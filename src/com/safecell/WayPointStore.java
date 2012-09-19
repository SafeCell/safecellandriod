package com.safecell;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import org.json.JSONObject;

import android.content.Context;

import com.safecell.dataaccess.TempTripJourneyWayPointsRepository;
import com.safecell.model.SCWayPoint;

public class WayPointStore {
	public static final String WAY_POINT_FILE = "waypoints.txt";
	private FileOutputStream fileOutputWrite;
	private JSONObject jsonObject;
	private OutputStreamWriter outputStreamWriter;
	//private boolean FIRST_NODE = true;
	private Context context;

	public WayPointStore(Context ctx) {
		this.context =ctx;
	}

	public void  insertWayPoint(SCWayPoint scWayPoint) {
		
	/*
		Map<String, Object> jMap = new HashMap<String, Object>();
		jMap.put("longitude", wayPoint.getLongitude());
		jMap.put("estimatedSpeed", wayPoint.getEstimatedSpeed());
		jMap.put("timeStamp", "" + wayPoint.getTimeStamp());
		jMap.put("latitude", wayPoint.getLatitude());
		jsonObject = new JSONObject(jMap);

		try {
			Log.v("Safecell :"+"Json Object", jsonObject.toString(4));
		} catch (Exception e) {
			// TODO: handle exception
		}
		*/
		
		TempTripJourneyWayPointsRepository tempTripJourneyWayPointsRepository = new TempTripJourneyWayPointsRepository(context);
		tempTripJourneyWayPointsRepository.intsertWaypoint(scWayPoint);
		
		

	}

	/*public void storeWayPoint() {

		try {
			fileOutputWrite = context.openFileOutput(WAY_POINT_FILE,
					Context.MODE_APPEND);

			// writeToFile(jsonObject.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try { 

			outputStreamWriter = new OutputStreamWriter(fileOutputWrite);

			if (FIRST_NODE) {
				outputStreamWriter.append("[");
				outputStreamWriter.append(jsonObject.toString());
				Log.v("Safecell :"+"First Node 1",""+FIRST_NODE);
				FIRST_NODE = false;
				outputStreamWriter.flush();
			} else {
				outputStreamWriter.append(",");
				Log.v("Safecell :"+"First Node 2",""+FIRST_NODE);
				outputStreamWriter.append(jsonObject.toString());
				outputStreamWriter.flush();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void closeFile() {

		try {
			if(outputStreamWriter!=null)
			{
			outputStreamWriter.append("]");
			outputStreamWriter.flush();
			}//osw.close();

		} catch (IOException e) {
			e.printStackTrace();
		+-}
	}*/
}
