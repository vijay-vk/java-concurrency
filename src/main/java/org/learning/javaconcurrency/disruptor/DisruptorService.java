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
			Executors.newFixedThreadPool(11, new CustomizableThreadFactory("Disruptor-Service-Pool-Size-11-")));

	static {
		int userId = new Random().nextInt(10) + 1;
		
		WorkHandler<Event> postsApiHandler1 = (event) -> {
			event.posts = JsonService.getPosts();
			event.countDownLatch.countDown();
		};
		WorkHandler<Event> postsApiHandler2 = (event) -> {
			event.posts = JsonService.getPosts();
			event.countDownLatch.countDown();
		};
		WorkHandler<Event> postsApiHandler3 = (event) -> {
			event.posts = JsonService.getPosts();
			event.countDownLatch.countDown();
		};
		WorkHandler<Event> commentsApiHandler1 = (event) -> {
			event.comments = JsonService.getComments();
			event.countDownLatch.countDown();
		};
		WorkHandler<Event> albumsApiHandler1 = (event) -> {
			event.albums = JsonService.getAlbums();
			event.countDownLatch.countDown();
		};
		WorkHandler<Event> albumsApiHandler2 = (event) -> {
			event.albums = JsonService.getAlbums();
			event.countDownLatch.countDown();
		};
		WorkHandler<Event> albumsApiHandler3 = (event) -> {
			event.albums = JsonService.getAlbums();
			event.countDownLatch.countDown();
		};

		WorkHandler<Event> photosApiHandler1 = (event) -> {
			event.photos = JsonService.getPhotos();
			event.countDownLatch.countDown();
		};
		WorkHandler<Event> photosApiHandler2 = (event) -> {
			event.photos = JsonService.getPhotos();
			event.countDownLatch.countDown();
		};

		WorkHandler<Event> postsAndCommentsResponseHandler1 = (event) -> {
			event.postsAndCommentsResponse = ResponseUtil.getPostsAndCommentsOfRandomUser(userId, event.posts,
					event.comments);
			event.countDownLatch.countDown();
		};

		WorkHandler<Event> albumsAndPhotosResponseHandler1 = (event) -> {
			event.albumsAndPhotosResponse = ResponseUtil.getAlbumsAndPhotosOfRandomUser(userId, event.albums,
					event.photos);
			event.countDownLatch.countDown();
		};

		DISRUPTOR.handleEventsWithWorkerPool(postsApiHandler1, postsApiHandler2, postsApiHandler3)
				.handleEventsWithWorkerPool(commentsApiHandler1)
				.handleEventsWithWorkerPool(albumsApiHandler1, albumsApiHandler2, albumsApiHandler3)
				.handleEventsWithWorkerPool(photosApiHandler1, photosApiHandler2)
				.thenHandleEventsWithWorkerPool(postsAndCommentsResponseHandler1)
				.handleEventsWithWorkerPool(albumsAndPhotosResponseHandler1);
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
