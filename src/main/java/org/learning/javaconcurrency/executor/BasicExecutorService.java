package org.learning.javaconcurrency.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.learning.javaconcurrency.service.ActivityService;
import org.learning.javaconcurrency.service.UserService;
import org.springframework.stereotype.Component;

/**
 * Created by vkasiviswanathan on 1/6/19.
 */
@Component
public class BasicExecutorService {

	private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(2);

	static {
		int cores = Runtime.getRuntime().availableProcessors();
		System.out.println("No of. cores in System : " + cores);
	}

	public String getResponse() {

		String response = "";
		long startTime = System.currentTimeMillis();
		try {

			List<Callable<String>> callableTasks = new ArrayList<>();
			callableTasks.add(UserService::getUserDetails);
			callableTasks.add(ActivityService::getActivityDetails);

			List<Future<String>> futures = EXECUTOR_SERVICE.invokeAll(callableTasks);

			for (Future<String> future : futures) {

				response = response + future.get();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
		long timeTaken = endTime - startTime;
		System.out.println("Time taken to build response from Executor :: " + timeTaken + " - in Thread "
				+ Thread.currentThread().getName());
		return response;
	}
}
