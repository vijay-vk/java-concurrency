package org.learning.javaconcurrency.disruptor;

import java.util.Random;
import java.util.concurrent.Executors;

import javax.ws.rs.container.AsyncResponse;

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
public class NonBlockingAsyncDisruptorService {

	@SuppressWarnings("deprecation")
	private static final Disruptor<Event> DISRUPTOR = new Disruptor<>(Event::new, 1024, Executors.newFixedThreadPool(12,
			new CustomizableThreadFactory("Non-Blocking-Async-Disruptor-Service-Pool-Size-12-")));

	static {
		DISRUPTOR
				.handleEventsWithWorkerPool((event) -> event.posts = JsonService.getPosts(),
						(event) -> event.posts = JsonService.getPosts(),
						(event) -> event.posts = JsonService.getPosts())
				.handleEventsWithWorkerPool((event) -> event.comments = JsonService.getComments())
				.handleEventsWithWorkerPool((event) -> event.albums = JsonService.getAlbums(),
						((event) -> event.albums = JsonService.getAlbums()),
						((event) -> event.albums = JsonService.getAlbums()))
				.handleEventsWithWorkerPool((event) -> event.photos = JsonService.getPhotos(),
						(event) -> event.photos = JsonService.getPhotos())
				.then(new PostsAndCommentsResponseBuilder(), new AlbumsAndPhotosResponseBuilder())
				.then(new MergeAllResponseBuilder());
		DISRUPTOR.start();
	}

	public void sendAsyncResponse(AsyncResponse httpResponse) {

		RingBuffer<Event> ringBuffer = DISRUPTOR.getRingBuffer();
		long sequence = ringBuffer.next();
		try {
			Event event = ringBuffer.get(sequence);
			event.asyncHttpResponse = httpResponse;
			event.startTime = System.currentTimeMillis();
		} finally {
			ringBuffer.publish(sequence);
		}
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
			buildPostsAndCommentsResponse(event);
			LOG.info("Build PostsAndCommentsResponseBuilder");
		}

		private void buildPostsAndCommentsResponse(Event event) {

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
			buildAlbumsAndPhotosresponse(event);
			LOG.info("Build AlbumsAndPhotosResponseBuilder ");
		}

		private void buildAlbumsAndPhotosresponse(Event event) {

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

			long endTime = System.currentTimeMillis();
			long timeTaken = endTime - event.startTime;
			LOG.info("Time taken to build response from Disruptor :: " + timeTaken + " - for sequence : " + sequence
					+ " - in Thread " + Thread.currentThread().getName());
			buildAndSendResponse(event);
		}

		private void buildAndSendResponse(Event event) {

			String response = event.postsAndCommentsResponse + event.albumsAndPhotosResponse;

			AsyncResponse asyncHttpResponse = event.asyncHttpResponse;

			asyncHttpResponse.resume(response);
		}
	}
}
