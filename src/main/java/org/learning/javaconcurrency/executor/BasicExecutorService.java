package org.learning.javaconcurrency.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
public class BasicExecutorService {

	private static final Logger LOG = LoggerFactory.getLogger(BasicExecutorService.class);

	public static final ExecutorService EXECUTOR_SERVICE_2 = Executors.newFixedThreadPool(2,
			new CustomizableThreadFactory("Executor-Service-Pool-Size-2-"));
	public static final ExecutorService EXECUTOR_SERVICE_4 = Executors.newFixedThreadPool(4,
			new CustomizableThreadFactory("Executor-Service-Pool-Size-4-"));
	public static final ExecutorService EXECUTOR_SERVICE_8 = Executors.newFixedThreadPool(8,
			new CustomizableThreadFactory("Executor-Service-Pool-Size-8-"));

	public String getResponse(int ioPoolSize, int nonIOPoolSize) {

		String response = "";
		long ioTasksStartTime = System.currentTimeMillis();
		try {

			List<Callable<String>> ioCallableTasks = new ArrayList<>();
			ioCallableTasks.add(JsonService::getPosts);
			ioCallableTasks.add(JsonService::getComments);
			ioCallableTasks.add(JsonService::getAlbums);
			ioCallableTasks.add(JsonService::getPhotos);

			ExecutorService ioExecutorService = getExecutorService(ioPoolSize);
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

			if (nonIOPoolSize < 2) {
				String postsAndCommentsOfRandomUser = ResponseUtil.getPostsAndCommentsOfRandomUser(userId, posts,
						comments);
				String albumsAndPhotosOfRandomUser = ResponseUtil.getAlbumsAndPhotosOfRandomUser(userId, albums,
						photos);

				response = postsAndCommentsOfRandomUser + albumsAndPhotosOfRandomUser;
			} else {

				List<Callable<String>> nonIoCallableTasks = new ArrayList<>();
				nonIoCallableTasks.add(() -> ResponseUtil.getPostsAndCommentsOfRandomUser(userId, posts, comments));
				nonIoCallableTasks.add(() -> ResponseUtil.getAlbumsAndPhotosOfRandomUser(userId, albums, photos));

				ExecutorService nonIOExecutorService = getExecutorService(nonIOPoolSize);
				List<Future<String>> futuresOfNonIOTasks = nonIOExecutorService.invokeAll(nonIoCallableTasks);

				String postsAndCommentsOfRandomUser = futuresOfNonIOTasks.get(0).get();
				String albumsAndPhotosOfRandomUser = futuresOfNonIOTasks.get(1).get();

				response = postsAndCommentsOfRandomUser + albumsAndPhotosOfRandomUser;
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

	private ExecutorService getExecutorService(int poolSize) {
		if (poolSize == 2) {
			return EXECUTOR_SERVICE_2;
		} else if (poolSize == 4) {
			return EXECUTOR_SERVICE_4;
		} else if (poolSize == 8) {
			return EXECUTOR_SERVICE_8;
		}
		return EXECUTOR_SERVICE_2;
	}

	@Override
	protected void finalize() throws Throwable {
		EXECUTOR_SERVICE_2.shutdown();
		EXECUTOR_SERVICE_4.shutdown();
		EXECUTOR_SERVICE_8.shutdown();
	}
}
