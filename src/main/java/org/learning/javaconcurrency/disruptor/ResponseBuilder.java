package org.learning.javaconcurrency.disruptor;

import java.util.concurrent.TimeUnit;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.lmax.disruptor.EventHandler;

public class ResponseBuilder implements EventHandler<Event> {

	@Override
	public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {
		System.out.println("Response Builder " + Thread.currentThread().getName());
		buildAndSendResponse(event);
	}

	private void buildAndSendResponse(Event event) {

		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			e.printStackTrace();
		}
		String userDetails = event.getUserDetails();
		String activityDetails = event.getActivityDetails();
		String response = event.getResponse().append(userDetails).append(activityDetails).toString();

		AsyncResponse httpResponse = event.getHttpResponse();

		Response successResponse = Response.ok().entity(response).type(MediaType.TEXT_PLAIN).build();
		httpResponse.resume(successResponse);
	}
}
