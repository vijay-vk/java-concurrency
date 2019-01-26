package org.learning.javaconcurrency.akka;

import java.util.Random;

import org.learning.javaconcurrency.Event;
import org.learning.javaconcurrency.service.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class Worker1 extends AbstractActor {
	
	private static final Logger LOG = LoggerFactory.getLogger(Worker1.class);

	static public Props props() {
		return Props.create(Worker1.class, () -> new Worker1());
	}

	public static class Request {
		public String message;
		public Event event;
		public ActorRef master;

		public Request(String message, Event event, ActorRef master) {
			this.message = message;
			this.event = event;
			this.master = master;
		}
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(Request.class, request -> {
			LOG.info("Inside Worker 1 : " + Thread.currentThread().getName());
			if (request.event.posts != null && request.event.comments != null) {
				int userId = new Random().nextInt(10) + 1;
				String postsAndCommentsOfRandomUser = ResponseUtil.getPostsAndCommentsOfRandomUser(userId,
						request.event.posts, request.event.comments);
				request.event.postsAndCommentsResponse = postsAndCommentsOfRandomUser;
				request.master.tell(request.event, getSelf());
			}
		}).build();
	}

}
