package com.safecell.dataaccess;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

import com.safecell.model.SCInterruption;
import com.safecell.utilities.DateUtils;

public class InteruptionRepository extends DBAdapter {
    
    private final String TAG = InteruptionRepository.class.getSimpleName();

	public static final String CREATE_TABLE_QUERY = "CREATE TABLE interuptions (id integer primary key autoincrement," + "started_at nvarchar(250) NOT NULL," + "ended_at  nvarchar(250) DEFAULT '' NOT NULL," + "latitude double NOT NULL," + "longitude double NOT NULL," + "isTerminated boolean DEFAULT false," + "isPaused boolean DEFAULT false," + "estimated_speed double DEFAULT 0," + "is_school_zone_active nvarchar(10) NOT NULL," + "is_phone_rule_active boolean DEFAULT false," + "is_sms_rule_active boolean DEFAULT false ," + "type nvarchar(10) NOT NULL" + ")";

	public InteruptionRepository(Context context) {
		super(context);
	}

	public void insertInterupt(SCInterruption inturuption) {
		// Log.v("Safecell :"+"insertInteruption getEstimatedSpeed",""+inturuption.getEstimatedSpeed());
		Object[] args = { inturuption.getStarted_at(), inturuption.getLatitude(), inturuption.getLongitude(), inturuption.getEstimatedSpeed(), "" + inturuption.isPaused(), "" + inturuption.getType(), "" + inturuption.isPhoneRuleActive(), "" + inturuption.isSmsRuleActive(),"" + inturuption.getType()

		};
//		String sql = inturuption.getStarted_at()+ " " + inturuption.getLatitude()+ " " + inturuption.getLongitude()+ " " + inturuption.getEstimatedSpeed()+ " " + inturuption.isPaused() +  " " + inturuption.getType()+ " " + inturuption.isPhoneRuleActive()+ " " + inturuption.isSmsRuleActive() + " " + inturuption.getType();
//		generateMessageOnSD(sql);
		this.Query("INSERT INTO interuptions (" + "started_at, latitude, longitude, " + "estimated_speed, isPaused ," + "is_school_zone_active, is_phone_rule_active, is_sms_rule_active , type)" + "VALUES(?, ?, ?, ?, ?, ?, ?, ? , ?)", args);
	}

	public void deleteInteruptions() {
		this.deleteQuery("Delete from interuptions");
	}

	public void updateEndedAt(String endedAt) {
		Object[] args = { endedAt };
		this.Query("Update interuptions SET ended_at=? where id=(Select max(id) from interuptions)", args);
	}

	public void updateIsTerminated(Boolean isTerminated) {
		Object[] args = {};
		this.Query("Update interuptions SET isTerminated = 'true' where id=(Select max(id) from interuptions)", args);
	}

	public void updateIsPaused(Boolean isPaused) {
		Object[] args = { isPaused };
		this.Query("Update interuptions SET isPaused = ? where id=(Select max(id) from interuptions)", args);
	}

	public void updateEstimated_speed(double speed) {
		Object[] args = { speed };
		this.Query("Update interuptions SET estimated_speed = ? where id=(Select max(id) from interuptions)", args);

	}

	public String firstInterruptionStartTime() {
		String startTimeStr = null;
		String selectQuery = "select started_at from interuptions order by id asc limit 1";
		Cursor cursor = this.selectQuery(selectQuery, null);
		this.selectQuery(selectQuery, null).close();
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			startTimeStr = cursor.getString(0);
		}
		cursor.close();
		return startTimeStr;
	}

	public void deleteFirstInterruption() {
		this.deleteQuery("Delete from interuptions where id= (Select min(id) from interuptions)");
	}

	public void deleteLastInterruption() {
		this.deleteQuery("Delete from interuptions where id= (Select max(id) from interuptions)");
	}

	public JSONArray getInteruptions() {
		JSONArray interuptionsJSONArray = new JSONArray();
		JSONObject interuptionJSONObject;
		String[] args = {};
		String startedAt = "", endedAt = "";
		double latitude = 0;
		double longitude = 0;
		double estimated_speed;
		boolean isTerminated = false;
		boolean isPaused;
		// Map<String, Object> interruptionMap = new HashMap<String, Object>();
		Cursor cursor = this.selectQuery("Select * from interuptions", args);
		Log.d(TAG, "Count = "+cursor.getCount());
		this.selectQuery("Select * from interuptions", args).close();
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			for (int i = 0; i < cursor.getCount(); i++) {

				try {

					startedAt = cursor.getString(1);
					endedAt = startedAt;

					latitude = cursor.getDouble(3);
					longitude = cursor.getDouble(4);
					isTerminated = Boolean.valueOf(cursor.getString(5));
					isPaused = Boolean.valueOf(cursor.getString(6));
					String school_zone_flag = cursor.getString(cursor.getColumnIndex("is_school_zone_active"));
					boolean sms_rule_flag = Boolean.valueOf(cursor.getString(cursor.getColumnIndex("is_phone_rule_active")));
					boolean phone_rule_flag = Boolean.valueOf(cursor.getString(cursor.getColumnIndex("is_sms_rule_active")));
					String type = cursor.getString(cursor.getColumnIndex("type"));
					// Log.v("Safecell :"+"InterPepo Cursor ","Started at= "+startedAt+" Ended At= "+endedAt);
					estimated_speed = cursor.getDouble(7);

					interuptionJSONObject = new JSONObject();
					interuptionJSONObject.put("terminated_app", isTerminated);
					interuptionJSONObject.put("started_at", startedAt);
					interuptionJSONObject.put("longitude", longitude);
					interuptionJSONObject.put("latitude", latitude);
					interuptionJSONObject.put("ended_at", endedAt);
					interuptionJSONObject.put("paused", isPaused);
					interuptionJSONObject.put("estimated_speed", estimated_speed);
					interuptionJSONObject.put("school_zone_flag", school_zone_flag);
					interuptionJSONObject.put("sms_rule_flag", sms_rule_flag);
					interuptionJSONObject.put("phone_rule_flag", phone_rule_flag);
					interuptionJSONObject.put("type", type);
					Log.d(TAG, "Type = "+type);

					interuptionsJSONArray.put(interuptionJSONObject);
					cursor.moveToNext();

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
		cursor.close();
		try {
			// Log.v("Safecell :"+"Interuptions",""+interuptionsJSONArray.toString(4));

		} catch (Exception e) {
			// TODO: handle exception
		}

		return interuptionsJSONArray;
	}

	public boolean isAppTermited() {
		boolean isTermited = false;
		String[] argv = {};
		Cursor cursor = this.selectQuery("SELECT isTerminated from interuptions where id=(Select max(id) from interuptions)", argv);
		// this.selectQuery("SELECT isTerminated from interuptions where id=(Select max(id) from interuptions)",
		// argv).close();
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			isTermited = Boolean.getBoolean(cursor.getString(0));
		}
		cursor.close();
		return isTermited;
	}
	/*
	 * public void updateIsTerminated(Boolean isTerminated) { Object[] args
	 * ={isTerminated};this.Query(
	 * "Update interuptions SET ended_at=? where id=(Select max(id) from interuptions)"
	 * , args); }
	 */
	
	   /**
     * 
     * 
     */
    
    public void generateMessageOnSD(String sBody) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "SafeCell");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, "" + new Date().getTime());
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
