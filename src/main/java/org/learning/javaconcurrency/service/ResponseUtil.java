package org.learning.javaconcurrency.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.learning.javaconcurrency.models.Album;
import org.learning.javaconcurrency.models.Comment;
import org.learning.javaconcurrency.models.Photo;
import org.learning.javaconcurrency.models.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResponseUtil {

	private static final Logger LOG = LoggerFactory.getLogger(ResponseUtil.class);
	private static final ObjectMapper om = new ObjectMapper();

	static {
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public static String getPostsAndCommentsOfRandomUser(int userId, String posts, String comments) {

		long startTime = System.currentTimeMillis();

		List<Post> listOfPosts = getList(posts, new TypeReference<List<Post>>() {
		});
		List<Comment> listOfComments = getList(comments, new TypeReference<List<Comment>>() {
		});

		Map<Integer, String> filteredPosts = listOfPosts.stream().filter(p -> p.userId == userId)
				.collect(Collectors.toMap(p -> p.id, p -> p.title));
		List<Comment> filteredComments = listOfComments.stream().filter(c -> filteredPosts.keySet().contains(c.postId))
				.collect(Collectors.toList());

		StringBuilder sb = new StringBuilder();

		for (Integer postId : filteredPosts.keySet()) {
			sb.append("Post Title : " + filteredPosts.get(postId) + "<br/>");
			for (Comment comment : filteredComments) {
				if (comment.postId == postId)
					sb.append("---" + comment.body + "<br/>");
			}
		}
		List<Integer> randomList = RandomOperation.getIntList().stream().sorted().map(i -> i * userId)
				.collect(Collectors.toList());
		sb.append("Random Operation : Sort : Last Integer : " + randomList.get(randomList.size() - 1));
		sb.append("<br/>");
		sb.append("<br/>");
		sb.append("<br/>");
		sb.append("<br/>");

		long endTime = System.currentTimeMillis();
		long timeTaken = endTime - startTime;
		LOG.info("Time taken to parse and build response of Posts and Comments :: " + timeTaken + " - in Thread "
				+ Thread.currentThread().getName());

		return sb.toString();
	}

	public static String getAlbumsAndPhotosOfRandomUser(int userId, String albums, String photos) {

		long startTime = System.currentTimeMillis();

		List<Album> listOfAlbums = getList(albums, new TypeReference<List<Album>>() {
		});
		List<Photo> listOfPhotos = getList(photos, new TypeReference<List<Photo>>() {
		});

		Map<Integer, String> filteredAlbums = listOfAlbums.stream().filter(a -> a.userId == userId)
				.collect(Collectors.toMap(a -> a.id, a -> a.title));
		List<Photo> filteredPhotos = listOfPhotos.stream().filter(p -> filteredAlbums.keySet().contains(p.albumId))
				.collect(Collectors.toList());

		StringBuilder sb = new StringBuilder();

		for (Integer albumId : filteredAlbums.keySet()) {
			sb.append("Album Title : " + filteredAlbums.get(albumId) + "<br/>");
			for (Photo photo : filteredPhotos) {
				if (photo.albumId == albumId)
					sb.append("---" + photo.title + "<br/>");
			}
		}
		List<Long> randomList = RandomOperation.getLongList().stream().sorted().map(i -> i * userId)
				.collect(Collectors.toList());
		sb.append("Random Operation : Sort : Last Long : " + randomList.get(randomList.size() - 1));
		sb.append("<br/>");
		sb.append("<br/>");
		sb.append("<br/>");
		sb.append("<br/>");

		long endTime = System.currentTimeMillis();
		long timeTaken = endTime - startTime;
		LOG.info("Time taken to parse and build response of Albums and Photos :: " + timeTaken + " - in Thread "
				+ Thread.currentThread().getName());

		return sb.toString();
	}

	private static <T> T getList(String json, TypeReference<T> t) {

		try {
			return om.readValue(json, t);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
