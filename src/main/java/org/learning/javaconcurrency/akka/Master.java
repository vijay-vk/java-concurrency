package org.learning.javaconcurrency.akka;

import java.util.Random;

import org.learning.javaconcurrency.Event;
import org.learning.javaconcurrency.service.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class Master extends AbstractActor {

	private static final Logger LOG = LoggerFactory.getLogger(Master.class);

	static public Props props() {
		return Props.create(Master.class, () -> new Master());
	}

	static public class Request {
		public String message;
		public Event event;
		public ActorRef worker;

		public Request(String message, Event event, ActorRef worker) {
			this.message = message;
			this.event = event;
			this.worker = worker;
		}
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(Request.class, request -> {

			// Ideally, immutable data structures should have been used instead
			// of event.

			Event event = request.event;
			request.worker.tell(new JsonServiceWorker.Request("posts", event), getSelf());
			request.worker.tell(new JsonServiceWorker.Request("comments", event), getSelf());
			request.worker.tell(new JsonServiceWorker.Request("albums", event), getSelf());
			request.worker.tell(new JsonServiceWorker.Request("photos", event), getSelf());
		}).match(Event.class, e -> {
			if (e.posts != null && e.comments != null & e.albums != null & e.photos != null) {
				int userId = new Random().nextInt(10) + 1;
				String postsAndCommentsOfRandomUser = ResponseUtil.getPostsAndCommentsOfRandomUser(userId, e.posts,
						e.comments);
				String albumsAndPhotosOfRandomUser = ResponseUtil.getAlbumsAndPhotosOfRandomUser(userId, e.albums,
						e.photos);

				String response = postsAndCommentsOfRandomUser + albumsAndPhotosOfRandomUser;
				LOG.info("Building final response in Thread : " + Thread.currentThread().getName());
				e.response = response;
			}
		}).build();
	}

}
