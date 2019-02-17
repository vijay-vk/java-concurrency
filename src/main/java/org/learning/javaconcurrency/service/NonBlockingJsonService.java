package org.learning.javaconcurrency.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.netty.buffer.ByteBuf;
import io.reactivex.Observable;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;

public class NonBlockingJsonService {

	private static final Logger LOG = LoggerFactory.getLogger(NonBlockingJsonService.class);

	// JSON Place Holder
	private static final String MOCKY_IO_SERVICE = "www.mocky.io";
	private static final String JSON_SERVICE = "jsonplaceholder.typicode.com";
//	private static final String POSTS_API = "/posts";
	private static final String POSTS_API = "/v2/5c3ddcc235000012003e9646?mocky-delay=200ms";
	private static final String COMMENTS_API = "/comments";
//	private static final String ALBUMS_API = "/albums";
	private static final String ALBUMS_API = "/v2/5c3ddf1c3500002d003e9651?mocky-delay=200ms";
	private static final String PHOTOS_API = "/photos";

	public static Observable<String> getPosts() {

		long startTime = System.currentTimeMillis();

		HttpClientRequest<ByteBuf, ByteBuf> request = HttpClient.newClient(MOCKY_IO_SERVICE, 80)
				.createGet(POSTS_API).addHeader("content-type", "application/json; charset=utf-8");

		rx.Observable<String> rx1ObservableResponse = request.flatMap(HttpClientResponse::getContent)
				.map(buf -> getBytesFromResponse(buf))
				.reduce(new byte[0], (acc, bytes) -> reduceAndAccumulateBytes(acc, bytes))
				.map(bytes -> getStringResponse(bytes, "getPosts", startTime));

		return RxJavaInterop.toV2Observable(rx1ObservableResponse);
	}

	public static Observable<String> getComments() {

		long startTime = System.currentTimeMillis();

		HttpClientRequest<ByteBuf, ByteBuf> request = HttpClient.newClient(JSON_SERVICE, 80)
				.createGet(COMMENTS_API).addHeader("content-type", "application/json; charset=utf-8");

		rx.Observable<String> rx1ObservableResponse = request.flatMap(HttpClientResponse::getContent)
				.map(buf -> getBytesFromResponse(buf))
				.reduce(new byte[0], (acc, bytes) -> reduceAndAccumulateBytes(acc, bytes))
				.map(bytes -> getStringResponse(bytes, "getComments", startTime));

		return RxJavaInterop.toV2Observable(rx1ObservableResponse);
	}

	public static Observable<String> getAlbums() {

		long startTime = System.currentTimeMillis();

		HttpClientRequest<ByteBuf, ByteBuf> request = HttpClient.newClient(MOCKY_IO_SERVICE, 80)
				.createGet(ALBUMS_API).addHeader("content-type", "application/json; charset=utf-8");

		rx.Observable<String> rx1ObservableResponse = request.flatMap(HttpClientResponse::getContent)
				.map(buf -> getBytesFromResponse(buf))
				.reduce(new byte[0], (acc, bytes) -> reduceAndAccumulateBytes(acc, bytes))
				.map(bytes -> getStringResponse(bytes, "getAlbums", startTime));

		return RxJavaInterop.toV2Observable(rx1ObservableResponse);
	}

	public static Observable<String> getPhotos() {

		long startTime = System.currentTimeMillis();

		HttpClientRequest<ByteBuf, ByteBuf> request = HttpClient.newClient(JSON_SERVICE, 80)
				.createGet(PHOTOS_API).addHeader("content-type", "application/json; charset=utf-8");

		rx.Observable<String> rx1ObservableResponse = request.flatMap(HttpClientResponse::getContent)
				.map(buf -> getBytesFromResponse(buf))
				.reduce(new byte[0], (acc, bytes) -> reduceAndAccumulateBytes(acc, bytes))
				.map(bytes -> getStringResponse(bytes, "getPhotos", startTime));

		return RxJavaInterop.toV2Observable(rx1ObservableResponse);
	}

	private static byte[] getBytesFromResponse(ByteBuf buf) {
		byte[] bytes = new byte[buf.readableBytes()];
		buf.readBytes(bytes);
		buf.release();
		return bytes;
	}

	private static byte[] reduceAndAccumulateBytes(byte[] acc, byte[] bytes) {
		byte[] result = new byte[acc.length + bytes.length];
		System.arraycopy(acc, 0, result, 0, acc.length);
		System.arraycopy(bytes, 0, result, acc.length, bytes.length);
		return result;
	}

	private static String getStringResponse(byte[] bytes, String methodName, long startTime) {
		String response = new String(bytes);

		long endTime = System.currentTimeMillis();
		long timeTaken = endTime - startTime;
		LOG.info("Time Taken for JSON Service " + methodName + " :: " + timeTaken + " - in Thread "
				+ Thread.currentThread().getName());

		return response;
	}

}
