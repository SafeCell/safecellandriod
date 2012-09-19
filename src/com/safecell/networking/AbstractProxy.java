package com.safecell.networking;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import android.content.Context;

public abstract class AbstractProxy {

	protected Context context;
	protected HttpResponse response;
	protected String failureMessage = "";
	private boolean consumedRespose = false;
	private String responseBody = null;
	protected int statusCode;
	protected AbstractProxy(Context context) {
		this.context = context;
	}
	
	public Context getContext() {
		return context;
	}

	public HttpResponse getResponse() {
		return response;
	}

	public String getFailureMessage() {
		return failureMessage;
	}
	
	public String getResponseBody() throws Exception {
		if(response == null) {
			return null;
		}
		
		if(!consumedRespose) {
			responseBody = EntityUtils.toString(response.getEntity());
			consumedRespose = true;
		}
		
		return responseBody;
	}

	public int getStatusCode() {
		return statusCode;
	}

}
