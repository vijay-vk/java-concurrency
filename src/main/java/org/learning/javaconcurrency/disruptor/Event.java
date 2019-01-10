package org.learning.javaconcurrency.disruptor;

import javax.ws.rs.container.AsyncResponse;

public class Event {

	private StringBuilder response;
	private String userDetails;
	private String activityDetails;
	private AsyncResponse httpResponse;

	public StringBuilder getResponse() {
		return response;
	}

	public void setResponse(StringBuilder response) {
		this.response = response;
	}

	public String getUserDetails() {
		return userDetails;
	}

	public void setUserDetails(String userDetails) {
		this.userDetails = userDetails;
	}

	public String getActivityDetails() {
		return activityDetails;
	}

	public void setActivityDetails(String activityDetails) {
		this.activityDetails = activityDetails;
	}

	public AsyncResponse getHttpResponse() {
		return httpResponse;
	}

	public void setHttpResponse(AsyncResponse httpResponse) {
		this.httpResponse = httpResponse;
	}
}
