package com.safecell.model;

public class SCAccountDetails {

	private int acccountId;
	private float point_balance;
	private boolean archived = false;
	private boolean activated = false;
	private String perks_id;
	private int master_profile_id;
	private String status;
	private String apikey;
	private String validation_code;
	private String chargify_id = null;
	
	public int getAcccountId() {
		return acccountId;
	}
	public void setAcccountId(int acccountId) {
		this.acccountId = acccountId;
	}
	public float getPoint_balance() {
		return point_balance;
	}
	public void setPoint_balance(float pointBalance) {
		point_balance = pointBalance;
	}
	
	public boolean isActivated() {
		return activated;
	}
	public void setActivated(boolean activated) {
		this.activated = activated;
	}
	public String getPerks_id() {
		return perks_id;
	}
	public void setPerks_id(String perksId) {
		perks_id = perksId;
	}
	
	public int getMaster_profile_id() {
		return master_profile_id;
	}
	public void setMaster_profile_id(int masterProfileId) {
		master_profile_id = masterProfileId;
	}
	
	public String getApikey() {
		return apikey;
	}
	public void setApikey(String apikey) {
		this.apikey = apikey;
	}
	public String getValidation_code() {
		return validation_code;
	}
	public void setValidation_code(String validationCode) {
		validation_code = validationCode;
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
	public String getChargify_id() {
		return chargify_id;
	}
	public void setChargify_id(String chargifyId) {
		chargify_id = chargifyId;
	}
	
	
}
