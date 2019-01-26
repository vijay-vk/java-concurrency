package org.learning.javaconcurrency.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.learning.javaconcurrency.CustomThreads;
import org.learning.javaconcurrency.service.JsonService;
import org.learning.javaconcurrency.service.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by vkasiviswanathan on 1/6/19.
 */
@Component
public class BasicExecutorService {

	private static final Logger LOG = LoggerFactory.getLogger(BasicExecutorService.class);

	public String getResponse(int ioPoolSize, int nonIOPoolSize, boolean fixedWorkerThread) {

		String response = "";
		long ioTasksStartTime = System.currentTimeMillis();
		try {

			List<Callable<String>> ioCallableTasks = new ArrayList<>();
			ioCallableTasks.add(JsonService::getPosts);
			ioCallableTasks.add(JsonService::getComments);
			ioCallableTasks.add(JsonService::getAlbums);
			ioCallableTasks.add(JsonService::getPhotos);

			ExecutorService ioExecutorService = CustomThreads.getExecutorService(ioPoolSize);
			List<Future<String>> futuresOfIOTasks = ioExecutorService.invokeAll(ioCallableTasks);

			String posts = futuresOfIOTasks.get(0).get();
			String comments = futuresOfIOTasks.get(1).get();
			String albums = futuresOfIOTasks.get(2).get();
			String photos = futuresOfIOTasks.get(3).get();

			long ioTasksEndTime = System.currentTimeMillis();

			LOG.info("Time taken for Executor Service I/O tasks :: " + (ioTasksEndTime - ioTasksStartTime)
					+ " - in Thread " + Thread.currentThread().getName());

			long nonIOTasksStartTime = System.currentTimeMillis();

			int userId = new Random().nextInt(10) + 1;

			if (nonIOPoolSize == 0) {
				response = getResponseByUsingHttpThreadForNonIoTasks(userId, posts, comments, albums, photos);
			} else if (fixedWorkerThread) {
				response = getResponseByUsingFixedWorkerThreadsForNonIoTasks(userId, posts, comments, albums, photos);
			} else {
				response = getResponseByUsingThreadPoolForNonIoTasks(nonIOPoolSize, userId, posts, comments, albums,
						photos);
			}

			long nonIOTasksEndTime = System.currentTimeMillis();
			LOG.info("Time taken for Executor Service non I/O Tasks :: " + (nonIOTasksEndTime - nonIOTasksStartTime)
					+ " - in Thread " + Thread.currentThread().getName());
			LOG.info("Time taken for Executor Service to build Response :: " + (nonIOTasksEndTime - ioTasksStartTime)
					+ " - in Thread " + Thread.currentThread().getName());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	/*
	 * run CPU intensive operations in same threads which handles http requests
	 */
	private String getResponseByUsingHttpThreadForNonIoTasks(int userId, String posts, String comments, String albums,
			String photos) {

		String postsAndCommentsOfRandomUser = ResponseUtil.getPostsAndCommentsOfRandomUser(userId, posts, comments);
		String albumsAndPhotosOfRandomUser = ResponseUtil.getAlbumsAndPhotosOfRandomUser(userId, albums, photos);

		return postsAndCommentsOfRandomUser + albumsAndPhotosOfRandomUser;
	}

	/*
	 * Run CPU Intensive operations in a Fixed worker thread; in this use-case,
	 * only 2 logical non IO tasks exists, so 2 fixed worker threads have been
	 * created to handle those 2 tasks
	 */
	private String getResponseByUsingFixedWorkerThreadsForNonIoTasks(int userId, String posts, String comments,
			String albums, String photos) throws InterruptedException, ExecutionException {
		ExecutorService worker1 = CustomThreads.EXECUTOR_SERVICE_WORKER_1;
		ExecutorService worker2 = CustomThreads.EXECUTOR_SERVICE_WORKER_2;
		Future<String> futuresOfWorker1Tasks = worker1
				.submit(() -> ResponseUtil.getPostsAndCommentsOfRandomUser(userId, posts, comments));
		Future<String> futuresOfWorker2Tasks = worker2
				.submit(() -> ResponseUtil.getAlbumsAndPhotosOfRandomUser(userId, albums, photos));
		String postsAndCommentsOfRandomUser = futuresOfWorker1Tasks.get();
		String albumsAndPhotosOfRandomUser = futuresOfWorker2Tasks.get();

		return postsAndCommentsOfRandomUser + albumsAndPhotosOfRandomUser;
	}

	/*
	 * Run CPU Intensive operations in a Thread Pool of given size from request.
	 */
	private String getResponseByUsingThreadPoolForNonIoTasks(int nonIOPoolSize, int userId, String posts,
			String comments, String albums, String photos) throws InterruptedException, ExecutionException {

		List<Callable<String>> nonIoCallableTasks = new ArrayList<>();
		nonIoCallableTasks.add(() -> ResponseUtil.getPostsAndCommentsOfRandomUser(userId, posts, comments));
		nonIoCallableTasks.add(() -> ResponseUtil.getAlbumsAndPhotosOfRandomUser(userId, albums, photos));

		ExecutorService nonIOExecutorService = CustomThreads.getExecutorService(nonIOPoolSize);
		List<Future<String>> futuresOfNonIOTasks = nonIOExecutorService.invokeAll(nonIoCallableTasks);

		String postsAndCommentsOfRandomUser = futuresOfNonIOTasks.get(0).get();
		String albumsAndPhotosOfRandomUser = futuresOfNonIOTasks.get(1).get();

		return postsAndCommentsOfRandomUser + albumsAndPhotosOfRandomUser;
	}
}
