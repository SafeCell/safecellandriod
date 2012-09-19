package com.safecell.networking;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.safecell.dataaccess.FakeLocationRepository;
import com.safecell.model.SCFakeLocation;

public class FakeLocationJSONhelper {

	Context context;
	FakeLocationRepository fakeLocationRepository;
	SCFakeLocation fakeLocation = new SCFakeLocation();

	public FakeLocationJSONhelper(Context context) {
		super();
		this.context = context;
		fakeLocationRepository = new FakeLocationRepository(context);
	}
	public FakeLocationJSONhelper(Context[] contexts)
	{
		super();
		this.context = contexts[0];
		fakeLocationRepository = new FakeLocationRepository(context);
	}
	public void storeFakeLocationFiles() {
		String filepath = "json/";

		try {
			String[] Array = context.getAssets().list("json");

			for (int j = 0; j < Array.length; j++) {

				//Log.v("Safecell :"+"j=" + j, "" + Array[j]);
				filepath = filepath + Array[j];
				
				if (fakeLocationRepository.isFileNameExist(filepath)) {
					InputStream is = context.getResources().getAssets().open(
							filepath);

					byte[] buffer = new byte[is.available()];
					while (is.read(buffer) != -1)
						;

					String jsontext = new String(buffer);
					try {
						JSONArray entries = new JSONArray(jsontext);
						for (int i = 0; i < entries.length(); i++) {
							JSONObject post = entries.getJSONObject(i);

							//Log.v("Safecell :"+"estimatedSpeed", post
//									.getString("estimatedSpeed"));
							fakeLocation.setEstimatedSpeed(post
									.getString("estimatedSpeed"));

							//Log.v("Safecell :"+"longitude", post.getString("longitude"));
							fakeLocation.setLongitude(post
									.getString("longitude"));

							fakeLocation.setFileName(filepath);

							//Log.v("Safecell :"+"timeStamp", post.getString("timeStamp"));
							fakeLocation.setTimeStamp(post
									.getString("timeStamp"));

							//Log.v("Safecell :"+"latitude", post.getString("latitude"));
							fakeLocation
									.setLatitude(post.getString("latitude"));

							fakeLocationRepository
									.insertFakeLocation(fakeLocation);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					//Log.v("Safecell :"+"cusor", "else Statment");
				}
				filepath = "json/";
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//class JSONAsynch
}
