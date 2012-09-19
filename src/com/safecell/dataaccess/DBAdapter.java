package com.safecell.dataaccess;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter{
	public static final String DATABASE_NAME = "SafeCell_Database";
	private static final int DATABASE_VERSION = 3;
	Context context;
	private DBHelper mDBHelper;
	private SQLiteDatabase db;
	
	
	public DBAdapter(Context context) {
		
		this.context=context;
		mDBHelper = new DBHelper(this.context);
		//Log.v("Safecell :"+"DBAdapter","context= "+this.context);
	}

	public class DBHelper extends SQLiteOpenHelper {

		public DBHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			
			db.execSQL(TripRepository.CREATE_QUERY);
			db.execSQL(TripJourneysRepository.CREATE_TABLE_QUERY);
			db.execSQL(JourneyEventsRepository.CREATE_TABLE_QUERY);
			db.execSQL(AccountRepository.CREATE_TABLE_QUERY);
			db.execSQL(ProfilesRepository.CREATE_TABLE_QUERY);
			db.execSQL(TripJourneyWaypointsRepository.CREATE_TABLE_QUERY);
			db.execSQL(ContactRepository.CREATE_TABLE_QUERY);
			db.execSQL(TempTripJourneyWayPointsRepository.CREATE_TABLE_QUERY);
			db.execSQL(InteruptionRepository.CREATE_TABLE_QUERY);
			db.execSQL(RulesRepository.CREATE_TABLE_QUERY);
			db.execSQL(FakeLocationRepository.CREATE_TABLE_QUERY);			
			
			db.execSQL(SMSRepository.CREATE_QUERY);	
			db.execSQL(LicenseRepository.CREATE_TABLE_QUERY);	
			//db = mDBHelper.getWritableDatabase();
		}

		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}	
	

	public Cursor selectQuery(String sql, String[] args) {
		 Cursor cursor = null;
			try {	
				
				db = mDBHelper.getWritableDatabase();
				//Log.v("Safecell :"+"DBAdapter","db = "+db);
				cursor = db.rawQuery(sql,args);
				
				if (cursor!= null) {
					 cursor.getCount();
				}
				db.close();
				//mDBHelper.close();
				
		} catch (Exception e) {
			//db.close();
			e.printStackTrace();
		}
		
		return cursor;
	}

	public void Query(String sql, Object[] bindArgs) {
		try {
			db = mDBHelper.getWritableDatabase();
			db.execSQL(sql, bindArgs);
			db.close();
		} catch (Exception e) {
			//Log.v("Safecell :"+"DBAdapter","Query");	
			db.close();
			e.printStackTrace();
		}
	}
	
	public DBAdapter open() throws SQLException {
		try {
			db = mDBHelper.getWritableDatabase();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return this;
		
	}
	public void closeDatabase() {
		mDBHelper.close();
		
	}
	
	protected void deleteQuery(String sql)
	{
		try {
			db = mDBHelper.getWritableDatabase();
			db.execSQL(sql);
			db.close();
		} catch (Exception e) {
			//Log.v("Safecell :"+"DBAdapter ","deleteQuery Exeption");			
			e.printStackTrace();
			db.close();
		}
	}

}// end DBAdapter