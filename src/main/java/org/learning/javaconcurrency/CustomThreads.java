package org.learning.javaconcurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

public class CustomThreads {

	private static final ExecutorService EXECUTOR_SERVICE_1 = Executors.newFixedThreadPool(1,
			new CustomizableThreadFactory("Executor-Service-Pool-Size-1-"));
	private static final ExecutorService EXECUTOR_SERVICE_2 = Executors.newFixedThreadPool(2,
			new CustomizableThreadFactory("Executor-Service-Pool-Size-2-"));
	private static final ExecutorService EXECUTOR_SERVICE_4 = Executors.newFixedThreadPool(4,
			new CustomizableThreadFactory("Executor-Service-Pool-Size-4-"));
	private static final ExecutorService EXECUTOR_SERVICE_8 = Executors.newFixedThreadPool(8,
			new CustomizableThreadFactory("Executor-Service-Pool-Size-8-"));
	public static final ExecutorService EXECUTOR_SERVICE_WORKER_1 = Executors.newFixedThreadPool(1,
			new CustomizableThreadFactory("Executor-Service-Pool-Size-Worker-1-"));
	public static final ExecutorService EXECUTOR_SERVICE_WORKER_2 = Executors.newFixedThreadPool(1,
			new CustomizableThreadFactory("Executor-Service-Pool-Size-Worker-2-"));

	public static ExecutorService getExecutorService(int poolSize) {
		if (poolSize == 1) {
			return EXECUTOR_SERVICE_1;
		} else if (poolSize == 2) {
			return EXECUTOR_SERVICE_2;
		} else if (poolSize == 4) {
			return EXECUTOR_SERVICE_4;
		} else if (poolSize == 8) {
			return EXECUTOR_SERVICE_8;
		}
		return EXECUTOR_SERVICE_2;
	}

	/*public static ExecutorService getExecutorService(String worker) {

		if ("worker1".equals(worker)) {
			return EXECUTOR_SERVICE_WORKER_1;
		} else if ("worker2".equals(worker)) {
			return EXECUTOR_SERVICE_WORKER_2;
		} else {
			// shouldn't come here.
			return EXECUTOR_SERVICE_WORKER_1;
		}
	}*/

	@Override
	protected void finalize() throws Throwable {
		EXECUTOR_SERVICE_1.shutdownNow();
		EXECUTOR_SERVICE_2.shutdownNow();
		EXECUTOR_SERVICE_4.shutdownNow();
		EXECUTOR_SERVICE_8.shutdownNow();

		EXECUTOR_SERVICE_WORKER_1.shutdownNow();
		EXECUTOR_SERVICE_WORKER_2.shutdownNow();
	}

}
