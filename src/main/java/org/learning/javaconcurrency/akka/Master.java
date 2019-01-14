package org.learning.javaconcurrency.akka;

import javax.ws.rs.container.AsyncResponse;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class Master extends AbstractActor {

	static public Props props() {
		return Props.create(Master.class, () -> new Master());
	}

	static public class Request {
		public String message;
		public AsyncResponse httpResponse;
		public ActorRef userActor;
		public ActorRef activityActor;

		public Request(String message, AsyncResponse httpResponse, ActorRef userActor, ActorRef activityActor) {
			this.message = message;
			this.httpResponse = httpResponse;
			this.userActor = userActor;
			this.activityActor = activityActor;
		}
	}

	static public class Response {

		public String message;
		public AsyncResponse httpResponse;
		public String userDetails;
		public String activityDetails;

		public Response(String message, AsyncResponse httpResponse, String userDetails, String activitydetails) {
			this.message = message;
			this.httpResponse = httpResponse;
			this.userDetails = userDetails;
			this.activityDetails = activitydetails;
		}
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(Request.class, request -> {

			Response response = new Response("", request.httpResponse, null, null);

			request.userActor.tell(new UserWorker.User("Get User Details", response), getSelf());
			request.activityActor.tell(new ActivityWorker.Activity("Get Activity Details", response), getSelf());
		}).match(Response.class, r -> {
			if (r.userDetails != null && r.activityDetails != null) {
				r.httpResponse.resume(r.userDetails + r.activityDetails);
			}
		}).build();
	}

}
