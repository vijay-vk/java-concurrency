//package org.learning.javaconcurrency.akka;
//
//import javax.ws.rs.container.AsyncResponse;
//
//import org.learning.javaconcurrency.service.MockActivityService;
//
//import akka.actor.AbstractActor;
//import akka.actor.Props;
//
//public class MockActivityWorker extends AbstractActor {
//
//	static public Props props() {
//		return Props.create(MockActivityWorker.class, () -> new MockActivityWorker());
//	}
//
//	static public class Activity {
//		public String message;
//		public Master.Response response;
//
//		public Activity(String message, Master.Response response) {
//			this.message = message;
//			this.response = response;
//		}
//	}
//
//	@Override
//	public Receive createReceive() {
//		return receiveBuilder().match(Activity.class, activity -> {
//			String activityDetails = MockActivityService.getActivityDetails();
//			activity.response.activityDetails = activityDetails;
//			getSender().tell(activity.response, getSelf());
//		}).build();
//	}
//
//}
