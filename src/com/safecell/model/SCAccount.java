package com.safecell.model;


public class SCAccount{


	private int accountId;
	private int masterProfileId;
	String accountCode;
	private String apiKey;
    private String chargity_id = null;
    private boolean archived = false;
    private  String status = null;
	private boolean activated = false;
	private String perksId = null;
	
    
	public SCAccount() {
		
	}

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}

	public int getMasterProfileId() {
		return masterProfileId;
	}

	public void setMasterProfileId(int masterProfileId) {
		this.masterProfileId = masterProfileId;
	}

	public String getAccountCode() {
		return accountCode;
	}

	public void setAccountCode(String accountCode) {
		this.accountCode = accountCode;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getChargity_id() {
		return chargity_id;
	}

	public void setChargity_id(String chargityId) {
		chargity_id = chargityId;
	}

	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean activated) {
		this.activated = activated;
	}

	public String getPerksId() {
		return perksId;
	}

	public void setPerksId(String perksId) {
		this.perksId = perksId;
	}
}

  
	

