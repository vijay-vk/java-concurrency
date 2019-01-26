package org.learning.javaconcurrency.akka;

import org.learning.javaconcurrency.Event;
import org.learning.javaconcurrency.service.JsonService;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class IoOperationWorker extends AbstractActor {

	static public Props props() {
		return Props.create(IoOperationWorker.class, () -> new IoOperationWorker());
	}

	public static class Request {
		public String message;
		public Event event;
		public ActorRef worker1;
		public ActorRef worker2;

		public Request(String message, Event event, ActorRef worker1, ActorRef worker2) {
			this.message = message;
			this.event = event;
			this.worker1 = worker1;
			this.worker2 = worker2;
		}
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(Request.class, request -> {

			switch (request.message) {
			case "posts":
				request.event.posts = JsonService.getPosts();
				request.worker1.tell(new Worker1.Request("MergePostsAndComments", request.event, getSender()), getSelf());
				break;
			case "comments":
				request.event.comments = JsonService.getComments();
				request.worker1.tell(new Worker1.Request("MergePostsAndComments", request.event, getSender()), getSelf());
				break;
			case "albums":
				request.event.albums = JsonService.getAlbums();
				request.worker2.tell(new Worker2.Request("MergeAlbumsAndPhotos", request.event, getSender()), getSelf());
				break;
			case "photos":
				request.event.photos = JsonService.getPhotos();
				request.worker2.tell(new Worker2.Request("MergeAlbumsAndPhotos", request.event, getSender()), getSelf());
				break;

			default:
				break;
			}
		}).build();
	}

}
