package com.safecell.dataaccess;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;

import com.safecell.model.SCAccount;

public class AccountRepository extends DBAdapter{
	
	private static final int KEY_ACCOUNT_ID = 0;
	private static final int KEY_MASTER_PROFILE_ID = 1;
	private static final int KEY_APIKEY = 2;
	private static final int KEY_ACCOUNTS_CODE = 3;
	
	
	private String accountsCode = null;
	
	public static final String CREATE_TABLE_QUERY =
		"CREATE TABLE accounts (id integer primary key autoincrement,"
		+"master_profile_id integer NOT NULL,"
		+"api_key nvarchar(250) NULL,"
		+"account_code nvarchar(250) NULL ," +
		"chargify_id TEXT," +
		"perks_id TEXT, " +
		"activated boolean DEFAULT false," +
		"archived boolean DEFAULT false, " +
		"status TEXT)";
	
	
	
	public AccountRepository(Context context){
		super(context);
	}
	
	
	
	public void insertAccount(SCAccount scAccount)
	{
		deleteAccount();
		
		String insertQuery = "INSERT INTO accounts ("
			+ " id, master_profile_id, api_key, account_code, chargify_id, perks_id, activated, archived, status "
			+ ") VALUES (" 
			+ " ?, ?, ? ,?, ?, ?, ?, ?, ?"
			+ ")";
		
		Object[] args = { scAccount.getAccountId(),scAccount.getMasterProfileId(),scAccount.getApiKey(),
				scAccount.getAccountCode(), scAccount.getChargity_id(), scAccount.getPerksId(), scAccount.isActivated(), 
				scAccount.isArchived(), scAccount.getStatus()};
		
		this.Query(insertQuery, args);
	}
	
	public Cursor SelectAccount() {

		String selectQuery = "Select * from accounts";
		
		Cursor cursor = this.selectQuery(selectQuery, null);
		this.selectQuery(selectQuery, null).close();
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			
		}
		return cursor;
	}
	

	public void deleteAccount()
	{
		Object[] args ={};
		String deleteQuery = "Delete from accounts";
		
		this.Query(deleteQuery,args);
	}
	
	
	public String currentAPIKey() {
	
		String selectQuery = "Select api_key from accounts";
		Cursor cursor = this.selectQuery(selectQuery, null);
		this.selectQuery(selectQuery, null).close();
		
		cursor.moveToFirst();
		String APIKey = "";

		
		if (cursor.getCount() > 0) {
			APIKey = cursor.getString(0);

		}
		
		cursor.close();
		
		return APIKey;
	}

	

	public void updateAccount(SCAccount scAccount) {

		String updateQuery = "Update accounts SET master_profile_id=?, api_key =?,account_code=? WHERE id=?";

		Object[] args = { scAccount.getAccountId(),
				scAccount.getMasterProfileId(), scAccount.getApiKey(),
				scAccount.getAccountCode() };

		this.Query(updateQuery, args);
	}


	public String getAccountsCode() {
		
		Cursor cursor = SelectAccount();
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			accountsCode = cursor.getString(KEY_ACCOUNTS_CODE);
		}
		cursor.close();
		return accountsCode;

	}
	
	public SCAccount getAccountInformation(){
		ArrayList<SCAccount> accounts = intialiseAccountArrayList(SelectAccount());
		SelectAccount().close();
		if (accounts.size()>= 1) {
			return accounts.get(0);
		}
		return null;
	}
	
	
	public HashMap<Object, Object> selectApiKeyAndAccountID()
	{
		HashMap<Object, Object> ApikeyAndAccountId = new HashMap<Object, Object>();
		//String selectQuery = "Select id, api_key from accounts";
		Cursor cursor = this.SelectAccount();
		
		if(cursor.getCount()>0)
		{
			cursor.moveToFirst();
			ApikeyAndAccountId.put("AccountId",cursor.getInt(KEY_ACCOUNT_ID));
			ApikeyAndAccountId.put("ApiKey",cursor.getString(KEY_APIKEY));
			cursor.close();
			return ApikeyAndAccountId;
		}
		else{
			cursor.close();
			return ApikeyAndAccountId;		
		}
	}
	
	public ArrayList<SCAccount> intialiseAccountArrayList(Cursor cursor) {
		
		
		ArrayList<SCAccount> accountArrayList = new ArrayList<SCAccount>();
		
		SCAccount scAccount=new SCAccount();
		SCAccount tempScAccount;
		if(cursor.getCount()>0)
		{
			cursor.moveToFirst();
			do
			{
				tempScAccount=new SCAccount();
				tempScAccount.setAccountId(cursor.getInt(KEY_ACCOUNT_ID));
				tempScAccount.setMasterProfileId(cursor.getInt(KEY_MASTER_PROFILE_ID));
				tempScAccount.setApiKey(cursor.getString(KEY_APIKEY));
				tempScAccount.setAccountCode(cursor.getString(KEY_ACCOUNTS_CODE));
				
				scAccount=tempScAccount;
				accountArrayList.add(scAccount);
				
			}while(cursor.moveToNext());
		}
		cursor.close();
		return accountArrayList;
	}
}
