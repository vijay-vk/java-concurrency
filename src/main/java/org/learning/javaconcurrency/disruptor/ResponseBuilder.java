package org.learning.javaconcurrency.disruptor;

import java.util.Random;

import javax.ws.rs.container.AsyncResponse;

import org.learning.javaconcurrency.service.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.EventHandler;

public class ResponseBuilder implements EventHandler<Event> {

	private static final Logger LOG = LoggerFactory.getLogger(ResponseBuilder.class);

	@Override
	public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {
		buildAndSendResponse(event);
		long endTime = System.currentTimeMillis();
		long timeTaken = endTime - event.startTime;
		LOG.info("Time taken to build response from Disruptor :: " + timeTaken + " - for sequence : " + sequence
				+ " - in Thread " + Thread.currentThread().getName());
	}

	private void buildAndSendResponse(Event event) {

		int userId = new Random().nextInt(10) + 1;
		String postsAndCommentsOfRandomUser = ResponseUtil.getPostsAndCommentsOfRandomUser(userId, event.posts,
				event.comments);
		String albumsAndPhotosOfRandomUser = ResponseUtil.getAlbumsAndPhotosOfRandomUser(userId, event.albums,
				event.photos);

		String response = postsAndCommentsOfRandomUser + albumsAndPhotosOfRandomUser;

		AsyncResponse httpResponse = event.httpResponse;

		httpResponse.resume(response);
	}
}
