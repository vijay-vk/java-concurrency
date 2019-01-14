package org.learning.javaconcurrency.akka;

import javax.ws.rs.container.AsyncResponse;

import org.learning.javaconcurrency.service.UserService;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class UserWorker extends AbstractActor {

	static public Props props() {
		return Props.create(UserWorker.class, () -> new UserWorker());
	}

	static public class User {
		public String message;
		public Master.Response response;

		public User(String message, Master.Response response) {
			this.message = message;
			this.response = response;
		}
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(User.class, user -> {
			String userDetails = UserService.getUserDetails();
			user.response.userDetails = userDetails;
			getSender().tell(user.response, getSelf());
		}).build();
	}

}
