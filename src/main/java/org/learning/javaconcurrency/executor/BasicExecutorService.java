package org.learning.javaconcurrency.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.learning.javaconcurrency.service.ActivityService;
import org.learning.javaconcurrency.service.UserService;
import org.springframework.stereotype.Component;

/**
 * Created by vkasiviswanathan on 1/6/19.
 */
@Component
public class BasicExecutorService {

	private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(3);

	public String getResponse() {

		String response = "";

		try {

			List<Callable<String>> callableTasks = new ArrayList<>();
			callableTasks.add(UserService::getUserDetails);
			callableTasks.add(ActivityService::getActivityDetails);

			List<Future<String>> futures = EXECUTOR_SERVICE.invokeAll(callableTasks);

			for (Future<String> future : futures) {

				response = response + future.get();
			}
			// time to build response
			TimeUnit.SECONDS.sleep(1); 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}
}
