package org.learning.javaconcurrency;

import javax.ws.rs.container.AsyncResponse;

public class Event {

	public volatile String users;
	public volatile String posts;
	public volatile String comments;
	public volatile String albums;
	public volatile String photos;
	public volatile String postsAndCommentsResponse;
	public volatile String albumsAndPhotosResponse;
	public volatile String response;
	public volatile long startTime;
	public volatile AsyncResponse asyncHttpResponse;
	public volatile String message;
}
