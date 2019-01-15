package org.learning.javaconcurrency.disruptor;

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
	public AsyncResponse httpResponse;
	public String message;
}
