package org.learning.javaconcurrency.controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

import org.learning.javaconcurrency.akka.JsonServiceWorker;
import org.learning.javaconcurrency.akka.Master;
import org.learning.javaconcurrency.disruptor.AsyncDisruptorService;
import org.learning.javaconcurrency.disruptor.DisruptorService;
import org.learning.javaconcurrency.executor.AsyncExecutorService;
import org.learning.javaconcurrency.executor.BasicExecutorService;
import org.learning.javaconcurrency.sequential.SequentialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.routing.RoundRobinPool;

/**
 * Created by vkasiviswanathan on 1/2/19.
 */
@Controller
@Path("")
public class ConcurrencyAnalysisController {

	private static final Logger LOG = LoggerFactory.getLogger(ConcurrencyAnalysisController.class);

	private static ActorSystem system = null;
	private static ActorRef workerActor = null;
	private static ActorRef masterActor = null;

	static {
		system = ActorSystem.create("helloakka");
		workerActor = system.actorOf(JsonServiceWorker.props().withRouter(new RoundRobinPool(4)), "postsWorker");
		masterActor = system.actorOf(Master.props(), "masterActor");

		int cores = Runtime.getRuntime().availableProcessors();
		LOG.info("No of. cores in System : " + cores);
	}

	@Autowired
	SequentialService sequentialService;

	@Autowired
	BasicExecutorService basicExecutorService;

	@Autowired
	AsyncExecutorService asyncExecutorService;

	@Autowired
	DisruptorService disruptorService;

	@Autowired
	AsyncDisruptorService asyncDisruptorService;

	@GET
	@Path("/sequential-processing")
	public String analyseSequentialProcessing() {
		LOG.info("Analyse Sequential service");
		return sequentialService.getResponse();
	}

	@GET
	@Path("/executor-service")
	public String analyseExecutorService(@QueryParam("ioPoolSize") int ioPoolSize,
			@QueryParam("nonIOPoolSize") int nonIOPoolSize) {
		LOG.info("Analyse Executor service");
		return basicExecutorService.getResponse(ioPoolSize, nonIOPoolSize);
	}

	@GET
	@Path("/executor-service-completable")
	public void analyseExecutorServiceCompletable(@QueryParam("ioPoolSize") int ioPoolSize,
			@QueryParam("nonIOPoolSize") int nonIOPoolSize, @Suspended AsyncResponse response) {
		LOG.info("Analyse Executor service - Completable");
		asyncExecutorService.sendAsyncResponse(ioPoolSize, nonIOPoolSize, response);
	}

	@GET
	@Path("/disruptor")
	public void analyseDisruptor(@Suspended AsyncResponse response) {
		LOG.info("Analyse Disruptor service ");
		disruptorService.sendAsyncResponse(response);
	}

	@GET
	@Path("/disruptor-completable")
	public void analyseDisruptorWithCompletableFutures(@Suspended AsyncResponse response) {
		LOG.info("Analyse Disruptor service With Competable Futures for IO Operations");
		asyncDisruptorService.sendAsyncResponse(response);
	}

	@GET
	@Path("/akka")
	public void analyseAkka(@Suspended AsyncResponse response) {
		LOG.info("Analyse Akka framework ");
		masterActor.tell(new Master.Request("Send Async Response", response, workerActor), ActorRef.noSender());
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		system.terminate();
	}
}
