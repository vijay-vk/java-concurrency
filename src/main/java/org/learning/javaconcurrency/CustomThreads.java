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
	private static final ExecutorService EXECUTOR_SERVICE_16 = Executors.newFixedThreadPool(16,
			new CustomizableThreadFactory("Executor-Service-Pool-Size-16-"));
	private static final ExecutorService EXECUTOR_SERVICE_24 = Executors.newFixedThreadPool(24,
			new CustomizableThreadFactory("Executor-Service-Pool-Size-24-"));
	private static final ExecutorService EXECUTOR_SERVICE_32 = Executors.newFixedThreadPool(32,
			new CustomizableThreadFactory("Executor-Service-Pool-Size-32-"));
	private static final ExecutorService EXECUTOR_SERVICE_40 = Executors.newFixedThreadPool(40,
			new CustomizableThreadFactory("Executor-Service-Pool-Size-40-"));
	private static final ExecutorService EXECUTOR_SERVICE_48 = Executors.newFixedThreadPool(48,
			new CustomizableThreadFactory("Executor-Service-Pool-Size-48-"));
	public static final ExecutorService EXECUTOR_SERVICE_WORKER_1 = Executors.newFixedThreadPool(1,
			new CustomizableThreadFactory("Executor-Service-Pool-Size-Worker-1-"));
	public static final ExecutorService EXECUTOR_SERVICE_WORKER_2 = Executors.newFixedThreadPool(1,
			new CustomizableThreadFactory("Executor-Service-Pool-Size-Worker-2-"));

	public static ExecutorService getExecutorService(int poolSize) {

		switch (poolSize) {
		case 1:
			return EXECUTOR_SERVICE_1;
		case 2:
			return EXECUTOR_SERVICE_2;
		case 4:
			return EXECUTOR_SERVICE_4;
		case 8:
			return EXECUTOR_SERVICE_8;
		case 16:
			return EXECUTOR_SERVICE_16;
		case 24:
			return EXECUTOR_SERVICE_24;
		case 32:
			return EXECUTOR_SERVICE_32;
		case 40:
			return EXECUTOR_SERVICE_40;
		case 48:
			return EXECUTOR_SERVICE_48;
		default:
			return EXECUTOR_SERVICE_8;
		}
	}

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
