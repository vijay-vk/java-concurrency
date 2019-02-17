package org.learning.javaconcurrency.reactive;

import java.util.Random;

import javax.ws.rs.container.AsyncResponse;

import org.learning.javaconcurrency.service.NonBlockingJsonService;
import org.learning.javaconcurrency.service.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.reactivex.Observable;

/**
 * Created by vkasiviswanathan on 1/6/19.
 */
@Component
public class ReactiveService {

	private static final Logger LOG = LoggerFactory.getLogger(ReactiveService.class);

	public void sendAsyncResponse(AsyncResponse asyncResponse) {

		int userId = new Random().nextInt(10) + 1;

		Observable<String> postsObservable = Observable.just(userId).flatMap(o -> NonBlockingJsonService.getPosts());
		Observable<String> commentsObservable = Observable.just(userId)
				.flatMap(o -> NonBlockingJsonService.getComments());
		Observable<String> albumsObservable = Observable.just(userId).flatMap(o -> NonBlockingJsonService.getAlbums());
		Observable<String> photosObservable = Observable.just(userId).flatMap(o -> NonBlockingJsonService.getPhotos());

		Observable<String> postsAndCommentsObservable = Observable.zip(postsObservable, commentsObservable,
				(posts, comments) -> ResponseUtil.getPostsAndCommentsOfRandomUser(userId, posts, comments));

		Observable<String> albumsAndPhotosObservable = Observable.zip(albumsObservable, photosObservable,
				(albums, photos) -> ResponseUtil.getAlbumsAndPhotosOfRandomUser(userId, albums, photos));

		Observable.zip(postsAndCommentsObservable, albumsAndPhotosObservable, (r1, r2) -> r1 + r2)
				.subscribe((response) -> asyncResponse.resume(response), e -> {
					LOG.error("Error", e);
					asyncResponse.resume("Error");
				});

	}
}
