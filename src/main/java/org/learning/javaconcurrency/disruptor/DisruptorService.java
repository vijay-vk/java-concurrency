package org.learning.javaconcurrency.disruptor;

import java.util.Random;
import java.util.concurrent.Executors;

import org.learning.javaconcurrency.Event;
import org.learning.javaconcurrency.service.JsonService;
import org.learning.javaconcurrency.service.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

/**
 * Created by vkasiviswanathan on 1/6/19.
 */
@Component
public class DisruptorService {

	private static final Logger LOG = LoggerFactory.getLogger(DisruptorService.class);

	@SuppressWarnings("deprecation")
	private static final Disruptor<Event> DISRUPTOR = new Disruptor<>(Event::new, 1024,
			Executors.newFixedThreadPool(7, new CustomizableThreadFactory("Disruptor-Service-Pool-Size-7-")));

	static {
		DISRUPTOR
				.handleEventsWith((event, sequence, endOfBatch) -> event.posts = JsonService.getPosts(),
						(event, sequence, endOfBatch) -> event.comments = JsonService.getComments(),
						(event, sequence, endOfBatch) -> event.albums = JsonService.getAlbums(),
						(event, sequence, endOfBatch) -> event.photos = JsonService.getPhotos())
				.then(new PostsAndCommentsResponseBuilder(), new AlbumsAndPhotosResponseBuilder())
				.then(new MergeAllResponseBuilder());
		DISRUPTOR.start();
	}

	public String getResponse() {

		RingBuffer<Event> ringBuffer = DISRUPTOR.getRingBuffer();
		long sequence = ringBuffer.next();
		Event event = null;
		try {
			event = ringBuffer.get(sequence);
			event.startTime = System.currentTimeMillis();
		} finally {
			ringBuffer.publish(sequence);
		}

		while (event.response == null) {
		}
		LOG.info("Sending response from DisruptorService : " + Thread.currentThread().getName());
		return event.response;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		System.out.println("inside finalize");
		DISRUPTOR.shutdown();
	}

	static class PostsAndCommentsResponseBuilder implements EventHandler<Event> {

		private static final Logger LOG = LoggerFactory.getLogger(PostsAndCommentsResponseBuilder.class);

		@Override
		public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {
			setPostsAndCommentsResponse(event);
			LOG.info("Build PostsAndCommentsResponseBuilder");
		}

		private void setPostsAndCommentsResponse(Event event) {

			int userId = new Random().nextInt(10) + 1;
			String postsAndCommentsOfRandomUser = ResponseUtil.getPostsAndCommentsOfRandomUser(userId, event.posts,
					event.comments);

			event.postsAndCommentsResponse = postsAndCommentsOfRandomUser;
		}
	}

	static class AlbumsAndPhotosResponseBuilder implements EventHandler<Event> {

		private static final Logger LOG = LoggerFactory.getLogger(AlbumsAndPhotosResponseBuilder.class);

		@Override
		public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {
			setAlbumsAndPhotosResponse(event);
			LOG.info("Build AlbumsAndPhotosResponseBuilder ");
		}

		private void setAlbumsAndPhotosResponse(Event event) {

			int userId = new Random().nextInt(10) + 1;
			String albumsAndPhotosOfRandomUser = ResponseUtil.getAlbumsAndPhotosOfRandomUser(userId, event.albums,
					event.photos);
			event.albumsAndPhotosResponse = albumsAndPhotosOfRandomUser;

		}
	}

	static class MergeAllResponseBuilder implements EventHandler<Event> {

		private static final Logger LOG = LoggerFactory.getLogger(MergeAllResponseBuilder.class);

		@Override
		public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {
			setResponse(event);
			long endTime = System.currentTimeMillis();
			long timeTaken = endTime - event.startTime;
			LOG.info("Time taken to build response from Disruptor :: " + timeTaken + " - for sequence : " + sequence
					+ " - in Thread " + Thread.currentThread().getName());
		}

		private void setResponse(Event event) {

			String response = event.postsAndCommentsResponse + event.albumsAndPhotosResponse;
			event.response = response;
		}
	}
}
