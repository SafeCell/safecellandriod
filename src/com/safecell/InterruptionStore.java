package com.safecell;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.util.Log;

import com.safecell.model.SCInterruption;


public class InterruptionStore {

	public static final String INTERRUPTION_POINT_FILE = "interruptions.txt";
	private FileOutputStream fileOutputWrite;
	private JSONObject jsonObject;
	private OutputStreamWriter outputStreamWriter;
	private static boolean firstNode = true;
	//public static Context context;

	public InterruptionStore(){
		
	}
	public InterruptionStore(SCInterruption interruption) {
		
		//context = WayPointStore.context;
		Map<String, Object> interruptionMap = new HashMap<String, Object>();
		
		//Log.v("Safecell :"+"Langitude String Value",""+interruption.getLongitude());
		//Log.v("Safecell :"+"latitude String Value",""+interruption.getLatitude());
		
		//Log.v("Safecell :"+"estimated_speed String Value",""+interruption.getEstimatedSpeed());
		//Log.v("Safecell :"+"Started",""+interruption.getStarted_at());
		//Log.v("Safecell :"+"Terminated ",""+interruption.isTerminated_app());
		//Log.v("Safecell :"+"Paused",""+interruption.isPaused());
		
		interruptionMap.put("terminated_app",interruption.isTerminated_app());
		interruptionMap.put("started_at", interruption.getStarted_at());		
		interruptionMap.put("longitude",  Double.valueOf(interruption.getLongitude()).doubleValue());		
		interruptionMap.put("latitude", Double.valueOf(interruption.getLatitude()).doubleValue());		
		interruptionMap.put("ended_at", interruption.getEnded_at());
		interruptionMap.put("paused", interruption.isPaused());		
		interruptionMap.put("estimated_speed", Float.valueOf(interruption.getEstimatedSpeed()).floatValue());
		
		jsonObject = new JSONObject(interruptionMap);		
		
		//Log.v("Safecell :"+"Json Object", jsonObject.toString());
	}

	/*public void storeInterruption() {

		try {
			
			//fileOutputWrite = context.openFileOutput(INTERRUPTION_POINT_FILE,Context.MODE_APPEND);

			// writeToFile(jsonObject.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try { // catches IOException below

			outputStreamWriter = new OutputStreamWriter(fileOutputWrite);

			if (firstNode) {
				outputStreamWriter.append("[");
				outputStreamWriter.append(jsonObject.toString());
				Log.v("Safecell :"+"First Node 1",""+firstNode);
				firstNode = false;
				outputStreamWriter.flush();
			} else {
				outputStreamWriter.append(",");
				Log.v("Safecell :"+"First Node 2",""+firstNode);
				outputStreamWriter.append(jsonObject.toString());
				outputStreamWriter.flush();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void closeFile() {
		try {
			outputStreamWriter.append("]");
			outputStreamWriter.flush();
			//osw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/
}
