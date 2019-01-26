package org.learning.javaconcurrency.akka;

import java.util.Random;

import org.learning.javaconcurrency.Event;
import org.learning.javaconcurrency.service.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class Worker2 extends AbstractActor {

	private static final Logger LOG = LoggerFactory.getLogger(Worker2.class);

	static public Props props() {
		return Props.create(Worker2.class, () -> new Worker2());
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
			LOG.info("Inside Worker 2 : " + Thread.currentThread().getName());
			if (request.event.albums != null && request.event.photos != null) {
				int userId = new Random().nextInt(10) + 1;
				String albumsAndPhotosOfRandomUser = ResponseUtil.getAlbumsAndPhotosOfRandomUser(userId,
						request.event.albums, request.event.photos);
				request.event.albumsAndPhotosResponse = albumsAndPhotosOfRandomUser;
				request.master.tell(request.event, getSelf());
			}
		}).build();
	}

}
