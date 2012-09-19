package com.safecell.dataaccess;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;

import com.safecell.model.SCLicense;

public class LicenseRepository extends DBAdapter{

	public static final String CREATE_TABLE_QUERY = "CREATE TABLE licenses (" +
			"id INTEGER NOT NULL, name TEXT, " +
			"description TEXT, key TEXT)";
	
	public LicenseRepository(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public void insertQuery (SCLicense scLicense)
	{
		Object[] args = {scLicense.getId(), scLicense.getName(), scLicense.getDescription(), scLicense.getKey()};
		
		String insertQuery = "Insert into licenses (id, name, description, key)" +
				"values (? ,? ,? ,?)";
		this.Query(insertQuery, args);
	}
	
	public void updateQuery(SCLicense scLicense){
		
		String updateQuery = "Update licenses " +
				"Set name = ?," +
				"description = ?," +
				"key =?" +
				"where id = ?";
		Object[] args = {scLicense.getName(), scLicense.getDescription(), scLicense.getKey(), scLicense.getId()};
		
		this.Query(updateQuery, args);
	}
	
	public boolean licensesIdPresent(String id){
		
		String[] args = { id };
		String selectQuery = "select id from licenses where id = ?";
		Cursor cursor = this.selectQuery(selectQuery, args);
		this.selectQuery(selectQuery, args).close();
		
		if (cursor.getCount() > 0) {
			cursor.close();
			return true;
		}
		cursor.close();
		return false;
	}
	
	public String selectGetLicenseKey(String licenseName)
	{
		String args[] = {licenseName};
		String key = "";
		String selectQuery = "select key from licenses where name = ?";
		Cursor cursor = this.selectQuery(selectQuery, args);
		this.selectQuery(selectQuery, args).close();
		if (cursor.getCount()>0) {
			cursor.moveToFirst();
			key = cursor.getString(cursor.getColumnIndex("key"));
		}
		cursor.close();
		return key;
	}
	
	public String selectGetLicenseName(String licenseKey)
	{
		String args[] = {licenseKey};
		String name = "";
		String selectQuery = "select name from licenses where key = ?";
		Cursor cursor = this.selectQuery(selectQuery, args);
		this.selectQuery(selectQuery, args).close();
		if (cursor.getCount()>0) {
			cursor.moveToFirst();
			name = cursor.getString(cursor.getColumnIndex("name"));
		}
		cursor.close();
		return name;
	}
	
	public ArrayList<SCLicense> scLicensesArrayList ()
	{
		ArrayList<SCLicense> licenseArrayList = new ArrayList<SCLicense>();
		SCLicense scLicense = new SCLicense();
		SCLicense tempScLicense;
		
		String selectQuery = "select * from licenses";
		Cursor cursor = this.selectQuery(selectQuery, null);
		this.selectQuery(selectQuery, null).close();
		
		if (cursor.getCount()> 0) {
			cursor.moveToFirst();
			do {
				tempScLicense = new SCLicense();
				tempScLicense.setId(cursor.getInt(cursor.getColumnIndex("id")));
				tempScLicense.setDescription(cursor.getString(cursor.getColumnIndex("description")));
				tempScLicense.setName(cursor.getString(cursor.getColumnIndex("name")));
				tempScLicense.setId(cursor.getInt(cursor.getColumnIndex("key")));
				
				scLicense = tempScLicense;
				licenseArrayList.add(scLicense);
				
			} while (cursor.moveToNext());
			cursor.close();
		}
		return null;
		
	}
}
