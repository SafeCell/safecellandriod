package com.safecell.dataaccess;


import android.content.Context;
import android.database.Cursor;

import com.safecell.model.SCFakeLocation;

public class FakeLocationRepository extends DBAdapter {

	public static final String CREATE_TABLE_QUERY=
		"CREATE TABLE fakeLocation (fileName nvarchar(250),"
		+"estimatedSpeed nvarchar(250),"
		+"longitude nvarchar(250),"
		+"timeStamp nvarchar(250),"
		+"latitude nvarchar(250))";
	
	public FakeLocationRepository(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public void insertFakeLocation(SCFakeLocation fakeLocation)
	{
		String insertQuery = "INSERT INTO fakeLocation("
			+ " fileName, estimatedSpeed, longitude, timeStamp, latitude"
			+ ") VALUES (" 
			+ " ?, ?, ?, ?, ? "
			+ ")";
		
		Object[] args = { fakeLocation.getFileName(),fakeLocation.getEstimatedSpeed(),
				fakeLocation.getLongitude(),fakeLocation.getTimeStamp(),
				fakeLocation.getLatitude()};
		
		this.Query(insertQuery, args);
	}

	public boolean isFileNameExist(String FileName) {
		String selectQuery = "Select fileName from fakeLocation where fileName='"+FileName+"'";
		Cursor cursor = this.selectQuery(selectQuery, null);
		return (cursor.getCount()==0 )?true:false;
		
	}
}
