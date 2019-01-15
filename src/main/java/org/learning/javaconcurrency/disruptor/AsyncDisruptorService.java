package org.learning.javaconcurrency.disruptor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.container.AsyncResponse;

import org.learning.javaconcurrency.service.JsonService;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import com.lmax.disruptor.dsl.Disruptor;

/**
 * Created by vkasiviswanathan on 1/6/19.
 */
@Component
public class AsyncDisruptorService {

	private static final ExecutorService EXECUTOR_SERVICE_4 = Executors.newFixedThreadPool(4,
			new CustomizableThreadFactory("Disruptor-Service-Pool-Size-4-"));

	@SuppressWarnings("deprecation")
	private static final Disruptor<Event> DISRUPTOR = new Disruptor<>(Event::new, 1024,
			Executors.newFixedThreadPool(1, new CustomizableThreadFactory("Disruptor-Service-Pool-Size-1-")));

	private Producer producer = new Producer();

	static {
		DISRUPTOR.handleEventsWith(new ResponseBuilder());
		DISRUPTOR.start();
	}

	public void sendAsyncResponse(AsyncResponse httpResponse) {

		try {

			Event event = new Event();
			event.httpResponse = httpResponse;
			event.startTime = System.currentTimeMillis();

			CompletableFuture<String> postsFuture = CompletableFuture.supplyAsync(JsonService::getPosts,
					EXECUTOR_SERVICE_4);
			CompletableFuture<String> commentsFuture = CompletableFuture.supplyAsync(JsonService::getComments,
					EXECUTOR_SERVICE_4);
			CompletableFuture<String> albumsFuture = CompletableFuture.supplyAsync(JsonService::getAlbums,
					EXECUTOR_SERVICE_4);
			CompletableFuture<String> photosFuture = CompletableFuture.supplyAsync(JsonService::getPhotos,
					EXECUTOR_SERVICE_4);

			CompletableFuture<Void> postsAndCommentsFuture = postsFuture.thenAcceptBothAsync(commentsFuture,
					(posts, comments) -> {
						event.posts = posts;
						event.comments = comments;
					}, EXECUTOR_SERVICE_4);

			CompletableFuture<Void> albumsAndPhotosFuture = albumsFuture.thenAcceptBothAsync(photosFuture,
					(albums, photos) -> {
						event.albums = albums;
						event.photos = photos;
					}, EXECUTOR_SERVICE_4);

			postsAndCommentsFuture.thenAcceptBothAsync(albumsAndPhotosFuture, (s1, s2) -> {
				producer.publish(DISRUPTOR, event);
			}, EXECUTOR_SERVICE_4);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		System.out.println("inside finalize");
		DISRUPTOR.shutdown();
		EXECUTOR_SERVICE_4.shutdown();
	}
}
