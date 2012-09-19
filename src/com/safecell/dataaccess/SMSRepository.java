package com.safecell.dataaccess;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;

import com.safecell.model.SCSms;

public class SMSRepository extends DBAdapter{

	public SMSRepository(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}


	public static final String CREATE_QUERY = "CREATE TABLE sms (_id INTEGER PRIMARY KEY," +
			"thread_id INTEGER," +
			"address TEXT, person INTEGER," +
			"date INTEGER," +
			"protocol INTEGER," +
			"read INTEGER DEFAULT 0," +
			"status INTEGER DEFAULT -1," +
			"type INTEGER," +
			"reply_path_present INTEGER," +
			"subject TEXT," +
			"body TEXT," +
			"service_center TEXT," +
			"locked INTEGER DEFAULT 0)";
	
	
	public void insertQuery(SCSms scSms)
	{
		Object [] args = {scSms.getThread_id(),scSms.getAddress(),scSms.getPerson(),
						  scSms.getDate(), scSms.getProtocol(), scSms.getRead(), scSms.getStatus(),
						  scSms.getType(), scSms.getReply_path_present(), scSms.getSubject(), 
						  scSms.getBody(),scSms.getService_center(), scSms.getLocked()};
		
		String insertQuery = "Insert Into sms (thread_id, " +
				"address, " +
				"person, " +
				"date, " +
				"protocol," +
				"read, " +
				"status, " +
				"type, reply_path_present, subject, body, service_center, locked ) " +
				"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" ;
			
		this.Query(insertQuery, args);
	}
	
	public void deleteSms()
	
	{
		String deleteQuery = "Delete from sms";

		this.deleteQuery(deleteQuery);
		
	}
	
	public  Cursor selectSMs() {
		
		String selectQuery = "Select * from sms";
		
		Cursor cursor = this.selectQuery(selectQuery, null);
		this.selectQuery(selectQuery, null).close();
		
		if (cursor.getCount()>0) {
			cursor.moveToFirst();
		}
		return cursor;
		
	}
	public ArrayList<SCSms> scSmsArrayList() {
		
		ArrayList<SCSms> smsArrayList = new ArrayList<SCSms>();
		SCSms scSms = new SCSms();
		SCSms tempScSms ;
		
		Cursor cursor = selectSMs();
		selectSMs().close();
		
		if (cursor.getCount()>0) {
			cursor.moveToFirst();
			do {
				tempScSms = new SCSms();
				tempScSms.setAddress(cursor.getString(cursor.getColumnIndex("address")));
				tempScSms.setBody(cursor.getString(cursor.getColumnIndex("body")));
				tempScSms.setDate(cursor.getLong(cursor.getColumnIndex("date")));
				tempScSms.setLocked(cursor.getInt(cursor.getColumnIndex("locked")));
				
				tempScSms.setPerson(cursor.getInt(cursor.getColumnIndex("person")));
				tempScSms.setProtocol(cursor.getInt(cursor.getColumnIndex("protocol")));
				tempScSms.setRead(cursor.getInt(cursor.getColumnIndex("read")));
				tempScSms.setReply_path_present(cursor.getInt(cursor.getColumnIndex("reply_path_present")));
				tempScSms.setService_center(cursor.getString(cursor.getColumnIndex("service_center")));
				tempScSms.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
				tempScSms.setSubject(cursor.getString(cursor.getColumnIndex("subject")));
				tempScSms.setThread_id(cursor.getInt(cursor.getColumnIndex("thread_id")));
				tempScSms.setType(cursor.getInt(cursor.getColumnIndex("type")));
				
				scSms = tempScSms;
				smsArrayList.add(scSms);
				
			} while (cursor.moveToNext());
		}
		cursor.close();
		return smsArrayList;
		}
}
