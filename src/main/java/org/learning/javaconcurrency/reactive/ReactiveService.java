package org.learning.javaconcurrency.reactive;

import java.util.Random;
import java.util.concurrent.ExecutorService;

import javax.ws.rs.container.AsyncResponse;

import org.learning.javaconcurrency.CustomThreads;
import org.learning.javaconcurrency.service.JsonService;
import org.learning.javaconcurrency.service.ResponseUtil;
import org.springframework.stereotype.Component;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by vkasiviswanathan on 1/6/19.
 */
@Component
public class ReactiveService {

	public void sendAsyncResponse(AsyncResponse asyncResponse) {

		int userId = new Random().nextInt(10) + 1;
		ExecutorService executor = CustomThreads.getExecutorService(16);

		Observable<String> postsObservable = Observable.just(userId).map(o -> JsonService.getPosts())
				.subscribeOn(Schedulers.from(executor));
		Observable<String> commentsObservable = Observable.just(userId).map(o -> JsonService.getComments())
				.subscribeOn(Schedulers.from(executor));
		Observable<String> albumsObservable = Observable.just(userId).map(o -> JsonService.getAlbums())
				.subscribeOn(Schedulers.from(executor));
		Observable<String> photosObservable = Observable.just(userId).map(o -> JsonService.getPhotos())
				.subscribeOn(Schedulers.from(executor));

		Observable<String> postsAndCommentsObservable = Observable
				.zip(postsObservable, commentsObservable,
						(posts, comments) -> ResponseUtil.getPostsAndCommentsOfRandomUser(userId, posts, comments))
				.subscribeOn(Schedulers.from(executor));

		Observable<String> albumsAndPhotosObservable = Observable
				.zip(albumsObservable, photosObservable,
						(albums, photos) -> ResponseUtil.getAlbumsAndPhotosOfRandomUser(userId, albums, photos))
				.subscribeOn(Schedulers.from(executor));

		Observable.zip(postsAndCommentsObservable, albumsAndPhotosObservable, (r1, r2) -> r1 + r2)
				.subscribeOn(Schedulers.from(executor))
				.subscribe((response) -> asyncResponse.resume(response), e -> asyncResponse.resume("error"));

	}
}
