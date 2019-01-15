package org.learning.javaconcurrency.disruptor;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.lmax.disruptor.EventHandler;

public class MockResponseBuilder implements EventHandler<MockEvent> {

	@Override
	public void onEvent(MockEvent event, long sequence, boolean endOfBatch) throws Exception {
		long endTime = System.currentTimeMillis();
		long timeTaken = endTime - event.getStartTime();
		System.out.println("Time taken to build response from Disruptor :: " + timeTaken + " - for sequence : "
				+ sequence + " - in Thread " + Thread.currentThread().getName());
		buildAndSendResponse(event);
	}

	private void buildAndSendResponse(MockEvent event) {

		String userDetails = event.getUserDetails();
		String activityDetails = event.getActivityDetails();
		String response = event.getResponse().append(userDetails).append(activityDetails).toString();

		AsyncResponse httpResponse = event.getHttpResponse();

		Response successResponse = Response.ok().entity(response).type(MediaType.TEXT_PLAIN).build();
		httpResponse.resume(successResponse);
	}
}
