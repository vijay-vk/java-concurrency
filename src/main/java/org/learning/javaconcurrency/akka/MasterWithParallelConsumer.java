package org.learning.javaconcurrency.akka;

import org.learning.javaconcurrency.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class MasterWithParallelConsumer extends AbstractActor {

	private static final Logger LOG = LoggerFactory.getLogger(MasterWithParallelConsumer.class);

	static public Props props() {
		return Props.create(MasterWithParallelConsumer.class, () -> new MasterWithParallelConsumer());
	}

	static public class Request {
		public String message;
		public Event event;
		public ActorRef ioOperationWorker;
		public ActorRef worker1;
		public ActorRef worker2;

		public Request(String message, Event event, ActorRef ioOperationWorker, ActorRef worker1, ActorRef worker2) {
			this.message = message;
			this.event = event;
			this.ioOperationWorker = ioOperationWorker;
			this.worker1 = worker1;
			this.worker2 = worker2;
		}
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(Request.class, request -> {

			// Ideally, immutable data structures should have been used instead
			// of event.

			Event event = request.event;
			request.ioOperationWorker
					.tell(new IoOperationWorker.Request("posts", event, request.worker1, request.worker2), getSelf());
			request.ioOperationWorker.tell(
					new IoOperationWorker.Request("comments", event, request.worker1, request.worker2), getSelf());
			request.ioOperationWorker
					.tell(new IoOperationWorker.Request("albums", event, request.worker2, request.worker2), getSelf());
			request.ioOperationWorker
					.tell(new IoOperationWorker.Request("photos", event, request.worker2, request.worker2), getSelf());
		}).match(Event.class, e -> {
			if (e.postsAndCommentsResponse != null && e.albumsAndPhotosResponse != null) {

				String response = e.postsAndCommentsResponse + e.albumsAndPhotosResponse;
				LOG.info("Building response in Thread : " + Thread.currentThread().getName());
				e.response = response;
			}
		}).build();
	}

}
