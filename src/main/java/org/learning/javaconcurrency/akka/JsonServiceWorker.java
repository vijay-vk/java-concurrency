package org.learning.javaconcurrency.akka;

import org.learning.javaconcurrency.disruptor.Event;
import org.learning.javaconcurrency.service.JsonService;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class JsonServiceWorker extends AbstractActor {

	static public Props props() {
		return Props.create(JsonServiceWorker.class, () -> new JsonServiceWorker());
	}

	public static class Request {
		public String message;
		public Event event;

		public Request(String message, Event event) {
			this.message = message;
			this.event = event;
		}
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(Request.class, request -> {

			switch (request.message) {
			case "posts":
				request.event.posts = JsonService.getPosts();
				break;
			case "comments":
				request.event.comments = JsonService.getComments();
				break;
			case "albums":
				request.event.albums = JsonService.getAlbums();
				break;
			case "photos":
				request.event.photos = JsonService.getPhotos();
				break;

			default:
				break;
			}
			getSender().tell(request.event, getSelf());
		}).build();
	}

}
