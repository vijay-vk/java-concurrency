package org.learning.javaconcurrency.executor;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.container.AsyncResponse;

import org.learning.javaconcurrency.service.JsonService;
import org.learning.javaconcurrency.service.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

/**
 * Created by vkasiviswanathan on 1/6/19.
 */
@Component
public class AsyncExecutorService {

	private static final Logger LOG = LoggerFactory.getLogger(AsyncExecutorService.class);

	public static final ExecutorService EXECUTOR_SERVICE_1 = Executors.newFixedThreadPool(1,
			new CustomizableThreadFactory("Async-Executor-Service-Pool-Size-1-"));
	public static final ExecutorService EXECUTOR_SERVICE_2 = Executors.newFixedThreadPool(2,
			new CustomizableThreadFactory("Async-Executor-Service-Pool-Size-2-"));
	public static final ExecutorService EXECUTOR_SERVICE_4 = Executors.newFixedThreadPool(4,
			new CustomizableThreadFactory("Async-Executor-Service-Pool-Size-4-"));
	public static final ExecutorService EXECUTOR_SERVICE_8 = Executors.newFixedThreadPool(8,
			new CustomizableThreadFactory("Async-Executor-Service-Pool-Size-8-"));

	public void sendAsyncResponse(int ioPoolSize, int nonIOPoolSize, AsyncResponse httpResponse) {

		try {

			int userId = new Random().nextInt(10) + 1;
			ExecutorService ioExecutorService = getExecutorService(ioPoolSize);

			CompletableFuture<String> postsFuture = CompletableFuture.supplyAsync(JsonService::getPosts,
					ioExecutorService);
			CompletableFuture<String> commentsFuture = CompletableFuture.supplyAsync(JsonService::getComments,
					ioExecutorService);
			CompletableFuture<String> albumsFuture = CompletableFuture.supplyAsync(JsonService::getAlbums,
					ioExecutorService);
			CompletableFuture<String> photosFuture = CompletableFuture.supplyAsync(JsonService::getPhotos,
					ioExecutorService);

			ExecutorService nonIOExecutorService = getExecutorService(nonIOPoolSize);

			CompletableFuture<String> postsAndCommentsFuture = postsFuture.thenCombineAsync(commentsFuture,
					(posts, comments) -> ResponseUtil.getPostsAndCommentsOfRandomUser(userId, posts, comments),
					nonIOExecutorService);

			CompletableFuture<String> albumsAndPhotosFuture = albumsFuture.thenCombineAsync(photosFuture,
					(albums, photos) -> ResponseUtil.getAlbumsAndPhotosOfRandomUser(userId, albums, photos),
					nonIOExecutorService);

			postsAndCommentsFuture.thenAcceptBothAsync(albumsAndPhotosFuture, (s1, s2) -> {
				LOG.info("Building Async Response in Thread " + Thread.currentThread().getName());
				String response = s1 + s2;
				httpResponse.resume(response);
			}, nonIOExecutorService);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private ExecutorService getExecutorService(int poolSize) {
		if (poolSize == 1) {
			return EXECUTOR_SERVICE_1;
		} else if (poolSize == 2) {
			return EXECUTOR_SERVICE_2;
		} else if (poolSize == 4) {
			return EXECUTOR_SERVICE_4;
		} else if (poolSize == 8) {
			return EXECUTOR_SERVICE_8;
		}
		return EXECUTOR_SERVICE_1;
	}

	@Override
	protected void finalize() throws Throwable {
		EXECUTOR_SERVICE_2.shutdown();
		EXECUTOR_SERVICE_4.shutdown();
		EXECUTOR_SERVICE_8.shutdown();
	}
}
