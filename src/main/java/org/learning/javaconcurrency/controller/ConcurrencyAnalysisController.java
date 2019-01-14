package org.learning.javaconcurrency.controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

import org.learning.javaconcurrency.akka.Master;
import org.learning.javaconcurrency.akka.Printer;
import org.learning.javaconcurrency.akka.UserWorker;
import org.learning.javaconcurrency.akka.ActivityWorker;
import org.learning.javaconcurrency.disruptor.DisruptorService;
import org.learning.javaconcurrency.executor.BasicExecutorService;
import org.learning.javaconcurrency.sequential.SequentialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

/**
 * Created by vkasiviswanathan on 1/2/19.
 */
@Controller
@Path("")
public class ConcurrencyAnalysisController {

	private static ActorSystem system = null;
	private static ActorRef userActor = null;
	private static ActorRef activityActor = null;
	private static ActorRef masterActor = null;

	static {
		system = ActorSystem.create("helloakka");
		userActor = system.actorOf(UserWorker.props(), "userActor");
		activityActor = system.actorOf(ActivityWorker.props(), "activityActor");
		masterActor = system.actorOf(Master.props(), "masterActor");
	}

	@Autowired
	SequentialService sequentialService;

	@Autowired
	BasicExecutorService basicExecutorService;

	@Autowired
	DisruptorService disruptorService;

	@GET
	@Path("/sequential-processing")
	public String analyseSequentialProcessing() {
		return sequentialService.getResponse();
	}

	@GET
	@Path("/executor-service")
	public String analyseExecutorService() {
		return basicExecutorService.getResponse();
	}

	@GET
	@Path("/disruptor")
	public void analyseDisruptor(@Suspended AsyncResponse response) {
		disruptorService.sendAsyncResponse(response);
	}

	@GET
	@Path("/akka")
	public void analyseAkka(@Suspended AsyncResponse response) {
		masterActor.tell(new Master.Request("Get Response", response, userActor, activityActor), ActorRef.noSender());
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		system.terminate();
	}
}
