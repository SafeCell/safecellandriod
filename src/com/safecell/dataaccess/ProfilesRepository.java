package com.safecell.dataaccess;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.safecell.model.SCProfile;

public class ProfilesRepository extends DBAdapter{
	private final String TAG="ProfilesRepository";
	
	private ArrayList<SCProfile> profilesArrayList =new ArrayList<SCProfile>();
	
	public static final String CREATE_TABLE_QUERY=
		"CREATE TABLE profiles (" +
		"id integer PRIMARY KEY  AUTOINCREMENT  NOT NULL ," +
		"device_key nvarchar(100) ," +
		"account_id integer ," +
		"first_name nvarchar(50) ," +
		"points_earned nvarchar(50) ,"+
		"last_name nvarchar(100) ," +
		"userImage blob , "+
		" email nvarchar(250)," +
		" phone nvarchar(50), " +
		"licenses TEXT, " +
		"device_family TEXT, " +
		"status TEXT, "+
		"app_version TEXT, "+
		"expires_on  TEXT"+
		" )"; 
		
	public ProfilesRepository(Context context){
		super(context);
		
	}
	
	private String insertQuery = "INSERT INTO profiles (" +
			"id, device_key, first_name, account_id, last_name, email, phone," +
			"points_earned, licenses, device_family, status, app_version, expires_on" +
			") VALUES (" +
			"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
			")";
		
   
	
	public void insertProfile (SCProfile scProfile)
	{
		
		Object[] args = {scProfile.getProfileId(), scProfile.getDeviceKey(), scProfile.getFirstName(),
				scProfile.getAccountID(), scProfile.getLastName(), scProfile.getEmail(),
				scProfile.getPhone(), scProfile.getPoints_earned(), scProfile.getLicenses(),
				scProfile.getDeviceFamily(), scProfile.getStatus(), scProfile.getAppVersion(),
				scProfile.getExpiresOn()
				};
		
		this.Query(insertQuery, args);
	}
	
	
	public Cursor selectProfiles() {
		
		String[] argv ={};
		String selectQuery = "Select * from profiles";
		Cursor cursor = this.selectQuery(selectQuery, argv);	
		this.selectQuery(selectQuery, argv).close();

		if (cursor.getCount()>0) {
			cursor.moveToFirst();			
				//Log.v("Safecell :"+"Value From Cursor", "" + cursor.getString(1));			
		}
	
		return cursor;
	}

	public boolean checkProfileInsertProperly() {
		
		String selectQuery = "Select * from profiles";
		Cursor cursor = this.selectQuery(selectQuery, null);		
		this.selectQuery(selectQuery, null).close();
		
		if (cursor.getCount()>0) {
			cursor.moveToFirst();
			cursor.close();
			return true;
		}
		cursor.close();
		return false;
		
	}

	public Cursor selectProfile(int id) {
		
		String[] argv ={""+id};
		String selectQuery = "Select * from profiles where id =?";
		Cursor cursor = this.selectQuery(selectQuery, argv);
		this.selectQuery(selectQuery, argv).close();
		if (cursor.getCount()>0) {
			cursor.moveToFirst();			
				//Log.v("Safecell :"+"Value From Cursor", "" + cursor.getCount());			
		}
		return cursor;
	}

	public String getLicenseKey()
	{
		String licenseKey = "";
		String selectQuery = "select licenses from profiles";
		Cursor cursor = this.selectQuery(selectQuery, null);
		this.selectQuery(selectQuery, null).close();
		
		if (cursor.getCount()>0) {
			cursor.moveToFirst();
			licenseKey = cursor.getString(cursor.getColumnIndex("licenses"));
		}
		cursor.close();
		return licenseKey;
		
	}
	
	public String getName(){
		String profileName="";
		String[] argv ={};
		String selectQuery = "Select * from profiles";
		Cursor cursor = this.selectQuery(selectQuery, argv);		
		this.selectQuery(selectQuery, argv).close();
		if (cursor.getCount()>0) {
			cursor.moveToFirst();			
			profileName = cursor.getString(cursor.getColumnIndex("first_name"))+" "+cursor.getString(cursor.getColumnIndex("last_name"))	;		
		}
		cursor.close();
		return profileName;
	}
	public int getId(){
		int id=0;
		String[] argv ={};
		String selectQuery = "Select * from profiles";
		Cursor cursor = this.selectQuery(selectQuery, argv);		
		this.selectQuery(selectQuery, argv).close();
		if (cursor.getCount()>0) {
			cursor.moveToFirst();			
			id = cursor.getInt(cursor.getColumnIndex("id"));		
		}
		cursor.close();
		return id;
	}
	
