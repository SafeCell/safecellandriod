package com.safecell.dataaccess;

import java.util.ArrayList;

import android.R.bool;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.safecell.model.SCRule;

public class RulesRepository extends DBAdapter{

	public static final String CREATE_TABLE_QUERY=
		"CREATE TABLE rules ( " +
		"id integer PRIMARY KEY ," +
		"when_enforced nvarchar(250)," +
		"label nvarchar(250)," +
		"created_at nvarchar(250)," +
		"is_busdriver boolean DEFAULT false," +
		"is_novice boolean DEFAULT false," +
		"updated_at nvarchar(250)," +
		"is_primary boolean DEFAULT false," +
		"is_crash_collection boolean DEFAULT false," +
		"zone_id integer," +
		"rule_type nvarchar(250)," +
		"is_preemption boolean DEFAULT false," +
		"detail text," +
		"licenses TEXT,"+
		"zone_name nvarchar(250)," +
		"is_alldrivers boolean DEFAULT false," +
		"is_active boolean DEFAULT true)";
	
	public RulesRepository(Context context) {
		super(context);
		
	}

	public void  insertRules(SCRule scRule)
	{
		String insertQuery = "INSERT INTO rules ("+
		" id, when_enforced," +
		"label,created_at," +
		"is_busdriver,is_novice," +
		"updated_at,is_primary,"+
		"is_crash_collection,zone_id," +
		"is_preemption,detail,"+
		"licenses,"+
		"zone_name,rule_type," +
		"is_alldrivers"+
		") VALUES (" 
		+ " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		
			
		Object[] args = {scRule.getId(),scRule.getWhen_enforced(),scRule.getLabel(),
					scRule.getCreated_at(),scRule.isBusdriver(),scRule.isNovice(),
					scRule.getUpdated_at(),scRule.isPrimary(),scRule.isCrash_collection(),
					scRule.getZone_id(),scRule.isPreemption(),scRule.getDetail(),
					scRule.getLicenses(),scRule.getZone_name(),
					scRule.getRule_type(),scRule.isAlldrivers()};
		
		this.Query(insertQuery, args);
		
	}
	
	public void updateRules(SCRule scRule)
	{
		
		String updateQuery = "Update rules " +
				"SET when_enforced = ?," +
				"label= ?," +
				"created_at = ?," +
				"is_busdriver = ?," +
				"is_novice= ?," +
				"updated_at = ?," +
				"is_primary = ?," +
				"is_crash_collection= ?," +
				"zone_id = ?," +
				"is_preemption = ?," +
				"zone_name = ?," +
				"rule_type = ?," +
				"is_alldrivers= ?," +
				"is_active ='true', " +
				"licenses = ?"+
				"Where id = ? ";
		
		Object[] args = {scRule.getWhen_enforced(),scRule.getLabel(),scRule.getCreated_at(),scRule.isBusdriver(),
						 scRule.isNovice(),scRule.getUpdated_at(),scRule.isPrimary(),scRule.isCrash_collection(),
						 scRule.getZone_id(),scRule.isPreemption(),scRule.getZone_name(),scRule.getRule_type(),
						 scRule.isAlldrivers(),scRule.getLicenses(),scRule.getId()};
		
		this.Query(updateQuery, args);
	}
	
	public void updateInActive()
	{
		String updateQuery = "UPDATE rules SET is_active = 'false'";
		
		Object[] args = {};
		
		this.Query(updateQuery, args);
	}
	
	public boolean ruleIdPresentInTable(String ruleID)
	{
		String[] args = { ruleID };
		String selectQuery = "Select id from rules where id = ?"  ;
		
		Cursor cursor = this.selectQuery(selectQuery, args);
		
		if (cursor != null && cursor.getCount()>0) {
			cursor.close();
			return true;
		}
		cursor.close();
		return false;
		
	}
	
	public Cursor SelectActiveRule() {

		String selectQuery = "Select * from rules where is_active = 'true' ";
		Cursor cursor = this.selectQuery(selectQuery, null);
		this.selectQuery(selectQuery, null).close();
		
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			
		}
		return cursor;
	}
	
	public Cursor SelectInActiveRule() {

		String selectQuery = "Select * from rules where is_active = 'false' ";
		Cursor cursor = this.selectQuery(selectQuery, null);
		this.selectQuery(selectQuery, null).close();
		
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			
		}
		return cursor;
	}
	
	public ArrayList<SCRule> intialiseRulesArrayList(Cursor cursor) {
		
		
		ArrayList<SCRule> accountArrayList = new ArrayList<SCRule>();
		
		SCRule scRule =new SCRule();
		SCRule tempScRule;
		if(cursor.getCount()>0)
		{
			cursor.moveToFirst();
			do
			{
				tempScRule = new SCRule();
				tempScRule.setId(cursor.getInt(cursor.getColumnIndex("id")));
				tempScRule.setWhen_enforced(cursor.getString(cursor.getColumnIndex("when_enforced")));
				tempScRule.setLabel(cursor.getString(cursor.getColumnIndex("label")));
				tempScRule.setCreated_at(cursor.getString(cursor.getColumnIndex("created_at")));
				
				int busDriver = cursor.getInt(cursor.getColumnIndex("is_busdriver"));
				tempScRule.setBusdriver(busDriver == 1 ? true : false);
				
				int novice = cursor.getInt(cursor.getColumnIndex("is_novice"));
				tempScRule.setNovice(novice == 1 ? true : false);
				
				tempScRule.setUpdated_at(cursor.getString(cursor.getColumnIndex("updated_at")));
				int primary = cursor.getInt(cursor.getColumnIndex("is_primary"));
				tempScRule.setPrimary(primary == 1 ? true : false);
				
				int crashCollection = cursor.getInt(cursor.getColumnIndex("is_crash_collection"));
				tempScRule.setCrash_collection(crashCollection == 1 ? true : false);
				tempScRule.setZone_id(cursor.getInt(cursor.getColumnIndex("zone_id")));
				tempScRule.setRule_type(cursor.getString(cursor.getColumnIndex("rule_type")));
				
				int preemption = (cursor.getInt(cursor.getColumnIndex("is_preemption")));
				tempScRule.setPreemption(preemption == 1 ? true : false);
				
				tempScRule.setZone_name(cursor.getString(cursor.getColumnIndex("zone_name")));
				tempScRule.setDetail(cursor.getString(cursor.getColumnIndex("detail")));
				tempScRule.setLicenses(cursor.getString(cursor.getColumnIndex("licenses"))); 
				
				int allDrivers = cursor.getInt(cursor.getColumnIndex("is_alldrivers"));
				tempScRule.setAlldrivers(allDrivers == 1 ? true : false);
				
				scRule = tempScRule;
				accountArrayList.add(scRule);
				
			}while(cursor.moveToNext());
		}
		cursor.close();
		return accountArrayList;
	}
	
}
