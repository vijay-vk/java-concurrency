package org.learning.javaconcurrency;

import org.learning.javaconcurrency.akka.IoOperationWorker;
import org.learning.javaconcurrency.akka.JsonServiceWorker;
import org.learning.javaconcurrency.akka.Master;
import org.learning.javaconcurrency.akka.MasterWithParallelConsumer;
import org.learning.javaconcurrency.akka.Worker1;
import org.learning.javaconcurrency.akka.Worker2;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.routing.RoundRobinPool;

public class Actors {

	public static ActorSystem system = null;
	public static ActorRef workerActor = null;
	public static ActorRef masterActor = null;

	public static ActorRef ioOperationWorker = null;
	public static ActorRef workerActor1 = null;
	public static ActorRef workerActor2 = null;
	public static ActorRef masterActorWithParallelConsumer = null;

	static {
		system = ActorSystem.create("Concurrency-Analysis-Akka-System");
		workerActor = system.actorOf(JsonServiceWorker.props().withRouter(new RoundRobinPool(8)), "worker");
		masterActor = system.actorOf(Master.props(), "masterActor");

		ioOperationWorker = system.actorOf(IoOperationWorker.props().withRouter(new RoundRobinPool(6)),
				"ioOperationWorker");
		workerActor1 = system.actorOf(Worker1.props(), "MergePostsAndCommentWorker");
		workerActor2 = system.actorOf(Worker2.props(), "MergeAlbumsAndPhotosWorker");
		masterActorWithParallelConsumer = system.actorOf(MasterWithParallelConsumer.props(),
				"masterActorWithParallelConsumers");
	}

}
