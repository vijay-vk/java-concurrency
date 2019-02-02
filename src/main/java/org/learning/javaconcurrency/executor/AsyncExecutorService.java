package org.learning.javaconcurrency.executor;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import org.learning.javaconcurrency.CustomThreads;
import org.learning.javaconcurrency.Event;
import org.learning.javaconcurrency.service.JsonService;
import org.learning.javaconcurrency.service.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by vkasiviswanathan on 1/6/19.
 */
@Component
public class AsyncExecutorService {

	private static final Logger LOG = LoggerFactory.getLogger(AsyncExecutorService.class);

	public String getAsyncResponse(int ioPoolSize, boolean fixedWorkerThreadForNonIoTasks) {

		Event event = new Event();
		String response = null;
		try {
			if (fixedWorkerThreadForNonIoTasks) {
				response = useThreadPoolForIoTasksAndWorkerThreadForNonIoTasks(ioPoolSize, event);
			} else {
				response = useCustomThreadPoolForIoAndHttpThreadForNonIoTasks(ioPoolSize, event);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		LOG.info("Sending response from AsyncExecutorService : " + Thread.currentThread().getName());
		return response;
	}

	private String useThreadPoolForIoTasksAndWorkerThreadForNonIoTasks(int ioPoolSize, Event event)
			throws InterruptedException, ExecutionException {

		int userId = new Random().nextInt(10) + 1;
		ExecutorService ioExecutorService = CustomThreads.getExecutorService(ioPoolSize);
		CompletableFuture<String> postsFuture = CompletableFuture.supplyAsync(JsonService::getPosts, ioExecutorService);
		CompletableFuture<String> commentsFuture = CompletableFuture.supplyAsync(JsonService::getComments,
				ioExecutorService);
		CompletableFuture<String> albumsFuture = CompletableFuture.supplyAsync(JsonService::getAlbums,
				ioExecutorService);
		CompletableFuture<String> photosFuture = CompletableFuture.supplyAsync(JsonService::getPhotos,
				ioExecutorService);

		CompletableFuture<String> postsAndCommentsFuture = postsFuture.thenCombineAsync(commentsFuture,
				(posts, comments) -> ResponseUtil.getPostsAndCommentsOfRandomUser(userId, posts, comments),
				CustomThreads.EXECUTOR_SERVICE_WORKER_1);

		CompletableFuture<String> albumsAndPhotosFuture = albumsFuture.thenCombineAsync(photosFuture,
				(albums, photos) -> ResponseUtil.getAlbumsAndPhotosOfRandomUser(userId, albums, photos),
				CustomThreads.EXECUTOR_SERVICE_WORKER_2);

		return postsAndCommentsFuture.thenCombineAsync(albumsAndPhotosFuture, (s1, s2) -> {
			LOG.info("Building Async Response in Thread " + Thread.currentThread().getName());
			return s1 + s2;
		}, CustomThreads.EXECUTOR_SERVICE_WORKER_1).get();

	}

	private String useCustomThreadPoolForIoAndHttpThreadForNonIoTasks(int ioPoolSize, Event event)
			throws InterruptedException, ExecutionException {

		int userId = new Random().nextInt(10) + 1;
		ExecutorService ioExecutorService = CustomThreads.getExecutorService(ioPoolSize);
		CompletableFuture<String> postsFuture = CompletableFuture.supplyAsync(JsonService::getPosts, ioExecutorService);
		CompletableFuture<String> commentsFuture = CompletableFuture.supplyAsync(JsonService::getComments,
				ioExecutorService);
		CompletableFuture<String> albumsFuture = CompletableFuture.supplyAsync(JsonService::getAlbums,
				ioExecutorService);
		CompletableFuture<String> photosFuture = CompletableFuture.supplyAsync(JsonService::getPhotos,
				ioExecutorService);

		CompletableFuture.allOf(postsFuture, commentsFuture, albumsFuture, photosFuture).get();

		String posts = postsFuture.get();
		String comments = commentsFuture.get();
		String albums = albumsFuture.get();
		String photos = photosFuture.get();

		String postsAndCommentsOfRandomUser = ResponseUtil.getPostsAndCommentsOfRandomUser(userId, posts, comments);
		String albumsAndPhotosOfRandomUser = ResponseUtil.getAlbumsAndPhotosOfRandomUser(userId, albums, photos);

		return postsAndCommentsOfRandomUser + albumsAndPhotosOfRandomUser;
	}
}
