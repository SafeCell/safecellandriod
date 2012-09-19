package com.safecell.model;

public class SCSms {

	private long thread_id = 0;
	private String address = null;
	private int person = 0 ;
	private long date ;
	private int protocol;
	private int read = 0;
	private int status;
	private int type = 0;
	private int reply_path_present;
	private String subject;
	private String body;
	private String service_center;
	private int locked = 0;
	
	
	public long getThread_id() {
		return thread_id;
	}
	public void setThread_id(long threadId) {
		thread_id = threadId;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public int getPerson() {
		return person;
	}
	public void setPerson(int person) {
		this.person = person;
	}
	public long getDate() {
		return date;
	}
	public void setDate(long date) {
		this.date = date;
	}
	public int getProtocol() {
		return protocol;
	}
	public void setProtocol(int protocol) {
		this.protocol = protocol;
	}
	public int getRead() {
		return read;
	}
	public void setRead(int read) {
		this.read = read;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getReply_path_present() {
		return reply_path_present;
	}
	public void setReply_path_present(int replyPathPresent) {
		reply_path_present = replyPathPresent;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getService_center() {
		return service_center;
	}
	public void setService_center(String serviceCenter) {
		service_center = serviceCenter;
	}
	public int getLocked() {
		return locked;
	}
	public void setLocked(int locked) {
		this.locked = locked;
	}
	
	
}
