//package org.learning.javaconcurrency.akka;
//
//import javax.ws.rs.container.AsyncResponse;
//
//import org.learning.javaconcurrency.service.MockUserService;
//
//import akka.actor.AbstractActor;
//import akka.actor.Props;
//
//public class MockUserWorker extends AbstractActor {
//
//	static public Props props() {
//		return Props.create(MockUserWorker.class, () -> new MockUserWorker());
//	}
//
//	static public class User {
//		public String message;
//		public Master.Response response;
//
//		public User(String message, Master.Response response) {
//			this.message = message;
//			this.response = response;
//		}
//	}
//
//	@Override
//	public Receive createReceive() {
//		return receiveBuilder().match(User.class, user -> {
//			String userDetails = MockUserService.getUserDetails();
//			user.response.userDetails = userDetails;
//			getSender().tell(user.response, getSelf());
//		}).build();
//	}
//
//}
