package org.learning.javaconcurrency.disruptor;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
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
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;

/**
 * Created by vkasiviswanathan on 1/6/19.
 */
@Component
public class DisruptorService {

	private static final Logger LOG = LoggerFactory.getLogger(DisruptorService.class);

	@SuppressWarnings("deprecation")
	private static final Disruptor<Event> DISRUPTOR = new Disruptor<>(Event::new, 1024,
			Executors.newFixedThreadPool(9, new CustomizableThreadFactory("Disruptor-Service-Pool-Size-9-")));

	static {
		int userId = new Random().nextInt(10) + 1;

		EventHandler<Event> postsApiHandler = (event, sequence, endOfBatch) -> {
			event.posts = JsonService.getPosts();
			event.countDownLatch.countDown();
		};
		EventHandler<Event> commentsApiHandler = (event, sequence, endOfBatch) -> {
			event.comments = JsonService.getComments();
			event.countDownLatch.countDown();
		};
		EventHandler<Event> albumsApiHandler = (event, sequence, endOfBatch) -> {
			event.albums = JsonService.getAlbums();
			event.countDownLatch.countDown();
		};

		/*
		 * Photos API takes bit more time than all three API's above, so worker
		 * pool has been created for photos API alone.
		 */

		WorkHandler<Event> photosApiHandler1 = (event) -> {
			event.photos = JsonService.getPhotos();
			event.countDownLatch.countDown();
		};
		WorkHandler<Event> photosApiHandler2 = (event) -> {
			event.photos = JsonService.getPhotos();
			event.countDownLatch.countDown();
		};

		/*
		 * Merging the response will be coupled with some CPU-Intensive
		 * operation, so worker pool has been created for that reason.
		 */

		WorkHandler<Event> postsAndCommentsResponseHandler1 = (event) -> {
			event.postsAndCommentsResponse = ResponseUtil.getPostsAndCommentsOfRandomUser(userId, event.posts,
					event.comments);
			event.countDownLatch.countDown();
		};

		WorkHandler<Event> postsAndCommentsResponseHandler2 = (event) -> {
			event.postsAndCommentsResponse = ResponseUtil.getPostsAndCommentsOfRandomUser(userId, event.posts,
					event.comments);
			event.countDownLatch.countDown();
		};

		WorkHandler<Event> albumsAndPhotosResponseHandler1 = (event) -> {
			event.albumsAndPhotosResponse = ResponseUtil.getAlbumsAndPhotosOfRandomUser(userId, event.albums,
					event.photos);
			event.countDownLatch.countDown();
		};

		WorkHandler<Event> albumsAndPhotosResponseHandler2 = (event) -> {
			event.albumsAndPhotosResponse = ResponseUtil.getAlbumsAndPhotosOfRandomUser(userId, event.albums,
					event.photos);
			event.countDownLatch.countDown();
		};

		DISRUPTOR.handleEventsWith(postsApiHandler, commentsApiHandler, albumsApiHandler)
				.handleEventsWithWorkerPool(photosApiHandler1, photosApiHandler2)
				.thenHandleEventsWithWorkerPool(postsAndCommentsResponseHandler1, postsAndCommentsResponseHandler2)
				.handleEventsWithWorkerPool(albumsAndPhotosResponseHandler1, albumsAndPhotosResponseHandler2);
		DISRUPTOR.start();

	}

	public String getResponse() {
		Event event = null;
		RingBuffer<Event> ringBuffer = DISRUPTOR.getRingBuffer();
		long sequence = ringBuffer.next();
		CountDownLatch countDownLatch = new CountDownLatch(6);

		try {
			event = ringBuffer.get(sequence);
			event.countDownLatch = countDownLatch;
			event.startTime = System.currentTimeMillis();
		} finally {
			ringBuffer.publish(sequence);
		}

		try {
			event.countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		LOG.info("Sending response from DisruptorService : " + Thread.currentThread().getName());

		return event.postsAndCommentsResponse + event.albumsAndPhotosResponse;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		System.out.println("inside finalize");
		DISRUPTOR.shutdown();
	}
}
