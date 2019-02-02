package org.learning.javaconcurrency;

import java.util.concurrent.CountDownLatch;

import javax.ws.rs.container.AsyncResponse;

public class Event {

	public String users;
	public String posts;
	public String comments;
	public String albums;
	public String photos;
	public String postsAndCommentsResponse;
	public String albumsAndPhotosResponse;
	public String response;
	public long startTime;
	public AsyncResponse asyncHttpResponse;
	public String message;
	public CountDownLatch countDownLatch;
}
