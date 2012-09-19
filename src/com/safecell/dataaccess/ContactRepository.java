package com.safecell.dataaccess;

import com.safecell.model.Emergency.Emergencies;
import com.safecell.model.SCContact;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

public class ContactRepository extends DBAdapter{
	public static final String CREATE_TABLE_QUERY=
		"CREATE TABLE Contacts (id integer PRIMARY KEY  AUTOINCREMENT  NOT NULL ," +
		"name varchar(50), number varchar(50))";
	public static final String INSERT_QUERY="INSERT INTO Contacts (name,number) VALUES (' ',0)";
	private static final int MAX_NUMBER_OF_EMERGENCY_CONTACTS = 4;
	
	public ContactRepository(Context context){
		super(context);
		
	}
	
	public void  insertContact(SCContact scContact)
	{
		String insertQuery = "INSERT INTO Contacts ("
			+ " name, number"
			+ ") VALUES (" 
			+ " ?, ? "+ ")";
		//Log.v("Safecell :"+"ContactRepositary","InsertContact");
		Object[] args = { scContact.getName(),scContact.getNumber()};
		
		this.Query(insertQuery, args);
		
		ContentValues values = new ContentValues();
		values.put(Emergencies.NAME, scContact.getName());
		values.put(Emergencies.NUMBER, scContact.getNumber());
		
		context.getContentResolver().insert(Emergencies.CONTENT_URI, values);
		
	}
	
	
	
	public void updateContact(SCContact scContact){
		
		String updateQuery ="Update Contacts SET name=?, number =? WHERE id=?";
		
		Object[] args = { scContact.getName(),scContact.getNumber(),scContact.getId()};
		
		this.Query(updateQuery, args);
		
	    ContentValues values = new ContentValues();
	    values.put(Emergencies.NAME, scContact.getName());
	    values.put(Emergencies.NUMBER, scContact.getNumber());

		context.getContentResolver().update(Emergencies.CONTENT_URI, values, " _id="+scContact.getId(), null);
	}
	
	public Cursor SelectContacts(){

		String selectQuery = "Select * from Contacts";
		Cursor cursor = this.selectQuery(selectQuery, null);
		cursor =  context.getContentResolver().query(Emergencies.CONTENT_URI,null, null, null, null);
		this.selectQuery(selectQuery, null).close();
		if (cursor.getCount()>0) 
		{
			cursor.moveToFirst();
			do {
				//Log.v("Safecell :"+"Value From Cursor", "" + cursor.getString(2));
			} while (cursor.moveToNext());
		}
		
		return cursor;	
		
	}
	
	public Cursor SelectContactsByWhere(int id){
		
		String[] args = {String.valueOf(id)};
		String selectQuery = "Select * from Contacts where id = ?";
		Cursor cursor = this.selectQuery(selectQuery, args);
		cursor =  context.getContentResolver().query(Emergencies.CONTENT_URI,null, " _id="+id, null, null);
		this.selectQuery(selectQuery, args).close();
		return cursor;
		
	}
	
	public ArrayList<SCContact> initialiseEmergencyContactArrayList() {
		ArrayList<SCContact> arrayList = new ArrayList<SCContact>();
		SCContact tempScContact;
		Cursor cursor = getEmergencyContactCurser();
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				tempScContact = new SCContact();
				tempScContact.setId(cursor.getInt(0));
				tempScContact.setName(cursor.getString(1));
				tempScContact.setNumber(cursor.getString(2));
				SCContact scContact = tempScContact;
				arrayList.add(scContact);

			} while (cursor.moveToNext());
			cursor.close();
		}
			int size=arrayList.size();
			
			
			for(int i=0;i<(MAX_NUMBER_OF_EMERGENCY_CONTACTS-size);i++)
			{
				tempScContact = new SCContact();
				tempScContact.setId(-1);
				tempScContact.setName("Enter Name");
				//tempScContact.setNumber(-1);
				SCContact scContact = tempScContact;
				arrayList.add(scContact);
			}
			
		
		return arrayList;
	
	
}

	private Cursor getEmergencyContactCurser() {
		Cursor cursor=SelectContacts();
		return cursor;
	}
	}