	public void updateProfile(SCProfile scProfile){
		
		String updateQuery ="Update profiles SET first_name =?,last_name=?,email=?,phone=?,licenses = ? WHERE id=?";
		
		Object[] args = { scProfile.getFirstName(), scProfile.getLastName(), scProfile.getEmail(),
				scProfile.getPhone(), scProfile.getLicenses(), scProfile.getProfileId()};
		
		this.Query(updateQuery, args);
	}
public void updateAppVersionProfile(String appVersionName){
		
		String updateQuery ="Update profiles SET app_version = "+appVersionName;
		
		this.Query(updateQuery, null);
	}
	// for image store in database  
	public void updateProfileImage(byte[] userImage)
	{
		Object [] args = {userImage};
		
		String updateQuery = "Update profiles SET userImage = ?";
		
		this.Query(updateQuery, args);
	}
	//retrieve from database
	public byte[] getProfileImage()
	{
		byte[] profileImage = null;
		String selectQuery = "SELECT userImage from profiles";
		Cursor cursor = this.selectQuery(selectQuery, null);
		this.selectQuery(selectQuery, null).close();
		
		
		
		if (cursor.getCount()>0) {
			cursor.moveToFirst();
			try {
				profileImage = cursor.getBlob(0);
				cursor.close();
			} catch (NullPointerException e) {
				// TODO: handle exception
				cursor.close();
			}
		}
		cursor.close();
		return profileImage;
		
	}
	
	public ArrayList<SCProfile> getProfilesArrayList() {
		
		Cursor cursor = selectProfiles();
		selectProfiles().close();
		
		//SCProfile scProfile = new SCProfile();
		SCProfile tempScProfile;
		if(cursor.getCount()>0)
		{
			cursor.moveToFirst();
			do
			{
				tempScProfile = new SCProfile();
				tempScProfile.setProfileId(cursor.getInt(cursor.getColumnIndex("id")));
				tempScProfile.setFirstName(cursor.getString(cursor.getColumnIndex("first_name")));
				tempScProfile.setLastName(cursor.getString(cursor.getColumnIndex("last_name")));
				tempScProfile.setEmail(cursor.getString(cursor.getColumnIndex("email")));
				tempScProfile.setPhone(cursor.getString(cursor.getColumnIndex("phone")));
				tempScProfile.setAccountId(cursor.getInt(cursor.getColumnIndex("account_id")));
				tempScProfile.setDeviceKey(cursor.getString(cursor.getColumnIndex("device_key")));
				tempScProfile.setLicenses(cursor.getString(cursor.getColumnIndex("licenses")));
				tempScProfile.setAccountID(cursor.getInt(cursor.getColumnIndex("account_id")));
				tempScProfile.setDeviceFamily(cursor.getString(cursor.getColumnIndex("device_family")));
				tempScProfile.setAppVersion(cursor.getString(cursor.getColumnIndex("app_version")));
				tempScProfile.setExpiresOn(cursor.getString(cursor.getColumnIndex("expires_on")));
				tempScProfile.setStatus(cursor.getString(cursor.getColumnIndex("status")));
				
				//scProfile = tempScProfile;
				profilesArrayList.add(tempScProfile);
				
			}while(cursor.moveToNext());
		}
		cursor.close();
		return profilesArrayList;
	}
	
	public SCProfile getCurrentProfile() {
		ArrayList<SCProfile> profiles = getProfilesArrayList();
		
		if(profiles.size() >= 1) {
			return profiles.get(0);
		} else {
			return null;
		}
	}

	public ArrayList<SCProfile> intialiseProfilesArrayList(Cursor cursor) {
		
		
		ArrayList<SCProfile> profilesArrayList = new ArrayList<SCProfile>();
		
		SCProfile scProfile=new SCProfile();
		SCProfile tempScProfile;
		if(cursor.getCount()>0)
		{
			cursor.moveToFirst();
			do
			{
				tempScProfile=new SCProfile();
				tempScProfile.setProfileId(cursor.getInt(cursor.getColumnIndex("id")));
				tempScProfile.setFirstName(cursor.getString(cursor.getColumnIndex("first_name")));
				tempScProfile.setLastName(cursor.getString(cursor.getColumnIndex("last_name")));
				tempScProfile.setEmail(cursor.getString(cursor.getColumnIndex("email")));
				tempScProfile.setPhone(cursor.getString(cursor.getColumnIndex("phone")));
				tempScProfile.setAccountId(cursor.getInt(cursor.getColumnIndex("account_id")));
				tempScProfile.setDeviceKey(cursor.getString(cursor.getColumnIndex("device_key")));
				tempScProfile.setLicenses(cursor.getString(cursor.getColumnIndex("licenses")));
				
				scProfile=tempScProfile;
				profilesArrayList.add(scProfile);
				
			}while(cursor.moveToNext());
		}
		cursor.close();
		return profilesArrayList;
	}

	public SCProfile getProfileInfo(int id)
	{
		SCProfile scProfile=new SCProfile();
		Cursor cursor=selectProfile(id);
		cursor.moveToFirst();
		//Log.v("Safecell :"+TAG+"/getProfileInfo", "cursor.getCount()="+cursor.getCount());
		if(cursor.getCount()>0)
		{
			scProfile.setFirstName(cursor.getString(cursor.getColumnIndex("first_name")));
			scProfile.setLastName(cursor.getString(cursor.getColumnIndex("last_name")));
			scProfile.setEmail(cursor.getString(cursor.getColumnIndex("email")));
			scProfile.setPhone(cursor.getString(cursor.getColumnIndex("phone")));

		}
		cursor.close();
		return scProfile;
	}

}
