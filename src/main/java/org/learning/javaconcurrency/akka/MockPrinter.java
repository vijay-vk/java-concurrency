//package org.learning.javaconcurrency.akka;
//
//import akka.actor.AbstractActor;
//import akka.actor.AbstractActor.Receive;
//import akka.actor.Props;
//import akka.event.Logging;
//import akka.event.LoggingAdapter;
//
//public class MockPrinter extends AbstractActor {
//	  static public Props props() {
//	    return Props.create(MockPrinter.class, () -> new MockPrinter());
//	  }
//
//	  static public class Greeting {
//	    public final String message;
//
//	    public Greeting(String message) {
//	      this.message = message;
//	    }
//	  }
//
//	  private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
//
//	  public MockPrinter() {
//	  }
//
//	  @Override
//	  public Receive createReceive() {
//	    return receiveBuilder()
//	        .match(Greeting.class, greeting -> {
//	            log.info(greeting.message);
//	        })
//	        .build();
//	  }
//	}
