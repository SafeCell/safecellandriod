package com.safecell.model;

import java.io.Serializable;

import android.util.Log;

@SuppressWarnings("serial")
public class SCRule implements Serializable{
	
	
	String when_enforced=null;
	String label=null;
	String created_at=null;
	boolean busdriver=false;
	boolean novice=false;
	String updated_at=null;
	boolean primary=false;
	boolean crash_collection;
    int zone_id;
    int id;
    String rule_type=null;
    boolean preemption;
    String  detail=null;
    String  zone_name=null;
    boolean  alldrivers;
    boolean is_active = true;
    private String licenses = null;
    
    public boolean isIs_active() {
		return is_active;
	}
	public void setIs_active(boolean isActive) {
		is_active = isActive;
	}
	public String getZone_name() {
		return zone_name;
	}
	public void setZone_name(String zoneName) {
		zone_name = zoneName;
	}
	
	public String getWhen_enforced() {
		return when_enforced;
	}
	public void setWhen_enforced(String whenEnforced) {
		when_enforced = whenEnforced;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getCreated_at() {
		return created_at;
	}
	public void setCreated_at(String createdAt) {
		created_at = createdAt;
	}
	public boolean isBusdriver() {
		return busdriver;
	}
	public void setBusdriver(boolean busdriver) {
		this.busdriver = busdriver;
	}
	public boolean isNovice() {
		return novice;
	}
	public void setNovice(boolean novice) {
		this.novice = novice;
	}
	public String getUpdated_at() {
		return updated_at;
	}
	public void setUpdated_at(String updatedAt) {
		updated_at = updatedAt;
	}
	public boolean isPrimary() {
		return primary;
	}
	public void setPrimary(boolean primary) {
		this.primary = primary;
	}
	public boolean isCrash_collection() {
		return crash_collection;
	}
	public void setCrash_collection(boolean crashCollection) {
		crash_collection = crashCollection;
	}
	public int getZone_id() {
		return zone_id;
	}
	public void setZone_id(int zoneId) {
		zone_id = zoneId;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getRule_type() {
		return rule_type;
	}
	public void setRule_type(String ruleType) {
		rule_type = ruleType;
	}
	public boolean isPreemption() {
		return preemption;
	}
	public void setPreemption(boolean preemption) {
		this.preemption = preemption;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	public boolean isAlldrivers() {
		return alldrivers;
	}
	public void setAlldrivers(boolean alldrivers) {
		this.alldrivers = alldrivers;
	}
	public String getLicenses() {
		return licenses;
	}
	public void setLicenses(String licenses) {
		this.licenses = licenses;
	}
	
	/*[11:35:01 PM] Pritam Barhate: -(BOOL) appliesToLicenseClass: (NSString *) licenseClass {
	 
	 if(!self.licenses) {
	  return NO;
	 }
	 
	 if ([self.licenses isEqualToString:ALL_LICENSE_CLASSES]) {
	  return YES;
	 }
	   
	 NSArray *individialLicenseClasses = [self.licenses componentsSeparatedByString:@"|"];
	 
	 for (NSString * licenseClassStr in individialLicenseClasses) {
	  licenseClassStr = [licenseClassStr stringByStrippingWhitespace];
	  if ([licenseClassStr isEqualToString:licenseClass]) {
	   return YES;
	  }
	 }*/
	
	public boolean appliesToLicenseClass (String licenseType){
		
		if (licenses.equalsIgnoreCase("")) {
			return false;
		}
		if (licenses.equalsIgnoreCase("all")) {
			//Log.v("Safecell :"+"apply to all", "All");
			return true;
		}

		licenses = licenses.replace("|", "/");
		String[]individialLicenseClasses=licenses.split("/");
		 
		  for (int i = 0; i < individialLicenseClasses.length; i++) {
		   //Log.v("Safecell :"+""+i,individialLicenseClasses[i].trim());
		   
		   String tempDisplayLicense = individialLicenseClasses[i].trim();
		  if (tempDisplayLicense.equalsIgnoreCase(licenseType)) {
			return true;
		}
		  }
		return false;
	}
}
