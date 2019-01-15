package org.learning.javaconcurrency.sequential;

import java.util.Random;

import org.learning.javaconcurrency.service.JsonService;
import org.learning.javaconcurrency.service.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by vkasiviswanathan on 1/6/19.
 */
@Component
public class SequentialService {

	private static final Logger LOG = LoggerFactory.getLogger(SequentialService.class);

	public String getResponse() {

		long startTimeOfIOTasks = System.currentTimeMillis();
		String posts = JsonService.getPosts();
		String comments = JsonService.getComments();
		String albums = JsonService.getAlbums();
		String photos = JsonService.getPhotos();
		long endTimeOfIOTasks = System.currentTimeMillis();
		long timeTakenOfIOTasks = endTimeOfIOTasks - startTimeOfIOTasks;
		LOG.info("Time Taken for Sequential Service IO Operations :: " + timeTakenOfIOTasks + " - in Thread "
				+ Thread.currentThread().getName());

		long startTimeOfNonIOTasks = System.currentTimeMillis();

		int userId = new Random().nextInt(10) + 1;
		String postsAndCommentsOfRandomUser = ResponseUtil.getPostsAndCommentsOfRandomUser(userId, posts, comments);
		String albumsAndPhotosOfRandomUser = ResponseUtil.getAlbumsAndPhotosOfRandomUser(userId, albums, photos);

		String response = postsAndCommentsOfRandomUser + albumsAndPhotosOfRandomUser;

		long endTimeOfNonIOTasks = System.currentTimeMillis();
		long timeTakenOfNonIOTasks = endTimeOfNonIOTasks - startTimeOfNonIOTasks;
		long timeTaken = endTimeOfNonIOTasks - startTimeOfIOTasks;
		LOG.info("Time Taken for Sequential Service non-IO Operations :: " + timeTakenOfNonIOTasks + " - in Thread "
				+ Thread.currentThread().getName());
		LOG.info("Time Taken for Sequential Service to build response :: " + timeTaken + " - in Thread "
				+ Thread.currentThread().getName());

		return response;
	}
}
